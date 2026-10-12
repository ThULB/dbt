<!-- ErrorState.vue -->
<template>
  <div
      id="podcast-error"
      class="alert alert-warning"
      role="alert"
  >
    <h2 class="h5 alert-heading">
      Podcastdaten konnten nicht geladen werden
    </h2>

    <p class="mb-0">
      <slot>Unbekannter Fehler</slot>
    </p>
  </div>

</template>
