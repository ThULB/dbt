import { defineStore } from 'pinia';
import { ref, shallowReactive } from 'vue';

import requestReceive from '../utils/requestReceive';
import parsePodcastXml from '../utils/XMLParser.ts';
import type { PodcastPlaylist } from '../types/types';
import { usePlayerStore } from './player.ts';

export const usePodcastsStore = defineStore('podcasts', () => {
  const playerStore = usePlayerStore();

  const playlists = ref<Record<string, PodcastPlaylist>>({});

  /*
   * Laufende Requests je Podcast. Verhindert doppelte Requests;
   * der Ladezustand wird daraus abgeleitet.
   */
  const pendingRequests = shallowReactive(new Map<string, Promise<PodcastPlaylist | null>>());

  /*
   * Nur der zuletzt angeforderte Podcast darf die Wiedergabe starten.
   */
  let playRequestSequence = 0;

  function cachePlaylist(podcastId: string, playlist: PodcastPlaylist): void {
    playlists.value[podcastId] = playlist;
  }

  function isPlaylistLoading(podcastId: string): boolean {
    return pendingRequests.has(podcastId);
  }

  /*
   * Bleibt beim Wechsel zwischen Folgen desselben Podcasts und beim Pausieren erhalten.
   */
  function isPodcastCurrent(podcastId: string): boolean {
    return !!playerStore.episode && playerStore.podcastId === podcastId;
  }

  function isPodcastPlaying(podcastId: string): boolean {
    return isPodcastCurrent(podcastId) && playerStore.isPlaying;
  }

  function loadPlaylist(podcastId: string): Promise<PodcastPlaylist | null> {
    const cachedPlaylist = playlists.value[podcastId];

    if (cachedPlaylist) {
      return Promise.resolve(cachedPlaylist);
    }

    const pendingRequest = pendingRequests.get(podcastId);

    if (pendingRequest) {
      return pendingRequest;
    }

    const request = (async () => {
      try {
        const playlist = parsePodcastXml(await requestReceive(podcastId));

        cachePlaylist(podcastId, playlist);

        return playlist;
      } catch (err) {
        console.error('Podcastdaten konnten nicht geladen werden:', err);
        return null;
      } finally {
        pendingRequests.delete(podcastId);
      }
    })();

    pendingRequests.set(podcastId, request);

    return request;
  }

  async function playLatestEpisode(podcastId: string): Promise<void> {
    if (isPodcastCurrent(podcastId)) {
      playerStore.togglePlayback();
      return;
    }

    const requestId = ++playRequestSequence;
    const episodeAtStart = playerStore.episode;

    const playlist = await loadPlaylist(podcastId);

    // Veraltet: inzwischen wurde ein anderer Podcast angefordert oder anderswo eine Folge gestartet.
    if (requestId !== playRequestSequence || playerStore.episode !== episodeAtStart) {
      return;
    }

    const latestEpisode = playlist?.episodes.length ? playlist.episodes[playlist.episodes.length - 1] : null;

    if (!playlist || !latestEpisode) {
      return;
    }

    playerStore.setPlaylist(playlist.episodes, playlist.title, podcastId);
    playerStore.selectEpisode(latestEpisode);
  }

  return {
    playlists,

    cachePlaylist,
    isPlaylistLoading,
    isPodcastCurrent,
    isPodcastPlaying,
    loadPlaylist,
    playLatestEpisode,
  };
});
