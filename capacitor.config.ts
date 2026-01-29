import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'ovh.loostick.stremiofork',
  appName: 'Stremio Fork',
  webDir: 'build',
  android: {
    allowMixedContent: true,
    webContentsDebuggingEnabled: true
  },
  server: {
    androidScheme: 'https',
    cleartext: true
  },
  plugins: {
    CapacitorNodeJS: {
      nodeDir: 'nodejs'
    }
  }
};

export default config;
