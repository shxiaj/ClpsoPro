package xyz.shxiaj.pso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 14:39
 */
class TestPso {
    public static void main(String[] args) {
        List<Particle> parts = new ArrayList<>();
        for (int i = 2; i < 5; i++) {
            Particle p = new Particle(i);
            parts.add(p);
            try {
                p.execFitness();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Particle p : parts) {
            try {
                p.process.waitFor();
                p.takeFitness();
                System.out.println(p.fitness);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
