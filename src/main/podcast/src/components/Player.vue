<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePlayerStore } from '../stores/player'

const playerStore = usePlayerStore()
const { t } = useI18n({ useScope: 'global' })

const audioElement = ref<HTMLAudioElement | null>(null)
const playerElement = ref<HTMLElement | null>(null)

let playerResizeObserver: ResizeObserver | null = null

const isLoading = ref(false)
const audioError = ref(false)
const loadedEpisodeGuid = ref<string | null>(null)
const loadedEpisodeUrl = ref<string | null>(null)

const imageLoaded = ref(false)
const imageError = ref(false)

const speedIndex = ref(1)
const speeds = [0.75, 1, 1.25, 1.5, 2]

const volume = ref(1)
const previousVolume = ref(1)
const isMuted = ref(false)

watch(
    () => playerStore.episode,

    async episode => {
      if (!episode) {
        return
      }

      imageLoaded.value = false
      imageError.value = false

      audioError.value = false
      isLoading.value = true

      playerStore.currentTime = 0
      playerStore.duration = 0

      await nextTick()

      const audio = audioElement.value

      if (!audio || episode !== playerStore.episode) {
        return
      }

      loadedEpisodeGuid.value = episode.guid
      loadedEpisodeUrl.value = episode.audioUrl
      audio.src = episode.audioUrl
      audio.load()

      audio.playbackRate = speeds[speedIndex.value]
      audio.volume = volume.value
      audio.muted = isMuted.value

      const request = playerStore.playbackRequest

      if (request.command === 'play') {
        await playAudio(episode, request.id)
      }
    },

    {
      immediate: true,
    }
)

watch(
    () => playerStore.playbackRequest,
    request => {
      const audio = audioElement.value

      if (!audio) return

      if (request.command === 'play') {
        const episode = playerStore.episode

        if (
            episode &&
            loadedEpisodeGuid.value === episode.guid &&
            loadedEpisodeUrl.value === episode.audioUrl
        ) {
          void playAudio(episode, request.id)
        }
      } else {
        audio.pause()
      }
    },
    {
      flush: 'sync',
    }
)

function isPlaybackError(error: unknown, name: string): boolean {
  return (
      typeof error === 'object' &&
      error !== null &&
      'name' in error &&
      error.name === name
  )
}

async function playAudio(
    episode: NonNullable<typeof playerStore.episode>,
    requestId: number
): Promise<void> {
  const audio = audioElement.value

  if (!audio) return

  try {
    await audio.play()
  } catch (error) {
    if (
        episode !== playerStore.episode ||
        requestId !== playerStore.playbackRequest.id ||
        isPlaybackError(error, 'AbortError')
    ) {
      return
    }

    isLoading.value = false
    playerStore.requestPause()

    if (isPlaybackError(error, 'NotAllowedError')) {
      return
    }

    console.error('Error playing audio:', error)
    audioError.value = true
  }
}

function skipBackward15(): void {
  if (!audioElement.value) {
    return
  }

  audioElement.value.currentTime =
      Math.max(0, audioElement.value.currentTime - 15)
}

function skipForward15(): void {
  if (!audioElement.value) {
    return
  }

  audioElement.value.currentTime =
      Math.min(
          audioElement.value.duration || Infinity,
          audioElement.value.currentTime + 15
      )
}

function setSpeed(): void {
  if (!audioElement.value) {
    return
  }

  speedIndex.value++

  if (speedIndex.value >= speeds.length) {
    speedIndex.value = 0
  }

  audioElement.value.playbackRate =
      speeds[speedIndex.value]
}

function toggleMute(): void {
  if (!audioElement.value) {
    return
  }

  if (isMuted.value) {
    isMuted.value = false

    volume.value = previousVolume.value

    audioElement.value.muted = false
    audioElement.value.volume = volume.value
  } else {
    if (volume.value > 0) {
      previousVolume.value = volume.value
    }

    volume.value = 0
    isMuted.value = true

    audioElement.value.muted = true
  }
}

function setVolume(): void {
  if (!audioElement.value) {
    return
  }

  audioElement.value.volume = volume.value

  if (volume.value > 0) {
    previousVolume.value = volume.value

    isMuted.value = false
    audioElement.value.muted = false
  } else {
    isMuted.value = true
    audioElement.value.muted = true
  }
}

function seekAudio(event: Event): void {
  if (!audioElement.value) {
    return
  }

  const input = event.target as HTMLInputElement

  audioElement.value.currentTime =
      Number(input.value)
}

function handleTimeUpdate(): void {
  if (!audioElement.value) {
    return
  }

  playerStore. currentTime = audioElement.value.currentTime
}

function handleLoadedMetadata(): void {
  if (!audioElement.value) {
    return
  }

  playerStore.duration =
      audioElement.value.duration
}

function handleEnded(): void {
  playerStore.isPlaying = false
  playerStore.requestPause()
}

onMounted(() => {
  const player = playerElement.value

  if (!player) return

  const updatePlayerHeight = (): void => {
    document.body.style.setProperty(
        '--podcast-player-height',
        `${player.offsetHeight}px`
    )
  }

  document.body.classList.add('has-podcast-player')
  updatePlayerHeight()

  playerResizeObserver = new ResizeObserver(updatePlayerHeight)
  playerResizeObserver.observe(player)
})

onUnmounted(() => {
  playerResizeObserver?.disconnect()
  document.body.classList.remove('has-podcast-player')
  document.body.style.removeProperty('--podcast-player-height')
})
</script>

<template>
  <section
      ref="playerElement"
      class="playlist-player"
      :aria-label="t('podcast.player.ariaLabel')"
  >
    <audio
        ref="audioElement"
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

    <div class="container playlist-player-inner">

      <!-- Episode -->
      <div class="playlist-player-track">

        <div class="playlist-player-artwork">

          <img
              v-if="
                playerStore.episode?.image &&
                !imageError
              "
              :src="playerStore.episode.image"
              :alt="t('podcast.image.coverAlt', { title: playerStore.episode.title })"
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
              class="playlist-player-artwork-placeholder"
              aria-hidden="true"
          >
            <i class="fas fa-headphones"></i>
          </span>

        </div>

        <div class="playlist-player-copy">

          <p
              class="playlist-player-status"
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

          <p class="playlist-player-title">
            {{ playerStore.episode?.title }}
          </p>

          <p class="playlist-player-context">
            {{ playerStore.playlistTitle }}
          </p>

        </div>

      </div>

      <!-- Controls -->
      <div
          class="playlist-player-transport"
          role="group"
          :aria-label="t('podcast.player.controls')"
      >

        <!-- Previous episode -->
        <button
            type="button"
            :aria-label="t('podcast.player.previousEpisode')"
            :title="t('podcast.player.previousEpisode')"
            :disabled="
              playerStore.currentEpisodeIndex <= 0
            "
            @click="playerStore.previousEpisode"
        >
          <i
              class="fas fa-backward-step"
              aria-hidden="true"
          ></i>
        </button>

        <!-- Back 15 sec -->
        <button
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
            class="playlist-player-play"
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
              class="fas"
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
            type="button"
            :aria-label="t('podcast.player.nextEpisode')"
            :title="t('podcast.player.nextEpisode')"
            :disabled="
              playerStore.currentEpisodeIndex >=
              playerStore.episodes.length - 1
            "
            @click="playerStore.nextEpisode"
        >
          <i
              class="fas fa-forward-step"
              aria-hidden="true"
          ></i>
        </button>

      </div>

      <!-- Tools -->
      <div class="playlist-player-tools">

        <!-- Speed -->
        <button
            class="playlist-player-speed"
            type="button"
            :aria-label="t('podcast.player.changeSpeed')"
            :title="`${speeds[speedIndex]}×`"
            @click="setSpeed"
        >
          {{ speeds[speedIndex] }}&times;
        </button>

        <!-- Volume -->
        <div class="playlist-player-volume">

          <button
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
      <div class="playlist-player-timeline">

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
          class="close-button"
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

<style scoped>
/* =========================
     Player
   ========================= */

.playlist-player {
  position: fixed;
  z-index: 1040;
  right: 0;
  bottom: 0;
  left: 0;

  width: 100%;

  background: #f0f3f1;
  color: var(--bs-body-color);
}

:global(body.has-podcast-player) {
  padding-bottom: var(--podcast-player-height, 0);
}

.playlist-player[hidden],
.playlist-player [hidden] {
  display: none !important;
}

.playlist-player audio {
  display: none;
}


/* =========================
   Layout
   ========================= */

.playlist-player-inner {
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


/* =========================
   Track information
   ========================= */

.playlist-player-track {
  display: flex;
  grid-area: track;

  min-width: 0;

  align-items: center;
  gap: 0.65rem;
}

.playlist-player-artwork {
  position: relative;

  flex: 0 0 3.25rem;
  overflow: hidden;

  width: 3.25rem;
  height: 3.25rem;

  border-radius: 0.25rem;
  background: #dce7e1;
}

.playlist-player-artwork img,
.playlist-player-artwork > span {
  position: absolute;
  inset: 0;

  width: 100%;
  height: 100%;
}

.playlist-player-artwork img {
  display: block;

  object-fit: cover;
  opacity: 0;

  transition: opacity 0.2s ease;
}

.playlist-player-artwork img.is-loaded {
  opacity: 1;
}

.playlist-player-artwork > span {
  display: grid;
  place-items: center;

  color: var(--bs-success);
  font-size: 1.35rem;
}

.playlist-player-copy {
  min-width: 0;
}

.playlist-player-status,
.playlist-player-title,
.playlist-player-context {
  overflow: hidden;

  margin: 0;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-player-status {
  color: var(--bs-secondary-color);

  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.playlist-player-status[data-tone="playing"] {
  color: var(--bs-success);
}

.playlist-player-status[data-tone="loading"] {
  color: #9a6819;
}

.playlist-player-status[data-tone="error"] {
  color: var(--bs-danger);
}

.playlist-player-title {
  font-size: 0.95rem;
  font-weight: 700;
}

.playlist-player-context {
  color: var(--bs-secondary-color);
  font-size: 0.75rem;
}


/* =========================
   Buttons
   ========================= */

.playlist-player button {
  display: inline-grid;
  place-items: center;

  flex: 0 0 auto;

  width: 2.25rem;
  height: 2.25rem;
  padding: 0;

  border: 0;
  border-radius: 50%;

  background: transparent;
  color: #34483e;

  cursor: pointer;
  line-height: 1;
}

.playlist-player button:hover:not(:disabled) {
  background: #dce7e1;
  color: var(--bs-success-text-emphasis);
}

.playlist-player button:disabled {
  cursor: not-allowed;
  opacity: 0.35;
}

.playlist-player button:focus-visible,
.playlist-player input:focus-visible {
  outline: 0.16rem solid rgba(var(--bs-success-rgb), 0.35);
  outline-offset: 0.14rem;
}


/* =========================
   Transport
   ========================= */

.playlist-player-transport {
  display: flex;
  grid-area: transport;

  align-items: center;
  justify-content: center;
  gap: clamp(0.15rem, 0.8vw, 0.55rem);
}

.playlist-player .playlist-player-play {
  width: 3rem;
  height: 3rem;

  background: var(--bs-success);
  color: #fff;

  font-size: 1.65rem;
}

.playlist-player .playlist-player-play:hover:not(:disabled) {
  background: var(--bs-success-text-emphasis);
  color: #fff;
}

.playlist-player .playlist-player-play > .fas {
  font-size: 1.65rem;
}

.fa-play {
  position: relative;
  left: 3%;
}


/* =========================
   Tools
   ========================= */

.playlist-player-tools {
  display: flex;
  grid-area: tools;

  min-width: 0;

  align-items: center;
  justify-content: flex-end;
  gap: clamp(0.25rem, 1vw, 0.65rem);
}

.playlist-player .playlist-player-speed {
  width: auto;
  min-width: 2.6rem;

  padding-inline: 0.35rem;

  border-radius: 0.25rem;

  color: var(--bs-success-text-emphasis);

  font-size: 0.78rem;
  font-weight: 700;
}


/* =========================
   Volume
   ========================= */

.playlist-player-volume {
  display: flex;

  min-width: 0;

  align-items: center;
  gap: 0.2rem;
}

.playlist-player-volume input {
  width: clamp(2.25rem, 11vw, 8rem);
}


/* =========================
   Range inputs
   ========================= */

.playlist-player input[type="range"] {
  min-width: 5rem;
  height: 1rem;

  margin: 0;
  padding: 0;

  accent-color: var(--bs-success);

  cursor: pointer;
}

.playlist-player input[type="range"]:disabled {
  cursor: wait;
  opacity: 0.55;
}


/* =========================
   Timeline
   ========================= */

.playlist-player-timeline {
  display: grid;
  grid-area: timeline;
  grid-template-columns:
    3.25rem
    minmax(0, 1fr)
    3.25rem;

  align-items: center;
  gap: 0.5rem;

  color: var(--bs-secondary-color);

  font-size: 0.72rem;
  font-variant-numeric: tabular-nums;
}

.playlist-player-timeline input {
  width: 100%;
}

.playlist-player-timeline > span:last-child {
  text-align: right;
}


/* =========================
   Close button
   ========================= */

.playlist-player .close-button {
  position: absolute;
  top: clamp(0.5rem, 1vw, 0.7rem);
  right: clamp(0.5rem, 1vw, 0.7rem);

  display: flex;
  align-items: center;
  justify-content: center;

  width: clamp(1.1rem, 2.2vw, 1.5rem);
  height: clamp(1.1rem, 2.2vw, 1.5rem);
  padding: 0;

  border: 0;
  border-radius: 50%;

  color: var(--bs-danger);
  background-color: rgba(var(--bs-danger-rgb), 0.1);

  font-size: clamp(0.55rem, 1.3vw, 0.75rem);
  line-height: 1;

  cursor: pointer;

  transition:
      background-color 0.15s ease,
      color 0.15s ease,
      transform 0.15s ease;
}

.playlist-player .close-button:hover {
  color: var(--bs-white);
  background-color: var(--bs-danger);
  transform: scale(1.08);
}

.playlist-player .close-button:active {
  transform: scale(0.92);
}


/* =========================
   Tablet
   ========================= */

@media (max-width: 767.98px) {
  .playlist-player-inner {
    grid-template-areas:
      "track tools"
      "transport transport"
      "timeline timeline";
    grid-template-columns: minmax(0, 1fr) auto;

    gap: 0.3rem 0.5rem;

    padding-block: 0.55rem;
  }
}


/* =========================
   Mobile
   ========================= */

@media (max-width: 575.98px) {
  .playlist-player-inner {
    grid-template-areas:
      "track track"
      "transport tools"
      "timeline timeline";
    grid-template-columns: minmax(0, 1fr) auto;

    gap: 0.25rem 0.2rem;

    padding-block: 0.45rem;
  }

  .playlist-player-artwork {
    flex-basis: 2.75rem;

    width: 2.75rem;
    height: 2.75rem;
  }

  .playlist-player-context {
    display: none;
  }

  .playlist-player-title {
    font-size: 0.84rem;
  }

  .playlist-player-transport {
    justify-content: flex-start;
    gap: 0.1rem;
  }

  .playlist-player-transport button,
  .playlist-player-volume button {
    width: 1.85rem;
    height: 1.85rem;
  }

  .playlist-player .playlist-player-play {
    width: 2.6rem;
    height: 2.6rem;
  }

  .playlist-player-tools {
    gap: 0;
  }

  .playlist-player .playlist-player-speed {
    min-width: 2.25rem;
    height: 1.85rem;

    padding-inline: 0.15rem;
  }

  .playlist-player-volume {
    gap: 0;
  }

  .playlist-player-volume input {
    width: clamp(2.25rem, 12vw, 3.75rem);
  }

  .playlist-player-timeline {
    grid-template-columns:
      max-content
      minmax(0, 1fr)
      max-content;

    gap: 0.2rem;

    font-size: 0.66rem;
  }

  .playlist-player input[type="range"] {
    height: 0.8rem;
  }
}
</style>
