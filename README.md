# Stremio Web Fork - Support DRM Widevine

Fork de Stremio Web avec support natif Widevine DRM pour les replays TF1+ et autres contenus protégés.

## Fonctionnalités

- Support Widevine DRM natif via Shaka Player
- Bypass automatique du streaming server pour les flux DRM
- Compatible navigateur web et Android (APK)

## Prérequis

- Node.js 22+
- npm
- Git

## Installation rapide

### 1. Cloner les repositories

```bash
# Cloner les deux repos côte à côte
git clone https://github.com/VOTRE_USERNAME/stremio-video-fork.git
git clone https://github.com/VOTRE_USERNAME/stremio-web-fork.git
```

### 2. Installer les dépendances

```bash
cd stremio-web-fork
npm install
```

### 3. Builder le projet

```bash
npm run build
```

### 4. Lancer le serveur

```bash
node http_server.js
# ou avec PM2:
pm2 start http_server.js --name stremio-fork
```

Le site sera accessible sur `http://localhost:8080`

## Configuration Apache (reverse proxy)

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

## Build Android APK

### Option 1: GitHub Actions (recommandé)

1. Fork les deux repositories sur votre compte GitHub
2. Activez GitHub Actions dans les paramètres du repo
3. Allez dans Actions > "Build Android APK" > "Run workflow"
4. Téléchargez l'APK depuis les artifacts

### Option 2: Build local

Prérequis:
- Java JDK 17
- Android SDK (Android Studio ou command-line tools)

```bash
# Sync et build
npx cap sync android
cd android
./gradlew assembleDebug
```

L'APK sera dans `android/app/build/outputs/apk/debug/`

### Secrets pour Release signée

Pour créer une APK signée (publication Play Store), ajoutez ces secrets dans GitHub:

| Secret | Description |
|--------|-------------|
| `SIGNING_KEY` | Keystore en base64 (`base64 -w 0 keystore.jks`) |
| `KEY_ALIAS` | Alias de la clé |
| `KEY_STORE_PASSWORD` | Mot de passe du keystore |
| `KEY_PASSWORD` | Mot de passe de la clé |

## Structure du projet

```
stremio-web-fork/
├── src/                    # Code source React
├── build/                  # Build de production
├── android/                # Projet Android (Capacitor)
├── .github/workflows/      # GitHub Actions
│   ├── build-android.yml   # Build APK debug
│   └── release-android.yml # Build APK release signée
├── capacitor.config.ts     # Config Capacitor
└── http_server.js          # Serveur Express
```

## Addon DRM

Pour utiliser le support DRM, l'addon doit retourner les `behaviorHints` suivants:

```javascript
{
  url: "https://url-du-mpd.mpd",
  behaviorHints: {
    key_systems: {
      "com.widevine.alpha": "https://license-server-url"
    },
    originalUrl: "https://url-originale-du-mpd.mpd"
  }
}
```

## Licence

Ce projet est un fork non-officiel de Stremio. Stremio est copyright 2017-2023 Smart code et disponible sous licence GPLv2.
