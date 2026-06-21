# Belirlenmemiş sürüm — yapılacaklar

Henüz hangi sürüme alınacağı netleşmemiş özellikler ve fikirler.

Kaynak: pwarp eklenti araştırması (PlayerWarps, Player Warps, Olzie, iWarp, BuiltByBit vb.) ve genel MaliNess planları.

---

## Pwarp / oyuncu warp

- [ ] **GUI listesi entegrasyonu** — `/pwarp` listesini MenuService ile açma (altyapı 0.1.6.3’te)
- [ ] **Ekonomi maliyeti** — oluşturma, taşıma, rename, ışınlanma (Vault entegrasyonu)
- [ ] **Item tabanlı maliyet** — ekonomi olmadan oluşturma/taşıma bedeli
- [ ] **Kategori sistemi** — shop, farm, base vb.; liste/GUI filtresi
- [ ] **Oyuncu puanlama (rate)** — warp değerlendirme
- [ ] **Sponsor / öne çıkan warp** — listenin üstünde vurgulu gösterim
- [ ] **Sahiplik devri (transfer)** — başka oyuncuya pwarp aktarma
- [ ] **GUI ikonu** — elindeki item veya material ile warp simgesi (GUI ile birlikte)
- [ ] **Çoklu sunucu senkronu** — MySQL / proxy üzerinden pwarp paylaşımı
- [ ] **Warp alias** — aynı noktaya ikinci isim
- [ ] **Favoriler** — oyuncu bazlı sık kullanılan pwarp listesi
- [ ] **Inaktif pwarp otomatik silme** — X gün ziyaret yoksa kaldır
- [ ] **Upkeep / yenileme ücreti** — periyodik ödeme ile pwarp sürdürme (iWarp modeli)
- [ ] **Kendi pwarp’ına ücretsiz ışınlanma** — ekonomi modülü ile birlikte
- [ ] **Popülerliğe göre sıralama** — `visit-count` altyapısı 0.1.6.2’de geliyor; sıralama modu sonraki sürümde açılabilir

---

## Genel / diğer sistemler

- [ ] **Sistem bazlı list-mode** — `chat | gui` config (warp, pwarp, home, …)
- [ ] **Varsayılan list-mode** — global veya sistem bazlı varsayılan seçim
- [ ] **Warp listesi GUI entegrasyonu**
- [ ] **Home listesi GUI entegrasyonu**
- [ ] **Onay GUI’si (huni)** — chat `/evet`/`/hayır` yerine veya yanında; ConfirmationService ile entegrasyon
- [ ] **Menü ses / partikül** — açılış, kapanış, tıklama, sayfa değişimi
- [ ] **ItemsAdder softdepend** — GUI item + filler; `itemsadder: true` iken `material` / `custom_model_data` yok sayılır; plugin.yml softdepend
- [ ] **GUI PlaceholderAPI** — menü title / item name / lore satırlarında `%placeholder%` (pwarp listesi vb.)
- [ ] _(Buraya diğer sistemlerden sürümü belirsiz maddeler eklenecek)_
