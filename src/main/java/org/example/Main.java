package org.example;

import java.util.Arrays;

public class Main {

    /**
     * Agentic akış:
     * 1) Ortamı kur (non-stationary server cluster)
     * 2) Algoritmayı kur (Softmax load balancer)
     * 3) Simülasyonu çalıştır
     * 4) Metrikleri raporla: ortalama latency, regret benzeri fark, seçim dağılımı, Q değerleri
     */
    public static void main(String[] args) {
        // ---- Parametreler ----
        int K = 8;                     // sunucu sayısı
        int T = 50_000;                // istek sayısı (simülasyon adımı)
        long seed = 42;

        // Ortam parametreleri
        double baseMinMs = 30;
        double baseMaxMs = 220;
        double noiseStdMs = 25;        // latency gürültüsü
        double driftStdMs = 0.03;      // non-stationary drift (her adımda küçük kayma)

        // Softmax parametreleri
        double initialQ = -100.0;      // reward = -latency, başlangıç tahmini
        double temperature = 10.0;     // tau
        double alpha = 0.05;           // non-stationary için constant step-size önerilir

        // ---- Kurulum ----
        NonStationaryServerCluster env = new NonStationaryServerCluster(
                K, baseMinMs, baseMaxMs, noiseStdMs, driftStdMs, seed
        );

        SoftmaxLoadBalancer lb = new SoftmaxLoadBalancer(
                K, initialQ, temperature, alpha, seed + 1
        );

        // ---- Ölçümler ----
        double totalLatency = 0.0;
        double totalBestPossibleLatency = 0.0; // her adımda gerçek ortalamalardan en iyisini seçseydik (oracle) gibi

        int reportEvery = 5_000;

        // ---- Simülasyon ----
        for (int t = 1; t <= T; t++) {
            // ortam drift etsin
            env.tick();

            // sunucu seç
            int s = lb.selectServer();

            // isteği gönder
            double latency = env.request(s);

            // reward tanımı (latency minimize etmek için)
            double reward = -latency;

            // öğren
            lb.update(s, reward);

            // metrikler
            totalLatency += latency;

            // "oracle" en iyi ortalama latency (gerçeği bilseydik)
            double[] means = env.getTrueMeanLatencyMsSnapshot();
            double bestMean = Arrays.stream(means).min().orElse(latency);
            totalBestPossibleLatency += bestMean;

            // rapor
            if (t % reportEvery == 0) {
                double avgLatency = totalLatency / t;
                double oracleAvg = totalBestPossibleLatency / t;
                double gap = avgLatency - oracleAvg;

                System.out.println("t=" + t
                        + " | avgLatency(ms)=" + round2(avgLatency)
                        + " | oracleAvg(ms)=" + round2(oracleAvg)
                        + " | gap(ms)=" + round2(gap)
                        + " | tau=" + lb.getTemperature());
            }

            // (opsiyonel) annealing: zamanla exploration azaltmak istersen
            // Örn: tau <- max(0.5, tau * 0.99999)
            // lb.setTemperature(Math.max(0.5, lb.getTemperature() * 0.99999));
        }

        // ---- Final rapor ----
        double finalAvg = totalLatency / T;
        double finalOracle = totalBestPossibleLatency / T;
        double finalGap = finalAvg - finalOracle;

        System.out.println("\n=== FINAL REPORT ===");
        System.out.println("Total requests: " + T);
        System.out.println("K servers: " + K);
        System.out.println("Average latency (ms): " + round2(finalAvg));
        System.out.println("Oracle avg latency (ms): " + round2(finalOracle));
        System.out.println("Gap (ms): " + round2(finalGap));

        System.out.println("\nChosen counts per server:");
        System.out.println(Arrays.toString(lb.getCounts()));

        System.out.println("\nEstimated Q values (reward=-latency):");
        System.out.println(Arrays.toString(lb.getQ()));

        System.out.println("\nInterpretation:");
        System.out.println("- Q daha yüksek (0'a yakın) ise latency daha düşük demektir.");
        System.out.println("- Non-stationary drift olduğu için alpha>0 sabit adım, ortama uyum sağlar.");
        System.out.println("- tau büyürse daha çok keşif (exploration), küçülürse daha çok sömürü (exploitation).");
    }

    private static String round2(double x) {
        return String.format("%.2f", x);
    }
}
