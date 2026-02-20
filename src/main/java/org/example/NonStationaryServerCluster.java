package org.example;

import java.util.Random;

/**
 * K sunuculu non-stationary (drifting) latency ortamı simülasyonu.
 *
 * - Her sunucunun "gerçek" ortalama latency'si zamanla drift eder
 * - Her istek latency'si gürültülüdür (Gaussian noise)
 *
 * İstemci load balancer sadece gözlenen latency'yi görür.
 */
public class NonStationaryServerCluster {
    private final int k;
    private final double[] trueMeanLatencyMs; // gerçek ama bilinmeyen ortalama latency
    private final double noiseStdMs;
    private final double driftStdMs;          // her adımda ortalamaya eklenecek drift gürültüsü
    private final Random rnd;

    public NonStationaryServerCluster(int k,
                                      double baseMinMs,
                                      double baseMaxMs,
                                      double noiseStdMs,
                                      double driftStdMs,
                                      long seed) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (baseMinMs <= 0 || baseMaxMs <= 0 || baseMaxMs < baseMinMs) {
            throw new IllegalArgumentException("invalid base latency range");
        }
        if (noiseStdMs < 0 || driftStdMs < 0) throw new IllegalArgumentException("std must be >=0");

        this.k = k;
        this.trueMeanLatencyMs = new double[k];
        this.noiseStdMs = noiseStdMs;
        this.driftStdMs = driftStdMs;
        this.rnd = new Random(seed);

        // başlangıç ortalamalarını rastgele dağıt
        for (int i = 0; i < k; i++) {
            trueMeanLatencyMs[i] = baseMinMs + rnd.nextDouble() * (baseMaxMs - baseMinMs);
        }
    }

    /**
     * Zaman ilerlesin: her sunucunun gerçek ortalama latency'si biraz kayar (drift).
     */
    public void tick() {
        if (driftStdMs == 0) return;
        for (int i = 0; i < k; i++) {
            trueMeanLatencyMs[i] += rnd.nextGaussian() * driftStdMs;

            // latency negatif olamaz, alt limit koy
            if (trueMeanLatencyMs[i] < 1) trueMeanLatencyMs[i] = 1;
        }
    }

    /**
     * chosenServer'a istek gönder, gözlenen latency döndür.
     */
    public double request(int chosenServer) {
        if (chosenServer < 0 || chosenServer >= k) throw new IllegalArgumentException("bad server index");

        double noise = rnd.nextGaussian() * noiseStdMs;
        double observed = trueMeanLatencyMs[chosenServer] + noise;

        // gözlenen latency de negatif olamaz
        if (observed < 1) observed = 1;
        return observed;
    }

    public double[] getTrueMeanLatencyMsSnapshot() {
        return trueMeanLatencyMs.clone();
    }
}
