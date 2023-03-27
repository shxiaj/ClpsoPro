package xyz.shxiaj.pso;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 10:02
 */
class Particle {
    public int id;
    // public double cos;
    public double fitness;
    public Process process = null;
    public static final int DIMENSION = 6;
    public final double[] X = new double[DIMENSION];
    public final double[] V = new double[DIMENSION];
    public final double[][] XLim = {{0, 359.9999}, {0, 359.9999}, {0, 359.9999}, {-0.5, 0.5}, {-0.5, 0.5}, {-0.05, 0.05}};
    public final double[][] VLim = {{-36, 36}, {-36, 36}, {-36, 36}, {-0.05, 0.05}, {-0.05, 0.05}, {-0.01, 0.01}};
    public double[] pX = new double[DIMENSION];
    public double pFitness = Double.MAX_VALUE;
    // public double pCos = 0;

    public void updatePartVAndV(int i, double v) {
        if (v < VLim[i][0]) v = VLim[i][0];
        if (v > VLim[i][1]) v = VLim[i][1];
        V[i] = v;

        double x = X[i] + V[i];
        if (x < XLim[i][0]) x = XLim[i][0];
        if (x > XLim[i][1]) x = XLim[i][1];
        X[i] = x;
    }

    public void updatePartBest() throws IOException {
        double fitness = takeFitness();
        if (fitness < pFitness) {
            pFitness = fitness;
            pX = X.clone();
        }
    }

    public Process execFitness() throws IOException {
        this.process = ScriptOperation.runEm(X, this.id);
        return this.process;
    }

    public double takeFitness() throws IOException {
        this.fitness = ScriptOperation.readDat(this.id);
        return this.fitness;
    }

    private void initialXAndV() {
        Random r = new Random();
        for (int i = 0; i < DIMENSION; i++) {
            X[i] = r.nextDouble() * (XLim[i][1] - XLim[i][0]) + XLim[i][0];
            V[i] = r.nextDouble() * (VLim[i][1] - VLim[i][0]) + VLim[i][0];
        }
    }

    // private void initialV() {
    //     Random r = new Random();
    //     for (int i = 0; i < DIMENSION; i++) {
    //         V[i] = r.nextDouble() * (VLim[i][1] - VLim[i][0]) + VLim[i][0];
    //     }
    // }

    public Particle(int id) {
        initialXAndV();
        // initialV();
        this.id = id;
    }

    // @Override
    // public String toString() {
    //     return "Particle{" +
    //             "id=" + id +
    //             ", fitness=" + fitness +
    //             ", X=[" + String.format("%10.3f%10.3f%10.3f%8.3f%8.3f", X[0], X[1], X[2], X[3], X[4]) +
    //             "], V=[" + String.format("%10.3f%10.3f%10.3f%8.3f%8.3f", V[0], V[1], V[2], V[3], V[4]) +
    //             "], pX=[" + String.format("%10.3f%10.3f%10.3f%8.3f%8.3f", pX[0], pX[1], pX[2], pX[3], pX[4]) +
    //             "], pFitness=" + pFitness +
    //             '}';
    // }

    @Override
    public String toString() {
        return "Particle{" +
                "id=" + id +
                ", fitness=" + fitness +
                ", X=" + Arrays.toString(X) +
                ", V=" + Arrays.toString(V) +
                ", pX=" + Arrays.toString(pX) +
                ", pFitness=" + pFitness +
                '}';
    }
}
