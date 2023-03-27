package xyz.shxiaj.pso;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 10:03
 */
class Pso {

    private double[] gX = new double[Particle.DIMENSION];
    private double gFitness;

    // setting args for PSO
    public static final int CORES = 28;
    public static final double PRECISION = 0.000001;
    public static final int sameNum = 50;
    private static final int particleNum = 56;
    private static final int N = 200;
    private static final double c1i = 2.5;
    private static final double c1f = 0.5;
    private static final double c2i = 0.5;
    private static final double c2f = 2.5;
    private static final double wmax = 0.9;
    private static final double wmin = 0.4;
    private static final String fitDat = "./dat/gene.dat";
    private static final String stepDat = "./dat/step-%d.dat";

    private final Random random = new Random();
    private final List<Particle> parts = new ArrayList<>();
    private final List<double[]> allgX = new ArrayList<>();
    private final List<Double> allgFitness = new ArrayList<>();

    /**
     * initial all particle
     */
    public void initialParts() {
        for (int i = 0; i < particleNum; i++) {
            Particle p = new Particle(i);
            parts.add(p);
        }
    }

    /**
     * update partBest and run em
     */
    public void updatePartBest() throws Exception {
        Deque<Particle> queue = new ArrayDeque<>();
        int i = 0;
        while (i < particleNum) {
            while (queue.size() < CORES) {
                Particle p = parts.get(i);
                p.execFitness();
                queue.offer(p);
                i++;
            }
            Particle p = queue.poll();
            p.process.waitFor();
            p.updatePartBest();
        }
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
            gX = parts.get(bestIndex).pX.clone();
        }
        allgFitness.add(gFitness);
        allgX.add(gX.clone());
    }

    /**
     * update particle Velocity and coord
     */
    public void updateVAndX(int n) {
        for (Particle p : parts) {
            // update velocity for every dimension
            for (int i = 0; i < Particle.DIMENSION; i++) {
                // Linearly-Decreasing Inertia Weight
                double w = wmax - (wmax - wmin) * n / N;
                // Time-Varying Acceleration Coefficients
                double c1 = c1i + (c1f - c1i) * n / N;
                double c2 = c2i + (c2f - c2i) * n / N;

                double v = w * p.V[i] + c1 * random.nextDouble() * (p.pX[i] - p.X[i])
                        + c2 * random.nextDouble() * (gX[i] - p.X[i]);

                p.updatePartVAndV(i, v);
            }
        }
    }

    public boolean isConverge() {
        int size = allgFitness.size();
        if (size >= sameNum) {
            for (int i = size - 2; i > size - 1 - sameNum; i--) {
                if (Math.abs(allgFitness.get(i) - allgFitness.get(i + 1)) >= PRECISION) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void writerFile() throws IOException {
        FileWriter fw = new FileWriter(fitDat, false);
        for (int i = 0; i < allgFitness.size(); i++) {
            // String s = String.format("%5d%15.6f%10.3f%10.3f%10.3f%8.3f%8.3f", i, allgFitness.get(i),
            //         allgX.get(i)[0], allgX.get(i)[1], allgX.get(i)[2], allgX.get(i)[3], allgX.get(i)[4]);
            String s = i + ": " + allgFitness.get(i) + Arrays.toString(allgX.get(i));
            fw.write(s);
            fw.write(System.getProperty("line.separator"));
        }
        fw.flush();
        fw.close();
    }

    public void writerEveryStep(int n) throws IOException {
        String datPath = String.format(stepDat, n);
        FileWriter fw = new FileWriter(datPath, false);
        for (Particle p : parts) {
            fw.write(p.toString());
            fw.write(System.getProperty("line.separator"));
        }
        // fw.write(String.format("step-%d: gX = %10.3f%10.3f%10.3f%8.3f%8.3f", n, gX[0], gX[1], gX[2], gX[3], gX[4]));
        fw.write(n + ": " + Arrays.toString(gX));
        fw.write(System.getProperty("line.separator"));
        fw.write(String.valueOf(gFitness));
        fw.write(System.getProperty("line.separator"));
        fw.flush();
        fw.close();
    }

    public void run() throws Exception {
        initialParts();
        for (int i = 0; i < N; i++) {
            updatePartBest();
            updateGlobalBest();
            writerEveryStep(i);
            updateVAndX(i);
            if (isConverge()) break;
        }
        writerFile();
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
