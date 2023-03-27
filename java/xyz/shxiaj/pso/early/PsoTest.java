package xyz.shxiaj.pso.early;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/23 15:47
 */
class PsoTest {

    private static double[] globalBest;
    private static double globalBestValue = Double.MAX_VALUE;
    private final Random random = new Random();

    // setting args for PSO
    private static final int particleNum = 200;
    private static final int N = 100;
    private static final double c1i = 2.5;
    private static final double c1f = 0.5;
    private static final double c2i = 0.5;
    private static final double c2f = 2.5;
    // private static final double w = 1.4;
    private static final double wmax = 0.9;
    private static final double wmin = 0.4;
    private final List<ParticleTest> particles = new ArrayList<>();
    private final List<Double> partValues = new ArrayList<>();

    /**
     * initial all particle to List particles
     */
    public void initialParticles() {
        for (int i = 0; i < particleNum; i++) {
            ParticleTest particle = new ParticleTest();
            particles.add(particle);
        }
    }

    /**
     * update globalBest
     */
    public void updateGlobalBest() {
        double currBestValue = Double.MAX_VALUE;
        int bestIndex = 0;
        // find bestValue and log bestIndex
        for (int i = 0; i < particleNum; i++) {
            if (particles.get(i).partBestValue < currBestValue) {
                currBestValue = particles.get(i).partBestValue;
                bestIndex = i;
            }
        }
        // update globalBestValue and globalBest
        if (currBestValue < globalBestValue) {
            globalBestValue = currBestValue;
            globalBest = particles.get(bestIndex).partBest.clone();
        }
    }

    /**
     * update particle Velocity
     */
    public void updateV(int n) {
        for (ParticleTest p : particles) {
            // update velocity for every dimension
            for (int i = 0; i < ParticleTest.DIMENSION; i++) {
                // Linearly-Decreasing Inertia Weight
                double w = wmax - (wmax - wmin) * n / N;
                // Time-Varying Acceleration Coefficients
                double c1 = c1i + (c1f - c1i) * n / N;
                double c2 = c2i + (c2f - c2i) * n / N;

                double v = w * p.V[i] + c1 * random.nextDouble() * (p.partBest[i] - p.X[i])
                        + c2 * random.nextDouble() * (globalBest[i] - p.partBest[i]);

                // limit v
                if (v > ParticleTest.VMAX) v = ParticleTest.VMAX;
                if (v < -ParticleTest.VMAX) v = -ParticleTest.VMAX;
                // update particle velocity
                p.V[i] = v;
            }
        }
    }

    /**
     * update particle coord and partBest
     */
    public void updateX() {
        for (ParticleTest p : particles) {
            for (int i = 0; i < ParticleTest.DIMENSION; i++) {
                p.X[i] = p.X[i] + p.V[i];
            }
        }
    }

    /**
     * update partBest
     */
    public void updatePartBest() {
        for (ParticleTest p : particles) {
            double currValue = p.calParticleCurrentValue();
            if (currValue < p.partBestValue) {
                p.partBestValue = currValue;
                p.partBest = p.X.clone();
            }
        }
    }

    /**
     * algorithm run
     */
    public void run() {
        initialParticles();
        List<Double> gValues = new ArrayList<>();
        List<double[]> gCoord = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            updatePartBest();
            updateGlobalBest();
            updateV(i);
            updateX();
            gValues.add(globalBestValue);
            gCoord.add(globalBest.clone());
            // System.out.println(globalBestValue);
            // System.out.println(Arrays.toString(globalBest));
        }
        for (int i = 0; i < gValues.size(); i++) {
            System.out.println(i + " x:" + gCoord.get(i)[0] + " y:" + gCoord.get(i)[1]
                    + " v:" + gValues.get(i));
        }
    }

    public static void main(String[] args) {
        PsoTest pso = new PsoTest();
        pso.run();
    }
}
