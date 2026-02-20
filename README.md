# 🚀 Softmax Yük Dengeleyici

Bu proje, **Softmax Action Selection** tabanlı istemci tarafı adaptif bir yük dengeleyici tasarımını içermektedir. Sistem, **non-stationary (zamanla değişen)** ve **gürültülü** sunucu ortamında ortalama gecikmeyi (latency) minimize etmeyi amaçlar.

---

## 🎯 Amaç

Round-Robin ve Random gibi klasik yöntemler sunucu performansını dikkate almaz ve değişen ortamlara uyum sağlayamaz. Bu projede, geçmiş performans verisine göre öğrenen ve olasılıksal seçim yapan Softmax yaklaşımı uygulanmıştır.

---

## 🧠 Özellikler

- Softmax Action Selection (Boltzmann keşfi)  
- Non-stationary sunucu simülasyonu (drift)  
- Gaussian gürültülü latency modeli  
- Sabit adım (alpha) ile adaptif öğrenme  
- Numerik stabil softmax uygulaması  
- Oracle karşılaştırmalı performans analizi  

---

## ⚙️ Çalışma Mantığı

1. K adet sunucudan oluşan ortam simüle edilir.  
2. Sunucuların ortalama gecikmeleri zamanla drift eder.  
3. Load balancer, Softmax ile sunucu seçer.  
4. Gözlenen latency’ye göre Q değerleri güncellenir.  
5. Sonuçlar oracle ile karşılaştırılarak raporlanır.

---

## ⏱️ Karmaşıklık

- İstek başına zaman karmaşıklığı: **O(K)**  
- Toplam simülasyon maliyeti: **O(T·K)**  
- Bellek karmaşıklığı: **O(K)**

---

## ▶️ Çalıştırma

Java 17+ ile:

çalıştırılarak simülasyon başlatılabilir.

---

## 📊 Sonuç

Softmax tabanlı yaklaşımın, non-stationary ve gürültülü ortamlarda oracle performansına yakınsadığı gözlemlenmiştir.

---




