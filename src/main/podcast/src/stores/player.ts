import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { PodcastEpisode } from '../types/types'

export const usePlayerStore = defineStore('player', () => {
    const episode = ref<PodcastEpisode | null>(null)
    const episodes = ref<PodcastEpisode[]>([])
    const isPlaying = ref(false)
    const playlistTitle = ref('')

    const currentTime = ref(0)
    const duration = ref(0)

    const remainingTime = computed(() =>
        Math.max(0, duration.value - currentTime.value)
    )

    const currentEpisodeIndex = computed(() => {
        if (!episode.value) {
            return -1
        }

        return episodes.value.findIndex(
            item => item.guid === episode.value?.guid
        )
    })

    function setPlaylist(newEpisodes: PodcastEpisode[], title: string): void {
        episodes.value = newEpisodes
        playlistTitle.value = title
    }

    function selectEpisode(newEpisode: PodcastEpisode): void {
        episode.value = newEpisode
    }

    function nextEpisode(): void {
        if (
            currentEpisodeIndex.value < 0 ||
            currentEpisodeIndex.value >= episodes.value.length - 1
        ) {
            return
        }

        episode.value =
            episodes.value[currentEpisodeIndex.value + 1]
    }

    function previousEpisode(): void {
        if (currentEpisodeIndex.value <= 0) {
            return
        }

        episode.value =
            episodes.value[currentEpisodeIndex.value - 1]
    }

    function closePlayer(){
        episode.value = null
    }

    function toggleEpisode(selectedEpisode: PodcastEpisode) {
        if (episode.value?.guid === selectedEpisode.guid) {
            isPlaying.value = !isPlaying.value
        } else {
            selectEpisode(selectedEpisode)
        }
    }

    function formatTime(seconds: number): string {
        if (!Number.isFinite(seconds)) {
            return '0:00'
        }

        const totalSeconds =
            Math.floor(seconds)

        const hours =
            Math.floor(totalSeconds / 3600)

        const minutes =
            Math.floor((totalSeconds % 3600) / 60)

        const secs =
            totalSeconds % 60

        if (hours > 0) {
            return `${hours}:${minutes
                .toString()
                .padStart(2, '0')}:${secs
                .toString()
                .padStart(2, '0')}`
        }

        return `${minutes}:${secs
            .toString()
            .padStart(2, '0')}`
    }


    return {
        episode,
        episodes,
        isPlaying,
        playlistTitle,
        currentEpisodeIndex,
        currentTime,
        duration,
        remainingTime,

        setPlaylist,
        selectEpisode,
        nextEpisode,
        previousEpisode,
        closePlayer,
        toggleEpisode,
        formatTime
    }
})