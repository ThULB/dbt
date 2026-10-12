<template>
  <section class="py-5" aria-labelledby="not-found-title">
    <h1 id="not-found-title" class="display-6 fw-semibold">
      Seite nicht gefunden
    </h1>
    <RouterLink :to="{ name: 'podcasts' }" class="text-success">
      Zur Podcast-&Uuml;bersicht
    </RouterLink>
  </section>
</template>
