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
  <div class="episode-list">
    <h2 id="episode-heading" class="episode-list__heading">
      {{ t('podcast.episode.heading') }}
    </h2>

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
        aria-labelledby="episode-heading"
    >
      <div
          v-for="(episode, index) in playlistMetadata.episodes"
          :key="episode.guid"
          :id="`episode-item-${index}`"
          class="episode-list__item accordion-item position-relative"
          :class="{ 'is-current': isCurrentEpisode(episode) }"
      >
        <div class="episode-list__item-header accordion-header position-relative">
          <h3 class="episode-list__item-heading m-0">
            <button
                class="episode-list__toggle accordion-button h5 mb-0"
                :class="{ collapsed: openEpisodeGuid !== episode.guid }"
                type="button"
                :aria-expanded="openEpisodeGuid === episode.guid"
                :aria-controls="`episode-${index}`"
                @click="toggleEpisodeDetails(episode.guid)"
            >
              <span class="episode-list__title">{{ episode.title }}</span>
            </button>
          </h3>

          <button
              class="episode-list__play btn btn-sm rounded-pill d-inline-flex align-items-center gap-1"
              type="button"
              :aria-pressed="isCurrentEpisode(episode) && playerStore.isPlaying"
              :aria-label="isCurrentEpisode(episode)
              ? playerStore.isPlaying
                  ? t('podcast.player.pause')
                  : t('podcast.player.resume')
              : t('podcast.player.playEpisode')"
              :class="isCurrentEpisode(episode)
              ? 'btn-success'
              : 'btn-outline-success'"
              @click.stop="buildPlayer(episode)"
          >
            <i
                class="fas m-0"
                :class="isCurrentEpisode(episode) && playerStore.isPlaying
                ? 'fa-pause'
                : 'fa-play'"
                aria-hidden="true"
            ></i>

            <span
                class="episode-list__play-separator"
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

        <div
            :id="`episode-${index}`"
            class="episode-list__collapse accordion-collapse collapse"
            :class="{ show: openEpisodeGuid === episode.guid }"
        >
          <div class="episode-list__collapse-inner overflow-hidden">
            <div class="accordion-body">
              <p class="small text-body-secondary">
                <time>{{ formatDate(episode.pubDate) }}</time>
              </p>

              <div
                  v-html="episode.description"
                  class="description rich-description"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.episode-list {
  --episode-action-gap: 1rem;
  --episode-action-space: 7rem;

  margin-top: clamp(1.5rem, 4vw, 3rem);

  &__heading {
    margin-bottom: 1rem;
  }

  &__item {
    scroll-margin-top: 6rem;
    transition: color 160ms ease;

    &::before {
      position: absolute;
      z-index: 4;
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

    &.is-current {
      &::before {
        opacity: 1;
        transform: scaleY(1);
      }

      .episode-list__toggle {
        color: var(--bs-success-text-emphasis);
      }
    }
  }

  &__toggle {
    min-height: 3.75rem;

    &:hover {
      background-color: transparent;
    }

    &:not(.collapsed) {
      color: var(--bs-success-text-emphasis);
      background-color: transparent;
      box-shadow: none;
    }

    &:focus {
      border-color: transparent;
      box-shadow: none;
    }

    &:focus-visible {
      outline: 2px solid var(--bs-success);
      outline-offset: -3px;
    }
  }

  &__title {
    flex: 1 1 auto;
    min-width: 0;
    padding-right: var(--episode-action-space);
    overflow-wrap: anywhere;
  }

  &__collapse {
    display: grid;
    grid-template-rows: 0fr;
    transition: grid-template-rows 350ms ease;

    &.show {
      grid-template-rows: 1fr;
    }
  }

  &__collapse-inner {
    min-height: 0;

    .accordion-body {
      overflow-wrap: anywhere;
    }
  }

  &__play {
    position: absolute;
    z-index: 5;
    top: 50%;
    right: calc(
        var(--bs-accordion-btn-padding-x) +
        var(--bs-accordion-btn-icon-width) +
        var(--episode-action-gap)
    );
    min-height: 2rem;
    padding: 0.2rem 0.75rem;
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
    transform: translateY(-50%);

    &:focus-visible {
      outline: 2px solid var(--bs-success);
      outline-offset: 2px;
    }

    .fas {
      font-size: 1.1rem;
      line-height: 1;
    }
  }

  &__play-separator {
    opacity: 0.6;
  }

  @media (max-width: 575.98px) {
    --episode-action-gap: clamp(0.5rem, 2.5vw, 0.625rem);
    --episode-action-space: clamp(5rem, 24vw, 5.5rem);

    &__heading {
      margin-bottom: 0.75rem;
      font-size: 1.375rem;
    }

    &__toggle {
      min-height: clamp(2.875rem, 13vw, 3.25rem);
      padding-block: clamp(0.45rem, 2vw, 0.625rem);
      padding-left: 0.625rem;
      font-size: clamp(0.8rem, 3.8vw, 0.9rem);
      line-height: 1.25;
    }

    &__play {
      min-height: clamp(1.5rem, 7vw, 1.875rem);
      padding-inline: clamp(0.25rem, 1.5vw, 0.375rem);
      font-size: clamp(0.65rem, 2.5vw, 0.7rem);

      .fas {
        font-size: clamp(0.8rem, 3vw, 0.9rem);
      }
    }

    &__collapse-inner .accordion-body {
      padding: 0.75rem 1rem;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &__item,
    &__item::before,
    &__collapse {
      transition: none;
    }
  }
}
</style>
