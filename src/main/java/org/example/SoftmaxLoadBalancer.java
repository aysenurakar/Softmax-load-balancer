package org.example;

import java.util.Arrays;
import java.util.Random;

/**
 * Softmax Action Selection (Boltzmann exploration) load balancer.
 *
 * Reward = -latency  (latency düşükse reward yüksek)
 *
 * Non-stationary ortamda uyum için:
 * Q değerlerini "constant step size" (alpha) ile güncelliyoruz (EWMA gibi).
 */
public class SoftmaxLoadBalancer {
    private final int k;
    private final double[] q;        // beklenen reward tahmini (Q-value)
    private final int[] n;           // seçilme sayıları (analiz için)
    private final Random rnd;

    // hiperparametreler
    private double temperature;      // tau: büyük => daha random, küçük => daha greedy
    private final double alpha;      // constant step size (0<alpha<=1)

    public SoftmaxLoadBalancer(int k, double initialQ, double temperature, double alpha, long seed) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (temperature <= 0) throw new IllegalArgumentException("temperature must be > 0");
        if (alpha <= 0 || alpha > 1) throw new IllegalArgumentException("alpha must be in (0,1]");

        this.k = k;
        this.q = new double[k];
        this.n = new int[k];
        Arrays.fill(this.q, initialQ);
        this.temperature = temperature;
        this.alpha = alpha;
        this.rnd = new Random(seed);
    }

    public int selectServer() {
        double[] probs = softmax(q, temperature);
        double r = rnd.nextDouble();
        double cum = 0.0;

        for (int i = 0; i < k; i++) {
            cum += probs[i];
            if (r <= cum) return i;
        }
        // Numerik taşma durumunda son index
        return k - 1;
    }

    public void update(int chosenServer, double reward) {
        n[chosenServer]++;

        // constant step-size update: Q <- Q + alpha*(r - Q)
        q[chosenServer] = q[chosenServer] + alpha * (reward - q[chosenServer]);
    }

    public double[] getQ() {
        return Arrays.copyOf(q, q.length);
    }

    public int[] getCounts() {
        return Arrays.copyOf(n, n.length);
    }

    public void setTemperature(double temperature) {
        if (temperature <= 0) throw new IllegalArgumentException("temperature must be > 0");
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    // Numerik stabil softmax
    private static double[] softmax(double[] values, double tau) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : values) max = Math.max(max, v);

        double sum = 0.0;
        double[] exp = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            // (v - max) / tau numerik stabilite sağlar
            exp[i] = Math.exp((values[i] - max) / tau);
            sum += exp[i];
        }

        double[] p = new double[values.length];
        if (sum == 0) {
            // çok uç bir durumda uniform dön
            Arrays.fill(p, 1.0 / values.length);
            return p;
        }

        for (int i = 0; i < values.length; i++) p[i] = exp[i] / sum;
        return p;
    }
}
