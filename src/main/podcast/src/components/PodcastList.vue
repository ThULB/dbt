<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router';
import { useI18n } from 'vue-i18n';

import requestSolr from '../utils/requestSolr.ts';
import applicationUrl from '../utils/applicationUrl.ts';
import podcastCategoryValue from '../utils/podcastCategory.ts';
import type { SolrDocument, SolrResponse } from '../types/types.ts';
import { usePodcastsStore } from '../stores/podcasts.ts';
import LoadingState from './LoadingState.vue';
import ErrorState from './ErrorState.vue';
import EmptyState from './EmptyState.vue';

const allowedSortValues = new Set(['created asc', 'created desc']);

const itunesCategoryFilterPattern = /^category:"itunes-podcast:[^"]+"$/;

interface PodcastCategoryBadge {
  label: string;
  value?: string;
}

const podcastsStore = usePodcastsStore();
const route = useRoute();
const router = useRouter();
const { t, te } = useI18n({ useScope: 'global' });

const podcastMetadata = ref<SolrResponse | null>(null);
let requestSequence = 0;

const totalItems = ref(0);
const itemsPerPage = ref(20);
const currentPage = computed(() => {
  const pageQuery = route.query.page;

  if (typeof pageQuery !== 'string' || !/^[1-9]\d*$/.test(pageQuery)) {
    return 1;
  }

  const page = Number(pageQuery);

  return Number.isSafeInteger(page) ? page : 1;
});

const isLoading = ref(false);
const isError = ref(false);

const coverErrors = ref<Record<string, boolean>>({});
const coverLoaded = ref<Record<string, boolean>>({});

const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value));

const visiblePages = computed<(number | string)[]>(() => {
  const pages: (number | string)[] = [];

  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 7) {
    for (let page = 1; page <= total; page++) {
      pages.push(page);
    }

    return pages;
  }

  pages.push(1);

  const start = Math.max(2, current - 2);
  const end = Math.min(total - 1, current + 2);

  if (start === 3) {
    pages.push(2);
  } else if (start > 3) {
    pages.push('...');
  }

  for (let page = start; page <= end; page++) {
    pages.push(page);
  }

  if (end === total - 2) {
    pages.push(total - 1);
  } else if (end < total - 2) {
    pages.push('...');
  }

  pages.push(total);

  return pages;
});

function createParams(): URLSearchParams {
  const params = new URLSearchParams();

  params.append('fq', 'mods.genre:podcast');
  params.set('wt', 'json');

  params.set('rows', `${itemsPerPage.value}`);

  params.set('start', `${(currentPage.value - 1) * itemsPerPage.value}`);

  const conditionalQuery = route.query.condQuery;

  if (typeof conditionalQuery === 'string') {
    params.set('condQuery', conditionalQuery);
  }

  const categoryFilter = route.query.fq;

  if (typeof categoryFilter === 'string' && itunesCategoryFilterPattern.test(categoryFilter)) {
    params.append('fq', categoryFilter);
  }

  const sort = route.query.sort;

  if (typeof sort === 'string' && allowedSortValues.has(sort)) {
    params.set('sort', sort);
  }

  return params;
}

function getItunesCategories(podcast: SolrDocument): PodcastCategoryBadge[] {
  const itunesCategories =
    podcast['category.top']
      ?.filter((category) => category.startsWith('itunes-podcast:'))
      .map((category) => category.replace('itunes-podcast:', ''))
      .map((category) => {
        const value = podcastCategoryValue(category);
        const key = value && `podcast.categories.${value}`;

        return {
          label: key && te(key) ? t(key) : category.replaceAll('_', ' '),
          value,
        };
      }) ?? [];

  if (itunesCategories.length === 0) {
    return [{ label: t('podcast.list.uncategorized') }];
  }

  return itunesCategories;
}

function getPodcastTitle(podcast: SolrDocument): string {
  return podcast['mods.title']?.[0] ?? t('podcast.list.untitled');
}

function getPodcastAuthors(podcast: SolrDocument): string {
  return podcast['mods.author']?.join(', ') ?? '';
}

function getCoverUrl(podcast: SolrDocument): string {
  return applicationUrl(
    `api/iiif/image/v2/thumbnail/${encodeURIComponent(podcast.id)}/full/!300,300/0/default.jpg`,
  );
}

function handleCoverLoad(podcastId: string): void {
  coverLoaded.value[podcastId] = true;
  coverErrors.value[podcastId] = false;
}

function handleCoverError(podcastId: string): void {
  coverLoaded.value[podcastId] = false;
  coverErrors.value[podcastId] = true;
}

function scrollToTop(): void {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  });
}

async function searchPodcasts(): Promise<void> {
  const requestId = ++requestSequence;

  isLoading.value = true;
  isError.value = false;

  const params = createParams();

  try {
    const response = await requestSolr(params);

    if (requestId !== requestSequence) {
      return;
    }

    podcastMetadata.value = JSON.parse(response) as SolrResponse;

    totalItems.value = podcastMetadata.value.response.numFound;

    coverErrors.value = {};
    coverLoaded.value = {};

    /*
     * Falls sich durch Filter die Anzahl der Seiten verkleinert
     * und currentPage nicht mehr existiert.
     */
    if (totalPages.value > 0 && currentPage.value > totalPages.value) {
      await router.replace({
        query: {
          ...route.query,
          page: `${totalPages.value}`,
        },
      });
    }
  } catch (err) {
    if (requestId !== requestSequence) {
      return;
    }

    podcastMetadata.value = null;
    console.error('Podcasts konnten nicht geladen werden:', err);
    isError.value = true;
  } finally {
    if (requestId === requestSequence) {
      isLoading.value = false;
    }
  }
}

async function setPage(page: number): Promise<void> {
  if (page < 1 || page > totalPages.value || page === currentPage.value) {
    return;
  }

  await router.push({
    query: {
      ...route.query,
      page: `${page}`,
    },
  });

  scrollToTop();
}

async function setCategoryFilter(category: string): Promise<void> {
  const query: LocationQueryRaw = {
    ...route.query,
    fq: `category:"itunes-podcast:${category}"`,
  };

  delete query.page;

  await router.push({
    name: 'podcasts',
    query,
  });

  scrollToTop();
}

/*
 * Bei Änderung der Seite oder der Filter neu laden.
 */
watch(
  () => route.query,
  () => {
    searchPodcasts();
  },
  {
    immediate: true,
    deep: true,
  },
);
</script>

<template>
  <section class="podcast-list">

    <LoadingState v-if="isLoading">
      {{ t('podcast.list.loading') }}
    </LoadingState>

    <ErrorState v-else-if="isError" @retry="searchPodcasts" />

    <EmptyState
        v-else-if="podcastMetadata?.response.numFound === 0"
        :description="t('podcast.list.emptyDescription')"
    />

    <!-- Results -->
    <div
        v-else-if="podcastMetadata"
        class="podcast-list__results"
    >
      <h2 class="mb-4">
        {{
          t(
              totalItems === 1
                  ? 'podcast.list.resultSingle'
                  : 'podcast.list.resultMultiple',
              { count: totalItems }
          )
        }}
      </h2>

      <div class="podcast-list__grid">

        <article
            v-for="podcast in podcastMetadata.response.docs"
            :key="podcast.id"
            class="podcast-list__card d-grid h-100 border-0 shadow-sm rounded-5 overflow-hidden position-relative"
        >

          <!-- Cover -->
          <div class="podcast-list__cover ratio ratio-1x1 bg-light border-bottom position-relative overflow-hidden">

            <img
                v-if="!coverErrors[podcast.id]"
                class="podcast-list__cover-image card-img-top position-absolute top-0 start-0 w-100 h-100 object-fit-cover z-1"
                :class="{ 'is-loaded': coverLoaded[podcast.id] }"
                :src="getCoverUrl(podcast)"
                :alt="t('podcast.image.coverAlt', { title: getPodcastTitle(podcast) })"
                loading="lazy"
                decoding="async"
                @load="handleCoverLoad(podcast.id)"
                @error="handleCoverError(podcast.id)"
            >

            <div
                v-if="!coverLoaded[podcast.id] || coverErrors[podcast.id]"
                class="podcast-list__cover-placeholder position-absolute top-0 start-0 d-flex align-items-center justify-content-center w-100 h-100 bg-body-secondary text-body-secondary z-1"
                aria-hidden="true"
            >
              <i class="fa-solid fa-images"></i>
            </div>

            <button
                class="podcast-list__play d-inline-flex align-items-center justify-content-center flex-shrink-0 rounded-circle border-0 p-0"
                :class="{ 'is-active': podcastsStore.isPodcastPlaying(podcast.id) || podcastsStore.isPlaylistLoading(podcast.id) }"
                type="button"
                :aria-pressed="podcastsStore.isPodcastPlaying(podcast.id)"
                :aria-busy="podcastsStore.isPlaylistLoading(podcast.id) || undefined"
                :aria-label="podcastsStore.isPodcastCurrent(podcast.id)
                    ? podcastsStore.isPodcastPlaying(podcast.id)
                        ? t('podcast.player.pause')
                        : t('podcast.player.resume')
                    : t('podcast.episode.latest')"
                :title="podcastsStore.isPodcastCurrent(podcast.id)
                    ? podcastsStore.isPodcastPlaying(podcast.id)
                        ? t('podcast.player.pause')
                        : t('podcast.player.resume')
                    : t('podcast.episode.latest')"
                @click="podcastsStore.playLatestEpisode(podcast.id)"
            >
              <i
                  class="podcast-list__play-icon fas"
                  :class="podcastsStore.isPlaylistLoading(podcast.id)
                      ? 'fa-spinner fa-spin'
                      : podcastsStore.isPodcastPlaying(podcast.id) ? 'fa-pause' : 'fa-play'"
                  aria-hidden="true"
              />
            </button>

          </div>

          <!-- Categories -->
          <div
              v-if="getItunesCategories(podcast).length"
              class="podcast-list__categories align-self-center position-relative z-3 mt-1 px-2"
          >
            <template
                v-for="category in getItunesCategories(podcast)"
                :key="category.value ?? category.label"
            >
              <button
                  v-if="category.value"
                  class="podcast-list__category badge rounded-4 border-0 text-bg-success text-wrap text-start me-1"
                  type="button"
                  @click="setCategoryFilter(category.value)"
              >
                {{ category.label }}
              </button>

              <span
                  v-else
                  class="badge rounded-4 text-bg-success text-wrap text-start me-1"
              >
                {{ category.label }}
              </span>
            </template>
          </div>

          <!-- Title -->
          <h3 class="h6 card-title px-2 mb-2">
            <RouterLink
                class="podcast-list__card-link stretched-link"
                :to="{
                  name: 'podcast',
                  params: { podcastId: podcast.id },
                }"
            >
              {{ getPodcastTitle(podcast) }}
            </RouterLink>
          </h3>

          <!-- Authors -->
          <p
              v-if="getPodcastAuthors(podcast)"
              class="small px-2 text-truncate mb-2"
          >
            {{ getPodcastAuthors(podcast) }}
          </p>

        </article>

      </div>

      <!-- Pagination -->
      <nav
          v-if="totalPages > 1"
          class="mt-5"
          :aria-label="t('podcast.pagination.ariaLabel')"
      >
        <ul class="podcast-list__pagination pagination justify-content-center">

          <!-- Previous -->
          <li
              class="page-item"
              :class="{ disabled: currentPage === 1 }"
          >
            <button
                class="page-link rounded-3"
                type="button"
                :disabled="currentPage === 1"
                :aria-label="t('podcast.pagination.previousAriaLabel')"
                @click="setPage(currentPage - 1)"
            >
              {{ t('podcast.pagination.previous') }}
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
                class="page-link rounded-3 text-body-secondary"
            >
              …
            </span>

            <!-- Page -->
            <button
                v-else
                class="page-link rounded-3"
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
                class="page-link rounded-3"
                type="button"
                :disabled="currentPage === totalPages"
                :aria-label="t('podcast.pagination.nextAriaLabel')"
                @click="setPage(currentPage + 1)"
            >
              {{ t('podcast.pagination.next') }}
            </button>
          </li>

        </ul>
      </nav>

    </div>

  </section>
</template>

<style scoped lang="scss">
.podcast-list {
  &__grid {
    display: grid;
    grid-template-columns:
      repeat(
        auto-fill,
        minmax(min(100%, 250px), 1fr)
      );
    gap: 1.5rem;
  }

  &__cover-image {
    opacity: 0;
    transition: opacity 0.2s ease;

    &.is-loaded {
      opacity: 1;
    }
  }

  &__cover-placeholder {
    font-size: 3rem;
  }

  &__categories {
    pointer-events: none;
  }

  &__category {
    position: relative;
    pointer-events: auto;
    cursor: pointer;

    &:hover {
      background-color: var(--bs-success-text-emphasis) !important;
    }

    &:focus-visible {
      outline: 0.16rem solid var(--bs-focus-ring-color);
      outline-offset: 0.14rem;
    }
  }

  &__play {
    position: absolute;
    z-index: 3;
    inset: auto 0.75rem 0.75rem auto;
    width: 2.75rem;
    height: 2.75rem;
    background-color: rgba(var(--bs-body-bg-rgb), 0.85);
    box-shadow: 0 0.25rem 0.75rem rgba(var(--bs-black-rgb), 0.25);
    backdrop-filter: blur(0.4rem);
    color: var(--bs-success-text-emphasis);
    cursor: pointer;
    font-size: var(--podcast-icon-control);
    line-height: 1;
    opacity: 0;
    transform: scale(0.9);
    transition:
        background-color 0.15s ease,
        color 0.15s ease,
        opacity 0.15s ease,
        transform 0.15s ease;

    // Nur bei Hover, Tastaturfokus oder laufender Wiedergabe sichtbar.
    .podcast-list__card:hover &,
    &:focus-visible,
    &.is-active {
      opacity: 1;
      transform: scale(1);
    }

    // Touch-Geräte haben kein Hover.
    @media (hover: none) {
      opacity: 1;
      transform: scale(1);
    }

    // Wie die Steuertasten im Player; dunkler bei Hover und laufender Wiedergabe.
    &:hover:not(:disabled),
    &.is-active {
      background-color: var(--bs-secondary-bg);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.35;
    }

    &:focus-visible {
      outline: 0.16rem solid var(--bs-focus-ring-color);
      outline-offset: 0.14rem;
    }
  }

  &__play-icon.fa-play {
    position: relative;
    left: 3%;
  }

  &__card {
    grid-template-rows: subgrid;
    grid-row: span 4;
    row-gap: 5px;
  }

  &__card-link {
    color: var(--bs-black);
    line-height: 1.35;
    overflow-wrap: anywhere;

    &::after {
      z-index: 2;
    }

    &:hover {
      color: var(--bs-success);
    }

    &:focus-visible {
      outline: none;
    }

    &:focus-visible::after {
      border-radius: var(--bs-border-radius-xxl);
      outline: 0.16rem solid var(--bs-focus-ring-color);
      outline-offset: -0.25rem;
    }
  }

  &__pagination {
    flex-wrap: wrap;
    gap: 0.375rem;

    .page-link {
      min-width: 2.25rem;
      text-align: center;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &__cover-image,
    &__play {
      transition: none;
    }
  }
}
</style>
