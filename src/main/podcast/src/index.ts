import { createApp, type App as VueApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router/router'

import './style.css'

const PODCAST_MOUNT_SELECTOR = '#main_content > .container'

export function mountPodcast(
    selector = PODCAST_MOUNT_SELECTOR
): VueApp<Element> {
  const container = document.querySelector(selector)

  if (!container) {
    throw new Error(
        `Podcast mount element not found: ${selector}`
    )
  }

  const app = createApp(App)

  app.use(createPinia())
  app.use(router)

  app.mount(container)

  return app
}

mountPodcast()