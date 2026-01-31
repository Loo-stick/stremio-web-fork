import type { PluginListenerHandle } from '@capacitor/core';

export interface SubtitleTrack {
  id: number;
  name: string;
  language?: string;
}

export interface PlayOptions {
  url: string;
  title?: string;
  subtitle?: string;
  startTime?: number;
}

export interface AddSubtitleOptions {
  url: string;
  name?: string;
  select?: boolean;
}

export interface SeekOptions {
  time: number;
}

export interface SelectSubtitleOptions {
  trackId: number;
}

export interface TimeUpdateData {
  time: number;
  duration: number;
}

export interface ErrorData {
  message: string;
  code?: number;
}

export interface PlayerState {
  isPlaying: boolean;
  time: number;
  duration: number;
}

export interface VLCPluginInterface {
  /**
   * Initialize and start video playback
   */
  play(options: PlayOptions): Promise<{ result: boolean }>;

  /**
   * Pause playback
   */
  pause(): Promise<void>;

  /**
   * Resume playback
   */
  resume(): Promise<void>;

  /**
   * Stop playback and close player
   */
  stop(): Promise<void>;

  /**
   * Seek to specific time
   */
  seekTo(options: SeekOptions): Promise<void>;

  /**
   * Add external subtitle during playback
   */
  addSubtitle(options: AddSubtitleOptions): Promise<{ trackId: number }>;

  /**
   * Get available subtitle tracks
   */
  getSubtitleTracks(): Promise<{ tracks: SubtitleTrack[] }>;

  /**
   * Select a subtitle track
   */
  selectSubtitleTrack(options: SelectSubtitleOptions): Promise<void>;

  /**
   * Disable subtitles
   */
  disableSubtitles(): Promise<void>;

  /**
   * Get current player state
   */
  getState(): Promise<PlayerState>;

  /**
   * Listen for player events
   */
  addListener(
    eventName: 'playing',
    listenerFunc: () => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'paused',
    listenerFunc: () => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'ended',
    listenerFunc: () => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'timeUpdate',
    listenerFunc: (data: TimeUpdateData) => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'error',
    listenerFunc: (data: ErrorData) => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'subtitleTracksChanged',
    listenerFunc: (data: { tracks: SubtitleTrack[] }) => void
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'playerClosed',
    listenerFunc: () => void
  ): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners
   */
  removeAllListeners(): Promise<void>;
}
