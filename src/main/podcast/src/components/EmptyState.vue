<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps<{
  title?: string;
  description?: string;
}>();

const { t } = useI18n({ useScope: 'global' });

const stateTitle = computed(() => props.title ?? t('podcast.state.empty.title'));
const stateDescription = computed(() => props.description ?? t('podcast.state.empty.description'));
</script>

<template>
  <div
      class="alert alert-light border text-center py-5"
      role="status"
  >
    <h2 class="h5">
      {{ stateTitle }}
    </h2>

    <p class="text-body-secondary mb-0">
      {{ stateDescription }}
    </p>
  </div>
</template>

<style scoped lang="scss">
.alert {
  border-radius: var(--bs-border-radius-lg);

  h2 {
    overflow-wrap: anywhere;
  }
}
</style>
