import { registerPlugin } from '@capacitor/core';
import type { VLCPluginInterface } from './definitions';

const VLCPlugin = registerPlugin<VLCPluginInterface>('VLCPlugin', {
  web: () => import('./web').then((m) => new m.VLCPluginWeb()),
});

export * from './definitions';
export { VLCPlugin };
