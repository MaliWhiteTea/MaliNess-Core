# MaliNess Core — Test Checklist

> Son güncelleme: 2026-06-20  
> Durum: **Test edilecek** — audit düzeltmeleri sonrası henüz oyun içi doğrulanmadı.

Bu dosya ileride test turuna dönmek için tutulur. Tamamlanan maddelerin başına `[x]` koyulur.

---

## Audit düzeltmeleri

### Ev sistemi — veri bütünlüğü
- [ ] Aynı anda hızlı `/sethome` + `/delhome` veya `/renamehome` — ev verisi kaybolmamalı (son işlem tutarlı kalmalı)
- [ ] Sunucu reload/shutdown sonrası ev dosyaları bozulmamalı (atomik yazım)

### Onay sistemi
- [ ] Onay sorusu + buton satırı: `prefix + [/evet] [/hayır] [/iptal]` (küçük harf, `/` ile)
- [ ] `/evet` başarılı → "Onaylandı."
- [ ] Onay callback hata verirse → "Onay işlemi tamamlanamadı. Lütfen tekrar deneyin." + konsol log
- [ ] Token yok; `/evet` tek başına son bekleyen onayı kabul eder

### Onay sonrası yeniden doğrulama
- [ ] Silme onayı sonrası ev zaten silinmişse → hata, işlem yapılmaz
- [ ] Güvensiz ev onayı sonrası bineğe binilmişse → engellenmeli
- [ ] Güvensiz ev onayı sonrası cooldown dolmuşsa → normal akış

### Geçersiz ev verisi
- [ ] Bozuk YAML (`x: "abc"` vb.) oyunda ev olarak görünmemeli
- [ ] Konsolda prefix ile uyarı logu
- [ ] `maliness-core.home.invalid-home.broadcast` yetkili oyuncuya **bir kez** oyun içi uyarı (aynı ev için tekrar spam olmamalı)

### Işınlanma
- [ ] Async chunk/teleport hata durumunda `teleport-failed` mesajı
- [ ] Binek hayvanı sürerken `/home` — normal oyuncu engelli
- [ ] OP / `maliness-core.home.bypasstime` — binekteyken de ışınlanabilmeli

### God mod
- [ ] `/mn reload` sonrası god modu korunmalı
- [ ] Quit sonrası god kapanmalı (bilinçli davranış)
- [ ] God cache diske yazılamazsa reload sonrası god kaybolmamalı (nadir senaryo)

### Disabled sistem (`enabled: false`)
- [ ] `configs/home.yml` → `enabled: false` + `/mn reload` → komutlar "sistem kapalı" demeli
- [ ] Aynı config'te `enabled: true` + reload → sistem tekrar çalışmalı (servisler yeniden init gerektirmeden)

### Tab / yetki
- [ ] Yetkisiz oyuncu `/god`, `/heal`, ev komutları tab'ında öneri görmemeli
- [ ] Yetkisiz komut çalıştırınca plugin'in kendi hata mesajı (Minecraft default değil)

### `/mn` yardım
- [ ] Başlık: "Kullanabileceğin komutlar:"
- [ ] Sayfa satırı: `prefix + Sayfa X/N < >`
- [ ] Komut sayısı 18+ olunca 2. sayfa (ileride komut eklenince)

### Global komutlar
- [ ] PlugMan veya tam plugin reload sonrası `/evet` iki kez çalışmamalı (handler birikimi yok)

---

## Daha önce onaylanmış (regression — hızlı kontrol)

- [ ] T1 — Admin `/homes <oyuncu>` başlık metni
- [ ] T6 — Hover metinleri
- [ ] D1 — Delhome / genel akış
- [ ] Konsol `delhome <oyuncu> <ev> --confirm` zorunlu

---

## Bilerek ertelenen

- [ ] **HomeRateLimiter thread-safety (#8)** — pratik bypass görülmediyse düşük öncelik; gerekirse sonra fix + test

---

## Notlar

_Test sırasında bulunan sorunları buraya yaz:_

- 
