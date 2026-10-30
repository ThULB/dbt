<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n({ useScope: 'global' });
</script>

<template>
  <div
      class="d-flex justify-content-center align-items-center gap-3 border rounded bg-light py-5"
      role="status"
      aria-live="polite"
  >
    <span
        class="spinner-border text-success"
        aria-hidden="true"
    ></span>

    <span>
      <slot>
        {{ t('podcast.state.loading') }}
      </slot>
    </span>
  </div>
</template>

<style scoped lang="scss">
[role="status"] {
  --bs-border-color: var(--bs-border-color-translucent);

  .spinner-border {
    width: 1.25rem;
    height: 1.25rem;
  }
}
</style>
