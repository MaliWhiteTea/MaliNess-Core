# MaliNess Core

MaliNess Network sunucuları için modüler bir Paper eklentisi. Her oyun sistemi kendi config ve lang dosyası ile yönetilir; sunucu açılışında aktif ve deaktif sistemler özet olarak konsola yazılır. Yetkililer `/systems` ve `/system` komutlarıyla sistemleri oyun içinden açıp kapatabilir.

**Sürüm:** `0.1.6.5-alpha.1` · **API:** Paper / Purpur **1.21.4** · **Java** **21**

## Gereksinimler

| Gereksinim | Sürüm |
|------------|-------|
| Sunucu yazılımı | Paper veya Purpur **1.21.4** |
| Java | **21** |
| Vault | Ekonomi sistemi için **zorunlu** — MaliNess `MaliNess Economy` adıyla Economy provider kaydı yapar |
| PlaceholderAPI | İsteğe bağlı — placeholder desteği için |
| ProtocolLib | İsteğe bağlı — vanish gelişmiş gizleme (ses, animasyon) için |

## Kurulum

1. Projeyi derleyin veya releases bölümünden jar dosyasını indirin.
2. `MaliNess-Core-*.jar` dosyasını sunucunun `plugins/` klasörüne atın.
3. Sunucuyu başlatın veya yeniden başlatın.
4. İlk açılışta `plugins/MaliNess-Core/` altında config ve lang dosyaları otomatik oluşur (jar içindeki varsayılanlar kullanıcı dosyalarıyla birleştirilir).

## Derleme

```bash
mvn clean package
```

Çıktı jar dosyası: `target/MaliNess-Core-<sürüm>.jar`

IntelliJ kullanıyorsanız: **Maven → Lifecycle → package**

## Dosya yapısı (sunucu)

```
plugins/MaliNess-Core/
├── config.yml              # Genel ayarlar (prefix, renkler, PlaceholderAPI)
├── pluginlang.yml          # Eklenti ana mesajları, /mn yardım, onay butonları, sistem yönetimi
├── configs/
│   ├── heal.yml
│   ├── feed.yml
│   ├── health.yml
│   ├── hunger.yml
│   ├── saturate.yml
│   ├── saturation.yml
│   ├── god.yml
│   ├── home.yml
│   ├── playtime.yml
│   ├── broadcast.yml
│   ├── vanish.yml
│   ├── warp.yml
│   ├── pwarp.yml
│   ├── gui.yml
│   └── economy.yml
├── guis/                   # Menü tanımları (<menu-id>.yml)
│   ├── demo-overlay-locked.yml
│   ├── demo-economy-shop.yml
│   └── ...
├── langs/
│   ├── heal.yml
│   ├── feed.yml
│   ├── health.yml
│   ├── hunger.yml
│   ├── saturate.yml
│   ├── saturation.yml
│   ├── god.yml
│   ├── home.yml
│   ├── playtime.yml
│   ├── broadcast.yml
│   ├── vanish.yml
│   ├── warp.yml
│   ├── pwarp.yml
│   ├── gui.yml
│   └── economy.yml
├── data/
│   ├── homes/              # Oyuncu ev verileri (<uuid>.yml)
│   ├── playtime/           # Oyuncu oynama süresi (<uuid>.yml)
│   ├── economy/
│   │   └── accounts/       # Oyuncu ekonomi hesapları (<uuid>.yml)
│   ├── warps.yml           # Global warp kayıtları
│   ├── pwarps.yml          # Oyuncu warp kayıtları
│   ├── vanish.yml          # Kalıcı vanish durumları
│   └── god-reload-cache.yml  # /mn reload sırasında geçici god durumu (otomatik)
└── logs/
    ├── home-player.log
    ├── home-admin.log
    ├── warp-player.log
    ├── warp-admin.log
    ├── pwarp-player.log
    ├── pwarp-admin.log
    ├── economy/
    │   └── transactions-YYYY-MM-DD.log
    └── systems-audit.log   # Sistem açma/kapama kayıtları
```

Her oyun sistemi config dosyasında `enabled: true/false` ile açılıp kapatılabilir. Kapalı sistemler yüklenir ancak **aktif olmaz** (dinleyiciler ve zamanlayıcılar çalışmaz); komutlar `system-disabled` mesajı verir. Komut kayıtları ve `/mn` entegrasyonu korunur.

Sistem durumu iki yolla değiştirilebilir:

- **Manuel:** `configs/<sistem>.yml` içinde `enabled` değerini düzenleyip `/mn reload`
- **Oyun içi:** `/system on|off <sistem>` — yalnızca ilgili sistemi reload eder, config dosyasını günceller

## Mesaj sistemi

Tüm mesajlarda ortak bir prefix ve mesaj tipleri kullanılır. Konsol logları da aynı prefix ile yazılır.

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
örnek-mesaj:
  type: success
  text: "{player} için işlem tamamlandı!"
```

Desteklenen renk kodları: `&a`, `&e` ve `&#RRGGBB` (hex).

Dinamik veriler iki yolla kullanılabilir:

| Yöntem | Örnek | Açıklama |
|--------|-------|----------|
| Dahili | `{player}`, `{home}` | Kod tarafından doldurulur |
| PlaceholderAPI | `%malinesscore_god%`, `%player_name%` | Lang metninde; PAPI yüklüyse çözülür |

Dahili placeholder'lar önce, PlaceholderAPI sonra işlenir. Konsol mesajlarında PAPI parse edilmez (oyuncu bağlamı yok).

### Lang senkronizasyonu (`lang-version`)

`pluginlang.yml` ve `lang-version` anahtarı içeren `langs/*.yml` dosyalarında sürüm numarası jar'dakinden düşükse, `/mn reload` veya sunucu açılışında jar'daki varsayılan mesajlar sunucu dosyasına yazılır. Böylece güncelleme sonrası yeni veya düzeltilmiş mesaj anahtarları otomatik uygulanır. Özel düzenlediğin metinler üzerine yazılır; değişiklik yaptıysan yedek al.

`lang-version` olmayan sistem lang dosyalarında yalnızca **eksik** anahtarlar eklenir; mevcut metinler korunur.

## Mimari

Eklenti modüler **sistem** yapısı kullanır. Her oyun sistemi `AbstractGameSystem` üzerinden yüklenir:

```
MaliNessCore
├── SystemManager              → heal, feed, health, hunger, saturate, saturation, god, home, playtime, broadcast, vanish, warp, pwarp, economy, gui
├── SystemControlService       → /systems, /system, audit log, sistem katalogu
├── MalinessCommand (/mn)      → tüm sistemlere delegasyon
├── ConfirmationService        → /evet /hayır /iptal
├── TeleportService            → ortak warmup ve ışınlanma (home, warp, pwarp; plugin genelinde singleton)
├── MenuService                → GUI menüleri, zorunlu ekranlar, YAML menü tanımları
├── EconomyService             → yerel TL ekonomisi, transfer, admin işlemleri
├── VaultIntegration           → MaliNess Economy provider (Vault softdepend)
├── VanishService              → gizli mod durumu, görünürlük, PAPI sayım
├── PlaceholderApiIntegration  → PlaceholderAPI expansion + lang parse
├── MaliNessColorUtil          → duyuru ve renkli mesaj kodları (&z*)
└── MessageService             → prefix, renkler, Adventure Component formatı
```

### Komut kaydı

- **Paper `BasicCommand`:** `/heal`, `/home`, `/god`, `/pay`, `/para`, `/eco`, `/mngui`, `/systems`, `/system` vb. doğrudan komutlar
- **Bukkit `CommandExecutor`:** `/mn` kök komutu ve paylaşılan handler mantığı
- Her oyun sistemi hem kendi komutunu hem `/mn <altkomut>` yolunu destekler
- Ana komut adları **İngilizce**; Türkçe alias'lar ayrıca tanımlıdır

### Reload

`/mn reload` (veya konsoldan eşdeğeri) şunları yapar:

- `config.yml`, `pluginlang.yml` ve tüm sistem config/lang dosyalarını yeniden yükler
- Bekleyen onayları ve aktif warmup'ları (home, warp, pwarp vb.) iptal eder
- Açık GUI menülerini kapatır; zorunlu menü oturumlarını temizler
- Ekonomi ve diğer sistem servislerini yeniden yükler
- God modu durumunu geçici cache dosyasına yazar, reload sonrası geri yükler
- PlaceholderAPI ayarlarını ve expansion kaydını yeniler
- Ev verilerini `flushAll()` ile diske yazar
- Dinleyici birikmesini önlemek için sistemler önce unregister, sonra yeniden register edilir

Tam plugin disable/enable (PlugMan vb.) farklı davranabilir; production'da tercih edilen yol `/mn reload`dır.

`/system on|off` ise yalnızca **tek bir sistemi** reload eder; tam eklenti reload'u yapmaz.

## Sistem yönetimi

Yetkililer oyun sistemlerini komut satırından yönetebilir. Çekirdek altyapı (`core`) listede görünür ancak kapatılamaz.

### Komutlar

| Komut | Alias | Açıklama |
|-------|-------|----------|
| `/systems [sayfa]` | `/sistemler` | Sayfalı sistem listesi (yalnızca oyuncu) |
| `/system <on\|off\|info> <sistem>` | `/sistem` | Sistemi aç, kapat veya bilgi göster |
| `/mn systems [sayfa]` | `/mn sistemler` | `/systems` ile aynı |
| `/mn system ...` | `/mn sistem ...` | `/system` ile aynı |

Alt komut alias'ları: `on`, `off`, `info`, `aç`, `kapa`, `bilgi`, `enable`, `disable`, `aktif`, `deaktif`

### Liste (`/systems`)

```
prefix + Sistemler:
• ✔ core
• ✔ heal [Kapat]
• ✕ god [Aç]
• ✔ home [Kapat]
prefix + Sayfa 1/1 < >
```

- Satırda yalnızca **sistem id** gösterilir (`home`, `god`, `core` …)
- **Aktif** → yeşil `✔` · **Kapalı** → kırmızı `✕`
- Aktif sistemde yalnızca **[Kapat]**, kapalı sistemde yalnızca **[Aç]** gösterilir
- Zaten açık/kapalıyken `/system on|off` → uyarı mesajı
- Sistem id'sine hover → kısa açıklama
- Yönetim yetkisi olmayan sistemlerin yanında kırmızı **`!`** — hover: *"Bu sistemi açıp kapatma yetkin yok."*
- Açma/kapama onay gerektirir; kapatırken yan etki uyarıları gösterilir (ör. home: warmup iptali, god: koruma kalkar)
- Başarılı açma/kapama sonrası güncel `/systems` menüsü otomatik yenilenir (ilgili sayfa)

### Bilgi (`/system info <sistem>`)

Sohbette sistem özeti gösterir (en fazla ~20 satır):

```
Sistem bilgisi:
Ad: Ev (home)
Durum: Aktif
Açıklama: Ev kaydetme ve ışınlanma sistemi.
Komutlar: /sethome, /home, /delhome, ...
Config: configs/home.yml
Kapatılabilir: Evet
```

### Konsol

Konsoldan yalnızca `/system on|off|info <sistem>` desteklenir. Onay akışı yoktur; değişiklik doğrudan uygulanır ve `logs/systems-audit.log` dosyasına yazılır.

### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.systems.list` | Sistem listesini görüntüleme | `op` |
| `maliness-core.systems.manage.*` | Tüm sistemleri açma/kapama | `op` |
| `maliness-core.systems.manage.<id>` | Belirli sistemi açma/kapama (ör. `.home`, `.god`) | `op` |

`systems.list` veya herhangi bir `systems.manage.*` izni olmadan `/systems` kullanılamaz. Tek bir sisteme yetki verilmiş admin tüm listeyi görür; yetkisiz sistemler **`!`** ile işaretlenir.

### Altyapı

| Bileşen | Amaç |
|---------|------|
| `SystemCatalog` | Kayıtlı sistemler + sanal `core` girdisi |
| `NonClosableSystemRegistry` | Kapatılamayan sistemler (`core` ve ileride eklenecekler) |
| `SystemDependencyRegistry` | Sistem bağımlılıkları için boş altyapı (gelecek kullanım) |
| `SystemsAuditLogger` | `logs/systems-audit.log` — kısa satır formatı |

## Sistem özeti

Eklenti **on beş** oyun sistemi içerir:

| Grup | Sistemler | Amaç |
|------|-----------|------|
| Hızlı doldurma | heal, feed, saturate | Tek komutla tam veya kısmi doldurma |
| Hassas ayar | health, hunger, saturation | `set` / `add` / `remove` ile değer yönetimi |
| Oyuncu modu | god | Hasar almama ve saldırgan mob koruması |
| Konum | home, warp, pwarp | Ev kaydetme; admin warp; oyuncu warp (herkese açık) |
| İstatistik | playtime | Toplam oynama süresi takibi ve sorgulama |
| Yönetim | broadcast, vanish | Duyuru gönderme ve gizli mod |
| Ekonomi | economy | Yerel TL ekonomisi ve Vault provider |
| Arayüz | gui | Menü altyapısı, zorunlu ekranlar, demo menüler |

| Minecraft değeri | Hızlı komut | Hassas komut |
|------------------|-------------|--------------|
| Can | `/heal` | `/health` |
| Açlık çubuğu | `/feed` | `/hunger` |
| Doygunluk çubuğu | `/saturate` | `/saturation` |

### `/mn` komutu

Tüm sistemler `/mn` (veya `/maliness`) alt komutu olarak da kullanılabilir. Başka bir eklentiyle komut çakışması olduğunda yedek yol olarak kullanılır.

```
/mn                    → yetkili olduğun komutların yardım listesi
/mn 2                  → 2. sayfa (komut sayısı arttıkça otomatik sayfalanır)
/mn reload             → config ve lang yenileme
/mn systems            → oyun sistemleri listesi
/mn system info home   → ev sistemi bilgisi
/mn heal ...
/mn home
/mn sethome maden
/mn ev
```

#### Yardım sayfalama

`/mn` veya `/mn <sayfa>` ile kullanılabileceğin komutlar listelenir:

- Her sayfa en fazla **20 satır:** 1 başlık + 18 komut + 1 sayfa navigasyonu
- Başlık: **Kullanabileceğin komutlar:**
- Komut satırları tıklanabilir; hover ile açıklama, `/mn` ve direkt komut kullanımı gösterilir
- Sayfa satırı: `prefix + Sayfa X/N < >` (ok tuşları tıklanabilir)

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.reload` | `/mn reload` | `op` |

### Tab tamamlama ve yetkiler

- Tab tamamlama yalnızca **yetkili** olduğun komut ve argümanları gösterir
- Komut çalıştırma yetkisi handler katmanında kontrol edilir; yetkisiz kullanımda sistem kendi hata mesajını gösterir (Minecraft varsayılan “unknown command” yerine)
- Boş argümanda Tab'a basıldığında o seviyedeki tüm seçenekler listelenir

### Onay sistemi

Silme, üzerine yazma, güvensiz ışınlanma ve sistem açma/kapama gibi işlemlerde genel onay akışı kullanılır:

| Komut | Açıklama |
|-------|----------|
| `/evet` | Bekleyen onayı kabul eder |
| `/hayır` | Bekleyen onayı reddeder |
| `/iptal` | Bekleyen onayı veya warmup'ı iptal eder |

Chat formatı:

1. Soru satırı (prefix + onay metni)
2. Buton satırı: **prefix + [/evet] [/hayır] [/iptal]** — tıklanabilir, tam olarak bu metinlerle gösterilir

`/evet` yazıldığında son bekleyen onaya otomatik uygulanır. Oyundan çıkış veya ölüm bekleyen onayı iptal eder.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.confirm.use` | Onay komutlarını kullanma | `true` |

Mesajlar: `pluginlang.yml`

---

## PlaceholderAPI

PlaceholderAPI **isteğe bağlıdır**. Yüklü değilse eklenti normal çalışır; placeholder desteği sessizce devre dışı kalır.

### Yapılandırma

`config.yml`:

```yaml
integrations:
  placeholderapi:
    enabled: true
    parse-in-messages: true
    labels:
      on: "Açık"
      off: "Kapalı"
      yes: "Evet"
      no: "Hayır"
  vault:
    enabled: true
```

| Ayar | Açıklama | Varsayılan |
|------|----------|------------|
| `enabled` | Expansion kaydı ve genel entegrasyon | `true` |
| `parse-in-messages` | Lang mesajlarında `%...%` çözümü | `true` |
| `labels.on` / `labels.off` | Açık/kapalı durum metinleri | `Açık` / `Kapalı` |
| `labels.yes` / `labels.no` | Evet/hayır metinleri | `Evet` / `Hayır` |

PAPI sunucuda yoksa `enabled: true` olsa bile entegrasyon otomatik kapanır.

### Identifier

Tüm placeholder'lar `%malinesscore_<anahtar>%` formatındadır.

Belirli oyuncu için `_<oyuncu>` eki eklenir (ör. `%malinesscore_god_MertAli%`).

### Placeholder listesi

#### Genel

| Placeholder | Açıklama | Örnek |
|-------------|----------|--------|
| `%malinesscore_version%` | Eklenti sürümü | `0.1.6.5-alpha.1` |

#### Sistem durumu

| Placeholder | Açıklama | Örnek |
|-------------|----------|--------|
| `%malinesscore_system_<id>%` | Sistem açık mı | `Açık` / `Kapalı` |
| `%malinesscore_system_<id>_bool%` | Makine okunur | `true` / `false` |

`<id>`: `core`, `heal`, `feed`, `health`, `hunger`, `saturate`, `saturation`, `god`, `home`, `playtime`, `broadcast`, `vanish`, `warp`, `pwarp`, `economy`, `gui`

#### Online (vanish)

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_online%` | Görüntüleyicinin görebildiği online sayısı (`online_visible` ile aynı) |
| `%malinesscore_online_visible%` | Görüntüleyicinin görebildiği online sayısı |
| `%malinesscore_online_vanished%` | Şu an online olan gizli oyuncu sayısı |
| `%malinesscore_online_visible_list%` | Görüntüleyicinin görebildiği oyuncu isimleri (virgülle) |

`vanish.see` izni olan yetkililer gizli oyuncuları da sayar; normal oyuncular sayılmaz. TAB/scoreboard için `%server_online%` yerine `%malinesscore_online_visible%` kullanılabilir.

#### Playtime

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_playtime%` | Görüntüleyen oyuncunun formatlı süresi |
| `%malinesscore_playtime_<oyuncu>%` | Belirli oyuncu |
| `%malinesscore_playtime_seconds%` | Ham saniye (viewer) |
| `%malinesscore_playtime_seconds_<oyuncu>%` | Ham saniye (hedef) |

#### Vanish

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_vanish%` | Görüntüleyen oyuncunun vanish durumu |
| `%malinesscore_vanish_<oyuncu>%` | Belirli oyuncu |
| `%malinesscore_vanish_bool%` | `true` / `false` (viewer) |
| `%malinesscore_vanish_bool_<oyuncu>%` | `true` / `false` (hedef) |
| `%malinesscore_can_see%` | Görüntüleyen, hedef oyuncuyu görebiliyor mu (`Evet` / `Hayır`) |
| `%malinesscore_can_see_<oyuncu>%` | Belirli hedef için |
| `%malinesscore_can_see_bool%` | Makine okunur |
| `%malinesscore_can_see_bool_<oyuncu>%` | Makine okunur (hedef) |

Vanish durumu kalıcıdır (`data/vanish.yml`); reconnect sonrası hatırlanır.

#### God

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_god%` | Görüntüleyen oyuncunun god durumu |
| `%malinesscore_god_<oyuncu>%` | Belirli oyuncu |
| `%malinesscore_god_bool%` | `true` / `false` (viewer) |
| `%malinesscore_god_bool_<oyuncu>%` | `true` / `false` (hedef) |

God durumu yalnızca oyuncu **online** iken `Açık` döner; offline veya quit sonrası `Kapalı`.

#### Home

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_homes_count%` | Ev sayısı |
| `%malinesscore_homes_limit%` | Ev limiti |
| `%malinesscore_homes_remaining%` | Kalan slot |
| `%malinesscore_homes_list%` | Ev isimleri (virgülle) |
| `%malinesscore_homes_overlimit%` | Limit aşımı (`Evet` / `Hayır`) |
| `%malinesscore_home_warmup%` | Işınlanma beklemesi kalan saniye |
| `%malinesscore_home_warmup_active%` | Warmup aktif mi |

Her biri `_<oyuncu>` eki ile belirli oyuncu için de kullanılabilir. Ev verileri offline oyuncular için de okunabilir.

#### Warp

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_warp_count%` | Görüntüleyicinin görebildiği açık warp sayısı |
| `%malinesscore_warp_count_all%` | Toplam warp sayısı (`see-closed` yoksa açık sayısı) |
| `%malinesscore_warp_list%` | Görünür warp isimleri (virgülle) |
| `%malinesscore_warp_<isim>%` | Warp durumu (`Açık` / `Kapalı` / `Yok`) |
| `%malinesscore_warp_desc_<isim>%` | Warp açıklaması (yoksa boş) |

İsim eşleştirmesi büyük/küçük harf duyarsızdır.

#### Pwarp (oyuncu warp)

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_pwarp_count%` | Görüntüleyen oyuncunun pwarp sayısı |
| `%malinesscore_pwarp_count_<oyuncu>%` | Belirli oyuncunun pwarp sayısı |
| `%malinesscore_pwarp_count_all%` | Sunucudaki toplam pwarp sayısı |
| `%malinesscore_pwarp_list%` | Görüntüleyen oyuncunun pwarp isimleri (virgülle) |
| `%malinesscore_pwarp_list_<oyuncu>%` | Belirli oyuncunun pwarp listesi |
| `%malinesscore_pwarp_limit%` | Görüntüleyen oyuncunun pwarp limiti |
| `%malinesscore_pwarp_limit_<oyuncu>%` | Belirli oyuncunun pwarp limiti |
| `%malinesscore_pwarp_<isim>%` | Pwarp var mı (`Evet` / `Hayır`) |
| `%malinesscore_pwarp_owner_<isim>%` | Pwarp sahibi adı (offline ad dahil) |
| `%malinesscore_pwarp_desc_<isim>%` | Pwarp açıklaması (yoksa boş) |

#### Ekonomi

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_balance%` | Görüntüleyen oyuncunun TL bakiyesi (ham) |
| `%malinesscore_balance_tl%` | TL bakiyesi (ham) |
| `%malinesscore_balance_formatted%` | Formatlı TL bakiyesi |
| `%malinesscore_balance_tl_formatted%` | Formatlı TL bakiyesi |
| `%malinesscore_balance_cosmetic%` | Jeton bakiyesi (ham) |
| `%malinesscore_balance_jeton%` | Jeton bakiyesi (ham) |
| `%malinesscore_balance_<para_birimi>%` | Belirli para birimi bakiyesi (ham) |
| `%malinesscore_balance_<para_birimi>_formatted%` | Belirli para birimi formatlı bakiye |
| `%malinesscore_currency_<id>_name%` | Para birimi görünen adı |
| `%malinesscore_currency_<id>_symbol%` | Para birimi sembolü |

Ekonomi sistemi kapalı veya Vault yoksa bakiye placeholder'ları `0` döner.

#### Onay

| Placeholder | Açıklama |
|-------------|----------|
| `%malinesscore_confirmation_pending%` | Bekleyen onay var mı |
| `%malinesscore_confirmation_pending_<oyuncu>%` | Belirli oyuncu |

### Lang dosyalarında kullanım

```yaml
örnek-mesaj:
  type: success
  text: "God: %malinesscore_god% | Evler: %malinesscore_homes_count%/%malinesscore_homes_limit%"
```

### Scoreboard örneği

```
God Mode: %malinesscore_god%
Evler: %malinesscore_homes_count%/%malinesscore_homes_limit%
```

### Test (scoreboard eklentisi gerekmez)

PlaceholderAPI yüklüyken:

```
/papi list
/papi parse me %malinesscore_god%
/papi parse me %malinesscore_system_home%
/papi parse me %malinesscore_online_visible%
/papi parse me %malinesscore_playtime%
/papi parse me %malinesscore_warp_count%
/papi parse me %malinesscore_warp_list%
/papi parse me %malinesscore_pwarp_count%
/papi parse me %malinesscore_pwarp_list%
/papi parse me %malinesscore_balance_formatted%
/papi parse me %malinesscore_currency_tl_name%
```

`/papi list` çıktısında `malinesscore` görünmelidir.

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

**Reload davranışı:** `/mn reload` sırasında aktif god oyuncuları geçici cache dosyasına yazılır ve reload bitince geri yüklenir. Oturum kapanışında (quit) god modu sıfırlanır.

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
| `/evayarla`, `/ev`, `/house` | Kısa / Türkçe alternatifler |
| `/evsil`, `/remhome` | `/delhome` alternatifleri |
| `/evadıdeğiştir`, `/evismideğiştir` | `/renamehome` alternatifleri |
| `/home <oyuncu> <ev>` | Yetkili: oyuncunun evine anında ışınlanma |
| `/delhome <oyuncu> <ev>` | Yetkili: oyuncunun evini silme (onay gerekir) |
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

Warmup sırasında **hareket** (mesafe eşiği), **hasar**, **saldırı**, **elytra** veya **binek hayvanı sürme** → anında iptal.

**OP** ve `maliness-core.home.bypasstime` izni olanlar warmup, cooldown ve **binek kısıtlamasından** muaf tutulur.

#### Güvenlik ve konum

- `sethome`: dünya sınırı ve yükseklik kontrolü
- `blocked-worlds` config ile belirli dünyalarda ev kaydı engellenir
- Işınlanmada güvensiz konum tespit edilirse onay istenir (lav, ateş, kaktüs vb.)
- Eve varınca kısa süreli fire resistance
- Chunk preload ile ışınlanma (async chunk yükleme + main thread teleport)

#### Ev limiti

- Varsayılan: **1 ev** (`limits.default-max-homes`)
- `maliness-core.home.count.N` izinleriyle artırılır (en yüksek N geçerli, örn. `.3` → 3 ev)
- Limit aşıldığında `/home` ve `/sethome` kapalıdır; mevcut evler silinmez
- `/homes` listesinde limit uyarısı gösterilir

#### Liste ve tıklama

- Kendi `/homes` listende satırlar tıklanabilir → `/home <ev>`
- Yetkili `/homes <oyuncu>` listesinde satırlar tıklanabilir → anında `/home <oyuncu> <ev>`
- Offline oyuncuların evleri de listelenebilir ve ışınlanılabilir (diskte kayıtlı UUID)

#### Veri doğrulama ve uyarılar

Ev dosyaları yüklenirken `HomeDataValidator` ile doğrulanır. Geçersiz kayıtlar atlanır:

- **Konsol:** klasik prefix ile uyarı logu
- **Oyunda:** `maliness-core.home.invalid-home.broadcast` iznine sahip yetkililere anlık bildirim (oturum başına dedupe)

Ev verileri oyuncu başına `data/homes/<uuid>.yml` dosyasında tutulur; yazımlar async kuyruk ile sıraya alınır, atomik temp+rename ile diske yazılır, shutdown/reload öncesi `flushAll()` ile tamamlanır.

#### Konsol

- Konsoldan yalnızca `delhome <oyuncu> <ev> --confirm` desteklenir (`--confirm` zorunlu)
- Diğer ev komutları oyuncu gerektirir
- Konsoldan eve ışınlanma desteklenmez

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
| `maliness-core.home.bypasstime` | Warmup, cooldown ve binek kısıtlaması atlama | `op` |
| `maliness-core.home.count.N` | En fazla N ev | — |
| `maliness-core.home.others.list` | Başkasının evlerini listeleme | `op` |
| `maliness-core.home.others.teleport` | Başkasının evine ışınlanma | `op` |
| `maliness-core.home.others.delete` | Başkasının evini silme | `op` |
| `maliness-core.home.invalid-home.broadcast` | Geçersiz ev verisi uyarısı (oyun içi) | `op` |

Config: `configs/home.yml` — Lang: `langs/home.yml`

Home, warp ve pwarp aynı `TeleportService` warmup altyapısını kullanır. Biri warmup'tayken diğer komutlar engellenir (iptal olmaz); her sistem kendi lang mesajını gösterir.

---

### Warp — Sabit nokta ışınlanma

Adminlerin belirlediği konumlara ışınlanma. Ev sistemiyle aynı warmup, güvenlik ve onay kalıplarını paylaşır.

#### Komutlar (oyuncu)

| Komut | Açıklama |
|-------|----------|
| `/warp` | Sayfalı warp listesi (tıklanabilir satırlar) |
| `/warp <sayfa>` | Belirli sayfa (ör. `/warp 2`) |
| `/warp <isim>` | Warp noktasına ışınlanır |
| `/warps [sayfa]` | `/warp` ile aynı liste |
| `/warplar [sayfa]` | `/warps` ile aynı |
| `/mn warp ...` / `/mn warps` | Alternatif komut |

#### Komutlar (admin)

| Komut | Açıklama |
|-------|----------|
| `/warp ekle\|set <isim>` | Bulunduğun konuma warp kaydeder |
| `/warp sil\|remove <isim>` | Onaylı silme |
| `/warp düzenle\|edit <isim> <yeniisim>` | Yeniden adlandırma |
| `/warp düzenle <isim> konum` | Admin konumuna taşıma |
| `/warp düzenle <isim> açıklama <metin>` | Açıklama (renk kodları destekli; boş = sil) |
| `/warp düzenle <isim> açık\|kapalı\|on\|off\|...` | Warp aç/kapa |

Konsol: silme (`--confirm`), yeniden adlandırma, açık/kapalı, açıklama. `ekle` ve `konum` konsoldan desteklenmez.

#### İsim kuralları

- En az **2** karakter; en az bir harf (Türkçe dahil); sadece rakamdan oluşamaz
- Max **20** karakter; boşluk yok; `_` ve `-` dışında özel karakter yok
- Büyük/küçük harf ve Türkçe karakter serbest; arama case-insensitive (`market` → `Market`)
- Rezerve kelimeler: `ekle`, `sil`, `düzenle`, `set`, `remove`, `edit`, `açık`, `kapalı`, `warps`, `warplar` vb.

#### Warmup ve cooldown

Varsayılan (`configs/warp.yml`):

| Ayar | Varsayılan |
|------|------------|
| Warmup | 5 saniye |
| Warp cooldown | 5 saniye |
| Fire resistance | 3 saniye |

Warmup mesajı: `{warp} warpına ışınlanıyorsun... {seconds} saniye bekle.`

Warmup sırasında hareket, hasar, saldırı, elytra veya binek → iptal. `maliness-core.warp.bypasstime` warmup ve cooldown'u atlar.

#### Liste ve görünürlük

- Kapalı warplar normal oyuncuya görünmez
- `maliness-core.warp.see-closed` yetkisi olanlar listede `✕` ile görür; hover'da kapalı bilgisi
- Açıklamalı warplarda hover'da renkli açıklama; açıklama yoksa yalnızca isim

#### Güvenlik

- Güvensiz konumda onay sorusu (home ile aynı mantık)
- Geçersiz dünya: ışınlanma engeli + `maliness-core.warp.invalid.broadcast` ile yetkili uyarısı

#### Loglar

| Dosya | İçerik |
|-------|--------|
| `logs/warp-admin.log` | Ekleme, silme, düzenleme |
| `logs/warp-player.log` | Yalnızca başarılı oyuncu ışınlanmaları |

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.warp.use` | Liste ve ışınlanma | `true` |
| `maliness-core.warp.manage` | Warp CRUD | `op` |
| `maliness-core.warp.see-closed` | Kapalı warpları görme | `op` |
| `maliness-core.warp.bypasstime` | Warmup ve cooldown atlama | `op` |
| `maliness-core.warp.invalid.broadcast` | Geçersiz warp uyarısı (oyun içi) | `op` |

Config: `configs/warp.yml` — Lang: `langs/warp.yml` (`lang-version` destekli) — Veri: `data/warps.yml`

Admin warp ile pwarp isim çakışması config ile yönetilir (`name-collision` bölümü).

---

### Pwarp — Oyuncu warp sistemi

Oyuncuların kendi konumlarında herkese açık warp noktası oluşturduğu sistem. Admin warp'tan bağımsızdır; isimler sunucu genelinde benzersizdir.

#### Komutlar (oyuncu)

| Komut | Açıklama |
|-------|----------|
| `/pwarp` | Sayfalı pwarp listesi (tıklanabilir satırlar) |
| `/pwarp <sayfa>` | Belirli sayfa (ör. `/pwarp 2`) |
| `/pwarp <isim>` | Pwarp noktasına ışınlanır |
| `/pwarps [sayfa]` | `/pwarp list` ile aynı liste |
| `/pwarp ekle\|set <isim>` | Bulunduğun konuma pwarp kaydeder (açıklama istenmez) |
| `/pwarp sil\|delete\|remove <isim>` | Onaylı silme |
| `/pwarp düzenle\|edit <isim> <yeniisim\|konum\|açıklama>` | Yeniden adlandırma, konum veya açıklama |
| `/mn pwarp ...` / `/mn pwarps` | Alternatif komut |

Ana komut yalnızca **`/pwarp`** (Türkçe kök alias yok).

#### Komutlar (admin)

| Komut | Açıklama |
|-------|----------|
| `/pwarp sil <oyuncu> <isim>` | Başka oyuncunun pwarpını siler (onaylı) |
| `/pwarp düzenle <oyuncu> <isim> konum\|açıklama\|YeniAd` | Başka oyuncunun pwarpını düzenler |
| Konsol: `/pwarp sil <oyuncu> <isim> --confirm` | Onaysız admin silme |

#### İsim kuralları

- Admin warp ile aynı format (2–20 karakter, en az bir harf, case-insensitive)
- Varsayılan limit: **1 pwarp** — `maliness-core.pwarp.count.N` ile artırılır
- Config `names.blacklist` ile alt komut ve rezerve kelimeler engellenir
- Admin warp ile isim çakışması config ile kontrol edilir (varsayılan: pwarp engellenir)

#### Warmup, ziyaret ve liste

- Home/warp ile aynı warmup koruması; çapraz engelleme (iptal yok)
- Sahip kendi pwarpına giderse ziyaret sayacı artmaz
- Liste hover: sahip, konum, oluşturulma, ziyaret sayısı; **son ziyaret** yalnızca `pwarp.manage` izinlisinde
- İleride `/pwarp` listesi GUI menüye taşınacak (şu an chat listesi; demo: `/mngui pwarp`)

#### Loglar

| Dosya | İçerik |
|-------|--------|
| `logs/pwarp-player.log` | Oluşturma, silme, başarılı ışınlanma |
| `logs/pwarp-admin.log` | Admin silme ve düzenleme |

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.pwarp.use` | Işınlanma | `true` |
| `maliness-core.pwarp.set` | Pwarp oluşturma | `true` |
| `maliness-core.pwarp.delete` | Kendi pwarpını silme | `true` |
| `maliness-core.pwarp.list` | Liste görüntüleme | `true` |
| `maliness-core.pwarp.edit` | Kendi pwarpını düzenleme | `true` |
| `maliness-core.pwarp.manage` | Başkasının pwarpını silme/düzenleme | `op` |
| `maliness-core.pwarp.bypasstime` | Warmup ve cooldown atlama | `op` |
| `maliness-core.pwarp.count.N` | En fazla N pwarp | — |
| `maliness-core.pwarp.invalid.broadcast` | Geçersiz pwarp uyarısı (oyun içi) | `op` |

Config: `configs/pwarp.yml` — Lang: `langs/pwarp.yml` (`lang-version` destekli) — Veri: `data/pwarps.yml`

Test listesi: `TEST_CHECKLISTS/0.1.6.2.md`

---

### Playtime — Oynama süresi

| Komut | Açıklama |
|-------|----------|
| `/playtime` | Kendi toplam oynama süreni gösterir |
| `/playtime <oyuncu>` | Belirtilen oyuncunun süresini gösterir |
| `/oynamasüresi ...` | `/playtime` ile aynı (Türkçe alternatif) |
| `/mn playtime ...` / `/mn oynamasüresi ...` | Alternatif komut |

Süre oturum bazında takip edilir; periyodik flush ile `data/playtime/<uuid>.yml` dosyalarına yazılır. Format birimleri ve sırası `configs/playtime.yml` → `format` bölümünden özelleştirilebilir (varsayılan: `1g 2s 30dk` tarzı).

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.playtime.use` | Kendi süresini görüntüleme | `true` |
| `maliness-core.playtime.use.others` | Başkasının süresini görüntüleme | `op` |

Config: `configs/playtime.yml` — Lang: `langs/playtime.yml`

---

### Broadcast — Duyuru

| Komut | Açıklama |
|-------|----------|
| `/broadcast <hepsi\|dünya> <mesaj>` | Tüm sunucuya veya belirli dünyaya duyuru gönderir |
| `/bc`, `/duyur`, `/duyuruyap` | Kısa alternatifler |
| `/mn broadcast ...` / `/mn duyur ...` | Alternatif komut |

Hedef `hepsi` (veya config'teki alias'lar: `all`, `everyone`) tüm online oyunculara; dünya adı yalnızca o dünyadaki oyunculara gönderir. Mesajlarda `&` renk kodları, `&#RRGGBB` hex ve MaliNess semantik kodları (`&zh`, `&zb`, `&zu`, `&zn`, `&zv`) kullanılabilir.

| Kod | Anlam | Varsayılan renk |
|-----|-------|-----------------|
| `&zh` | Hata | `messages.colors.error` |
| `&zb` | Başarı | `messages.colors.success` |
| `&zu` | Uyarı | `messages.colors.warning` |
| `&zn` | Normal | `messages.colors.normal` |
| `&zv` | Vurgu / belirteç | `messages.colors.token` |

Renk kullanımı `maliness-core.colors.use` izni gerektirir; izinsiz gönderenlerde renkler strip edilir. Konsol her zaman renk kullanabilir.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.broadcast.use` | Duyuru gönderme | `op` |
| `maliness-core.colors.use` | Renk kodlarını kullanma | `op` |

Config: `configs/broadcast.yml` — Lang: `langs/broadcast.yml`

---

### Vanish — Gizli mod

| Komut | Açıklama |
|-------|----------|
| `/vanish` | Kendi gizli modunu açar veya kapatır (toggle) |
| `/vanish <oyuncu>` | Başka oyuncunun gizli modunu toggle eder |
| `/vanish list` | Gizli moddaki online oyuncuları listeler |
| `/gizlen ...` | `/vanish` ile aynı (Türkçe alternatif) |
| `/mn vanish ...` / `/mn gizlen ...` | Alternatif komut |

Gizli mod **kalıcıdır** — sunucudan çıkıp girince durum korunur; reconnect sonrası hatırlatma mesajı gösterilir. Gizli oyuncular `vanish.see` izni olmayanlardan saklanır; join/quit mesajları, ölüm ve advancement duyuruları gizlenebilir.

ProtocolLib yüklüyse sandık animasyonu/sesi, lever/buton sesi, entity sesleri (yemek bitişi vb.) ve benzeri etkileşim ifşaları paket düzeyinde gizlenir. Sculk sensör ve basınç plakası etkileşimleri Bukkit/Paper event'leri ile engellenir.

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.vanish.use` | Kendi gizli modunu yönetme | `op` |
| `maliness-core.vanish.use.others` | Başkasının gizli modunu yönetme | `op` |
| `maliness-core.vanish.see` | Gizli oyuncuları görme ve listeleme | `op` |

Config: `configs/vanish.yml` — Lang: `langs/vanish.yml`

`configs/vanish.yml` önemli ayarlar:

| Ayar | Açıklama | Varsayılan |
|------|----------|------------|
| `join-quit-messages-hidden` | Gizli oyuncu giriş/çıkış mesajını gizle | `true` |
| `block-private-messages` | Gizli hedefe `/msg` vb. engelle | `true` |
| `prevent-damage` / `prevent-dealing-damage` | Hasar alma/verme engeli | `true` |
| `hide-death-messages` / `hide-advancement-messages` | Ölüm ve advancement duyurularını gizle | `true` |
| `hide-sculk-sensor` / `hide-pressure-plates` | Sculk ve basınç plakası ifşasını engelle | `true` |
| `protocol-lib.*` | ProtocolLib gelişmiş gizleme | `true` |

---

### GUI — Menü altyapısı

YAML tabanlı menü sistemi. Warp/pwarp/home listelerine bağlama ileride eklenecek; şu an demo menüler ve ekonomi entegrasyonu mevcuttur.

#### Genel kavramlar

| Kavram | Açıklama |
|--------|----------|
| `player-inventory: locked` | Alt envanter, zırh ve offhand kilitli; yerden eşya alma serbest |
| `player-inventory: allowed` | Oyuncu kendi envanterini kullanabilir; menü slotları yine korumalı |
| `close-policy: normal` | Oyuncu menüyü kapatabilir |
| `close-policy: mandatory` | Oyuncu kapatamaz; ölüm/çıkış sonrası yeniden açılır veya `/mngui release` gerekir |

Desteklenen envanter tipleri: `CHEST` (9–54 slot), `HOPPER`, `DROPPER`, `DISPENSER`, `BARREL`, `SHULKER_BOX`, `ENDER_CHEST`.

Menü item'ları **sanal**dır — tıklamada envantere geçmez (dup koruması). Çift tıklama koruması ve buton cooldown `configs/gui.yml` → `click-protection` altındadır.

#### Komutlar (demo / test)

| Komut | Açıklama |
|-------|----------|
| `/mngui list` | Yüklü menü id'lerini listeler |
| `/mngui open <id>` | Belirtilen menüyü açar |
| `/mngui pwarp` | Demo pwarp listesi menüsü |
| `/mngui pwarp-empty` | Boş liste demo menüsü |
| `/mngui mandatory` | Zorunlu (kapatılamaz) demo menüsü |
| `/mngui locked` | Kilitli envanter overlay demo |
| `/mngui allowed` | Serbest envanter overlay demo |
| `/mngui hopper` / `/mngui dropper` | Küçük envanter tipi demoları |
| `/mngui click-test` | Tıklama ve koruma test menüsü |
| `/mngui economy` | Ekonomi aksiyonlu demo mağaza |
| `/mngui reload-test` | Reload sonrası zorunlu menü davranışı testi |
| `/mngui release [oyuncu]` | Zorunlu menüyü admin olarak serbest bırakır |

#### Menü aksiyonları (YAML)

| Aksiyon | Açıklama |
|---------|----------|
| `close` | Menüyü kapatır |
| `noop` | Hiçbir şey yapmaz |
| `command: <komut>` | Oyuncu adına komut çalıştırır |
| `economy:withdraw:<miktar>` | TL çeker |
| `economy:deposit:<miktar>` | TL ekler |
| `economy:require:<miktar>` | Yeterli bakiye yoksa hata; varsa çeker |
| `economy:require:<miktar>:then:<aksiyon>` | Zincirli ekonomi aksiyonu |

Menü başına `economy-behavior` ile yetersiz bakiye / başarı / hata sonrası davranış (`close`, `stay`, `message`) ayarlanır. Title ve lore'da `{balance}` ile dahili placeholder; PlaceholderAPI parse desteklenir.

#### Yapılandırma

- Global: `configs/gui.yml` — kilitleme varsayılanları, tıklama koruması, ekonomi varsayılanları
- Menü başına: `guis/<menu-id>.yml`
- Lang: `langs/gui.yml`

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.mngui` | `/mngui` demo komutları | `op` |
| `maliness-core.gui.demo-pwarp` | Demo pwarp menüleri | `op` |
| `maliness-core.gui.demo-mandatory` | Zorunlu AFK demo | `op` |
| `maliness-core.gui.demo-overlay` | Overlay demoları | `op` |
| `maliness-core.gui.demo-types` | Huni/dropper demoları | `op` |
| `maliness-core.gui.demo-click-test` | Tıklama test menüsü | `op` |
| `maliness-core.gui.demo-economy` | Ekonomi demo menüleri | `op` |
| `maliness-core.systems.manage.gui` | GUI sistemini açma/kapama | `op` |

Test listesi: `TEST_CHECKLISTS/0.1.6.3.md`

---

### Economy — Yerel ekonomi

MaliNess kendi **TL** ekonomisini yönetir ve Vault'a **MaliNess Economy** adıyla kayıt olur. EssentialsX economy gerekmez. Vault jar yüklü değilse veya `integrations.vault.enabled: false` ise ekonomi sistemi devre dışı kalır.

#### Para birimleri

| ID | Ad | Kullanım |
|----|-----|----------|
| `tl` | TL (₺) | Birincil para; `/pay`, Vault, GUI withdraw/deposit |
| `cosmetic` | Jeton (✦) | Altyapı hazır; yalnızca admin `/eco` ve PAPI — oyuncu transferi yok |

Bakiyeler `data/economy/accounts/<uuid>.yml` dosyalarında tutulur. Başlangıç bakiyesi varsayılan **0 TL** (`configs/economy.yml` → `starting-balance`).

#### Komutlar (oyuncu)

| Komut | Açıklama |
|-------|----------|
| `/pay <oyuncu> <miktar>` | Başka oyuncuya TL gönderir |
| `/paragönder <oyuncu> <miktar>` | `/pay` ile aynı (Türkçe alias) |
| `/para [oyuncu]` | Bakiyeni veya (yetkiliyse) başkasının bakiyesini gösterir |
| `/bal`, `/balance`, `/bakiye` | `/para` alias'ları |
| `/mn pay ...` | Alternatif komut |

Transfer kuralları:

- Min **0,01 TL**, max **10.000.000 TL**
- **≥1.000.000 TL** transferlerde sohbet onayı (`/evet` / `/hayır`) — `pay.confirmation.enabled` ile kapatılabilir
- Çevrimdışı oyuncuya gönderimde ek onay
- Vergi config'te tanımlı; varsayılan **kapalı**

#### Komutlar (admin)

| Komut | Açıklama |
|-------|----------|
| `/eco give <oyuncu> <miktar> [para_birimi]` | Bakiye ekler |
| `/eco take <oyuncu> <miktar> [para_birimi]` | Bakiye düşer |
| `/eco set <oyuncu> <miktar> [para_birimi]` | Bakiyeyi ayarlar |
| `/eco reset <oyuncu> [para_birimi]` | Bakiyeyi sıfırlar |
| `/eco info <oyuncu> [para_birimi]` | Bakiye bilgisi |
| `/eco server` | Sunucu (SERVER) hesabı bakiyesi |
| `/mn eco ...` | Alternatif komut |

#### Vault entegrasyonu

- Provider adı: **MaliNess Economy**
- Vault bank API desteklenmez
- Diğer eklentiler Vault üzerinden TL bakiyesini okuyup yazabilir

#### Loglar

| Dosya | İçerik |
|-------|--------|
| `logs/economy/transactions-YYYY-MM-DD.log` | Transfer ve admin işlemleri |

#### İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `maliness-core.economy.pay` | `/pay` kullanımı | `true` |
| `maliness-core.economy.balance` | Kendi bakiyesini görme | `true` |
| `maliness-core.economy.balance.others` | Başkasının bakiyesini görme | `op` |
| `maliness-core.economy.admin` | `/eco` ve `/mn eco` | `op` |
| `maliness-core.systems.manage.economy` | Ekonomi sistemini açma/kapama | `op` |

Config: `configs/economy.yml` — Lang: `langs/economy.yml` (`lang-version` destekli)

Test listesi: `TEST_CHECKLISTS/0.1.6.4.md`

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
| `/delhome` | `/evsil`, `/remhome` |
| `/renamehome` | `/evadıdeğiştir`, `/evismideğiştir` |
| `/systems` | `/sistemler` |
| `/system` | `/sistem` |
| `/playtime` | `/oynamasüresi` |
| `/broadcast` | `/bc`, `/duyur`, `/duyuruyap` |
| `/vanish` | `/gizlen` |
| `/warp` | `/warps`, `/warplar` |
| `/pwarp` | `/pwarps` |
| `/pay` | `/paragönder` |
| `/para` | `/bal`, `/balance`, `/bakiye` |
| — | `/evet`, `/hayır`, `/iptal` |

Hassas ayar sistemlerinde alt komutlar: `set`/`ayarla`, `add`/`ekle`, `remove`/`azalt`

God modu durumları: `aktif`/`deaktif`, `on`/`off`, `active`/`deactivate`

Sistem yönetimi alt komutları: `on`/`aç`, `off`/`kapa`, `info`/`bilgi`

## Yeni sistem ekleme

1. `AbstractGameSystem` alt sınıfı oluştur (`getSystemId()` → config/lang dosya adı)
2. `configs/<id>.yml` ve `langs/<id>.yml` ekle
3. `MaliNessCore.registerSystems()` içine kaydet
4. Gerekirse `MalinessCommand` ve `MnCommandHelp` entegrasyonu ekle
5. `plugin.yml` izinlerini tanımla (`maliness-core.systems.manage.<id>` dahil)
6. `pluginlang.yml` içine `systems-display-<id>`, `systems-desc-<id>`, `systems-commands-<id>` ekle

Yeni sistem otomatik olarak `SystemCatalog`'a dahil olur. Kapatılamaz yapmak için `NonClosableSystemRegistry.register("<id>")` kullanılabilir.

`configs/example.yml` ve `langs/example.yml` şablon olarak jar içinde durur; yüklenmez.

## Proje yapısı (kaynak kod)

```
src/main/java/com/mertaliakcay/malinesscore/
├── MaliNessCore.java
├── command/
│   ├── MalinessCommand.java    # /mn delegasyon + tab
│   ├── MnCommandHelp.java      # sayfalı yardım
│   ├── MnguiCommand.java       # /mngui demo komutları
│   └── MnguiBasicCommand.java
├── confirmation/               # /evet /hayır /iptal
├── teleport/                   # TeleportService, TeleportListener, SafeTeleport
├── gui/                        # MenuService, MenuRegistry, MenuListener, YAML loader
├── integrations/
│   ├── placeholderapi/         # Expansion, resolver, ayarlar
│   └── vault/                  # VaultIntegration, MaliNessVaultEconomy
├── messages/                   # MessageService, MessageType
├── systems/
│   ├── AbstractGameSystem.java
│   ├── SystemManager.java
│   ├── control/                # /systems, /system, audit, katalog
│   ├── heal/
│   ├── feed/
│   ├── health/
│   ├── hunger/
│   ├── saturate/
│   ├── saturation/
│   ├── god/                    # GodStateStorage, GodListener
│   ├── home/                   # HomeService, HomeStorage, ...
│   ├── warp/                   # WarpService, WarpStorage, WarpListHelp, ...
│   ├── pwarp/                  # PwarpService, PwarpStorage, PwarpListHelp, ...
│   ├── playtime/               # PlaytimeService, PlaytimeStorage, PlaytimeTracker
│   ├── broadcast/              # BroadcastCommand
│   ├── vanish/                 # VanishService, ProtocolLibVanishEnhancer, VanishListener
│   ├── economy/                # EconomyService, Vault provider, pay/para/eco komutları
│   └── gui/                    # GuiSystem → MenuService entegrasyonu
└── util/                       # Config, lang, renk, tab tamamlama, MaliNessColorUtil

TEST_CHECKLISTS/                # Sürüm bazlı test listeleri (ör. 0.1.6.md, 0.1.6.3.md, 0.1.6.4.md)

src/main/resources/
├── config.yml
├── plugin.yml
├── pluginlang.yml
├── configs/
├── langs/
└── guis/
```

## Bilinen sınırlamalar

| Öncelik | Konu |
|---------|------|
| Düşük | `HomeRateLimiter` thread-safety (çok eşzamanlı erişimde teorik risk) |

## Lisans

Bu proje MaliNess Network için geliştirilmektedir.

## Yazar

**Mert Ali AKÇAY** — [GitHub](https://github.com/MaliWhiteTea)
