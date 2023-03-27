package xyz.shxiaj.clpsoChange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 10:03
 */
class Pso {

    // 当前最优位置和适应值(能量), cos
    private double[] gPositionX = new double[Particle.DIMENSION];
    private double gFitness = Double.MAX_VALUE;
    private double gCos;
    public int fitSum = 0;

    // CPU 核心大小, 并行em队列的任务数量
    public static final int CORES = 28;
    // gmx 能量精度
    // public static final double PRECISION = 0.000001;
    // 最大适应度评估; 1029已取消变量作用
    // public static final int MAXFITASS = 10000;
    // public static final int sameNum = 50;
    // 种群大小
    private static final int particleNum = 28;
    // 最大迭代次数
    private static final int N = 1000;
    // 最大未进化代数
    private static final int MAXSTAY = 5;
    // 惯性权重
    private static final double wmax = 0.9;
    private static final double wmin = 0.2;
    // 加速因子
    private static final double c = 1.49445;
    // 生成的文件位置
    private static final String datDir = "./dat";
    private static final String fitDat = "./dat/gene.dat";
    private static final String stepDat = "./dat/step-%d.dat";
    // 当前系统换行符
    private static final String lineEnd = System.getProperty("line.separator");

    private final Random random = new Random();
    // 粒子对象列表
    private final List<Particle> parts = new ArrayList<>();
    // 记录所有 最优位置, 适应值和Cos 的列表
    // private final List<double[]> allgPositionX = new ArrayList<>();
    // private final List<Double> allgFitness = new ArrayList<>();
    // private final List<Double> allCos = new ArrayList<>();

    // private final static Logger log = Logger.getGlobal();

    /**
     * initial all particle
     */
    public void initialParts() {
        for (int i = 0; i < particleNum; i++) {
            double t = i * 1.0 / (particleNum - 1) * 5;
            double pc = 0.5 * (Math.exp(t) - 1) / (Math.exp(5) - 1);
            Particle p = new Particle(i, pc, MAXSTAY);
            parts.add(p);
        }
    }

    /**
     * reassign one particle's learnPart array
     */
    public void reassignLearnPart(Particle p) {
        boolean flag = true;
        for (int i = 0; i < Particle.DIMENSION; i++) {
            double rand = random.nextDouble();
            // log.info(String.valueOf(rand));
            if (rand < p.Pc) {
                flag = false;
                // 生成两个不重复的粒子id, 并且不与自身id相同
                int fi1 = p.id;
                int fi2 = p.id;
                while (fi1 == fi2 || fi1 == p.id || fi2 == p.id) {
                    fi1 = random.nextInt(particleNum);
                    fi2 = random.nextInt(particleNum);
                }
                if (parts.get(fi1).pFitness < parts.get(fi2).pFitness) {
                    p.learnPart[i] = fi1;
                } else {
                    p.learnPart[i] = fi2;
                }
            } else {
                p.learnPart[i] = p.id;
            }
        }
        // log.info(String.valueOf(flag));
        if (flag) {
            int fi = p.id;
            while (fi == p.id) {
                fi = random.nextInt(particleNum);
            }
            int i = random.nextInt(Particle.DIMENSION);
            p.learnPart[i] = fi;
        }
    }

    /**
     * update partBest and run em
     */
    public void updatePartBest() throws Exception {
        Deque<Particle> queue = new ArrayDeque<>();
        int i = 0;
        // 运行队列, 大小为CORES
        while (i < particleNum) {
            // 队列未满, 加入队列
            while (queue.size() < CORES && i < particleNum) {
                Particle p = parts.get(i);
                /* 1029已取消判断:
                判断是否超出边界, 在边界内才进行计算; 适应度评估+1
                没进入队列的粒子也不会更新gbest,stayNum
                if (p.isInLimit()) {
                    p.execFitness();
                    queue.offer(p);
                    fitSum++;
                }*/
                p.execFitness();
                queue.offer(p);
                i++;
            }
            // 队列满, 等待队首的进程结束
            if (!queue.isEmpty()) {
                Particle p = queue.poll();
                p.process.waitFor();
                p.updatePartBest();
            }
        }
        // 等待所有进程结束
        // ? 如果这里不等待, 是不是就可以不同代数的粒子同时进行了?
        while (!queue.isEmpty()) {
            Particle p = queue.poll();
            p.process.waitFor();
            p.updatePartBest();
        }
    }

    /**
     * update globalBest
     */
    public void updateGlobalBest() {
        double currBestFitness = Double.MAX_VALUE;
        int bestIndex = 0;
        // find bestValue and log bestIndex
        for (int i = 0; i < particleNum; i++) {
            if (parts.get(i).pFitness < currBestFitness) {
                currBestFitness = parts.get(i).pFitness;
                bestIndex = i;
            }
        }
        // update globalBestValue and globalBest
        if (currBestFitness < gFitness) {
            gFitness = currBestFitness;
            gPositionX = parts.get(bestIndex).pPositionX.clone();
            gCos = parts.get(bestIndex).pCos;
        }
        // 1029取消记录
        // allgFitness.add(gFitness);
        // allgPositionX.add(gPositionX.clone());
        // allCos.add(gCos);
    }

    /**
     * update particle Velocity and coord
     */
    public void updateVAndX(int n) {
        for (Particle p : parts) {
            if (p.stayNum >= MAXSTAY) {
                reassignLearnPart(p);
                p.stayNum = 0;
            }
            // update velocity for every dimension
            for (int i = 0; i < Particle.DIMENSION; i++) {
                // Linearly-Decreasing Inertia Weight
                double w = wmax - (wmax - wmin) * n / N;
                Particle pf = parts.get(p.learnPart[i]);
                double v = w * p.V[i] + c * random.nextDouble() * (pf.pPositionX[i] - p.X[i]);
                p.updatePartVAndV(i, v);
            }
        }
    }

    public boolean isConverge(int i) {
        return i >= N;
        // 1029修改判断
        // return fitSum >= MAXFITASS && i >= N;
    }

    // 1029废弃
    // public void writerFile() throws IOException {
    //     FileWriter fw = new FileWriter(fitDat, false);
    //     for (int i = 0; i < allgFitness.size(); i++) {
    //         String s = i
    //                 + " " + allgFitness.get(i)
    //                 + " " + allCos.get(i)
    //                 + " " + Arrays.toString(allgPositionX.get(i));
    //         fw.write(s);
    //         fw.write(System.getProperty("line.separator"));
    //     }
    //     fw.flush();
    //     fw.close();
    // }

    public void writerGlobalBest(int n) throws IOException {
        FileWriter fw = new FileWriter(fitDat, true);
        String s = n
                + " " + gFitness
                + " " + gCos
                + " " + Arrays.toString(gPositionX);
        fw.write(s);
        fw.write(lineEnd);
        fw.flush();
        fw.close();
    }


    public void writerStepInfo(int n) throws IOException {
        String datPath = String.format(stepDat, n);
        FileWriter fw = new FileWriter(datPath, false);
        for (Particle p : parts) {
            fw.write(p.toString());
            fw.write(lineEnd);
        }
        fw.write(n + Arrays.toString(gPositionX));
        fw.write(lineEnd);
        fw.write(String.valueOf(gFitness));
        fw.write(lineEnd);
        fw.flush();
        fw.close();
    }

    public void createDir() {
        File file = new File(datDir);
        if (!file.exists()) file.mkdir();
    }

    public void run() throws Exception {
        // 1029创建文件夹
        createDir();
        // 初始化粒子对象, 粒子历史最优, 全局最优; 第0次
        int i = 0;
        initialParts();
        updatePartBest();
        updateGlobalBest();
        writerStepInfo(i);
        writerGlobalBest(i);

        while (!isConverge(i)) {
            i++;
            updateVAndX(i);
            updatePartBest();
            updateGlobalBest();
            writerStepInfo(i);
            writerGlobalBest(i);
        }
        // writerFile();
    }

    public List<Particle> getParts() {
        return parts;
    }

    public static void main(String[] args) {
        Pso pso = new Pso();
        try {
            pso.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
