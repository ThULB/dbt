<!-- EmptyState.vue -->

<script setup lang="ts">
withDefaults(
    defineProps<{
      title?: string,
      description?: string,
    }>(),
    {
      title: 'Keine Daten gefunden',
      description: 'Passen Sie den Suchbegriff an.',
    }
)
</script>

<template>
  <div
      class="alert alert-light border text-center py-5"
      role="status"
  >
    <h2 class="h5">
      {{ title }}
    </h2>

    <p class="text-body-secondary mb-0">
      {{ description }}
    </p>
  </div>
</template>