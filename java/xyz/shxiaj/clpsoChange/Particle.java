package xyz.shxiaj.clpsoChange;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 10:02
 */
class Particle {
    public int id;
    public double cos;
    public double fitness;
    public double Pc;
    public int stayNum;
    public Process process = null;
    public static final int DIMENSION = 6;
    public static final double VMax = 0.2;
    public int[] learnPart = new int[DIMENSION];
    public final double[] X = new double[DIMENSION];
    public final double[] V = new double[DIMENSION];
    public final double[][] XLim = {{0, 360}, {0, 360}, {0, 360}, {-1, 1}, {-1, 1}, {-0.05, 0.05}};
    public final double[][] VLim = new double[XLim.length][2];
    public double[] pPositionX = new double[DIMENSION];
    public double pFitness = Double.MAX_VALUE;
    public double pCos = 0;

    /**
     * 更新粒子速度和位置
     *
     * @param i 维度
     * @param v 更新的速度
     */
    public void updatePartVAndV(int i, double v) {
        V[i] = Math.min(VLim[i][1], Math.max(VLim[i][0], v));
        // 上面一句相当于下面三句, 确实牛
        // if (v < VLim[i][0]) v = VLim[i][0];
        // if (v > VLim[i][1]) v = VLim[i][1];
        // V[i] = v;

        // 1029增加位置限制, 取消边界判断
        double x = X[i] + V[i];
        X[i] = Math.min(XLim[i][1], Math.max(XLim[i][0], x));
        // GLPSO 对于粒子position不做调整
    }

    /**
     * 更新粒子最优值
     */

    public void updatePartBest() throws IOException {
        getFitnessAndCos();
        if (fitness < pFitness) {
            pFitness = fitness;
            pPositionX = X.clone();
            pCos = cos;
        } else {
            stayNum++;
        }
    }

    public void execFitness() throws IOException {
        this.process = ScriptOperation.runEm(X, this.id);
    }

    public void getFitnessAndCos() throws IOException {
        this.fitness = ScriptOperation.getEneDat(this.id);
        this.cos = ScriptOperation.getDipoleDat(this.id);
    }

    /**
     * 判断当前位置是否超过了边界
     * 1029取消作用
     */
    public boolean isInLimit() {
        for (int i = 0; i < DIMENSION; i++) {
            if (X[i] < XLim[i][0] || X[i] > XLim[i][1]) return false;
        }
        return true;
    }

    private void initialXAndV() {
        Random r = new Random();
        for (int i = 0; i < DIMENSION; i++) {
            X[i] = r.nextDouble() * (XLim[i][1] - XLim[i][0]) + XLim[i][0];
            V[i] = r.nextDouble() * (VLim[i][1] - VLim[i][0]) + VLim[i][0];
        }
    }

    private void initialVLim() {
        for (int i = 0; i < XLim.length; i++) {
            // 先*10, 后/10 取消计算浮点数的误差, 舒服!
            VLim[i][1] = (XLim[i][1] - XLim[i][0]) * 10 * VMax / 10;
            VLim[i][0] = -VLim[i][1];
        }
    }

    /**
     * 类构造函数
     *
     * @param id      设置序号
     * @param Pc      设置交叉概率
     * @param stayNum 初始化未进化代数
     */
    public Particle(int id, double Pc, int stayNum) {
        initialVLim();
        initialXAndV();
        this.id = id;
        this.Pc = Pc;
        this.stayNum = stayNum;
    }

    @Override
    public String toString() {
        String sb = "Particle{" + "id=" + id +
                ", cos=" + cos +
                ", fitness=" + fitness +
                ", Pc=" + Pc +
                ", stayNum=" + stayNum +
                ", X=" + Arrays.toString(X) +
                ", V=" + Arrays.toString(V) +
                ", pX=" + Arrays.toString(pPositionX) +
                ", pFitness=" + pFitness +
                ", pCos=" + pCos +
                '}';
        return sb;
    }

}
