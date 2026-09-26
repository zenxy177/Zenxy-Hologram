# 🌟 ZenxyHologram

**Next-Generation TextDisplay & Packet-Based Hologram Plugin for Minecraft**

[![Spigot](https://img.shields.io/badge/Spigot-1.20%2B-orange?style=for-the-badge&logo=spigotmc)](https://www.spigotmc.org)
[![Java](https://img.shields.io/badge/Java-17%2B-blue?style=for-the-badge&logo=java)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-Proprietary-red?style=for-the-badge)](LICENSE)
[![bStats](https://img.shields.io/badge/bStats-34332-blueviolet?style=for-the-badge)](https://bstats.org/plugin/bukkit/ZenxyHologram/34332)

> Modern, hafif ve tam özelleştirilebilir bir hologram eklentisi.  
> Vanilla TextDisplay entity'lerini kullanır — hiçbir NMS hackına ihtiyaç duymazsınız.

---

## ✨ Özellikler

### 🎯 Modern TextDisplay Motoru
ZenxyHologram, Minecraft'ın 1.20 ile gelen yerel `TextDisplay` entity'lerini kullanır. Yani eski hologram eklentilerindeki armor stand veya invisible mob hileleri yok — her şey oyunun kendi sistemiyle çalışır. Görseller net, performans yüksek.

### 📦 Paket Tabanlı Oluşturma (PacketEvents)
Hologramlar gerçek dünyaya spawn edilmez; sadece her oyuncuya **paket olarak** gönderilir. Bu sayede:
- Sunucu dünyası kirlenmez
- Hologramı görmeyen oyuncular için hiç paket gönderilmez (culling)
- Yeniden bağlanan oyuncular hologramları anında görür

### 🖱️ Tıklama Eylemleri (Click Actions)
Her holograma tıklanabilir eylemler ekleyebilirsiniz:

| Eylem Türü | Sözdizimi | Açıklama |
|-----------|-----------|----------|
| Oyuncu Komutu | `[player] /warp spawn` | Oyuncunun adına komut çalıştırır |
| Konsol Komutu | `[console] /give %player% diamond 1` | Konsol üzerinden komut çalıştırır |
| Mesaj | `[message] Merhaba %player%!` | Oyuncuya özel mesaj gönderir |
| Ses | `[sound] ENTITY_EXPERIENCE_ORB_PICKUP` | Oyuncuya ses çalar |
| Sunucu | `[server] lobby` | BungeeCord/Velocity ile sunucu değiştirir |

### 📝 PlaceholderAPI Desteği
Hologram satırlarında `%player_name%`, `%vault_eco_balance%` gibi tüm PAPI placeholder'larını kullanabilirsiniz. PlaceholderAPI opsiyoneldir — kurulu değilse eklenti sorunsuz çalışmaya devam eder.

### 💾 Çift Depolama Motoru (SQLite / YAML)
`config.yml` içinde `storage-type` ile istediğiniz depolama motorunu seçebilirsiniz:
- **SQLite** (Önerilen): WAL modu ile sıfır gecikme, crash güvenliği ve eşzamanlı erişim kilitleri
- **YAML**: Basit kurulumlar için klasik dosya tabanlı depolama

### 🌐 Çoklu Dil Desteği
`config.yml` içinde tek bir satırla dili değiştirebilirsiniz:
```yaml
settings:
  language: "en-US"   # ya da "tr-TR"
```
Her dil kendi mesaj dosyasını kullanır (`messages-en-US.yml` veya `messages.yml` Türkçe için).

### ⚙️ Billboard Modları
Her hologramın görünme yönünü ayarlayabilirsiniz:
- **CENTER** — Her yöne döner (varsayılan)
- **FIXED** — Sabit yönde durur
- **VERTICAL** — Dikey eksende döner
- **HORIZONTAL** — Yatay eksende döner

### 🔭 Akıllı Culling Sistemi
Hologramlar yalnızca belirlenen `render-distance` içindeki oyunculara gönderilir. Oyuncu uzaklaştığında despawn paketi otomatik gönderilir. Sunucunuz ne kadar kalabalık olursa olsun performans sabittir.

---

## 📋 Komutlar

Tüm komutlar için alias: `/zholo` · `/zh` · `/hologram`

| Komut | Açıklama |
|-------|----------|
| `/zholo create <isim>` | Bulunduğunuz konumda yeni hologram oluşturur |
| `/zholo delete <isim>` | Hologramı kalıcı olarak siler |
| `/zholo list` | Tüm hologramları listeler |
| `/zholo tp <isim>` | Holograma ışınlanır |
| `/zholo movehere <isim>` | Hologramı bulunduğunuz konuma taşır |
| `/zholo addline <isim> <metin>` | Holograma yeni satır ekler |
| `/zholo setline <isim> <index> <metin>` | Belirtilen satırı günceller |
| `/zholo removeline <isim> <index>` | Belirtilen satırı siler |
| `/zholo setscale <isim> <index> <boyut>` | Satır boyutunu ayarlar |
| `/zholo setbg <isim> <index> <renk>` | Satır arka plan rengini ayarlar |
| `/zholo setbillboard <isim> <mod>` | Billboard modunu değiştirir |
| `/zholo addaction <isim> <eylem>` | Tıklama eylemi ekler |
| `/zholo removeaction <isim> <index>` | Tıklama eylemini siler |
| `/zholo listactions <isim>` | Tüm tıklama eylemlerini listeler |
| `/zholo reload` | Config ve hologramları yeniden yükler |

**İzin:** `zenxyhologram.admin` (varsayılan: OP)

---

## 🔧 Kurulum

1. **JAR dosyasını** `plugins/` klasörüne koyun
2. Sunucuyu başlatın — konfigürasyon dosyaları otomatik oluşturulur
3. İsteğe bağlı olarak [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) kurun
4. `config.yml` dosyasını düzenleyin ve `/zholo reload` komutuyla uygulayın

---

## 🛠️ Konfigürasyon

### `config.yml`
```yaml
settings:
  # Depolama motoru: SQLITE (Önerilen) veya YAML
  storage-type: "SQLITE"

  # Hologram render mesafesi (blok cinsinden)
  render-distance: 48

  # Güncelleme aralığı (tick cinsinden, 20 tick = 1 saniye)
  update-interval-ticks: 5

  # Görünürlük kontrolü aralığı (tick)
  culling-interval-ticks: 10

  # Varsayılan satır aralığı (Y ekseni)
  default-line-spacing: 0.3

  # Varsayılan billboard modu: CENTER, FIXED, VERTICAL, HORIZONTAL
  default-billboard: "CENTER"

  # Paket motoru: AUTO, PACKETEVENTS, PROTOCOLLIB, BUKKIT
  protocol-provider: "AUTO"

  # Debug modu (geliştirici logları)
  debug: false

  # Dil seçimi: en-US veya tr-TR
  language: "en-US"
```

### Renkli Metin & MiniMessage
Hologram satırlarında [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatı desteklenir:
```
/zholo addline test <gradient:#FF5555:#FFAA00><b>Merhaba!</b></gradient>
/zholo addline test <rainbow>Gökkuşağı yazı!</rainbow>
/zholo addline test <blue>Mavi metin</blue>
```
Legacy `&` renk kodları da desteklenir.

---

## 📊 Gereksinimler

| Gereksinim | Versiyon | Zorunlu mu? |
|-----------|---------|------------|
| Spigot / Paper | 1.20+ | ✅ Evet |
| Java | 17+ | ✅ Evet |
| PacketEvents | Dahili | ✅ Evet (bundled) |
| PlaceholderAPI | Herhangi | ❌ Opsiyonel |
| ProtocolLib | Herhangi | ❌ Opsiyonel |

---

## 📈 bStats Metrikleri

ZenxyHologram, anonim kullanım istatistiklerini [bStats](https://bstats.org/plugin/bukkit/ZenxyHologram/34332) aracılığıyla toplar:
- Kullanılan paket motoru
- Depolama motoru
- Toplam hologram sayısı

İstemiyorsanız `plugins/bStats/config.yml` içinden kapatabilirsiniz.

---

## 🤝 Katkı

Pull request ve önerilere açığız! Bir hata bulduysanız ya da yeni bir özellik istiyorsanız lütfen bir issue açın.

---

## 📜 Lisans

Bu proje **ZenxyTeam'e ait özel/proprietary bir lisans** altındadır.

- ✅ Kendi sunucunuzda çalıştırabilirsiniz
- ✅ Kaynak kodunu inceleyebilirsiniz
- ❌ Yeniden dağıtamazsınız (SpigotMC, GitHub vb.)
- ❌ Adını/kodunu değiştirip kendinizmiş gibi yayınlayamazsınız
- ❌ Satışa sunamazsınız

Tüm hakları saklıdır. Detaylar için [LICENSE](LICENSE) dosyasını inceleyiniz.

---

**ZenxyTeam** tarafından ❤️ ile yapıldı.
