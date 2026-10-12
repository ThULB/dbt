<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import type {PodcastEpisode, PodcastPlaylist} from '../types/types.ts'
import { usePlayerStore } from '../stores/player.ts'

const playerStore = usePlayerStore()

const props = defineProps<{
  playlistMetadata: PodcastPlaylist
}>()

const episodes = props.playlistMetadata.episodes

const lastEpisode = ref<PodcastEpisode | null>(
    episodes.length > 0
        ? episodes[episodes.length - 1]
        : null
)

const imageError = ref(false)
const imageLoaded = ref(false)
const isDescriptionExpanded = ref(false)
const isDescriptionOverflowing = ref(false)

const descriptionElement = ref<HTMLElement | null>(null)


function handleImageLoad(): void {
  imageLoaded.value = true
  imageError.value = false
}

function handleImageError(): void {
  imageLoaded.value = false
  imageError.value = true
}

function toggleDescription(): void {
  isDescriptionExpanded.value = !isDescriptionExpanded.value
}

function checkDescriptionOverflow(): void {
  const element = descriptionElement.value

  if (!element) {
    return
  }

  isDescriptionOverflowing.value =
      element.scrollHeight > element.clientHeight
}

async function scrollToEpisode(episode: PodcastEpisode): Promise<void> {
  await nextTick()

  requestAnimationFrame(() => {
    document
        .getElementById(`episode-item-${episode.guid}`)
        ?.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
        })
  })
}

async function buildPlayer(episode: PodcastEpisode): Promise<void> {
  playerStore.setPlaylist(
      props.playlistMetadata.episodes,
      props.playlistMetadata.title
  )

  playerStore.selectEpisode(episode)
  await scrollToEpisode(episode)
}

onMounted(async () => {
  await nextTick()
  checkDescriptionOverflow()
})
</script>

<template>
  <header class="playlist-header mt-5 px-3 pt-5 pb-4 bg-light">
    <div class="ratio ratio-1x1 podcast-cover">
      <img
          v-if="playlistMetadata.image && !imageError"
          class="object-fit-cover rounded-2"
          :class="{ 'is-loaded': imageLoaded }"
          :alt="`Cover von ${playlistMetadata.title}`"
          :src="playlistMetadata.image"
          @load="handleImageLoad"
          @error="handleImageError"
      >

      <div
          v-if="!imageLoaded || imageError || !playlistMetadata.image"
          class="podcast-cover-placeholder"
          aria-hidden="true"
      >
        <i class="fa-solid fa-images"></i>
      </div>
    </div>

    <div class="playlist-info">
      <h1>
        {{ playlistMetadata.title }}
      </h1>

      <div class="topic d-flex gap-1 mb-3">
        <span class="badge text-bg-success text-wrap text-start">
          {{ playlistMetadata.category }}
        </span>

        <span
            v-if="playlistMetadata.subcategory"
            class="badge text-bg-success text-wrap text-start"
        >
          {{ playlistMetadata.subcategory }}
        </span>
      </div>

      <dl class="row mb-2 w-100">
        <dt class="col-3">Sprache:</dt>
        <dd class="col-9">
          {{ playlistMetadata.language.toUpperCase() }}
        </dd>

        <dt class="col-3">Besitzer:</dt>
        <dd class="col-9">
          {{ playlistMetadata.ownerName }}
        </dd>

        <dt class="col-3">E-Mail:</dt>
        <dd class="col-9">
          {{ playlistMetadata.ownerEmail }}
        </dd>
      </dl>

      <p class="playlist-authors small text-body-secondary mb-2">
        {{ playlistMetadata.author.join(', ') }}
      </p>

      <div
          ref="descriptionElement"
          class="playlist-description rich-description"
          :class="{ 'is-expanded': isDescriptionExpanded }"
          v-html="playlistMetadata.description"
      />

      <button
          v-if="isDescriptionOverflowing || isDescriptionExpanded"
          class="playlist-description-toggle"
          type="button"
          :aria-expanded="isDescriptionExpanded"
          @click="toggleDescription"
      >
        <span>
          {{ isDescriptionExpanded ? 'Weniger anzeigen' : 'Mehr anzeigen' }}
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
          class="btn btn-success mt-3 py-1 gap-1 d-flex justify-content-center align-items-center align-self-end"
          type="button"
          :disabled="!lastEpisode"
          @click="lastEpisode && buildPlayer(lastEpisode)"
      >
        <i
            class="fas fa-play m-0"
            aria-hidden="true"
        />
        Neueste Folge
      </button>
    </div>
  </header>
</template>

<style scoped>
.playlist-header {
  display: grid;
  grid-template-columns: clamp(12rem, 24vw, 17.5rem) minmax(0, 1fr);
  gap: clamp(1rem, 3vw, 2rem);
  padding: clamp(1.5rem, 4vw, 3rem);
}

.podcast-cover {
  position: relative;
  width: 100%;
  max-width: 17.5rem;
  align-self: start;
  justify-self: center;
  overflow: hidden;
  background: var(--bs-secondary-bg);
}

.podcast-cover > img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;

  opacity: 0;
  transition: opacity 0.2s ease;
}

.podcast-cover > img.is-loaded {
  opacity: 1;
}

.podcast-cover-placeholder {
  position: absolute;
  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: var(--bs-secondary-bg);
  color: var(--bs-secondary-color);
  font-size: 3rem;
}

.playlist-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.playlist-info > h1 {
  margin-bottom: clamp(0.75rem, 2vw, 1rem);
  font-size: clamp(1.75rem, 1.35rem + 1.4vw, 2.5rem);
  line-height: 1.15;
  overflow-wrap: anywhere;
}

.playlist-info > .btn {
  margin-top: auto !important;
}

.playlist-authors:empty {
  display: none;
}

.playlist-description {
  margin-bottom: 0.65rem;
  line-height: 1.5;
}

.playlist-description:not(.is-expanded) {
  display: -webkit-box;
  overflow: hidden;
  max-height: 7.5em;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 5;
}

.playlist-description-toggle {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: 0.3rem;
  margin-bottom: 0.75rem;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--bs-success);
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
}

.playlist-description-toggle:hover {
  color: var(--bs-success-text-emphasis);
}

.playlist-description-toggle:focus-visible {
  outline: 0.16rem solid rgba(var(--bs-success-rgb), 0.35);
  outline-offset: 0.18rem;
}

@media (max-width: 767.98px) {
  .playlist-header {
    grid-template-columns: minmax(0, 1fr);
    gap: clamp(1.25rem, 5vw, 2rem);
    padding: clamp(1rem, 5vw, 2rem) !important;
  }

  .podcast-cover {
    width: min(100%, 22rem);
    max-width: none;
  }

  .playlist-info {
    align-items: stretch;
  }

  .playlist-info > .topic {
    align-self: flex-start;
  }

  .playlist-info > .btn {
    width: 100%;
    align-self: stretch !important;
  }

}

@media (max-width: 575.98px) {
  .playlist-header {
    margin-top: 2rem !important;
  }

  .playlist-info > h1 {
    font-size: clamp(1.5rem, 7vw, 1.9rem);
  }
}
</style>