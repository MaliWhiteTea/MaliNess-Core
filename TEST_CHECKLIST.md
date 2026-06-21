# MaliNess Core 0.1.6 — Warp Test Checklist

Sunucu: Paper/Purpur 1.21.4 · Jar: `MaliNess-Core-0.1.6.jar`  
Test öncesi: `/mn reload` veya sunucu restart. En az **2 oyuncu** (normal + admin/OP) hazır olsun.

Durum sütunu: `[ ]` yapılmadı · `[x]` geçti · `[!]` hata

---

## 1. Kurulum ve config

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 1.1 | İlk açılış / reload sonrası `plugins/MaliNess-Core/configs/warp.yml` var | Dosya oluşur, `enabled: true` |[x]|
| 1.2 | `data/warps.yml` yokken reload | Boş `warps: []` ile oluşur |[x]|
| 1.3 | `configs/warp.yml` → `enabled: false` + reload | `/warp` → sistem kapalı mesajı |[x]|
| 1.4 | `/system off warp` + tekrar `on` | Sistem kapanır/açılır, veri korunur |[x]|

---

## 2. Admin — warp oluşturma

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 2.1 | `/warp ekle Market` (admin konumunda) | Başarı mesajı, `data/warps.yml` kaydı |[x]|
| 2.2 | `/warp set Spawn` (İngilizce alias) | Aynı davranış |[x]|
| 2.3 | İzinsiz oyuncu `/warp ekle Test` | Yetki hatası | |
| 2.4 | Konsol `/warp ekle Test` | Konsol desteklenmiyor mesajı | |
| 2.5 | Aynı isimle tekrar ekleme | Zaten var hatası | |
| 2.6 | Geçersiz isim: `A` (1 karakter) | Geçersiz isim | |
| 2.7 | Geçersiz isim: `12` (sadece rakam) | Geçersiz isim | |
| 2.8 | Geçersiz isim: `ekle`, `düzenle`, `warps` | Rezerve kelime / geçersiz | |
| 2.9 | Geçerli: `Market2`, `Pazar-Alanı`, `İstanbul` | Kabul edilir | |
| 2.10 | `market` ekle, sonra `Market` ekle | İkincisi çakışma (case-insensitive) | |
| 2.11 | `blocked-worlds` içindeki dünyada ekleme | Engellenir | |
| 2.12 | `max-warps: 2` ile limit aşımı | Limit mesajı | |

---

## 3. Admin — silme, düzenleme, açıklama

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 3.1 | `/warp sil Market` | Onay sorusu + butonlar | |
| 3.2 | `/evet` | Warp silinir, `logs/warp-admin.log` kaydı | |
| 3.3 | `/hayır` veya `/iptal` | Silinmez | |
| 3.4 | Konsol `/warp sil Market --confirm` | Onaysız silme | |
| 3.5 | `/warp düzenle Market YeniAd` | Yeniden adlandırma | |
| 3.6 | `/warp edit Market` + rename (İngilizce) | Çalışır | |
| 3.7 | `/warp duzenle Market` | **Çalışmamalı** (alias kaldırıldı) | |
| 3.8 | `/warp düzenle Market konum` (başka yerde) | Konum güncellenir | |
| 3.9 | `/warp düzenle Market açıklama &aPazar alanı` | Açıklama kaydedilir | |
| 3.10 | `/warp düzenle Market açıklama` (boş) | Açıklama silinir | |
| 3.11 | `/warp düzenle Market kapalı` | Warp kapanır | |
| 3.12 | `/warp düzenle Market açık` | Warp açılır | |
| 3.13 | Tab: `/warp düzenle Market ` | `konum`, `açıklama`, `açık`, `kapalı`, `on`, `off` vb. önerilir | |
| 3.14 | Konsol: rename, açık/kapalı, açıklama | Çalışır | |
| 3.15 | Konsol: `/warp düzenle Market konum` | Desteklenmiyor mesajı | |

---

## 4. Liste komutları

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 4.1 | `/warp` (argümansız) | Sayfalı liste, başlık | |
| 4.2 | `/warps` | `/warp` ile aynı liste | |
| 4.3 | `/warplar` | `/warps` ile aynı | |
| 4.4 | `/mn warps` | Liste çalışır | |
| 4.5 | 8+ warp ile `/warp 2` | 2. sayfa, ok tuşları | |
| 4.6 | Satıra tıklama (açık warp) | `/warp <isim>` çalışır | |
| 4.7 | Hover — açıklamalı warp | İsim + renkli açıklama | |
| 4.8 | Hover — açıklamasız warp | Sadece isim | |
| 4.9 | Normal oyuncu: kapalı warp | Listede **görünmez** | |
| 4.10 | Admin (`see-closed`): kapalı warp | Listede `✕` ile görünür | |
| 4.11 | Kapalı warp satırına tıklama | Kapalı mesajı, ışınlanmaz | |
| 4.12 | Kapalı warp hover (admin) | "Kapalı" bilgisi | |

---

## 5. Oyuncu — ışınlanma

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 5.1 | `/warp Market` | **"Market warpına ışınlanıyorsun... 5 saniye bekle"** | |
| 5.2 | `/warp market` (küçük harf) | `Market` warpına gider (case-insensitive) | |
| 5.3 | Warmup geri sayımı `4→3→2→1` | Mesajlar gelir | |
| 5.4 | Warmup sırasında hareket | İptal + uyarı | |
| 5.5 | Warmup sırasında hasar alma | İptal | |
| 5.6 | Warmup sırasında saldırı | İptal | |
| 5.7 | Warmup sırasında bineğe binme | İptal | |
| 5.8 | Warmup sırasında elytra | İptal | |
| 5.9 | Başarılı ışınlanma | Fire resistance, başarı mesajı | |
| 5.10 | `logs/warp-player.log` | Yalnızca **başarılı** ışınlanma yazılır | |
| 5.11 | İptal edilen ışınlanma | Loga **yazılmaz** | |
| 5.12 | Cooldown (5 sn) içinde tekrar warp | Cooldown mesajı | |
| 5.13 | `bypasstime` izni ile cooldown/warmup | Atlanır | |
| 5.14 | Kapalı warp'a `/warp Market` | Kapalı mesajı | |
| 5.15 | Olmayan warp | Bulunamadı mesajı | |
| 5.16 | Tab completion `/warp ` | Yalnızca açık warplar | |

---

## 6. Güvensiz konum

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 6.1 | Lav/ateş üstüne warp ekle + oyuncu ışınlan | Onay sorusu | |
| 6.2 | Onayla (`/evet`) | Işınlanır | |
| 6.3 | Reddet (`/hayır`) | Işınlanmaz | |
| 6.4 | `safe-teleport.ask-on-unsafe: false` | Doğrudan engel veya farklı davranış (config'e göre) | |

---

## 7. Geçersiz dünya

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 7.1 | Var olmayan dünya adıyla kayıtlı warp | Oyuncu: dünya yüklenemedi | |
| 7.2 | `invalid.broadcast` izinli admin | Oyun içi uyarı | |
| 7.3 | Reload sonrası tekrar uyarı | Dedupe (spam yok) | |

---

## 8. Ortak teleport (home etkileşimi)

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 8.1 | Home warmup varken `/warp Market` | Önceki warmup iptal, warp başlar | |
| 8.2 | Warp warmup varken `/home` | Önceki warmup iptal | |
| 8.3 | `/home` warmup/iptal/koruma | Önceki gibi çalışır (regresyon) | |

---

## 9. PlaceholderAPI

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 9.1 | `/papi parse me %malinesscore_warp_count%` | Açık warp sayısı | |
| 9.2 | `%malinesscore_warp_count_all%` | Toplam (see-closed ile artar) | |
| 9.3 | `%malinesscore_warp_list%` | Açık warp isimleri | |
| 9.4 | `%malinesscore_warp_market%` | Açık/Kapalı/Yok | |
| 9.5 | `%malinesscore_warp_desc_market%` | Açıklama metni | |

---

## 10. Reload ve kalıcılık

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 10.1 | Warp oluştur → `/mn reload` | Warplar korunur | |
| 10.2 | Reload sırasında aktif warmup | İptal olur | |
| 10.3 | Sunucu restart | `data/warps.yml` korunur | |

---

## 11. `/mn` entegrasyonu

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 11.1 | `/mn` yardımında warp + warps görünür | Yetkiye göre listelenir | |
| 11.2 | `/mn warp ekle Test` | Çalışır | |
| 11.3 | `/mn warps` | Liste çalışır | |

---

## Notlar / bulunan hatalar

```
(test sırasında buraya yaz)
```
