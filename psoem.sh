#!/usr/bin/bash

surfaceFile=sam_nh3.gro
proteinFile=protein.gro

surfTopArgs=("SDM  372"   "SEM  28")
surfName="SDM SEM"
dipoleGroup="Protein CU2"

postiveCharges=25
negativeCharges=0

dist2Bottom=0.1
dist2Top=0.1

distPro2Surf=0.25
distpro2Top=1.78

X0=$1
X1=$2
X2=$3
X3=$4
X4=$5
X5=$6

wd=$PWD

if [ ! -d "$wd/part/p$7/" ]; then
  mkdir -p $wd/part/p$7/
  cp -rf $wd/model/* $wd/part/p$7/
else
  rm $wd/part/p$7/\#*\#
  rm $wd/part/p$7/*/\#*\#
  cp -f $wd/model/topol.top $wd/part/p$7/
fi

cd $wd/part/p$7/
########################################################################

# $1 输入文件名; $2 输出文件名; $3 移动后到底边距离(nm)
function g_adjPlus {
  trans=`sed '$d' $1 | awk -v dist="$3" 'BEGIN {FIELDWIDTHS="36 8"; min = 100;} 
    NR > 2 { if ($2 < min) { min = $2; } }
    END { trans = dist - min; print trans; }'`
  gmx editconf -f $1 -o $2 -translate 0 0 $trans
}

# $1 输入文件名; $2 额外厚度默认为0; return 高度+额外
function g_height {
  sed '$d' $1 |
  awk -v dist="$2" '
    BEGIN {
      FIELDWIDTHS="36 8";
      min = 100;
      max = -100;
    } 
    NR > 2 {
      if ($2 < min) {
        min = $2;
      } else if ($2 > max) {
        max = $2;
      }
    }
    END {
      thickness = max - min + dist + 0.02;
      print thickness;
    }'
}

# 获取gro文件的box大小, 输入文件名
function g_boxSize {
  awk 'END {printf("%10.5f%10.5f%10.5f",$1,$2,$3)}' $1
}

# $1 组名; $2 gro文件名
function g_findIndex {
  echo q | gmx make_ndx -f $2 -o 2>/dev/null |
  sed -n '/atoms$/p' |
  awk -v resName="$1" '
    BEGIN {split(resName, s);}
    { 
      for (i in s) {
        if ($2 == s[i]) { idx[s[i]]=$1;}
      }
    } END {for (i in idx) {print idx[i]}}'
}

# 获取能量组, 不可调
function getEneGroup {
  echo -e "\n\n" | gmx energy -f ./em/em.edr -sum 2>&1 | 
  awk 'NF == 4 {
        for(i=1;i<=NF;i++) {
          if ($i ~ "Coul-SR:Protein-Surface") {print $(i-1);} 
          else if($i ~ "LJ-SR:Protein-Surface") {print $(i-1);}
        }
      }'
}

# $1 fileName; $2 dist(nm)
function g_boxzPlus {
  boxz=`sed '$d' $1 | awk -v dist="$2" 'BEGIN {FIELDWIDTHS="36 8"; max = -50} 
  NR > 2 {if ($2 > max) max = $2} END {print max+dist}' $1`
  sed -i -e "\$s/[0-9]*.[0-9]*\$/${boxz}/" $1
}

function gmxem {
  gmx grompp -f em.mdp -c ions.gro -p topol.top -o ./em/em.tpr -po ./em/emout.mdp -n
  gmx mdrun -v -deffnm ./em/em -nt 1
}

function g_mer {
  if [ -z $2 ]; then p4="ptw4.gro"; else p4=$2; fi
  if [ -z $1 ]; then echo "No surface's .gro file"; return; else surf=$1; fi
  sed '$d' $p4 > box.gro
  sed '1,2d' $surf >> box.gro
  line_number=$((`cat box.gro|wc -l` - 3))
  sed -i -e "2s/.*/${line_number}/1" box.gro
}

########################################################################

# type gmx5 >/dev/null 2>&1 || { echo >&2 "Gromacs5.0 version not exist! Use System defalut version"; alias gmx5="gmx"; }

### 1. surface preparation
{
g_adjPlus $surfaceFile surf.gro $dist2Bottom
surfBoxThickness=`g_height surf.gro $dist2Bottom`
surfBoxEdges=($(g_boxSize $surfaceFile))
} > $7.log 2>&1

### 2. protein preparation
{
gmx editconf -f $proteinFile -o ptw1-1.gro -rotate $X0 $X1 $X2
proBoxThickness=`g_height ptw1-1.gro $distpro2Top`
gmx editconf -f ptw1-1.gro -o ptw1-2.gro -box ${surfBoxEdges[0]} ${surfBoxEdges[1]} $proBoxThickness
gmx editconf -f ptw1-2.gro -o ptw1-3.gro -translate $X3 $X4 0
distPro2Surf=`echo ${distPro2Surf} ${X5} | awk '{print $1+$2}'`
g_adjPlus ptw1-3.gro ptw2.gro $distPro2Surf
gmx solvate -cp ptw2.gro -cs spc216.gro -o ptw3.gro -p topol.top
} >> $7.log 2>&1

### 3. box Setting
gmx editconf -f ptw3.gro -o ptw4.gro -translate 0 0 $surfBoxThickness >> $7.log 2>&1
g_mer surf.gro ptw4.gro


### 4. writer2top
for (( i = 0; i < ${#surfTopArgs[@]}; i++ )); do
  echo -e ${surfTopArgs[$i]} >> topol.top
done


### 5. ions and em
{
mkdir ion em
gmx grompp -f ion.mdp -c box.gro -p topol.top -o ./ion/ions.tpr -po ./ion/ionout.mdp -maxwarn 2
g_findIndex SOL box.gro | gmx genion -s ./ion/ions.tpr -o ions.gro -p topol.top -np $negativeCharges -pname NA -pq 1 -nn $postiveCharges -nname CL -nq -1
g_boxzPlus ions.gro $dist2Top
} >> $7.log 2>&1

### 6. make_ndx for surface
{
surfId=($(g_findIndex "$surfName" ions.gro))

idxSelect=`for i in ${surfId[@]}; do printf "%d | " $i;done`
idxSelect=${idxSelect::-2}

echo -e "$idxSelect \n q" | gmx make_ndx -f ions.gro -o
lastId=$(($(grep -e "\[.*\]" index.ndx | wc -l) - 1))
echo -e "name $lastId Surface\n q" | gmx make_ndx -f ions.gro -o -n
} >> $7.log 2>&1

### 7. runem
gmxem >> $7.log 2>&1


### 8. get Protein-Surface
echo -e "`getEneGroup`\n\n" | gmx energy -f ./em/em.edr -sum >> $7.log 2>&1
awk 'END{print $2}' energy.xvg > ene.dat


### 9. make_ndx for dipoles
{
dipId=($(g_findIndex "$dipoleGroup" ./em/em.tpr))
idxSelect=`for i in ${dipId[@]}; do printf "%d | " $i;done`
idxSelect=${idxSelect::-2}

echo -e "$idxSelect \n q" | gmx make_ndx -f ./em/em.tpr -o

lastId=$(($(grep -e "\[.*\]" index.ndx | wc -l) - 1))
echo $lastId | gmx dipoles -f ./em/em.trr -s ./em/em.tpr -n

# 修改了awk只输出一个cos
awk '!/^(#|@)/ {theta = $4 / $5; print theta}' Mtot.xvg > dipoles.dat
} >> $7.log 2>&1
