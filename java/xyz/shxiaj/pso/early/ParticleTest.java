package xyz.shxiaj.pso.early;

import java.util.Random;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/23 15:18
 */

class ParticleTest {
    // setting dimension for args
    public static final int DIMENSION = 2;
    // setting V maximum
    public static final double VMAX = 2;
    // setting particle coord
    public double[] X = new double[DIMENSION];
    // setting local optimum coord
    public double[] partBest = new double[DIMENSION];
    // setting particle velocity
    public double[] V = new double[DIMENSION];
    // setting current Value
    public double partBestValue = Double.MAX_VALUE;

    /**
     * The value getting by Rosenbrock function
     *
     * @return curValue
     */
    public double calParticleCurrentValue() {
        return Math.pow(1 - X[0], 2)
                + 100 * Math.pow(X[1] - X[0] * X[0], 2);
    }

    private void initialX() {
        Random random = new Random();
        for (int i = 0; i < DIMENSION; i++) {
            X[i] = random.nextInt(50);
        }
    }

    private void initialV() {
        Random random = new Random();
        for (int i = 0; i < DIMENSION; i++) {
            V[i] = random.nextDouble() * 4 - 2;
        }
    }

    public ParticleTest() {
        initialX();
        initialV();
    }

}
