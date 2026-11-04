<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import applicationUrl from '../utils/applicationUrl.ts';

const { t } = useI18n({ useScope: 'global' });

const startPageUrl = applicationUrl('content/index.xml');

const emit = defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div
      id="podcast-error"
      class="podcast-error d-flex flex-column flex-lg-row align-items-center rounded-5 p-4 p-lg-5"
      role="alert"
  >
    <div class="podcast-error__illustration flex-shrink-0">
      <svg
          class="podcast-error__blob"
          viewBox="0 0 200 200"
          aria-hidden="true"
      >
        <!-- Blob -->
        <path
            class="podcast-error__blob-shape"
            d="M44 19
              C67 7, 99 14, 116 31
              C132 47, 135 57, 150 67
             C171 81, 191 95, 187 118
             C184 139, 162 145, 146 153
             C129 161, 122 181, 99 184
             C76 187, 61 173, 45 160
             C27 146, 17 126, 20 104
             C22 85, 10 69, 20 49
             C25 36, 32 26, 44 19
             Z"
        />

        <!-- Decorative rays -->
        <g
            class="podcast-error__rays"
            fill="none"
            stroke="#ffcf55"
            stroke-width="5"
            stroke-linecap="round"
        >
          <path d="M145 15 L140 29" />
          <path d="M164 26 L153 37" />
          <path d="M175 47 L160 50" />
        </g>
      </svg>

      <i
          class="podcast-error__icon fa-solid fa-plug-circle-xmark"
          aria-hidden="true"
      ></i>
    </div>

    <div class="podcast-error__divider d-none d-lg-block"></div>

    <div class="podcast-error__body text-center text-lg-start">
      <h2 class="podcast-error__title h4 fw-bold mb-3">
        {{ t('podcast.state.error.title') }}
      </h2>

      <p class="podcast-error__description text-body-secondary mb-4">
        {{ t('podcast.state.error.description') }}
      </p>

      <div
          class="podcast-error__actions d-flex flex-wrap justify-content-center justify-content-lg-start gap-3"
      >
        <button
            type="button"
            class="btn btn-warning rounded-4 px-4 py-2 fw-semibold"
            @click="emit('retry')"
        >
          <i class="fa-solid fa-rotate-right me-2" aria-hidden="true"></i>
          {{ t('podcast.state.error.tryAgain') }}
        </button>

        <a
            :href="startPageUrl"
            class="text-black rounded-4 px-3 py-2 border"
        >
          <i class="fa fa-home me-2" aria-hidden="true"></i>
          {{ t('podcast.navigation.toHomepage') }}
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.podcast-error {
  gap: 2rem;

  background-color: rgba(var(--bs-warning-rgb), 0.06);
  border-left: 0.35rem solid var(--bs-warning);

  &__illustration {
    position: relative;
    display: grid;
    place-items: center;

    width: 12rem;
    height: 12rem;
  }

  &__blob {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  &__blob-shape {
    fill: rgba(var(--bs-warning-rgb), 0.18);
    transform-origin: center;
    transform: rotate(-8deg);
  }

  &__rays {
    fill: none;
    stroke: var(--bs-warning);
    stroke-width: 5;
    stroke-linecap: round;
  }

  &__icon {
    position: relative;
    z-index: 1;

    font-size: 6rem;
    color: var(--bs-dark);
  }

  &__divider {
    width: 1px;
    align-self: stretch;

    background-color: var(--bs-border-color);
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__title {
    overflow-wrap: break-word;
  }

  &__description {
    max-width: 55rem;
    line-height: 1.6;
  }

  &__actions {
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.25rem;

      &:focus-visible {
        outline: 0.16rem solid var(--bs-focus-ring-color);
        outline-offset: 0.2rem;
      }
    }
  }

  @media (max-width: 991.98px) {
    &__illustration {
      width: 9rem;
      height: 9rem;
    }

    &__icon {
      font-size: 4.5rem;
    }
  }
}
</style>
