import { WebPlugin } from '@capacitor/core';
import type {
  VLCPluginInterface,
  PlayOptions,
  AddSubtitleOptions,
  SeekOptions,
  SelectSubtitleOptions,
  PlayerState,
  SubtitleTrack,
} from './definitions';

export class VLCPluginWeb extends WebPlugin implements VLCPluginInterface {
  private throwUnavailable(): never {
    throw new Error('VLC plugin is not available on web platform');
  }

  async play(_options: PlayOptions): Promise<{ result: boolean }> {
    this.throwUnavailable();
  }

  async pause(): Promise<void> {
    this.throwUnavailable();
  }

  async resume(): Promise<void> {
    this.throwUnavailable();
  }

  async stop(): Promise<void> {
    this.throwUnavailable();
  }

  async seekTo(_options: SeekOptions): Promise<void> {
    this.throwUnavailable();
  }

  async addSubtitle(_options: AddSubtitleOptions): Promise<{ trackId: number }> {
    this.throwUnavailable();
  }

  async getSubtitleTracks(): Promise<{ tracks: SubtitleTrack[] }> {
    this.throwUnavailable();
  }

  async selectSubtitleTrack(_options: SelectSubtitleOptions): Promise<void> {
    this.throwUnavailable();
  }

  async disableSubtitles(): Promise<void> {
    this.throwUnavailable();
  }

  async getState(): Promise<PlayerState> {
    this.throwUnavailable();
  }
}
