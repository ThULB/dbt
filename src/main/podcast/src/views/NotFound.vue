<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import applicationUrl from '../utils/applicationUrl';

const { t } = useI18n({ useScope: 'global' });
const startPageUrl = applicationUrl('content/index.xml');
</script>

<template>
  <section class="not-found py-5" aria-labelledby="not-found-title">
    <div class="not-found__content d-flex flex-column align-items-center text-center gap-3 pt-5">
      <span class="not-found__error fw-semibold text-danger">
        404
      </span>

      <h1
          id="not-found-title"
          class="not-found__title fw-semibold m-0"
      >
        {{ t('podcast.notFound.title') }}
      </h1>

      <p class="not-found__description text-muted fs-5 mb-0">
        {{ t('podcast.notFound.description') }}
      </p>

      <div class="not-found__actions d-flex flex-wrap justify-content-center align-items-center gap-4 mt-4">
        <RouterLink
            :to="{ name: 'podcasts' }"
            class="btn btn-success px-4 py-3"
        >
          {{ t('podcast.navigation.toOverview') }} &rarr;
        </RouterLink>

        <a
            :href="startPageUrl"
            class="not-found__link text-success text-decoration-none border-bottom pb-1"
        >
          {{ t('podcast.navigation.toHomepage') }} &rarr;
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.not-found {
  &__content {
    padding-inline: 1rem;
  }

  &__error {
    font-size: clamp(3.5rem, 11vw, 4.7rem);
    line-height: 1;
  }

  &__title {
    font-size: clamp(1.5rem, 5vw, 2.2rem);
    overflow-wrap: anywhere;
  }

  &__description {
    max-width: 33rem;
  }

  &__link:hover {
    opacity: 0.8;
  }

  &__actions :is(.btn, .not-found__link):focus-visible {
    outline: 0.16rem solid rgba(var(--bs-success-rgb), 0.45);
    outline-offset: 0.2rem;
  }
}
</style>
