<script setup lang="ts">
import applicationUrl from '../utils/applicationUrl'

const startPageUrl = applicationUrl('content/index.xml')
</script>

<template>
  <section class="not-found py-5" aria-labelledby="not-found-title">
    <div class="not-found__content d-flex flex-column align-items-center text-center">
      <span class="not-found__error fw-semibold text-danger">
        404
      </span>

      <h1
          id="not-found-title"
          class="not-found__title fw-semibold"
      >
        Seite nicht gefunden
      </h1>

      <p class="not-found__description text-muted fs-5">
        Die von Ihnen aufgerufene Seite existiert nicht
        oder wurde möglicherweise verschoben.
      </p>

      <div class="not-found__actions d-flex flex-wrap justify-content-center align-items-center gap-4 mt-4">
        <RouterLink
            :to="{ name: 'podcasts' }"
            class="btn btn-success px-4 py-3"
        >
          Zur Podcast-Übersicht &rarr;
        </RouterLink>

        <a
            :href="startPageUrl"
            class="not-found__link text-success"
        >
          Zur Startseite &rarr;
        </a>
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.not-found {
  &__content {
    padding-top: 3rem;
    gap: 1rem;
  }

  &__error {
    font-size: 4.7rem;
    line-height: 1;
  }

  &__title {
    font-size: 2.2rem;
    margin: 0;
  }

  &__description {
    max-width: 33rem;
    margin: 0;
  }

  &__link {
    text-decoration: none;
    border-bottom: 1px solid currentColor;
    padding-bottom: 0.25rem;

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>
