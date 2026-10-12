<!-- LoadingState.vue -->
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
        Wird geladen ...
      </slot>
    </span>
  </div>
</template>