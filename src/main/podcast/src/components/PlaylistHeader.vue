<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { PodcastEpisode, PodcastPlaylist } from '../types/types.ts';
import { usePlayerStore } from '../stores/player.ts';
import podcastCategoryValue from '../utils/podcastCategory.ts';

const playerStore = usePlayerStore();
const { t, te } = useI18n({ useScope: 'global' });

const props = defineProps<{
  playlistMetadata: PodcastPlaylist;
}>();

const lastEpisode = computed<PodcastEpisode | null>(() => {
  const episodes = props.playlistMetadata.episodes;

  return episodes.length > 0 ? episodes[episodes.length - 1] : null;
});

const imageError = ref(false);
const imageLoaded = ref(false);
const isDescriptionExpanded = ref(false);
const isDescriptionOverflowing = ref(false);

const descriptionElement = ref<HTMLElement | null>(null);
let descriptionResizeObserver: ResizeObserver | null = null;

function categoryLabel(category: string): string {
  const value = podcastCategoryValue(category);
  const key = value && `podcast.categories.${value}`;

  return key && te(key) ? t(key) : category;
}

function handleImageLoad(): void {
  imageLoaded.value = true;
  imageError.value = false;
}

function handleImageError(): void {
  imageLoaded.value = false;
  imageError.value = true;
}

function toggleDescription(): void {
  isDescriptionExpanded.value = !isDescriptionExpanded.value;
}

function checkDescriptionOverflow(): void {
  const element = descriptionElement.value;

  if (!element) {
    return;
  }

  isDescriptionOverflowing.value = element.scrollHeight > element.clientHeight;
}

async function scrollToEpisode(episode: PodcastEpisode): Promise<void> {
  await nextTick();

  const episodeIndex = props.playlistMetadata.episodes.findIndex(
    (item) => item.guid === episode.guid,
  );

  if (episodeIndex < 0) {
    return;
  }

  requestAnimationFrame(() => {
    document.getElementById(`episode-item-${episodeIndex}`)?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  });
}

async function buildPlayer(episode: PodcastEpisode): Promise<void> {
  playerStore.setPlaylist(props.playlistMetadata.episodes, props.playlistMetadata.title);

  playerStore.selectEpisode(episode);
  await scrollToEpisode(episode);
}

onMounted(async () => {
  await nextTick();

  const element = descriptionElement.value;

  if (!element) {
    return;
  }

  descriptionResizeObserver = new ResizeObserver(checkDescriptionOverflow);
  descriptionResizeObserver.observe(element);

  checkDescriptionOverflow();
});

onUnmounted(() => {
  descriptionResizeObserver?.disconnect();
});
</script>

<template>
  <header class="playlist-header bg-light">
    <div class="playlist-header__cover ratio ratio-1x1 overflow-hidden bg-body-secondary">
      <img
          v-if="playlistMetadata.image && !imageError"
          class="position-absolute top-0 start-0 d-block w-100 h-100 object-fit-cover rounded-2"
          :class="{ 'is-loaded': imageLoaded }"
          :alt="t('podcast.image.coverAlt', { title: playlistMetadata.title })"
          :src="playlistMetadata.image"
          @load="handleImageLoad"
          @error="handleImageError"
      >

      <div
          v-if="!imageLoaded || imageError || !playlistMetadata.image"
          class="playlist-header__cover-placeholder position-absolute top-0 start-0 d-flex align-items-center justify-content-center w-100 h-100 bg-body-secondary text-body-secondary"
          aria-hidden="true"
      >
        <i class="fa-solid fa-images"></i>
      </div>
    </div>

    <div class="playlist-header__info d-flex flex-column align-items-stretch align-items-md-start">
      <h1>
        {{ playlistMetadata.title }}
      </h1>

      <div class="playlist-header__topics d-flex align-self-start gap-1">
        <span class="badge text-bg-success text-wrap text-start">
          {{ categoryLabel(playlistMetadata.category) }}
        </span>

        <span
            v-if="playlistMetadata.subcategory"
            class="badge text-bg-success text-wrap text-start"
        >
          {{ categoryLabel(playlistMetadata.subcategory) }}
        </span>
      </div>

      <dl class="playlist-header__details row w-100">
        <dt class="col-3">{{ t('podcast.details.language') }}:</dt>
        <dd class="col-9">
          {{ playlistMetadata.language.toUpperCase() }}
        </dd>

        <dt class="col-3">{{ t('podcast.details.owner') }}:</dt>
        <dd class="col-9">
          {{ playlistMetadata.ownerName }}
        </dd>

        <dt class="col-3">{{ t('podcast.details.email') }}:</dt>
        <dd class="col-9">
          {{ playlistMetadata.ownerEmail }}
        </dd>
      </dl>

      <p class="playlist-header__authors text-body-secondary">
        {{ playlistMetadata.author.join(', ') }}
      </p>

      <div
          ref="descriptionElement"
          class="playlist-header__description rich-description"
          :class="{ 'is-expanded': isDescriptionExpanded }"
          v-html="playlistMetadata.description"
      />

      <button
          v-if="isDescriptionOverflowing || isDescriptionExpanded"
          class="playlist-header__description-toggle d-inline-flex align-items-center align-self-start p-0 border-0 bg-transparent"
          type="button"
          :aria-expanded="isDescriptionExpanded"
          @click="toggleDescription"
      >
        <span>
          {{
            t(
                isDescriptionExpanded
                    ? 'podcast.description.showLess'
                    : 'podcast.description.showMore'
            )
          }}
        </span>

        <i
            class="fas"
            :class="
            isDescriptionExpanded
              ? 'fa-chevron-up'
              : 'fa-chevron-down'
          "
            aria-hidden="true"
        />
      </button>

      <button
          class="playlist-header__latest btn btn-success py-1 gap-1 d-flex justify-content-center align-items-center align-self-stretch align-self-md-end mt-auto"
          type="button"
          :disabled="!lastEpisode"
          @click="lastEpisode && buildPlayer(lastEpisode)"
      >
        <i
            class="fas fa-play m-0"
            aria-hidden="true"
        />
        {{ t('podcast.episode.latest') }}
      </button>
    </div>
  </header>
</template>

<style scoped lang="scss">
.playlist-header {
  display: grid;
  grid-template-columns: clamp(12rem, 24vw, 17.5rem) minmax(0, 1fr);
  gap: clamp(1rem, 3vw, 2rem);
  margin-top: 3rem;
  padding: clamp(1.25rem, 2.5vw, 2rem);
  border: 1px solid var(--bs-border-color-translucent);
  border-radius: var(--bs-border-radius-lg);

  &__cover {
    max-width: 17.5rem;
    align-self: start;
    justify-self: center;
    border-radius: var(--bs-border-radius);

    > img {
      opacity: 0;
      transition: opacity 0.2s ease;

      &.is-loaded {
        opacity: 1;
      }
    }
  }

  &__cover-placeholder {
    font-size: 3rem;
  }

  &__info {
    min-width: 0;

    > h1 {
      margin-bottom: clamp(0.75rem, 2vw, 1rem);
      font-size: clamp(1.75rem, 1.35rem + 1.4vw, 2.5rem);
      line-height: 1.15;
      overflow-wrap: anywhere;
    }
  }

  &__topics {
    margin-bottom: 1rem;
  }

  &__details {
    --bs-gutter-x: 0;

    margin-bottom: 0.5rem;

    dt,
    dd {
      overflow-wrap: anywhere;
    }
  }

  &__authors {
    margin-bottom: 0.5rem;
    font-size: 0.875rem;
    overflow-wrap: anywhere;

    &:empty {
      display: none;
    }
  }

  &__description {
    margin-bottom: 0.65rem;
    line-height: 1.5;
    overflow-wrap: anywhere;

    &:not(.is-expanded) {
      display: -webkit-box;
      overflow: hidden;
      max-height: 7.5em;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 5;
    }
  }

  &__description-toggle {
    gap: 0.3rem;
    margin-bottom: 0.75rem;
    color: var(--bs-success);
    cursor: pointer;
    font-size: 0.85rem;
    font-weight: 600;

    &:hover {
      color: var(--bs-success-text-emphasis);
    }

    &:focus-visible {
      outline: 0.16rem solid rgba(var(--bs-success-rgb), 0.35);
      outline-offset: 0.18rem;
    }
  }

  &__latest {
    min-height: 2.5rem;
  }

  @media (max-width: 767.98px) {
    grid-template-columns: minmax(0, 1fr);
    gap: clamp(1.25rem, 5vw, 2rem);
    padding: clamp(1rem, 5vw, 2rem);

    &__cover {
      width: min(100%, 22rem);
      max-width: none;
    }

    &__latest {
      width: 100%;
    }
  }

  @media (max-width: 575.98px) {
    gap: clamp(0.75rem, 3vw, 1rem);
    margin-top: 1.5rem;
    padding: clamp(0.75rem, 3.5vw, 1rem);

    &__cover {
      width: min(100%, 18rem);
    }

    &__info > h1 {
      margin-bottom: 0.5rem;
      font-size: clamp(1.25rem, 6vw, 1.5rem);
    }

    &__topics {
      margin-bottom: 0.5rem;

      .badge {
        padding: 0.3em 0.55em;
        font-size: 0.7rem;
      }
    }

    &__details {
      margin-bottom: 0.25rem;
      font-size: 0.85rem;
      line-height: 1.35;

      dt,
      dd {
        flex: 0 0 100%;
        width: 100%;
      }

      dt {
        margin-bottom: 0.125rem;
      }

      dd {
        margin-bottom: 0.5rem;
      }
    }

    &__authors {
      margin-bottom: 0.375rem;
      font-size: 0.75rem;
    }

    &__description {
      margin-bottom: 0.4rem;
      font-size: 0.875rem;
      line-height: 1.4;

      &:not(.is-expanded) {
        max-height: 5.6em;
        -webkit-line-clamp: 4;
      }
    }

    &__description-toggle {
      gap: 0.25rem;
      margin-bottom: 0.5rem;
      font-size: 0.75rem;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &__cover > img {
      transition: none;
    }
  }
}
</style>
