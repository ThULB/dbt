import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import type { PodcastEpisode } from '../types/types';

type PlaybackCommand = 'play' | 'pause';

interface PlaybackRequest {
  id: number;
  command: PlaybackCommand;
}

export const usePlayerStore = defineStore('player', () => {
  const episode = ref<PodcastEpisode | null>(null);
  const episodes = ref<PodcastEpisode[]>([]);
  const isPlaying = ref(false);
  const playbackRequest = ref<PlaybackRequest>({
    id: 0,
    command: 'pause',
  });
  const playlistTitle = ref('');

  let playbackRequestId = 0;

  const currentTime = ref(0);
  const duration = ref(0);

  const remainingTime = computed(() => Math.max(0, duration.value - currentTime.value));

  const currentEpisodeIndex = computed(() => {
    if (!episode.value) {
      return -1;
    }

    return episodes.value.findIndex((item) => item.guid === episode.value?.guid);
  });

  function setPlaylist(newEpisodes: PodcastEpisode[], title: string): void {
    episodes.value = newEpisodes;
    playlistTitle.value = title;
  }

  function selectEpisode(newEpisode: PodcastEpisode): void {
    episode.value = newEpisode;
    requestPlay();
  }

  function requestPlay(): void {
    playbackRequest.value = {
      id: ++playbackRequestId,
      command: 'play',
    };
  }

  function requestPause(): void {
    playbackRequest.value = {
      id: ++playbackRequestId,
      command: 'pause',
    };
  }

  function previousEpisode(): void {
    if (currentEpisodeIndex.value < 0 || currentEpisodeIndex.value >= episodes.value.length - 1) {
      return;
    }

    selectEpisode(episodes.value[currentEpisodeIndex.value + 1]);
  }

  function nextEpisode(): void {
    if (currentEpisodeIndex.value <= 0) {
      return;
    }

    selectEpisode(episodes.value[currentEpisodeIndex.value - 1]);
  }

  function closePlayer(): void {
    requestPause();
    isPlaying.value = false;
    episode.value = null;
  }

  function togglePlayback(): void {
    if (isPlaying.value) {
      requestPause();
    } else {
      requestPlay();
    }
  }

  function toggleEpisode(selectedEpisode: PodcastEpisode): void {
    if (episode.value?.guid === selectedEpisode.guid) {
      togglePlayback();
    } else {
      selectEpisode(selectedEpisode);
    }
  }

  function formatTime(seconds: number): string {
    if (!Number.isFinite(seconds)) {
      return '0:00';
    }

    const totalSeconds = Math.floor(seconds);

    const hours = Math.floor(totalSeconds / 3600);

    const minutes = Math.floor((totalSeconds % 3600) / 60);

    const secs = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  return {
    episode,
    episodes,
    isPlaying,
    playbackRequest,
    playlistTitle,
    currentEpisodeIndex,
    currentTime,
    duration,
    remainingTime,

    setPlaylist,
    selectEpisode,
    requestPause,
    togglePlayback,
    nextEpisode,
    previousEpisode,
    closePlayer,
    toggleEpisode,
    formatTime,
  };
});
