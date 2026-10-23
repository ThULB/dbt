<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({ useScope: 'global' })
</script>

<template>
  <div
      id="podcast-error"
      class="alert alert-warning"
      role="alert"
  >
    <h2 class="h5 alert-heading">
      {{ t('podcast.state.error.title') }}
    </h2>

    <p class="mb-0">
      {{ t('podcast.state.error.description') }}
    </p>
  </div>

</template>
