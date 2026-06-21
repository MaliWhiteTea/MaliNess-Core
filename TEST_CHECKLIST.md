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
| 2.3 | İzinsiz oyuncu `/warp ekle Test` | Yetki hatası |[x]|
| 2.4 | Konsol `/warp ekle Test` | Konsol desteklenmiyor mesajı |[x]|
| 2.5 | Aynı isimle tekrar ekleme | Zaten var hatası |[x]|
| 2.6 | Geçersiz isim: `A` (1 karakter) | Geçersiz isim |[x]|
| 2.7 | Geçersiz isim: `12` (sadece rakam) | Geçersiz isim |[x]|
| 2.8 | Geçersiz isim: `ekle`, `düzenle`, `warps` | Rezerve kelime / geçersiz |[x]|
| 2.9 | Geçerli: `Market2`, `Pazar-Alanı`, `İstanbul` | Kabul edilir |[x]|
| 2.10 | `market` ekle, sonra `Market` ekle | İkincisi çakışma (case-insensitive) |[x]|
| 2.11 | `blocked-worlds` içindeki dünyada ekleme | Engellenir |[x]|
| 2.12 | `max-warps: 2` ile limit aşımı | Limit mesajı |[x]|

---

## 3. Admin — silme, düzenleme, açıklama

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 3.1 | `/warp sil Market` | Onay sorusu + butonlar |[x]|
| 3.2 | `/evet` | Warp silinir, `logs/warp-admin.log` kaydı |[x]|
| 3.3 | `/hayır` veya `/iptal` | Silinmez |[x]|
| 3.4 | Konsol `/warp sil Market --confirm` | Onaysız silme |[x]|
| 3.5 | `/warp düzenle Market YeniAd` | Yeniden adlandırma |[x]|
| 3.6 | `/warp edit Market` + rename (İngilizce) | Çalışır |[x]|
| 3.7 | `/warp duzenle Market` | **Çalışmamalı** (alias kaldırıldı) |[x]|
| 3.8 | `/warp düzenle Market konum` (başka yerde) | Konum güncellenir |[x]|
| 3.9 | `/warp düzenle Market açıklama &aPazar alanı` | Açıklama kaydedilir |[x]|
| 3.10 | `/warp düzenle Market açıklama` (boş) | Açıklama silinir |[x]|
| 3.11 | `/warp düzenle Market kapalı` | Warp kapanır |[x]|
| 3.12 | `/warp düzenle Market açık` | Warp açılır |[x]|
| 3.13 | Tab: `/warp düzenle Market ` | `konum`, `açıklama`, `açık`, `kapalı`, `on`, `off` vb. önerilir |[x]|
| 3.14 | Konsol: rename, açık/kapalı, açıklama | Çalışır |[x]|
| 3.15 | Konsol: `/warp düzenle Market konum` | Desteklenmiyor mesajı |[x]|

---

## 4. Liste komutları

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 4.1 | `/warp` (argümansız) | Sayfalı liste, başlık |[x]|
| 4.2 | `/warps` | `/warp` ile aynı liste |[x]|
| 4.3 | `/warplar` | `/warps` ile aynı |[x]|
| 4.4 | `/mn warps` | Liste çalışır |[x]|
| 4.5 | 8+ warp ile `/warp 2` | 2. sayfa, ok tuşları |[x]|
| 4.6 | Satıra tıklama (açık warp) | `/warp <isim>` çalışır |[x]|
| 4.7 | Hover — açıklamalı warp | İsim + renkli açıklama |[x]|
| 4.8 | Hover — açıklamasız warp | Sadece isim |[x]|
| 4.9 | Normal oyuncu: kapalı warp | Listede **görünmez** |[x]|
| 4.10 | Admin (`see-closed`): kapalı warp | Listede `✕` ile görünür |[x]|
| 4.11 | Kapalı warp satırına tıklama | Kapalı mesajı, ışınlanmaz |[x]|
| 4.12 | Kapalı warp hover (admin) | "Kapalı" bilgisi |[x]|

---

## 5. Oyuncu — ışınlanma

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 5.1 | `/warp Market` | **"Market warpına ışınlanıyorsun... 5 saniye bekle"** |[x]|
| 5.2 | `/warp market` (küçük harf) | `Market` warpına gider (case-insensitive) |[x]|
| 5.3 | Warmup geri sayımı `4→3→2→1` | Mesajlar gelir |[x]|
| 5.4 | Warmup sırasında hareket | İptal + uyarı |[x]|
| 5.5 | Warmup sırasında hasar alma | İptal |[x]|
| 5.6 | Warmup sırasında saldırı | İptal |[x]|
| 5.7 | Warmup sırasında bineğe binme | İptal |[x]|
| 5.8 | Warmup sırasında elytra | İptal |[x]|
| 5.9 | Başarılı ışınlanma | Fire resistance, başarı mesajı |[x]|
| 5.10 | `logs/warp-player.log` | Yalnızca **başarılı** ışınlanma yazılır |[x]|
| 5.11 | İptal edilen ışınlanma | Loga **yazılmaz** |[x]|
| 5.12 | Cooldown (5 sn) içinde tekrar warp | Cooldown mesajı |[x]|
| 5.13 | `bypasstime` izni ile cooldown/warmup | Atlanır |[x]|
| 5.14 | Kapalı warp'a `/warp Market` | Kapalı mesajı |[x]|
| 5.15 | Olmayan warp | Bulunamadı mesajı |[x]|
| 5.16 | Tab completion `/warp ` | Yalnızca açık warplar |[x]|

---

## 6. Güvensiz konum

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 6.1 | Lav/ateş üstüne warp ekle + oyuncu ışınlan | Onay sorusu |[x]|
| 6.2 | Onayla (`/evet`) | Işınlanır |[x]|
| 6.3 | Reddet (`/hayır`) | Işınlanmaz |[x]|
| 6.4 | `safe-teleport.ask-on-unsafe: false` | Doğrudan engel veya farklı davranış (config'e göre) |[x]|

---

## 7. Geçersiz dünya

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 7.1 | Var olmayan dünya adıyla kayıtlı warp | Oyuncu: dünya yüklenemedi |[x]|
| 7.2 | `invalid.broadcast` izinli admin | Oyun içi uyarı |[x]|
| 7.3 | Reload sonrası tekrar uyarı | Dedupe (spam yok) |[x]|

---

## 8. Ortak teleport (home etkileşimi)

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 8.1 | Home warmup varken `/warp Market` | Önceki warmup iptal, warp başlar |[!]|
| 8.2 | Warp warmup varken `/home` | Önceki warmup iptal |[!]|
| 8.3 | `/home` warmup/iptal/koruma | Önceki gibi çalışır (regresyon) |[x]|

---

## 9. PlaceholderAPI

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 9.1 | `/papi parse me %malinesscore_warp_count%` | Açık warp sayısı |[x]|
| 9.2 | `%malinesscore_warp_count_all%` | Toplam (see-closed ile artar) |[x]|
| 9.3 | `%malinesscore_warp_list%` | Açık warp isimleri |[x]|
| 9.4 | `%malinesscore_warp_market%` | Açık/Kapalı/Yok |[x]|
| 9.5 | `%malinesscore_warp_desc_market%` | Açıklama metni |[x]|

---

## 10. Reload ve kalıcılık

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 10.1 | Warp oluştur → `/mn reload` | Warplar korunur |[x]|
| 10.2 | Reload sırasında aktif warmup | İptal olur |[x]|
| 10.3 | Sunucu restart | `data/warps.yml` korunur |[x]|

---

## 11. `/mn` entegrasyonu

| # | Test | Beklenen | Durum |
|---|------|----------|-------|
| 11.1 | `/mn` yardımında warp + warps görünür | Yetkiye göre listelenir |[x]|
| 11.2 | `/mn warp ekle Test` | Çalışır |[x]|
| 11.3 | `/mn warps` | Liste çalışır |[x]|

---

## Notlar / bulunan hatalar

```
Adminler kapalı olan bir warpa ışınlanmak istediklerinde bu warp kapalı yine de ışınlanmak istiyor musun evet hayır sorusu sorulmalı. oyunculara böyle bir warp olmadığı söylenmeli


8.1 8.2 de oyuncu bir warpa ışınlanmaktayken evine gitmeye çalışırsa zaten eve gidiyorsun mesajı alıyor ama oyuncu eve gitmiyor aslında warpa gidiyor. eve gitmekteyken de warpa gitmeye çalışırsa tam tersi oluyor.
```
