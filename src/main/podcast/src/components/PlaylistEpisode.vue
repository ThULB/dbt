<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { PodcastEpisode, PodcastPlaylist } from '../types/types.ts'
import { usePlayerStore } from '../stores/player.ts'

const playerStore = usePlayerStore()
const { locale, t } = useI18n({ useScope: 'global' })

const props = defineProps<{
  playlistMetadata: PodcastPlaylist
}>()

const openEpisodeGuid = ref<string | null>(null)

function isCurrentEpisode(episode: PodcastEpisode): boolean {
  return playerStore.episode?.guid === episode.guid
}

function buildPlayer(episode: PodcastEpisode): void {
  playerStore.setPlaylist(
      props.playlistMetadata.episodes,
      props.playlistMetadata.title
  )

  playerStore.toggleEpisode(episode)
}

function formatDate(value: string): string {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return new Intl.DateTimeFormat(locale.value, {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  }).format(date)
}

function toggleEpisodeDetails(guid: string): void {
  openEpisodeGuid.value =
      openEpisodeGuid.value === guid
          ? null
          : guid
}

watch(
    () => playerStore.episode?.guid,
    guid => {
      if (!guid) return

      openEpisodeGuid.value = guid
    },
    {
      immediate: true,
    }
)
</script>

<template>
  <div class="episode">
    <h2 class="visually-hidden">{{ t('podcast.episode.heading') }}</h2>

    <p
        v-if="!playlistMetadata.episodes.length"
        id="episode-empty"
        class="alert alert-light border mb-0"
    >
      {{ t('podcast.episode.empty') }}
    </p>

    <div
        id="episode-list"
        class="accordion accordion-flush rounded-0"
    >
      <div
          v-for="(episode, index) in playlistMetadata.episodes"
          :key="episode.guid"
          :id="`episode-item-${index}`"
          class="accordion-item"
          :class="{ 'is-current': isCurrentEpisode(episode) }"
      >
        <h3 class="accordion-header">
          <button
              class="accordion-button h5"
              :class="{ collapsed: openEpisodeGuid !== episode.guid }"
              type="button"
              :aria-expanded="openEpisodeGuid === episode.guid"
              :aria-controls="`episode-${index}`"
              @click="toggleEpisodeDetails(episode.guid)"
          >
            {{ episode.title }}
          </button>
        </h3>

        <div
            :id="`episode-${index}`"
            class="accordion-collapse collapse episode-collapse"
            :class="{ show: openEpisodeGuid === episode.guid }"
        >
          <div class="episode-collapse-inner">
            <div class="accordion-body">
              <p class="small text-body-secondary">
                <time>{{ formatDate(episode.pubDate) }}</time>
              </p>

              <div
                  v-html="episode.description"
                  class="description rich-description"
              ></div>

              <button
                  class="episode-play-button btn btn-sm rounded-pill d-inline-flex align-items-center gap-1"
                  type="button"
                  :aria-pressed="isCurrentEpisode(episode) && playerStore.isPlaying"
                  :class="isCurrentEpisode(episode)
                  ? 'btn-success'
                  : 'btn-outline-success'"
                  @click="buildPlayer(episode)"
              >
                <i
                    class="fas m-0"
                    :class="isCurrentEpisode(episode) && playerStore.isPlaying
                  ? 'fa-pause'
                  : 'fa-play'"
                    aria-hidden="true"
                ></i>

                <span>
                  {{
                    isCurrentEpisode(episode)
                        ? playerStore.isPlaying
                            ? t('podcast.player.pause')
                            : t('podcast.player.resume')
                        : t('podcast.player.play')
                  }}
                </span>

                <span
                    class="episode-play-separator"
                    aria-hidden="true"
                >
                  &middot;
                </span>

                <span>
                  {{
                    isCurrentEpisode(episode)
                        ? playerStore.formatTime(playerStore.remainingTime)
                        : episode.duration
                  }}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.episode {
  margin-top: clamp(1.5rem, 4vw, 3rem);
}

.episode .accordion-item {
  position: relative;
  padding-bottom: 0.1rem;
  scroll-margin-top: 6rem;
  transition:
      color 160ms ease,
      background-color 160ms ease;
}

.episode .accordion-item::before {
  position: absolute;
  z-index: 2;
  top: 0;
  bottom: 0;
  left: 0;
  width: 0.25rem;
  background: var(--bs-success);
  content: "";
  opacity: 0;
  transform: scaleY(0.55);
  transform-origin: center;
  transition:
      opacity 160ms ease,
      transform 160ms ease;
}

.episode .accordion-item.is-current {
  background: var(--bs-success-bg-subtle);
}

.episode .accordion-item.is-current::before {
  opacity: 1;
  transform: scaleY(1);
}

.episode .accordion-button:not(.collapsed) {
  color: var(--bs-success-text-emphasis);
  background-color: var(--bs-success-bg-subtle);
}

.episode .accordion-item.is-current .accordion-button {
  color: var(--bs-success-text-emphasis);
}

.episode .accordion-button:focus {
  border-color: transparent;
  box-shadow: none;
}

.episode .accordion-collapse.episode-collapse {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 350ms ease;
}

.episode .accordion-collapse.episode-collapse.show {
  grid-template-rows: 1fr;
}

.episode-collapse-inner {
  min-height: 0;
  overflow: hidden;
}

.episode-play-button {
  min-height: 2rem;
  padding: 0.2rem 0.75rem;
  font-variant-numeric: tabular-nums;
}

.episode-play-button .fas {
  font-size: 1.1rem;
  line-height: 1;
}

.episode-play-separator {
  opacity: 0.6;
}
</style>
