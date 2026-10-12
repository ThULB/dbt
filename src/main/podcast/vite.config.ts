import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'node:path'

export default defineConfig({
  plugins: [vue()],

  server: {
    proxy: {
      '/mir/servlets/solr': {
        target: 'http://localhost:8291',
        changeOrigin: true,
      },
      '/mir/api/iiif/image/v2/thumbnail': {
        target: 'http://localhost:8291',
        changeOrigin: true,
      },
      '/mir/receive/': {
        target: 'http://localhost:8291',
        changeOrigin: true,
      }
    }
  },

  // This bundle is loaded directly by the browser, not by another bundler.
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },

  build: {
    lib: {
      entry: resolve(import.meta.dirname, 'src/index.ts'),
      name: 'Podcast',
      formats: ['es'],
      fileName: () => 'podcast.js',
      cssFileName: 'podcast',
    },
  },
})
