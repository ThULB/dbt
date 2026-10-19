<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import { useRoute } from 'vue-router'

import requestSolr from '../utils/requestSolr.ts'
import applicationUrl from '../utils/applicationUrl.ts'
import type { SolrDocument, SolrResponse } from '../types/types.ts'
import LoadingState from "./LoadingState.vue";
import ErrorState from "./ErrorState.vue";
import EmptyState from "./EmptyState.vue";

const route = useRoute()

const podcastMetadata = ref<SolrResponse | null>(null)

const totalItems = ref(0)
const itemsPerPage = ref(20)
const currentPage = ref(1)

const isLoading = ref(false)
const isError = ref(false)
const error = ref<string | null>(null)

const coverErrors = ref<Record<string, boolean>>({})
const coverLoaded = ref<Record<string, boolean>>({})

const totalPages = computed(() =>
    Math.ceil(totalItems.value / itemsPerPage.value)
)

const visiblePages = computed<(number | string)[]>(() => {
  const pages: (number | string)[] = []

  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let page = 1; page <= total; page++) {
      pages.push(page)
    }

    return pages
  }

  pages.push(1)

  if (current > 4) {
    pages.push('...')
  }

  const start = Math.max(2, current - 2)
  const end = Math.min(total - 1, current + 2)

  for (let page = start; page <= end; page++) {
    pages.push(page)
  }

  if (current < total - 3) {
    pages.push('...')
  }

  pages.push(total)

  return pages
})

function createParams(): URLSearchParams {
  const params = new URLSearchParams()

  params.append('fq', 'mods.genre:podcast')
  params.set('wt', 'json')

  params.set('rows', `${itemsPerPage.value}`)

  params.set(
      'start',
      `${(currentPage.value - 1) * itemsPerPage.value}`
  )

  for (const [key, value] of Object.entries(route.query)) {
    if (typeof value === 'string') {
      params.append(key, value)
    }
  }

  return params
}

function getItunesCategories(podcast: SolrDocument): string[] {
  const itunesCategories =
      podcast['category.top']
          ?.filter(category =>
              category.startsWith('itunes-podcast:')
          )
          .map(category =>
              category
                  .replace('itunes-podcast:', '')
                  .replaceAll('_', ' ')
          ) ?? []

  if (itunesCategories.length === 0) {
    return ['Ohne Kategorie']
  }

  return itunesCategories
}

function getPodcastTitle(podcast: SolrDocument): string {
  return podcast['mods.title']?.[0] ?? 'Ohne Titel'
}

function getPodcastAuthors(podcast: SolrDocument): string {
  return podcast['mods.author']?.join(', ') ?? ''
}

function getCoverUrl(podcast: SolrDocument): string {
  return applicationUrl(
      `api/iiif/image/v2/thumbnail/${encodeURIComponent(podcast.id)}/full/!300,300/0/default.jpg`
  )
}

function handleCoverLoad(podcastId: string): void {
  coverLoaded.value[podcastId] = true
  coverErrors.value[podcastId] = false
}

function handleCoverError(podcastId: string): void {
  coverLoaded.value[podcastId] = false
  coverErrors.value[podcastId] = true
}

function scrollToTop(): void {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

async function searchPodcasts(): Promise<void> {
  isLoading.value = true
  isError.value = false
  error.value = null

  const params = createParams()

  try {
    const response = await requestSolr(params)

    podcastMetadata.value = JSON.parse(response) as SolrResponse

    totalItems.value =
        podcastMetadata.value.response.numFound

    coverErrors.value = {}
    coverLoaded.value = {}

    /*
     * Falls sich durch Filter die Anzahl der Seiten verkleinert
     * und currentPage nicht mehr existiert.
     */
    if (
        totalPages.value > 0 &&
        currentPage.value > totalPages.value
    ) {
      currentPage.value = totalPages.value

      await searchPodcasts()
    }
  } catch (err) {
    podcastMetadata.value = null

    error.value =
        err instanceof Error
            ? err.message
            : 'Unknown error'

    isError.value = true
  } finally {
    isLoading.value = false
  }
}

async function setPage(page: number): Promise<void> {
  if (
      page < 1 ||
      page > totalPages.value ||
      page === currentPage.value
  ) {
    return
  }

  currentPage.value = page

  await searchPodcasts()

  scrollToTop()
}


/*
 * Bei Änderung der Filter / Query-Parameter
 * wieder auf Seite 1 springen.
 */
watch(
    () => route.query,
    () => {
      currentPage.value = 1
      searchPodcasts()
    },
    {
      immediate: true,
      deep: true
    }
)
</script>

<template>
  <section class="podcast-list">

    <LoadingState v-if="isLoading">
      Podcasts werden geladen ...
    </LoadingState>

    <ErrorState v-else-if="error">{{error}}</ErrorState>

    <EmptyState
        v-else-if="podcastMetadata?.response.numFound === 0"
        description="Keine passenden Podcasts gefunden. Ändern Sie den Suchbegriff oder wählen Sie eine andere Kategorie."
    />

    <!-- Results -->
    <div
        v-else-if="podcastMetadata"
        class="wrapper"
    >
      <h2 class="mb-4">
        {{ totalItems }}
        {{
          totalItems === 1
              ? 'Podcast gefunden'
              : 'Podcasts gefunden'
        }}
      </h2>

      <div class="cards-collection">

        <article
            v-for="podcast in podcastMetadata.response.docs"
            :key="podcast.id"
            class="card-item h-100 border-0 shadow-sm rounded-2 overflow-hidden position-relative"
        >

          <!-- Cover -->
          <div class="podcast-card-cover ratio ratio-1x1 bg-light border-bottom">

            <img
                v-if="!coverErrors[podcast.id]"
                class="card-img-top w-100 h-100 object-fit-cover"
                :class="{ 'is-loaded': coverLoaded[podcast.id] }"
                :src="getCoverUrl(podcast)"
                :alt="`Cover von ${getPodcastTitle(podcast)}`"
                loading="lazy"
                decoding="async"
                @load="handleCoverLoad(podcast.id)"
                @error="handleCoverError(podcast.id)"
            >

            <div
                v-if="!coverLoaded[podcast.id] || coverErrors[podcast.id]"
                class="podcast-card-cover-placeholder"
                aria-hidden="true"
            >
              <i class="fa-solid fa-images"></i>
            </div>

          </div>

          <!-- Categories -->
          <div
              v-if="getItunesCategories(podcast).length"
              class="topic mt-1 px-2"
          >
            <span
                v-for="category in getItunesCategories(podcast)"
                :key="category"
                class="badge rounded-2 text-bg-success text-wrap text-start me-1"
            >
              {{ category }}
            </span>
          </div>

          <!-- Title -->
          <h3 class="h6 card-title px-2 mb-2">
            <RouterLink
                class="podcast-card-link stretched-link"
                :to="podcast.id"
            >
              {{ getPodcastTitle(podcast) }}
            </RouterLink>
          </h3>

          <!-- Authors -->
          <p
              v-if="getPodcastAuthors(podcast)"
              class="small px-2 text-truncate position-relative z-2 mb-2"
          >
            {{ getPodcastAuthors(podcast) }}
          </p>

        </article>

      </div>

      <!-- Pagination -->
      <nav
          v-if="totalPages > 1"
          class="mt-5"
          aria-label="Podcast-Seitennavigation"
      >
        <ul class="pagination justify-content-center">

          <!-- Previous -->
          <li
              class="page-item"
              :class="{ disabled: currentPage === 1 }"
          >
            <button
                class="page-link text-success"
                type="button"
                :disabled="currentPage === 1"
                aria-label="Vorherige Seite"
                @click="setPage(currentPage - 1)"
            >
              Zurück
            </button>
          </li>

          <!-- Page numbers -->
          <li
              v-for="(page, index) in visiblePages"
              :key="`${page}-${index}`"
              class="page-item"
              :class="{
              active: page === currentPage,
              disabled: page === '...'
            }"
          >

            <!-- ... -->
            <span
                v-if="page === '...'"
                class="page-link text-body-secondary"
            >
              …
            </span>

            <!-- Page -->
            <button
                v-else
                class="page-link"
                :class="
                page === currentPage
                  ? 'text-white'
                  : 'text-success'
              "
                type="button"
                :aria-current="
                page === currentPage
                  ? 'page'
                  : undefined
              "
                @click="setPage(page as number)"
            >
              {{ page }}
            </button>

          </li>

          <!-- Next -->
          <li
              class="page-item"
              :class="{
              disabled: currentPage === totalPages
            }"
          >
            <button
                class="page-link text-success"
                type="button"
                :disabled="currentPage === totalPages"
                aria-label="Nächste Seite"
                @click="setPage(currentPage + 1)"
            >
              Weiter
            </button>
          </li>

        </ul>
      </nav>

    </div>

  </section>
</template>

<style lang="scss" scoped>
.cards-collection {
  display: grid;

  grid-template-columns:
    repeat(
      auto-fill,
      minmax(min(100%, 250px), 1fr)
    );

  gap: 25px;
}

.podcast-card-cover {
  position: relative;
  overflow: hidden;
  background: var(--bs-secondary-bg);
}

.podcast-card-cover > img {
  position: absolute;
  inset: 0;

  width: 100%;
  height: 100%;

  object-fit: cover;

  opacity: 0;
  transition: opacity 0.2s ease;

  z-index: 2;
}

.podcast-card-cover > img.is-loaded {
  opacity: 1;
}

.podcast-card-cover-placeholder {
  position: absolute;
  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: var(--bs-secondary-bg);
  color: var(--bs-secondary-color);

  font-size: 3rem;

  z-index: 1;
}

.card-item {
  display: grid;
  grid-template-rows: subgrid;
  grid-row: span 4;

  row-gap: 5px;
}

.podcast-card-link {
  color: var(--bs-black);

  &:hover {
    color: var(--bs-success);
  }
}

.topic {
  align-self: center;
}

.page-item.active {
  .page-link {
    background-color: var(--bs-success) !important;
    border-color: var(--bs-success) !important;
  }
}

.page-link {
  color: var(--bs-secondary);
}

.page-link:hover {
  color: var(--bs-success);
}

.page-item.active .page-link:hover {
  color: var(--bs-white);
}

.page-item.disabled {
  .page-link {
    color: var(--bs-secondary);
  }
}
</style>
