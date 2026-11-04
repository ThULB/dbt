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
const displayedBackgroundImage = ref<string | null>(null);

const isExpanded = ref(false);
const isClosing = ref(false);

const playerCloseAnimationDuration = 280;
let playerCloseAnimationTimer: number | null = null;

const speedIndex = ref(1);
const speeds = [0.75, 1, 1.25, 1.5, 2];

const volume = ref(1);
const previousVolume = ref(1);
const isMuted = ref(false);

watch(
  () => playerStore.episode,

  async (episode) => {
    if (!episode) {
      displayedBackgroundImage.value = null;
      return;
    }

    imageLoaded.value = false;
    imageError.value = false;

    if (!episode.image) {
      displayedBackgroundImage.value = null;
    }

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

function handleArtworkLoad(event: Event): void {
  const imageUrl = (event.currentTarget as HTMLImageElement).getAttribute('src');

  if (!imageUrl || imageUrl !== playerStore.episode?.image) {
    return;
  }

  imageLoaded.value = true;
  imageError.value = false;
  displayedBackgroundImage.value = imageUrl;
}

function handleArtworkError(event: Event): void {
  const imageUrl = (event.currentTarget as HTMLImageElement).getAttribute('src');

  if (imageUrl !== playerStore.episode?.image) {
    return;
  }

  imageLoaded.value = false;
  imageError.value = true;
  displayedBackgroundImage.value = null;
}

function prefersReducedMotion(): boolean {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function animatePlayerOut(onFinished: () => void): void {
  if (isClosing.value) {
    return;
  }

  if (prefersReducedMotion()) {
    onFinished();
    return;
  }

  isClosing.value = true;
  playerCloseAnimationTimer = window.setTimeout(() => {
    playerCloseAnimationTimer = null;
    isClosing.value = false;
    onFinished();
  }, playerCloseAnimationDuration);
}

function expandPlayer(): void {
  if (isExpanded.value || isClosing.value) {
    return;
  }

  isExpanded.value = true;
}

function collapsePlayer(): void {
  if (!isExpanded.value || isClosing.value) {
    return;
  }

  animatePlayerOut(() => {
    isExpanded.value = false;
  });
}

function closePlayer(): void {
  animatePlayerOut(() => {
    playerStore.closePlayer();
  });
}

async function playAudio(episode: NonNullable<typeof playerStore.episode>, requestId: number,): Promise<void> {
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

function getRangeProgress(value: number, max: number): string {
  if (!Number.isFinite(value) || !Number.isFinite(max) || max <= 0) {
    return '0%';
  }

  const progress = Math.min(100, Math.max(0, (value / max) * 100));

  return `${progress}%`;
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
  if (playerCloseAnimationTimer !== null) {
    window.clearTimeout(playerCloseAnimationTimer);
  }

  playerResizeObserver?.disconnect();
  document.body.classList.remove('has-podcast-player');
  document.body.style.removeProperty('--podcast-player-height');
});
</script>

<template>
  <section
      ref="playerElement"
      class="playlist-player position-fixed bottom-0 start-0 w-100 text-body"
      :class="{
        'playlist-player--expanded': isExpanded,
        'playlist-player--closing': isClosing,
      }"
      :aria-label="t('podcast.player.ariaLabel')"
  >
    <Transition name="playlist-player-background">
      <div
          v-if="displayedBackgroundImage"
          :key="displayedBackgroundImage"
          class="playlist-player__background"
          aria-hidden="true"
      >
        <img
            :src="displayedBackgroundImage"
            alt=""
        >
      </div>
    </Transition>

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

    <div
        class="playlist-player__inner"
        :class="{'container': !isExpanded}"
    >

      <!-- Episode -->
      <div
          class="playlist-player__track d-flex align-items-center"
          @click="expandPlayer"
      >

        <div
            class="playlist-player__artwork position-relative flex-shrink-0 overflow-hidden rounded-4"
        >

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
              @load="handleArtworkLoad"
              @error="handleArtworkError"
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

          <span
              v-if="!isExpanded"
              class="playlist-player__artwork-expand"
              aria-hidden="true"
          >
            <i class="fa-solid fa-up-right-and-down-left-from-center"></i>
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
            class="playlist-player__button playlist-player__speed d-inline-flex align-items-center justify-content-center flex-shrink-0 border-0 py-0 rounded-5"
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
              :style="{ '--range-progress': getRangeProgress(volume, 1) }"
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
            :style="{
              '--range-progress': getRangeProgress(
                playerStore.currentTime,
                playerStore.duration,
              ),
            }"
            :aria-label="t('podcast.player.position')"
            :disabled="!playerStore.duration"
            @input="seekAudio"
        >

        <span>
          {{ playerStore.formatTime(playerStore.duration) }}
        </span>

      </div>

      <!-- Collapse Button -->
      <button
          v-if="isExpanded"
          class="playlist-player__button playlist-player__collapse d-inline-flex align-items-center justify-content-center rounded-circle border-0 p-0"
          type="button"
          :aria-label="t('podcast.player.collapse')"
          :title="t('podcast.player.collapse')"
          @click="collapsePlayer"
      >
        <i class="fa-solid fa-chevron-down" aria-hidden="true"></i>
      </button>
    </div>

    <!-- Close Button -->
    <button
        class="playlist-player__button playlist-player__close d-inline-flex align-items-center justify-content-center rounded-circle p-0"
        type="button"
        :aria-label="t('podcast.player.close')"
        :title="t('podcast.player.close')"
        @click="closePlayer"
    >
      <i class="fa fa-times" aria-hidden="true"></i>
    </button>
  </section>
</template>

<style scoped lang="scss">
.playlist-player {
  --player-artwork-size: 3.25rem;
  --player-control-size: 2.25rem;
  --player-play-size: 3rem;

  z-index: 1040;
  border-top: 1px solid var(--bs-border-color);
  background: var(--bs-secondary-bg);
  box-shadow: 0 -0.5rem 1.5rem var(--podcast-color-neutral-08);
  animation: playlist-player-mini-enter 280ms cubic-bezier(0.22, 1, 0.36, 1) both;

  &--closing:not(&--expanded) {
    animation: playlist-player-mini-exit 280ms cubic-bezier(0.4, 0, 1, 1) both;
    pointer-events: none;
  }

  &__background {
    position: absolute;
    z-index: 0;
    inset: 0;
    overflow: hidden;
    pointer-events: none;

    img {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
      opacity: 0.18;
      filter: blur(3rem) brightness(0.72);
      transform: scale(1.18);
    }
  }

  &-background-enter-active,
  &-background-leave-active {
    transition: opacity 350ms ease;
  }

  &-background-enter-from,
  &-background-leave-to {
    opacity: 0;
  }

  &__inner {
    position: relative;
    z-index: 1;
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
    cursor: pointer;
  }

  &__artwork {
    flex-basis: var(--player-artwork-size);
    width: var(--player-artwork-size);
    height: var(--player-artwork-size);
    background: var(--bs-body-bg);
    background: var(--podcast-gradient-artwork);

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
    color: var(--bs-white);
    font-size: calc(var(--player-artwork-size) * 0.45);
  }

  &__artwork-expand {
    position: absolute;
    z-index: 1;
    inset: 0;
    display: grid;
    place-items: center;
    color: var(--bs-white);
    background: var(--podcast-color-neutral-28);
    pointer-events: none;
    opacity: 0;
    transition: opacity 0.18s ease;

    i {
      font-size: clamp(1.4rem, 1.25rem + 0.5vw, 2rem);
      transform: scale(0.82);
      transition: transform 0.18s ease;
    }
  }

  @media (hover: hover) and (pointer: fine) {
    &__track:hover &__artwork-expand {
      opacity: 1;

      i {
        transform: scale(1);
      }
    }
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
    background-color: var(--podcast-color-control-background);
    color: var(--bs-success-text-emphasis);
    cursor: pointer;
    font-size: var(--podcast-icon-control);
    line-height: 1;
    transition:
        background-color 0.15s ease,
        border-color 0.15s ease,
        color 0.15s ease;

    &:hover:not(:disabled) {
      background-color: var(--podcast-color-neutral-08);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.35;
    }
  }

  &__button:focus-visible,
  input:focus-visible {
    outline: 0.16rem solid var(--bs-focus-ring-color);
    outline-offset: 0.14rem;
  }

  &__transport {
    grid-area: transport;
    gap: clamp(0.15rem, 0.8vw, 0.55rem);
  }

  &__play {
    width: var(--player-play-size);
    height: var(--player-play-size);
    background-color: var(--bs-success);
    color: var(--bs-white);
    font-size: var(--podcast-icon-action);

    &:hover:not(:disabled) {
      background-color: var(--bs-success-text-emphasis);
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
    height: 1.75rem;
    padding-inline: 0.35rem;
    background-color: var(--podcast-color-control-background);
    color: var(--bs-success-text-emphasis);
    font-size: var(--podcast-font-control);
    font-weight: 700;
  }

  &__volume {
    min-width: 0;
    gap: 0.2rem;

    button {
      background-color: transparent;
    }

    input {
      width: clamp(2.25rem, 11vw, 8rem);
      min-width: 0;
    }
  }

  input[type="range"] {
    -webkit-appearance: none;
    appearance: none;
    height: 1rem;
    margin: 0;
    padding: 0;
    border: 0;
    background: transparent;
    cursor: pointer;

    &::-webkit-slider-runnable-track {
      height: 0.25rem;
      border-radius: var(--bs-border-radius-pill);
      background: linear-gradient(
          to right,
          var(--bs-success) 0,
          var(--bs-success) var(--range-progress, 0%),
          var(--bs-border-color-translucent) var(--range-progress, 0%),
          var(--bs-border-color-translucent) 100%
      );
    }

    &::-moz-range-track {
      height: 0.25rem;
      border: 0;
      border-radius: var(--bs-border-radius-pill);
      background: var(--bs-border-color-translucent);
    }

    &::-moz-range-progress {
      height: 0.25rem;
      border-radius: var(--bs-border-radius-pill);
      background: var(--bs-success);
    }

    &::-webkit-slider-thumb {
      -webkit-appearance: none;
      appearance: none;
      box-sizing: border-box;
      width: 0.75rem;
      height: 0.75rem;
      margin-top: -0.25rem;
      border: 0;
      border-radius: 50%;
      background: var(--bs-success);
    }

    &::-moz-range-thumb {
      box-sizing: border-box;
      width: 0.75rem;
      height: 0.75rem;
      border: 0;
      border-radius: 50%;
      background: var(--bs-success);
    }

    &:disabled {
      cursor: wait;
      opacity: 0.55;
    }
  }

  @media (hover: hover) and (pointer: fine) {
    input[type="range"] {
      &::-webkit-slider-thumb {
        opacity: 0;
        transition: opacity 0.15s ease;
      }

      &::-moz-range-thumb {
        opacity: 0;
        transition: opacity 0.15s ease;
      }

      &:hover::-webkit-slider-thumb,
      &:focus-visible::-webkit-slider-thumb,
      &:active::-webkit-slider-thumb {
        opacity: 1;
      }

      &:hover::-moz-range-thumb,
      &:focus-visible::-moz-range-thumb,
      &:active::-moz-range-thumb {
        opacity: 1;
      }
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
    z-index: 2;
    top: max(clamp(0.5rem, 1vw, 0.7rem), env(safe-area-inset-top));
    right: max(clamp(0.5rem, 1vw, 0.7rem), env(safe-area-inset-right));
    width: clamp(1rem, 1.2vw, 1.25rem);
    height: clamp(1rem, 1.2vw, 1.25rem);
    border: 1px solid var(--bs-danger);
    font-size: var(--podcast-font-caption);
    transition:
        background-color 0.15s ease,
        border-color 0.15s ease,
        color 0.15s ease;

    border-color: var(--bs-danger);
    color: var(--bs-danger);
    background-color: transparent;

    &:hover:not(:disabled) {
      color: var(--bs-white);
      background-color: var(--bs-danger);
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
      gap: 0.25rem;
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

  &--expanded {
    --player-artwork-size: clamp(9rem, min(60vw, 48dvh), 31rem);
    --player-control-size: clamp(2.25rem, 5.5vmin, 3rem);
    --player-play-size: clamp(3rem, 7vmin, 4rem);

    inset: 0;
    width: 100%;
    height: 100vh;
    height: 100dvh;
    overflow-x: hidden;
    overflow-y: auto;
    border-top: 0;
    overscroll-behavior: contain;
    isolation: isolate;
    animation: playlist-player-expand 360ms cubic-bezier(0.22, 1, 0.36, 1) both;
    will-change: transform, opacity;

    &.playlist-player--closing {
      animation: playlist-player-collapse 280ms cubic-bezier(0.4, 0, 1, 1) both;
      pointer-events: none;
    }

    .playlist-player__inner {
      display: grid;
      grid-template-areas:
        "track"
        "timeline"
        "transport"
        "tools";
      grid-template-columns: minmax(0, 1fr);
      width: min(100%, 48rem);
      min-height: 100%;
      margin-inline: auto;
      padding:
        max(1rem, env(safe-area-inset-top))
        max(clamp(1rem, 4vw, 3rem), env(safe-area-inset-right))
        max(1rem, env(safe-area-inset-bottom))
        max(clamp(1rem, 4vw, 3rem), env(safe-area-inset-left));
      align-items: center;
      align-content: center;
      justify-content: center;
      gap: clamp(0.65rem, 2dvh, 1.5rem);
    }

    .playlist-player__track,
    .playlist-player__timeline,
    .playlist-player__tools {
      width: 100%;
      justify-self: stretch;
    }

    .playlist-player__track {
      padding-right: 0;
      flex-direction: column;
      justify-content: center;
      gap: clamp(1.25rem, 3dvh, 2rem);
      text-align: center;
      cursor: default;
    }

    .playlist-player__artwork {
      box-shadow: 0 0.75rem 2.25rem var(--bs-border-color-translucent);
    }

    .playlist-player__copy {
      width: 100%;
    }

    .playlist-player__status {
      margin-bottom: 0.15rem !important;
    }

    .playlist-player__title {
      font-size: clamp(1rem, 2.8vmin, 1.5rem);
    }

    .playlist-player__context {
      display: block;
      margin-top: 0.2rem;
    }

    .playlist-player__transport {
      width: auto;
      justify-self: center;
      justify-content: center !important;
      gap: clamp(0.2rem, 1.4vw, 0.85rem);
    }

    .playlist-player__tools {
      justify-content: flex-end !important;
      gap: 0.25rem;

      > .playlist-player__button:not(.playlist-player__speed) {
        height: 2rem;
      }

      .playlist-player__volume .playlist-player__button {
        width: 2rem;
        height: 2rem;
        font-size: 0.85rem;
      }

      .playlist-player__speed .fa-gauge-high {
        font-size: 0.85rem;
      }
    }

    .playlist-player__volume {
      min-width: 0;
      flex: 1;
      gap: 0.35rem;

      input {
        width: auto;
        min-width: 0;
        flex: 1;
        height: 0.75rem;
      }
    }

    .playlist-player__timeline {
      grid-template-columns: max-content minmax(0, 1fr) max-content;
      gap: clamp(0.35rem, 1.5vw, 0.75rem);
    }

    .playlist-player__close {
      position: fixed;
      top: max(0.75rem, env(safe-area-inset-top));
      right: max(0.75rem, env(safe-area-inset-right));
      width: 2rem;
      height: 2rem;
      font-size: 0.85rem;
    }

    .playlist-player__collapse {
      position: fixed;
      z-index: 2;
      top: max(0.75rem, env(safe-area-inset-top));
      left: max(0.75rem, env(safe-area-inset-left));
      width: 2rem;
      height: 2rem;
      color: var(--bs-success-text-emphasis);
      background-color: var(--podcast-color-control-background);
      backdrop-filter: blur(0.4rem);
      font-size: 0.85rem;
      transition:
          background-color 0.15s ease,
          color 0.15s ease;
    }
  }

  @media (max-width: 575.98px) {
    &--expanded {
      --player-artwork-size: clamp(8.5rem, min(76vw, 43dvh), 20rem);
      --player-control-size: clamp(2rem, 10vw, 2.5rem);
      --player-play-size: clamp(3rem, 15vw, 3.75rem);

      .playlist-player__inner {
        grid-template-columns: minmax(0, 1fr);
        width: 100%;
        padding:
          max(2.75rem, env(safe-area-inset-top))
          max(0.75rem, env(safe-area-inset-right))
          max(0.75rem, env(safe-area-inset-bottom))
          max(0.75rem, env(safe-area-inset-left));
        gap: clamp(0.45rem, 1.5dvh, 0.75rem) 0.15rem;
      }

      .playlist-player__track {
        gap: clamp(1rem, 2.5dvh, 1.5rem);
      }

      .playlist-player__title {
        font-size: clamp(0.95rem, 4.5vw, 1.2rem);
      }

      .playlist-player__transport {
        min-width: 0;
        gap: clamp(0.05rem, 1vw, 0.35rem);
      }

      .playlist-player__tools {
        min-width: 0;
        gap: 0.2rem;
      }

      .playlist-player__speed {
        min-width: 2rem;
        padding-inline: 0.1rem;
        font-size: 0.75rem;
      }

      .playlist-player__volume {
        min-width: 0;
        flex: 1;
        gap: 0;

        input {
          display: block;
          width: auto;
        }
      }

      .playlist-player__timeline {
        gap: 0.25rem;
        font-size: 0.7rem;
      }

      .playlist-player__close {
        width: 2.25rem;
        height: 2.25rem;
      }

      .playlist-player__collapse {
        width: 2.25rem;
        height: 2.25rem;
      }
    }
  }

  @media (max-width: 360px) {
    &--expanded {
      --player-control-size: 1.9rem;
      --player-play-size: 2.8rem;

      .playlist-player__inner {
        padding-right: max(0.5rem, env(safe-area-inset-right));
        padding-left: max(0.5rem, env(safe-area-inset-left));
      }

      .playlist-player__status,
      .playlist-player__context {
        font-size: 0.68rem;
      }
    }
  }

  @media (max-height: 560px) and (orientation: landscape) {
    &--expanded {
      --player-artwork-size: clamp(6.5rem, 42dvh, 13rem);
      --player-control-size: 2rem;
      --player-play-size: 2.6rem;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;

    &--closing:not(.playlist-player--expanded),
    &--expanded,
    &--expanded.playlist-player--closing {
      animation: none;
    }

    &__artwork img,
    &__artwork-expand,
    &__artwork-expand i,
    &-background-enter-active,
    &-background-leave-active,
    &__collapse,
    &__close {
      transition: none;
    }

    input[type="range"]::-webkit-slider-thumb {
      transition: none;
    }

    input[type="range"]::-moz-range-thumb {
      transition: none;
    }
  }
}

@keyframes playlist-player-mini-enter {
  from {
    opacity: 0.72;
    transform: translateY(100%);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes playlist-player-mini-exit {
  from {
    opacity: 1;
    transform: translateY(0);
  }

  to {
    opacity: 0.72;
    transform: translateY(100%);
  }
}

@keyframes playlist-player-expand {
  from {
    opacity: 0.72;
    transform: translateY(100%);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes playlist-player-collapse {
  from {
    opacity: 1;
    transform: translateY(0);
  }

  to {
    opacity: 0.72;
    transform: translateY(100%);
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
