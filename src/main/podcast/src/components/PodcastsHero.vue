<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({ useScope: 'global' })
</script>

<template>
  <section class="podcast-hero row align-items-end g-4 pt-5 mb-5">
    <div class="col-lg-8">
      <p class="text-success fw-semibold text-uppercase small mb-2">
        {{ t('podcast.hero.eyebrow') }}
      </p>
      <h1 class="display-6 fw-semibold mb-3">
        {{ t('podcast.hero.title') }}
      </h1>
      <p class="lead text-body-secondary mb-0">
        {{ t('podcast.hero.description') }}
      </p>
    </div>
  </section>
</template>

<style scoped lang="scss">
.podcast-hero {
  .lead {
    max-width: 62ch;
  }
}
</style>
