<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import itunesPodcastCategories from '../data/podcast-categories.json'

const route = useRoute()
const router = useRouter()
const search = ref('')
const category = ref('')
const subCategory = ref('')
const sort = ref('created desc')
const selectedSubcategories = computed(() => {
  const selectedCategory = itunesPodcastCategories.find(item => item.value === category.value)

  return selectedCategory?.subcategories ?? []
})

function queryValue(value: unknown): string {
  return typeof value === 'string' ? value : ''
}

function syncFiltersFromRoute(): void {
  search.value = queryValue(route.query.condQuery)
  sort.value = ['created asc', 'created desc'].includes(
      queryValue(route.query.sort)
  )
      ? queryValue(route.query.sort)
      : 'created desc'

  category.value = ''
  subCategory.value = ''

  const filter = queryValue(route.query.fq)
  const categoryMatch = filter.match(
      /^category:"itunes-podcast:([^"]+)"$/
  )

  if (!categoryMatch) {
    return
  }

  const selectedValue = categoryMatch[1]
  const parentCategory = itunesPodcastCategories.find(item =>
      item.value === selectedValue ||
      item.subcategories.some(sub => sub.value === selectedValue)
  )

  if (!parentCategory) {
    return
  }

  category.value = parentCategory.value

  if (parentCategory.value !== selectedValue) {
    subCategory.value = selectedValue
  }
}


watch(
    category,
    () => {
      subCategory.value = ''
    },
    { flush: 'sync' }
)

watch(
    () => route.query,
    syncFiltersFromRoute,
    {
      immediate: true,
      deep: true,
    }
)

const handleSubmit = () => {
  const query: Record<string, string> = {}

  if (search.value) {
    query.condQuery = search.value
  }

  if (subCategory.value) {
    query.fq = `category:"itunes-podcast:${subCategory.value}"`
  }
  else if (category.value) {
    query.fq =  `category:"itunes-podcast:${category.value}"`
  }


  query.sort = sort.value

  router.push({
    name: 'podcasts',
    query,
  })
}

</script>

<template>
  <form
      id="podcast-filters"
      class="card border-0 bg-light shadow-sm mb-5"
      @submit.prevent="handleSubmit"
      aria-label="Podcasts filtern">
    <div class="card-body p-3 p-lg-4">
      <div class="row g-3 align-items-end justify-content-end">
        <div class="col-12">
          <label for="podcast-search" class="form-label fw-semibold">Podcasts durchsuchen</label>
          <input id="podcast-search" v-model="search" class="form-control" type="search" placeholder="Titel, Beschreibung, Herausgeber oder Autor" autocomplete="off">
        </div>

        <div class="col-md-4 col-lg-4">
          <label for="podcast-category" class="form-label fw-semibold">Kategorie</label>
          <select id="podcast-category" v-model="category" class="form-select">
            <option value="">Alle Kategorien</option>
            <option
                v-for="category in itunesPodcastCategories"
                :key="category.value"
                :value="category.value"
                :label="category.label"
            >
              {{category.label}}
            </option>
          </select>
        </div>

        <div class="col-md-4 col-lg-4">
          <label for="podcast-subcategory" class="form-label fw-semibold">Subkategorie</label>
          <select
              id="podcast-subcategory"
              v-model="subCategory"
              class="form-select"
              :disabled="selectedSubcategories.length === 0"
          >
            <option value="">Bitte wählen</option>
            <option
                v-for="sub in selectedSubcategories"
                :key="sub.value"
                :value="sub.value"
                :label="sub.label"
            >
              {{sub.label}}
            </option>
          </select>
        </div>

        <div class="col-md-4 col-lg-2">
          <label for="podcast-sort" class="form-label fw-semibold">Sortierung</label>
          <select id="podcast-sort" v-model="sort" class="form-select">
            <option value="created desc">Neueste zuerst</option>
            <option value="created asc">Älteste zuerst</option>
          </select>
        </div>

        <div class="col-md-3 col-lg-2 d-grid mt-4">
          <button id="podcast-submit" class="btn btn-success" type="submit">Suchen</button>
        </div>
      </div>
    </div>
  </form>
</template>

<style scoped lang="scss">
.form-control:focus,
.form-select:focus {
  border-color: var(--bs-success);
  box-shadow: 0 0 0 0.25rem rgba(var(--bs-success-rgb), 0.25);
}

.form-select:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
</style>
