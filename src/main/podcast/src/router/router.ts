import { createRouter, createWebHistory } from 'vue-router';
import Podcasts from '../views/Podcasts.vue';
import Playlist from '../views/Playlist.vue';
import NotFound from '../views/NotFound.vue';

const configuredBasePath = document.querySelector<HTMLMetaElement>(
  'meta[name="podcast-base-path"]',
)?.content;

const basePath = configuredBasePath
  ? new URL(configuredBasePath, window.location.origin).pathname
  : '/podcast/';

const router = createRouter({
  history: createWebHistory(basePath),
  routes: [
    {
      path: '/',
      name: 'podcasts',
      component: Podcasts,
    },
    {
      path: '/:podcastId',
      name: 'podcast',
      component: Playlist,
      props: true,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFound,
    },
  ],
});

export default router;
