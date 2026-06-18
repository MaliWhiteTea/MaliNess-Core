# MaliNess Core

MaliNess Network sunucuları için modüler bir Paper eklentisi. Her oyun sistemi kendi config ve lang dosyası ile yönetilir; sunucu açılışında aktif ve deaktif sistemler özet olarak konsola yazılır.

## Gereksinimler

| Gereksinim | Sürüm |
|------------|-------|
| Sunucu yazılımı | Paper veya Purpur **1.21.4** |
| Java | **21** |

## Kurulum

1. Projeyi derleyin veya releases bölümünden jar dosyasını indirin.
2. `MaliNess-Core-*.jar` dosyasını sunucunun `plugins/` klasörüne atın.
3. Sunucuyu başlatın veya yeniden başlatın.
4. İlk açılışta `plugins/MaliNess-Core/` altında config ve lang dosyaları otomatik oluşur.

## Derleme

```bash
mvn clean package
```

Çıktı jar dosyası: `target/MaliNess-Core-<sürüm>.jar`

IntelliJ kullanıyorsanız: **Maven → Lifecycle → package**

## Dosya yapısı (sunucu)

```
plugins/MaliNess-Core/
├── config.yml              # Genel ayarlar (prefix, renkler)
├── pluginlang.yml          # Eklenti ana mesajları ve onay butonları
├── configs/
│   ├── heal.yml
│   ├── feed.yml
│   ├── health.yml
│   ├── hunger.yml
│   ├── saturate.yml
│   ├── saturation.yml
│   ├── god.yml
│   └── home.yml
├── langs/
│   ├── heal.yml
│   ├── feed.yml
│   ├── health.yml
│   ├── hunger.yml
│   ├── saturate.yml
│   ├── saturation.yml
│   ├── god.yml
│   └── home.yml
├── data/
│   └── homes/              # Oyuncu ev verileri (<uuid>.yml)
└── logs/
    ├── home-player.log
    └── home-admin.log
```

Her sistem config dosyasında `enabled: true/false` ile açılıp kapatılabilir. Kapalı sistemler komut olarak kayıt edilmez.

## Mesaj sistemi

Tüm mesajlarda ortak bir prefix ve dört mesaj tipi kullanılır.

### Prefix

```
&#992fe0ᴍᴀʟɪɴᴇꜱꜱ ɴᴇᴛᴡᴏʀᴋ &f|
```

`config.yml` → `messages.prefix` bölümünden değiştirilebilir.

### Mesaj tipleri

| Tip | Renk | Kullanım |
|-----|------|----------|
| Uyarı | `&#ffd400` | Dikkat gerektiren durumlar |
| Hata | `&#ff1100` | Reddedilen işlemler |
| Normal | `&#f3e9e3` | Bilgi mesajları |
| Başarı | `&#87d498` | Başarılı işlemler |
| Belirteç | `&#ffdb57` | Dinamik veriler (`{player}`, `{home}` vb.) |

Renkler `config.yml` → `messages.colors` altından özelleştirilebilir.

### Lang dosyası formatı

```yaml
ornek-mesaj:
  type: success
  text: "{player} için işlem tamamlandı!"
```

Desteklenen renk kodları: `&a`, `&e` ve `&#RRGGBB` (hex).

## Sistem özeti

Eklenti **sekiz** oyun sistemi içerir:

| Grup | Sistemler | Amaç |
|------|-----------|------|
| Hızlı doldurma | heal, feed, saturate | Tek komutla tam veya kısmi doldurma |
| Hassas ayar | health, hunger, saturation | `set` / `add` / `remove` ile değer yönetimi |
| Oyuncu modu | god | Hasar almama ve saldırgan mob koruması |
| Konum | home | Ev kaydetme, ışınlanma ve yönetim |

| Minecraft değeri | Hızlı komut | Hassas komut |
|------------------|-------------|--------------|
| Can | `/heal` | `/health` |
| Açlık çubuğu | `/feed` | `/hunger` |
| Doygunluk çubuğu | `/saturate` | `/saturation` |

### `/mn` komutu

Tüm sistemler `/mn` (veya `/maliness`) alt komutu olarak da kullanılabilir. Başka bir eklentiyle komut çakışması olduğunda yedek yol olarak kullanılır.

```
/mn heal ...
/mn feed ...
/mn health set Oyuncu 10
/mn home
/mn sethome maden
/mn ev
```

Tab tamamlama tüm komutlarda desteklenir; boş argümanda Tab'a basıldığında o seviyedeki tüm seçenekler listelenir.

### Onay sistemi

Silme, üzerine yazma ve güvensiz ışınlanma gibi işlemlerde genel onay akışı kullanılır:

| Komut | Açıklama |
|-------|----------|
| `/evet` | Bekleyen onayı kabul eder |
| `/hayır` | Bekleyen onayı reddeder |
| `/iptal` | Bekleyen onayı veya warmup'ı iptal eder |

Onay mesajında `[/evet] [/hayır] [/iptal]` tıklanabilir butonları gösterilir. Komut olarak yazıldığında kod gerekmez; son bekleyen onaya otomatik uygulanır.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.confirm.use` | Onay komutlarını kullanma | `true` |

Mesajlar: `pluginlang.yml`

---

## Mevcut sistemler

### Heal — Can yenileme

| Komut | Açıklama |
|-------|----------|
| `/heal` | Kendi canını tam doldurur |
| `/heal <miktar>` | Kendine yarım kalp cinsinden can ekler (`2` = 1 kalp) |
| `/heal <oyuncu>` | Oyuncunun canını tam doldurur |
| `/heal <miktar> <oyuncu>` | Oyuncuya belirtilen miktarda can ekler |
| `/iyileştir ...` | `/heal` ile aynı (Türkçe alternatif) |
| `/mn heal ...` / `/mn iyileştir ...` | Alternatif komut |

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.heal.use` | Kendi canını yenileme | `op` |
| `maliness-core.heal.use.others` | Başkasının canını yenileme | `op` |

Config: `configs/heal.yml` — Lang: `langs/heal.yml`

---

### Feed — Açlık giderme

| Komut | Açıklama |
|-------|----------|
| `/feed` | Kendi açlık çubuğunu tam doldurur |
| `/feed <miktar>` | Kendine açlık puanı ekler (0–20) |
| `/feed <oyuncu>` | Oyuncunun açlık çubuğunu tam doldurur |
| `/feed <miktar> <oyuncu>` | Oyuncuya belirtilen miktarda açlık ekler |
| `/doyur ...` | `/feed` ile aynı (Türkçe alternatif) |
| `/mn feed ...` / `/mn doyur ...` | Alternatif komut |

> Yalnızca **açlık çubuğunu** etkiler; doygunluk çubuğuna dokunmaz.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.feed.use` | Kendi açlığını giderme | `op` |
| `maliness-core.feed.use.others` | Başkasının açlığını giderme | `op` |

Config: `configs/feed.yml` — Lang: `langs/feed.yml`

---

### Health — Can ayarlama

| Komut | Açıklama |
|-------|----------|
| `/health set <oyuncu> <1-20>` | Canı belirtilen değere ayarlar |
| `/health add <oyuncu> <1-20>` | Can ekler (üst sınır 20) |
| `/health remove <oyuncu> <1-19>` | Can azaltır |
| `/sağlık ayarla/ekle/azalt ...` | Türkçe komut ve alt komut alternatifleri |
| `/mn health ...` / `/mn sağlık ...` | Alternatif komut |

Alt komutlar İngilizce ve Türkçe birbirinin yerine kullanılabilir (`set` = `ayarla`, `add` = `ekle`, `remove` = `azalt`).

> `remove` ile azaltılacak miktar mevcut candan fazla olamaz. Can **1 veya daha az** ise azaltma yapılamaz (oyuncunun ölmesi engellenir).

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.health.use` | Can ayarlama komutları | `op` |

Config: `configs/health.yml` — Lang: `langs/health.yml`

---

### Hunger — Açlık ayarlama

| Komut | Açıklama |
|-------|----------|
| `/hunger set <oyuncu> <1-20>` | Açlığı belirtilen değere ayarlar |
| `/hunger add <oyuncu> <1-20>` | Açlık ekler |
| `/hunger remove <oyuncu> <1-19>` | Açlık azaltır |
| `/açlık ayarla/ekle/azalt ...` | Türkçe komut ve alt komut alternatifleri |
| `/mn hunger ...` / `/mn açlık ...` | Alternatif komut |

> `add` ile girilen değer 20'yi geçerse veya toplam 20'nin üstüne çıkarsa açlık doğrudan **20'ye** ayarlanır. `remove` ile azaltılacak miktar mevcut açlıktan fazla olamaz.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.hunger.use` | Açlık ayarlama komutları | `op` |

Config: `configs/hunger.yml` — Lang: `langs/hunger.yml`

---

### Saturate — Doygunluk doldurma

| Komut | Açıklama |
|-------|----------|
| `/saturate` | Kendi doygunluğunu tam doldurur ve açlığı giderir |
| `/saturate <miktar>` | Kendine doygunluk puanı ekler, açlığı giderir |
| `/saturate <oyuncu>` | Oyuncunun doygunluğunu tam doldurur ve açlığını giderir |
| `/saturate <miktar> <oyuncu>` | Oyuncuya kısmi doygunluk ekler ve açlığını giderir |
| `/tokla ...` | `/saturate` ile aynı (Türkçe alternatif) |
| `/mn saturate ...` / `/mn tokla ...` | Alternatif komut |

> Her kullanımda doygunluk doldurulur **ve** açlık çubuğu 20'ye çekilir (`/feed` gibi). Yalnızca doygunluk ayarlamak için `/saturation` kullanın.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.saturate.use` | Kendi doygunluğunu doldurma | `op` |
| `maliness-core.saturate.use.others` | Başkasının doygunluğunu doldurma | `op` |

Config: `configs/saturate.yml` — Lang: `langs/saturate.yml`

---

### Saturation — Doygunluk ayarlama

| Komut | Açıklama |
|-------|----------|
| `/saturation set <oyuncu> <1-20>` | Doygunluğu belirtilen değere ayarlar |
| `/saturation add <oyuncu> <1-20>` | Doygunluk ekler |
| `/saturation remove <oyuncu> <1-19>` | Doygunluk azaltır |
| `/doygunluk ayarla/ekle/azalt ...` | Türkçe komut ve alt komut alternatifleri |
| `/mn saturation ...` / `/mn doygunluk ...` | Alternatif komut |

> `add` ile girilen değer 20'yi geçerse veya toplam 20'nin üstüne çıkarsa doygunluk doğrudan **20'ye** ayarlanır. `remove` ile azaltılacak miktar mevcut doygunluktan fazla olamaz. Açlık çubuğuna dokunmaz.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.saturation.use` | Doygunluk ayarlama komutları | `op` |

Config: `configs/saturation.yml` — Lang: `langs/saturation.yml`

---

### God — God modu

| Komut | Açıklama |
|-------|----------|
| `/god` | Kendi god modunu açar veya kapatır (toggle) |
| `/god <oyuncu>` | Oyuncunun god modunu açar veya kapatır |
| `/god ayarla <oyuncu> <aktif\|deaktif>` | Oyuncunun god modunu belirtilen duruma ayarlar |
| `/god set <oyuncu> <on\|off>` | İngilizce alternatif (`active`/`deactivate` da geçerli) |
| `/tanrı ...` | `/god` ile aynı (Türkçe alternatif) |
| `/mn god ...` / `/mn tanrı ...` | Alternatif komut |

God modu açıkken oyuncu **hiçbir kaynaktan hasar almaz** ve **saldırgan moblar** oyuncuyu hedef almaz. Mod açıldığında yakındaki mevcut hedefler de temizlenir.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.god.use` | Kendi god modunu yönetme | `op` |
| `maliness-core.god.use.others` | Başkasının god modunu yönetme | `op` |

Config: `configs/god.yml` — Lang: `langs/god.yml`

`configs/god.yml` içinde `clear-target-radius` ile god açıldığında hedefi temizlenecek mob mesafesi ayarlanabilir (varsayılan: `64`).

---

### Home — Ev sistemi

#### Komutlar

| Komut | Açıklama |
|-------|----------|
| `/sethome [isim]` | Bulunduğun konumu ev olarak kaydeder |
| `/home [isim]` | Kayıtlı eve ışınlanır |
| `/delhome [isim]` | Evi siler (onay gerekir) |
| `/homes [oyuncu]` | Evleri listeler |
| `/renamehome <eski> <yeni>` | Ev adını değiştirir |
| `/evayarla`, `/ev`, `/house`, `/remhome` | Kısa / Türkçe alternatifler |
| `/evadıdeğiştir`, `/evismideğiştir` | `/renamehome` alternatifleri |
| `/home <oyuncu> <ev>` | Yetkili: oyuncunun evine anında ışınlanma |
| `/delhome <oyuncu> <ev>` | Yetkili: oyuncunun evini silme (oyuncu onaylı, konsol onaysız) |
| `/mn home ...`, `/mn sethome ...`, `/mn ev` vb. | Alternatif komut |

#### İsim kuralları

- Varsayılan isim: `ev` (isimsiz `/sethome` için)
- `ev` doluysa isimsiz kayıtta sırayla `ev-2`, `ev-3` … oluşur
- Geçerli karakterler: `a-z`, `0-9`, `-`, `_` (en fazla 12 karakter, küçük harfe normalize edilir)
- Rezerve isimler: `evet`, `hayir`, `hayır`, `yes`, `no`, `iptal`

#### `/home` davranışı

| Durum | Sonuç |
|-------|--------|
| Tek ev var | İsimsiz `/home` → o eve ışınlanır (adı `ev` olmasa bile) |
| Birden fazla ev var | İsimsiz `/home` → hata + ev listesi gösterilir |
| İsim belirtilmiş | Belirtilen eve ışınlanır |

#### Warmup ve cooldown

Varsayılan değerler (`configs/home.yml`):

| Ayar | Varsayılan |
|------|------------|
| Warmup | 5 saniye |
| Eve gitme cooldown | 10 saniye |
| Ev kaydetme cooldown | 3 saniye |
| Fire resistance süresi | 3 saniye |

Warmup akışı (sohbet):

1. `Eve ışınlanıyorsun... 5 saniye bekle`
2. Geri sayım: `4 → 3 → 2 → 1`
3. `Işınlanıyorsun...`

Warmup sırasında **hareket** (blok değişimi), **hasar**, **saldırı**, **elytra**, **araç** veya **düşme** → anında iptal.

**OP** oyuncular ve `maliness-core.home.bypasstime` izni olanlar warmup ile tüm cooldown'lardan muaf tutulur.

#### Güvenlik ve konum

- `sethome`: dünya sınırı ve yükseklik kontrolü
- `blocked-worlds` config ile belirli dünyalarda ev kaydı engellenir
- Işınlanmada güvensiz konum tespit edilirse onay istenir (lav, ateş, kaktüs vb.)
- Eve varınca kısa süreli fire resistance
- Chunk preload ile ışınlanma

#### Ev limiti

- Varsayılan: **1 ev** (`limits.default-max-homes`)
- `maliness-core.home.count.N` izinleriyle artırılır (en yüksek N geçerli, örn. `.3` → 3 ev)
- Limit aşıldığında `/home` ve `/sethome` kapalıdır; mevcut evler silinmez
- `/homes` listesinde limit uyarısı gösterilir

#### Liste ve tıklama

- Kendi `/homes` listende satırlar tıklanabilir → `/home <ev>`
- Yetkili `/homes <oyuncu>` listesinde satırlar tıklanabilir → anında `/home <oyuncu> <ev>`

#### Konsol

- Konsoldan yalnızca `delhome <oyuncu> <ev>` desteklenir (onay yok, anında siler)
- Diğer ev komutları oyuncu gerektirir

#### Rate limit

Çok sayıda hatalı komut (geçersiz isim, olmayan ev, olmayan oyuncu vb.) varsayılan olarak **5 hata / 30 saniye** penceresinde komutları geçici kilitler. Başarılı işlem sayacı sıfırlar.

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.home.use` | Eve ışınlanma | `true` |
| `maliness-core.home.sethome` | Ev kaydetme | `true` |
| `maliness-core.home.delhome` | Ev silme | `true` |
| `maliness-core.home.homes` | Kendi evlerini listeleme | `true` |
| `maliness-core.home.rename` | Ev adı değiştirme | `true` |
| `maliness-core.home.bypasstime` | Warmup ve cooldown atlama | `op` |
| `maliness-core.home.count.N` | En fazla N ev | — |
| `maliness-core.home.others.list` | Başkasının evlerini listeleme | `op` |
| `maliness-core.home.others.teleport` | Başkasının evine ışınlanma | `op` |
| `maliness-core.home.others.delete` | Başkasının evini silme | `op` |

Config: `configs/home.yml` — Lang: `langs/home.yml`

---

## Türkçe komut özeti

| İngilizce | Türkçe / alternatif |
|-----------|---------------------|
| `/heal` | `/iyileştir` |
| `/feed` | `/doyur` |
| `/health` | `/sağlık` |
| `/hunger` | `/açlık` |
| `/saturate` | `/tokla` |
| `/saturation` | `/doygunluk` |
| `/god` | `/tanrı` |
| `/sethome` | `/evayarla` |
| `/home` | `/ev`, `/house` |
| `/delhome` | `/remhome` |
| `/renamehome` | `/evadıdeğiştir`, `/evismideğiştir` |
| — | `/evet`, `/hayır`, `/iptal` |

Hassas ayar sistemlerinde alt komutlar: `set`/`ayarla`, `add`/`ekle`, `remove`/`azalt`

God modu durumları: `aktif`/`deaktif`, `on`/`off`, `active`/`deactivate`

## Proje yapısı (kaynak kod)

```
src/main/java/com/mertaliakcay/malinesscore/
├── MaliNessCore.java
├── command/                    # /mn komutu
├── confirmation/               # /evet /hayır /iptal onay sistemi
├── messages/                   # Mesaj tipi ve renk sistemi
├── systems/
│   ├── AbstractGameSystem.java
│   ├── SystemManager.java
│   ├── heal/
│   ├── feed/
│   ├── health/
│   ├── hunger/
│   ├── saturate/
│   ├── saturation/
│   ├── god/
│   └── home/
└── util/                       # Config, lang, renk, tab tamamlama

src/main/resources/
├── config.yml
├── plugin.yml
├── pluginlang.yml
├── configs/
└── langs/
```

## Lisans

Bu proje MaliNess Network için geliştirilmektedir.

## Yazar

**Mert Ali AKÇAY** — [GitHub](https://github.com/MaliWhiteTea)
