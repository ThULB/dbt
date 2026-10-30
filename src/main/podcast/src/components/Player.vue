<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { usePlayerStore } from '../stores/player';

const playerStore = usePlayerStore();
const { t } = useI18n({ useScope: 'global' });

const audioElement = ref<HTMLAudioElement | null>(null);
const playerElement = ref<HTMLElement | null>(null);

let playerResizeObserver: ResizeObserver | null = null;

const isLoading = ref(false);
const audioError = ref(false);
const loadedEpisodeGuid = ref<string | null>(null);
const loadedEpisodeUrl = ref<string | null>(null);

const imageLoaded = ref(false);
const imageError = ref(false);

const speedIndex = ref(1);
const speeds = [0.75, 1, 1.25, 1.5, 2];

const volume = ref(1);
const previousVolume = ref(1);
const isMuted = ref(false);

watch(
  () => playerStore.episode,

  async (episode) => {
    if (!episode) {
      return;
    }

    imageLoaded.value = false;
    imageError.value = false;

    audioError.value = false;
    isLoading.value = true;

    playerStore.currentTime = 0;
    playerStore.duration = 0;

    await nextTick();

    const audio = audioElement.value;

    if (!audio || episode !== playerStore.episode) {
      return;
    }

    loadedEpisodeGuid.value = episode.guid;
    loadedEpisodeUrl.value = episode.audioUrl;
    audio.src = episode.audioUrl;
    audio.load();

    audio.playbackRate = speeds[speedIndex.value];
    audio.volume = volume.value;
    audio.muted = isMuted.value;

    const request = playerStore.playbackRequest;

    if (request.command === 'play') {
      await playAudio(episode, request.id);
    }
  },

  {
    immediate: true,
  },
);

watch(
  () => playerStore.playbackRequest,
  (request) => {
    const audio = audioElement.value;

    if (!audio) return;

    if (request.command === 'play') {
      const episode = playerStore.episode;

      if (
        episode &&
        loadedEpisodeGuid.value === episode.guid &&
        loadedEpisodeUrl.value === episode.audioUrl
      ) {
        void playAudio(episode, request.id);
      }
    } else {
      audio.pause();
    }
  },
  {
    flush: 'sync',
  },
);

function isPlaybackError(error: unknown, name: string): boolean {
  return typeof error === 'object' && error !== null && 'name' in error && error.name === name;
}

async function playAudio(
  episode: NonNullable<typeof playerStore.episode>,
  requestId: number,
): Promise<void> {
  const audio = audioElement.value;

  if (!audio) return;

  try {
    await audio.play();
  } catch (error) {
    if (
      episode !== playerStore.episode ||
      requestId !== playerStore.playbackRequest.id ||
      isPlaybackError(error, 'AbortError')
    ) {
      return;
    }

    isLoading.value = false;
    playerStore.requestPause();

    if (isPlaybackError(error, 'NotAllowedError')) {
      return;
    }

    console.error('Error playing audio:', error);
    audioError.value = true;
  }
}

function skipBackward15(): void {
  if (!audioElement.value) {
    return;
  }

  audioElement.value.currentTime = Math.max(0, audioElement.value.currentTime - 15);
}

function skipForward15(): void {
  if (!audioElement.value) {
    return;
  }

  audioElement.value.currentTime = Math.min(
    audioElement.value.duration || Infinity,
    audioElement.value.currentTime + 15,
  );
}

function setSpeed(): void {
  if (!audioElement.value) {
    return;
  }

  speedIndex.value++;

  if (speedIndex.value >= speeds.length) {
    speedIndex.value = 0;
  }

  audioElement.value.playbackRate = speeds[speedIndex.value];
}

function toggleMute(): void {
  if (!audioElement.value) {
    return;
  }

  if (isMuted.value) {
    isMuted.value = false;

    volume.value = previousVolume.value;

    audioElement.value.muted = false;
    audioElement.value.volume = volume.value;
  } else {
    if (volume.value > 0) {
      previousVolume.value = volume.value;
    }

    volume.value = 0;
    isMuted.value = true;

    audioElement.value.muted = true;
  }
}

function setVolume(): void {
  if (!audioElement.value) {
    return;
  }

  audioElement.value.volume = volume.value;

  if (volume.value > 0) {
    previousVolume.value = volume.value;

    isMuted.value = false;
    audioElement.value.muted = false;
  } else {
    isMuted.value = true;
    audioElement.value.muted = true;
  }
}

function seekAudio(event: Event): void {
  if (!audioElement.value) {
    return;
  }

  const input = event.target as HTMLInputElement;

  audioElement.value.currentTime = Number(input.value);
}

function handleTimeUpdate(): void {
  if (!audioElement.value) {
    return;
  }

  playerStore.currentTime = audioElement.value.currentTime;
}

function handleLoadedMetadata(): void {
  if (!audioElement.value) {
    return;
  }

  playerStore.duration = audioElement.value.duration;
}

function handleEnded(): void {
  playerStore.isPlaying = false;
  playerStore.requestPause();
}

onMounted(() => {
  const player = playerElement.value;

  if (!player) return;

  const updatePlayerHeight = (): void => {
    document.body.style.setProperty('--podcast-player-height', `${player.offsetHeight}px`);
  };

  document.body.classList.add('has-podcast-player');
  updatePlayerHeight();

  playerResizeObserver = new ResizeObserver(updatePlayerHeight);
  playerResizeObserver.observe(player);
});

onUnmounted(() => {
  playerResizeObserver?.disconnect();
  document.body.classList.remove('has-podcast-player');
  document.body.style.removeProperty('--podcast-player-height');
});
</script>

<template>
  <section
      ref="playerElement"
      class="playlist-player position-fixed bottom-0 start-0 w-100 text-body"
      :aria-label="t('podcast.player.ariaLabel')"
  >
    <audio
        ref="audioElement"
        class="d-none"
        preload="metadata"
        @play="playerStore.isPlaying = true"
        @pause="playerStore.isPlaying = false"
        @ended="handleEnded"
        @waiting="isLoading = true"
        @playing="isLoading = false"
        @canplay="isLoading = false"
        @error="audioError = true"
        @timeupdate="handleTimeUpdate"
        @loadedmetadata="handleLoadedMetadata"
    />

    <div class="playlist-player__inner container">

      <!-- Episode -->
      <div class="playlist-player__track d-flex align-items-center">

        <div class="playlist-player__artwork position-relative flex-shrink-0 overflow-hidden rounded-1">

          <img
              v-if="
                playerStore.episode?.image &&
                !imageError
              "
              :src="playerStore.episode.image"
              :alt="t('podcast.image.coverAlt', { title: playerStore.episode.title })"
              class="position-absolute top-0 start-0 d-block w-100 h-100 object-fit-cover"
              :class="{
                'is-loaded': imageLoaded
              }"
              @load="imageLoaded = true"
              @error="imageError = true"
          >

          <span
              v-if="
                !imageLoaded ||
                imageError
              "
              class="playlist-player__artwork-placeholder position-absolute top-0 start-0 d-grid w-100 h-100"
              aria-hidden="true"
          >
            <i class="fas fa-headphones"></i>
          </span>

        </div>

        <div class="playlist-player__copy">

          <p
              class="playlist-player__status text-truncate mb-0"
              aria-live="polite"
              :data-tone="
                audioError
                  ? 'error'
                  : isLoading
                    ? 'loading'
                    : playerStore.isPlaying
                      ? 'playing'
                      : ''
              "
          >
            {{
              audioError
                  ? t('podcast.player.status.unavailable')
                  : isLoading
                      ? t('podcast.player.status.buffering')
                      : playerStore.isPlaying
                          ? t('podcast.player.status.playing')
                          : t('podcast.player.status.paused')
            }}
          </p>

          <p class="playlist-player__title text-truncate mb-0">
            {{ playerStore.episode?.title }}
          </p>

          <p class="playlist-player__context text-truncate mb-0 text-body-secondary">
            {{ playerStore.playlistTitle }}
          </p>

        </div>

      </div>

      <!-- Controls -->
      <div
          class="playlist-player__transport d-flex align-items-center justify-content-start justify-content-sm-center"
          role="group"
          :aria-label="t('podcast.player.controls')"
      >

        <!-- Previous episode -->
        <button
            class="playlist-player__button d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
            type="button"
            :aria-label="t('podcast.player.previousEpisode')"
            :title="t('podcast.player.previousEpisode')"
            :disabled="playerStore.currentEpisodeIndex >= playerStore.episodes.length - 1"
            @click="playerStore.previousEpisode"
        >
          <i
              class="fas fa-backward-step"
              aria-hidden="true"
          ></i>
        </button>

        <!-- Back 15 sec -->
        <button
            class="playlist-player__button d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
            type="button"
            :aria-label="t('podcast.player.back15')"
            :title="t('podcast.player.back15')"
            @click="skipBackward15"
        >
          <i
              class="fas fa-rotate-left"
              aria-hidden="true"
          ></i>
        </button>

        <!-- Play / Pause -->
        <button
            class="playlist-player__button playlist-player__play d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
            type="button"
            :aria-label="
              playerStore.isPlaying
                ? t('podcast.player.pause')
                : t('podcast.player.playEpisode')
            "
            :title="
              playerStore.isPlaying
                ? t('podcast.player.pause')
                : t('podcast.player.playEpisode')
            "
            @click="playerStore.togglePlayback"
        >
          <i
              class="playlist-player__play-icon fas"
              :class="
                playerStore.isPlaying
                  ? 'fa-pause'
                  : 'fa-play'
              "
              aria-hidden="true"
          ></i>
        </button>

        <!-- Forward 15 sec -->
        <button
            class="playlist-player__button d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
            type="button"
            :aria-label="t('podcast.player.forward15')"
            :title="t('podcast.player.forward15')"
            @click="skipForward15"
        >
          <i
              class="fas fa-rotate-right"
              aria-hidden="true"
          ></i>
        </button>

        <!-- Next episode -->
        <button
            class="playlist-player__button d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
            type="button"
            :aria-label="t('podcast.player.nextEpisode')"
            :title="t('podcast.player.nextEpisode')"
            :disabled="playerStore.currentEpisodeIndex <= 0"
            @click="playerStore.nextEpisode"
        >
          <i
              class="fas fa-forward-step"
              aria-hidden="true"
          ></i>
        </button>

      </div>

      <!-- Tools -->
      <div class="playlist-player__tools d-flex align-items-center justify-content-end">

        <!-- Speed -->
        <button
            class="playlist-player__button playlist-player__speed d-inline-flex align-items-center justify-content-center flex-shrink-0 border-0 py-0 rounded-1"
            type="button"
            :aria-label="t('podcast.player.changeSpeed')"
            :title="`${speeds[speedIndex]}×`"
            @click="setSpeed"
        >
          {{ speeds[speedIndex] }}&times;
        </button>

        <!-- Volume -->
        <div class="playlist-player__volume d-flex align-items-center">

          <button
              class="playlist-player__button d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
              type="button"
              :aria-label="
                isMuted
                  ? t('podcast.player.unmute')
                  : t('podcast.player.mute')
              "
              :title="
                isMuted
                  ? t('podcast.player.unmute')
                  : t('podcast.player.mute')
              "
              @click="toggleMute"
          >
            <i
                class="fas"
                :class="
                  isMuted
                    ? 'fa-volume-mute'
                    : 'fa-volume-high'
                "
                aria-hidden="true"
            ></i>
          </button>

          <input
              v-model.number="volume"
              type="range"
              min="0"
              max="1"
              step="0.05"
              :aria-label="t('podcast.player.volume')"
              @input="setVolume"
          >

        </div>

      </div>

      <!-- Timeline -->
      <div class="playlist-player__timeline text-body-secondary">

        <span>
          {{ playerStore.formatTime(playerStore.currentTime) }}
        </span>

        <input
            type="range"
            min="0"
            :max="playerStore.duration"
            :value="playerStore.currentTime"
            step="0.1"
            :aria-label="t('podcast.player.position')"
            :disabled="!playerStore.duration"
            @input="seekAudio"
        >

        <span>
          {{ playerStore.formatTime(playerStore.duration) }}
        </span>

      </div>

      <!-- Close Button -->
      <button
          class="playlist-player__button playlist-player__close d-inline-flex align-items-center justify-content-center rounded-circle border-0 p-0"
          type="button"
          :aria-label="t('podcast.player.close')"
          :title="t('podcast.player.close')"
          @click="playerStore.closePlayer"
      >
        <i class="fa fa-times" aria-hidden="true"></i>
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.playlist-player {
  --player-artwork-size: 3.25rem;
  --player-control-size: 2.25rem;
  --player-play-size: 3rem;

  z-index: 1040;
  border-top: 1px solid #c8c8c8;
  background: #e0e0e0;
  box-shadow: 0 -0.5rem 1.5rem rgba(0, 0, 0, 0.08);

  &__inner {
    display: grid;
    grid-template-areas:
      "track transport tools"
      "timeline timeline timeline";
    grid-template-columns:
      minmax(12rem, 1fr)
      auto
      minmax(10rem, 0.7fr);
    align-items: center;
    gap: 0.35rem clamp(0.65rem, 2vw, 1.5rem);
    padding-block: 0.65rem;
  }

  &__track {
    grid-area: track;
    min-width: 0;
    gap: 0.65rem;
  }

  &__artwork {
    flex-basis: var(--player-artwork-size);
    width: var(--player-artwork-size);
    height: var(--player-artwork-size);
    background: var(--bs-body-bg);

    img {
      opacity: 0;
      transition: opacity 0.2s ease;

      &.is-loaded {
        opacity: 1;
      }
    }
  }

  &__artwork-placeholder {
    place-items: center;
    color: var(--bs-success);
    font-size: var(--podcast-icon-artwork);
  }

  &__copy {
    min-width: 0;
  }

  &__status {
    color: var(--bs-secondary-color);
    font-size: var(--podcast-font-caption);
    line-height: var(--podcast-line-height-compact);
    font-weight: 700;
    letter-spacing: 0.04em;
    text-transform: uppercase;

    &[data-tone="playing"] {
      color: var(--bs-success);
    }

    &[data-tone="loading"] {
      color: var(--bs-warning-text-emphasis);
    }

    &[data-tone="error"] {
      color: var(--bs-danger);
    }
  }

  &__title {
    font-size: var(--podcast-font-item-title);
    line-height: var(--podcast-line-height-compact);
    font-weight: 700;
  }

  &__context {
    font-size: var(--podcast-font-meta);
    line-height: var(--podcast-line-height-compact);
  }

  &__button {
    width: var(--player-control-size);
    height: var(--player-control-size);
    background: transparent;
    color: var(--bs-success-text-emphasis);
    cursor: pointer;
    font-size: var(--podcast-icon-control);
    line-height: 1;

    &:hover:not(:disabled) {
      background: rgba(0, 0, 0, 0.07);
      color: var(--bs-success-text-emphasis);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.35;
    }
  }

  &__button:focus-visible,
  input:focus-visible {
    outline: 0.16rem solid rgba(var(--bs-success-rgb), 0.35);
    outline-offset: 0.14rem;
  }

  &__transport {
    grid-area: transport;
    gap: clamp(0.15rem, 0.8vw, 0.55rem);
  }

  &__play {
    width: var(--player-play-size);
    height: var(--player-play-size);
    background: var(--bs-success);
    color: var(--bs-white);
    font-size: var(--podcast-icon-action);

    &:hover:not(:disabled) {
      background: var(--bs-success-text-emphasis);
      color: var(--bs-white);
    }
  }

  &__play-icon {
    font-size: var(--podcast-icon-action);

    &.fa-play {
      position: relative;
      left: 3%;
    }
  }

  &__tools {
    grid-area: tools;
    min-width: 0;
    gap: clamp(0.25rem, 1vw, 0.65rem);
  }

  &__speed {
    width: auto;
    min-width: 2.6rem;
    padding-inline: 0.35rem;
    color: var(--bs-success-text-emphasis);
    font-size: var(--podcast-font-control);
    font-weight: 700;
  }

  &__volume {
    min-width: 0;
    gap: 0.2rem;

    input {
      width: clamp(2.25rem, 11vw, 8rem);
      min-width: 0;
    }
  }

  input[type="range"] {
    height: 1rem;
    margin: 0;
    padding: 0;
    accent-color: var(--bs-success);
    cursor: pointer;

    &:disabled {
      cursor: wait;
      opacity: 0.55;
    }
  }

  &__timeline {
    display: grid;
    grid-area: timeline;
    grid-template-columns:
      3.25rem
      minmax(0, 1fr)
      3.25rem;
    align-items: center;
    gap: 0.5rem;
    font-size: var(--podcast-font-caption);
    line-height: var(--podcast-line-height-compact);
    font-variant-numeric: tabular-nums;

    input {
      width: 100%;
      min-width: 0;
    }

    > span:last-child {
      text-align: right;
    }
  }

  &__close {
    position: absolute;
    top: clamp(0.5rem, 1vw, 0.7rem);
    right: clamp(0.5rem, 1vw, 0.7rem);
    width: clamp(1rem, 1.2vw, 1.25rem);
    height: clamp(1rem, 1.2vw, 1.25rem);
    color: var(--bs-danger);
    background-color: rgba(var(--bs-danger-rgb), 0.1);
    font-size: var(--podcast-font-caption);
    transition:
        background-color 0.15s ease,
        color 0.15s ease,
        transform 0.15s ease;

    &:hover {
      color: var(--bs-white);
      background-color: var(--bs-danger);
      transform: scale(1.08);
    }

    &:active {
      transform: scale(0.92);
    }
  }

  @media (max-width: 767.98px) {
    &__inner {
      grid-template-areas:
        "track tools"
        "transport transport"
        "timeline timeline";
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 0.3rem 0.5rem;
      padding-block: 0.55rem;
    }
  }

  @media (max-width: 575.98px) {
    --player-artwork-size: 2.75rem;
    --player-control-size: 2.25rem;
    --player-play-size: 2.75rem;

    &__inner {
      grid-template-areas:
        "track track"
        "transport tools"
        "timeline timeline";
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 0.25rem 0.2rem;
      padding-block: 0.45rem;
    }

    &__context {
      display: none;
    }

    &__track {
      padding-right: 2rem;
    }

    &__transport {
      gap: 0.1rem;
    }

    &__tools {
      gap: 0;
    }

    &__speed {
      min-width: 2.25rem;
      padding-inline: 0.15rem;
    }

    &__volume {
      gap: 0;

      input {
        width: clamp(4rem, 18vw, 5.25rem);
      }
    }

    &__timeline {
      grid-template-columns:
        max-content
        minmax(0, 1fr)
        max-content;
      gap: 0.2rem;
    }

    input[type="range"] {
      height: 0.8rem;
    }
  }

  @media (max-width: 375px) {
    --player-control-size: 2rem;
    --player-play-size: 2.5rem;

    &__inner {
      column-gap: 0;
    }

    &__transport {
      gap: 0;
    }

    &__volume input {
      width: clamp(3.5rem, 18vw, 4rem);
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &__artwork img,
    &__close {
      transition: none;
    }
  }
}

:global(body.has-podcast-player .back-to-top) {
  bottom: calc(var(--podcast-player-height, 0px) + 1rem);
}

:global(body.has-podcast-player > footer) {
  flex-shrink: 0;
  padding-bottom: calc(var(--podcast-player-height, 0px) + 20px);
}
</style>
