<script setup lang="ts">
import { ref, watch } from 'vue'

import type {
  PodcastPlaylist,
  SolrResponse,
} from '../types/types.ts'

import parsePodcastXml from '../utils/XMLParser.ts'
import requestReceive, {
  ReceiveRequestError,
} from '../utils/requestReceive'
import requestSolr from '../utils/requestSolr.ts'

import LoadingState from '../components/LoadingState.vue'
import ErrorState from '../components/ErrorState.vue'
import PlaylistHeader from '../components/PlaylistHeader.vue'
import PlaylistEpisode from '../components/PlaylistEpisode.vue'
import NotFound from './NotFound.vue'


const props = defineProps<{
  podcastId: string
}>()

const error = ref<string | null>(null)
const isError = ref(false)
const isLoading = ref(false)
const isNotFound = ref(false)

const playlistMetadata = ref<PodcastPlaylist | null>(null)
let requestSequence = 0

async function podcastExists(podcastId: string): Promise<boolean> {
  if (!/^dbt_mods_\d{8}$/.test(podcastId)) {
    return false
  }

  const params = new URLSearchParams()
  params.append('fq', 'mods.genre:podcast')
  params.append('fq', `id:"${podcastId}"`)
  params.set('rows', '0')
  params.set('wt', 'json')

  const response = await requestSolr(params)
  const result = JSON.parse(response) as SolrResponse

  return result.response.numFound > 0
}

async function getPlaylistMetadata(): Promise<void> {
  const requestId = ++requestSequence

  isError.value = false
  isNotFound.value = false
  error.value = null
  isLoading.value = true
  playlistMetadata.value = null

  try {
    const exists = await podcastExists(props.podcastId)

    if (requestId !== requestSequence) {
      return
    }

    if (!exists) {
      isNotFound.value = true
      return
    }

    const xmlString = await requestReceive(props.podcastId)

    if (requestId === requestSequence) {
      playlistMetadata.value =
          parsePodcastXml(xmlString)
    }

  } catch (err) {
    if (requestId !== requestSequence) {
      return
    }

    if (err instanceof ReceiveRequestError && err.status === 404) {
      isNotFound.value = true
      return
    }

    error.value =
        err instanceof Error
            ? err.message
            : 'Unknown error'

    isError.value = true

  } finally {
    if (requestId === requestSequence) {
      isLoading.value = false
    }
  }
}


watch(
    () => props.podcastId,
    () => {
      getPlaylistMetadata()
      window.scrollTo(0, 0)
    },
    { immediate: true }
)
</script>

<template>
  <NotFound v-if="isNotFound" />

  <section v-else class="py-5" aria-labelledby="podcast-title">
    <RouterLink
        :to="{ name: 'podcasts' }"
        class="text-success"
    >
      &larr; Zur Podcast-&Uuml;bersicht
    </RouterLink>

    <LoadingState v-if="isLoading" />

    <ErrorState v-else-if="isError">{{error}}</ErrorState>

    <section v-else-if="playlistMetadata" id="playlist-content">
      <PlaylistHeader :playlist-metadata="playlistMetadata"/>

      <PlaylistEpisode :playlist-metadata="playlistMetadata"/>
    </section>
  </section>
</template>
