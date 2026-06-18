# MaliNess Core

MaliNess Network sunucuları için modüler bir Paper eklentisi. Her oyun sistemi (heal, tpa, home vb.) kendi klasörü, config dosyası ve lang dosyası ile yönetilir.

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
├── pluginlang.yml          # Eklentinin ana mesajları
├── configs/
│   └── heal.yml            # Sistem ayarları
└── langs/
    └── heal.yml            # Sistem mesajları
```

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
| Belirteç | `&#ffdb57` | Dinamik veriler (`{player}`, `{seconds}` vb.) |

Renkler `config.yml` → `messages.colors` altından özelleştirilebilir.

### Lang dosyası formatı

```yaml
ornek-mesaj:
  type: success
  text: "{player} için işlem tamamlandı!"
```

Desteklenen renk kodları: `&a`, `&e` ve `&#RRGGBB` (hex).

## Mevcut sistemler

### Heal — Can yenileme

| Komut | Açıklama |
|-------|----------|
| `/heal` | Kendi canını tam doldurur |
| `/heal <miktar>` | Kendine yarım kalp cinsinden can ekler (`2` = 1 kalp) |
| `/heal <oyuncu>` | Oyuncunun canını tam doldurur |
| `/heal <miktar> <oyuncu>` | Oyuncuya belirtilen miktarda can ekler |
| `/mn heal ...` | Alternatif komut (çakışma durumunda) |

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.heal.use` | Kendi canını yenileme | `op` |
| `maliness-core.heal.use.others` | Başkasının canını yenileme | `op` |

#### Config — `configs/heal.yml`

```yaml
enabled: true
```

#### Lang — `langs/heal.yml`

Sistem mesajları bu dosyada tanımlıdır (`no-permission`, `healed-self-full` vb.).

## Proje yapısı (kaynak kod)

```
src/main/java/com/mertaliakcay/malinesscore/
├── MaliNessCore.java           # Ana plugin sınıfı
├── command/                    # Genel komutlar (/mn)
├── messages/                   # Mesaj tipi ve renk sistemi
├── systems/
│   ├── AbstractGameSystem.java # Sistem şablonu
│   ├── SystemManager.java      # Sistem yöneticisi
│   └── heal/                   # Heal sistemi
└── util/                       # Config, lang, renk yardımcıları

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

**Mert Ali AKÇAY** — [GitHub](https://github.com/MaliWhiteTea/MaliNess-Core)
