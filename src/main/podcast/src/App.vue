<script setup lang="ts">
import Player from './components/Player.vue';
import { usePlayerStore } from './stores/player';

const playerStore = usePlayerStore();
</script>

<template>
  <RouterView />

  <Player v-if="playerStore.episode"/>
</template>
