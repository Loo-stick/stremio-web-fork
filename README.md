# Stremio Fork - Support DRM Widevine

Fork de Stremio avec support natif **Widevine DRM** pour les contenus protégés (TF1+, etc.).

## Architecture du projet

Ce projet se compose de **2 repositories** qui fonctionnent ensemble :

| Repository | Description |
|------------|-------------|
| [stremio-video-fork](https://github.com/Loo-stick/stremio-video-fork) | Lecteur vidéo avec Shaka Player pour le support DRM |
| [stremio-web-fork](https://github.com/Loo-stick/stremio-web-fork) | Interface web Stremio modifiée |

**Pourquoi 2 repos ?**
- `stremio-video-fork` : Intègre Shaka Player qui gère le déchiffrement DRM Widevine
- `stremio-web-fork` : Interface utilisateur qui détecte les flux DRM et les envoie vers Shaka Player (bypass du streaming server)

---

## Installation

### Option 1 : APK Android (le plus simple)

1. **Télécharger l'APK**
   - Va sur [GitHub Actions](https://github.com/Loo-stick/stremio-web-fork/actions)
   - Clique sur le dernier workflow "Build Android APK" réussi
   - Télécharge l'artifact `stremio-fork-debug`

2. **Installer sur Android**
   - Transfère l'APK sur ton téléphone
   - Active "Sources inconnues" si nécessaire
   - Installe l'APK

3. **Configurer le Streaming Server**
   - Installe [Stremio Desktop](https://www.stremio.com/downloads) sur ton PC
   - Lance Stremio Desktop (le streaming server démarre automatiquement)
   - Dans l'APK, va dans Paramètres → Streaming Server URL
   - Entre : `http://IP_DE_TON_PC:11470`

> **Note :** Le streaming server est nécessaire uniquement pour les **torrents**. Les contenus DRM (TF1+) fonctionnent sans.

---

### Option 2 : Version Web auto-hébergée

#### Prérequis
- Node.js 22+
- Git

#### Installation

```bash
# 1. Cloner les 2 repositories côte à côte
git clone https://github.com/Loo-stick/stremio-video-fork.git
git clone https://github.com/Loo-stick/stremio-web-fork.git

# 2. Installer les dépendances de stremio-video-fork
cd stremio-video-fork
npm install

# 3. Installer les dépendances de stremio-web-fork
cd ../stremio-web-fork
npm install

# 4. Builder le projet
npm run build

# 5. Lancer le serveur
node http_server.js
# Ou avec PM2 pour production :
pm2 start http_server.js --name stremio-fork
```

Le site est accessible sur `http://localhost:8080`

#### Configuration Apache (reverse proxy)

```apache
<VirtualHost *:80>
    ServerName votre-domaine.com
    ProxyPreserveHost On
    ProxyPass / http://127.0.0.1:8080/
    ProxyPassReverse / http://127.0.0.1:8080/

    Header always set Access-Control-Allow-Origin "*"
    Header always set Access-Control-Allow-Methods "GET, POST, OPTIONS"
    Header always set Access-Control-Allow-Headers "Content-Type, Authorization"
</VirtualHost>
```

---

### Option 3 : Builder ton propre APK

#### Via GitHub Actions (recommandé)

1. **Fork les 2 repositories** sur ton compte GitHub :
   - Fork `stremio-video-fork`
   - Fork `stremio-web-fork`

2. **Activer GitHub Actions** dans les paramètres de `stremio-web-fork`

3. **Lancer le build**
   - Va dans Actions → "Build Android APK" → "Run workflow"

4. **Télécharger l'APK** depuis les artifacts

#### Build local

Prérequis : Java JDK 21, Android SDK

```bash
cd stremio-web-fork
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
```

L'APK sera dans `android/app/build/outputs/apk/debug/`

---

## Streaming Server

Le **Streaming Server** est un composant de Stremio qui gère :
- Le streaming de **torrents** (téléchargement + lecture en temps réel)
- Le transcodage vidéo si nécessaire
- La gestion des sous-titres

### Ai-je besoin du Streaming Server ?

| Type de contenu | Streaming Server |
|-----------------|------------------|
| Torrents | **Obligatoire** |
| DRM (TF1+, etc.) | Non nécessaire (bypass) |
| HTTP classique | Optionnel |

### Options pour le Streaming Server

1. **Stremio Desktop** (le plus simple)
   - Installe [Stremio Desktop](https://www.stremio.com/downloads) sur un PC
   - Le streaming server démarre automatiquement sur le port 11470

2. **Docker**
   ```bash
   docker run -d -p 11470:11470 -p 12470:12470 stremio/server
   ```

3. **Installation manuelle**
   - Voir [stremio-server sur GitHub](https://github.com/Stremio/stremio-server)

---

## Support DRM

### Comment ça fonctionne ?

1. L'addon fournit un flux avec des `behaviorHints` contenant les infos DRM
2. `stremio-web-fork` détecte la présence de `key_systems` dans les hints
3. Le flux est envoyé directement vers Shaka Player (bypass du streaming server)
4. Shaka Player gère la demande de licence Widevine et le déchiffrement

### Format pour les addons DRM

Pour qu'un addon supporte le DRM, il doit retourner ce format :

```javascript
{
  url: "https://url-du-manifest.mpd",
  behaviorHints: {
    key_systems: {
      "com.widevine.alpha": "https://url-du-serveur-licence"
    },
    originalUrl: "https://url-originale.mpd"  // Optionnel
  }
}
```

---

## Structure des repositories

```
stremio-video-fork/          # Lecteur vidéo
├── src/
│   └── withStreamingServer.js   # Bypass streaming server pour DRM
│   └── selectVideoImplementation.js  # Sélection Shaka pour DRM
└── ...

stremio-web-fork/            # Interface web
├── src/                     # Code source React
├── build/                   # Build de production
├── android/                 # Projet Android (Capacitor)
├── .github/workflows/       # GitHub Actions
│   ├── build-android.yml    # Build APK debug
│   └── release-android.yml  # Build APK release signée
├── capacitor.config.ts      # Config Capacitor
└── http_server.js           # Serveur Express
```

---

## APK Release signée (Play Store)

Pour créer une APK signée, ajoute ces secrets dans GitHub :

| Secret | Description |
|--------|-------------|
| `SIGNING_KEY` | Keystore en base64 (`base64 -w 0 keystore.jks`) |
| `KEY_ALIAS` | Alias de la clé |
| `KEY_STORE_PASSWORD` | Mot de passe du keystore |
| `KEY_PASSWORD` | Mot de passe de la clé |

Puis lance le workflow "Release Android APK".

---

## FAQ

**Q: L'APK ne lit pas les torrents ?**
R: Tu dois configurer un streaming server. Installe Stremio Desktop sur un PC et configure l'URL dans les paramètres de l'APK.

**Q: Les contenus DRM ne fonctionnent pas ?**
R: Vérifie que ton appareil supporte Widevine. La plupart des appareils Android le supportent nativement.

**Q: Comment ajouter des addons ?**
R: Dans l'app, va dans le catalogue d'addons ou ajoute-les via URL.

---

## Licence

Fork non-officiel de Stremio. Stremio est copyright 2017-2023 Smart Code Ltd et disponible sous licence GPLv2.
