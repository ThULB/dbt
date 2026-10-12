<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const search = ref('')
const category = ref('')
const subCategory = ref('')
const sort = ref('created desc')
const itunesPodcastCategories = [
  {
    value: 'Arts',
    label: 'Arts',
    subcategories: [
      { value: 'Books', label: 'Books' },
      { value: 'Design', label: 'Design' },
      { value: 'Fashion_Beauty', label: 'Fashion & Beauty' },
      { value: 'Food', label: 'Food' },
      { value: 'Performing_Arts', label: 'Performing Arts' },
      { value: 'Visual_Arts', label: 'Visual Arts' },
    ],
  },
  {
    value: 'Business',
    label: 'Business',
    subcategories: [
      { value: 'Careers', label: 'Careers' },
      { value: 'Entrepreneurship', label: 'Entrepreneurship' },
      { value: 'Investing', label: 'Investing' },
      { value: 'Management', label: 'Management' },
      { value: 'Marketing', label: 'Marketing' },
      { value: 'Non-Profit', label: 'Non-Profit' },
    ],
  },
  {
    value: 'Comedy',
    label: 'Comedy',
    subcategories: [
      { value: 'Comedy_Interviews', label: 'Comedy Interviews' },
      { value: 'Improv', label: 'Improv' },
      { value: 'Stand-Up', label: 'Stand-Up' },
    ],
  },
  {
    value: 'Education',
    label: 'Education',
    subcategories: [
      { value: 'Courses', label: 'Courses' },
      { value: 'How_To', label: 'How To' },
      { value: 'Language_Learning', label: 'Language Learning' },
      { value: 'Self-Improvement', label: 'Self-Improvement' },
    ],
  },
  {
    value: 'Fiction',
    label: 'Fiction',
    subcategories: [
      { value: 'Comedy_Fiction', label: 'Comedy Fiction' },
      { value: 'Drama', label: 'Drama' },
      { value: 'Science_Fiction', label: 'Science Fiction' },
    ],
  },
  {
    value: 'Government',
    label: 'Government',
    subcategories: [],
  },
  {
    value: 'History',
    label: 'History',
    subcategories: [],
  },
  {
    value: 'Health_Fitness',
    label: 'Health & Fitness',
    subcategories: [
      { value: 'Alternative_Health', label: 'Alternative Health' },
      { value: 'Fitness', label: 'Fitness' },
      { value: 'Medicine', label: 'Medicine' },
      { value: 'Mental_Health', label: 'Mental Health' },
      { value: 'Nutrition', label: 'Nutrition' },
      { value: 'Sexuality', label: 'Sexuality' },
    ],
  },
  {
    value: 'Kids_Family',
    label: 'Kids & Family',
    subcategories: [
      { value: 'Education_for_Kids', label: 'Education for Kids' },
      { value: 'Parenting', label: 'Parenting' },
      { value: 'Pets_Animals', label: 'Pets & Animals' },
      { value: 'Stories_for_Kids', label: 'Stories for Kids' },
    ],
  },
  {
    value: 'Leisure',
    label: 'Leisure',
    subcategories: [
      { value: 'Animation_Manga', label: 'Animation & Manga' },
      { value: 'Automotive', label: 'Automotive' },
      { value: 'Aviation', label: 'Aviation' },
      { value: 'Crafts', label: 'Crafts' },
      { value: 'Games', label: 'Games' },
      { value: 'Hobbies', label: 'Hobbies' },
      { value: 'Home_Garden', label: 'Home & Garden' },
      { value: 'Video_Games', label: 'Video Games' },
    ],
  },
  {
    value: 'Music',
    label: 'Music',
    subcategories: [
      { value: 'Music_Commentary', label: 'Music Commentary' },
      { value: 'Music_History', label: 'Music History' },
      { value: 'Music_Interviews', label: 'Music Interviews' },
    ],
  },
  {
    value: 'News',
    label: 'News',
    subcategories: [
      { value: 'Business_News', label: 'Business News' },
      { value: 'Daily_News', label: 'Daily News' },
      { value: 'Entertainment_News', label: 'Entertainment News' },
      { value: 'News_Commentary', label: 'News Commentary' },
      { value: 'Politics', label: 'Politics' },
      { value: 'Sports_News', label: 'Sports News' },
      { value: 'Tech_News', label: 'Tech News' },
    ],
  },
  {
    value: 'Religion_Spirituality',
    label: 'Religion & Spirituality',
    subcategories: [
      { value: 'Buddhism', label: 'Buddhism' },
      { value: 'Christianity', label: 'Christianity' },
      { value: 'Hinduism', label: 'Hinduism' },
      { value: 'Islam', label: 'Islam' },
      { value: 'Judaism', label: 'Judaism' },
      { value: 'Religion', label: 'Religion' },
      { value: 'Spirituality', label: 'Spirituality' },
    ],
  },
  {
    value: 'Science',
    label: 'Science',
    subcategories: [
      { value: 'Astronomy', label: 'Astronomy' },
      { value: 'Chemistry', label: 'Chemistry' },
      { value: 'Earth_Sciences', label: 'Earth Sciences' },
      { value: 'Life_Sciences', label: 'Life Sciences' },
      { value: 'Mathematics', label: 'Mathematics' },
      { value: 'Natural_Sciences', label: 'Natural Sciences' },
      { value: 'Nature', label: 'Nature' },
      { value: 'Physics', label: 'Physics' },
      { value: 'Social_Sciences', label: 'Social Sciences' },
    ],
  },
  {
    value: 'Society_Culture',
    label: 'Society & Culture',
    subcategories: [
      { value: 'Documentary', label: 'Documentary' },
      { value: 'Personal_Journals', label: 'Personal Journals' },
      { value: 'Philosophy', label: 'Philosophy' },
      { value: 'Places_Travel', label: 'Places & Travel' },
      { value: 'Relationships', label: 'Relationships' },
    ],
  },
  {
    value: 'Sports',
    label: 'Sports',
    subcategories: [
      { value: 'Baseball', label: 'Baseball' },
      { value: 'Basketball', label: 'Basketball' },
      { value: 'Cricket', label: 'Cricket' },
      { value: 'Fantasy_Sports', label: 'Fantasy Sports' },
      { value: 'Football', label: 'Football' },
      { value: 'Golf', label: 'Golf' },
      { value: 'Hockey', label: 'Hockey' },
      { value: 'Rugby', label: 'Rugby' },
      { value: 'Running', label: 'Running' },
      { value: 'Soccer', label: 'Soccer' },
      { value: 'Swimming', label: 'Swimming' },
      { value: 'Tennis', label: 'Tennis' },
      { value: 'Volleyball', label: 'Volleyball' },
      { value: 'Wilderness', label: 'Wilderness' },
      { value: 'Wrestling', label: 'Wrestling' },
    ],
  },
  {
    value: 'Technology',
    label: 'Technology',
    subcategories: [],
  },
  {
    value: 'True_Crime',
    label: 'True Crime',
    subcategories: [],
  },
  {
    value: 'TV_Film',
    label: 'TV & Film',
    subcategories: [
      { value: 'After_Shows', label: 'After Shows' },
      { value: 'Film_History', label: 'Film History' },
      { value: 'Film_Interviews', label: 'Film Interviews' },
      { value: 'Film_Reviews', label: 'Film Reviews' },
      { value: 'TV_Reviews', label: 'TV Reviews' },
    ],
  },
];
const selectedSubcategories = computed(() => {
  const selectedCategory = itunesPodcastCategories.find(item => item.value === category.value)

  return selectedCategory?.subcategories ?? []
})


watch(category, () => {
  subCategory.value = ''
})

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
                :value="category.value"
                :label="category.label"
            >
              {{category.value}}
            </option>
          </select>
        </div>

        <div class="col-md-4 col-lg-4">
          <label for="podcast-category" class="form-label fw-semibold">Subkategorie</label>
          <select
              id="podcast-category"
              v-model="subCategory"
              class="form-select"
              :disabled="selectedSubcategories.length === 0"
          >
            <option value="">Bitte wählen</option>
            <option
                v-for="sub in selectedSubcategories"
                :value="sub.value"
                :label="sub.label"
            >
              {{sub.value}}
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
          <button id="podcast-reset" class="btn btn-success" type="submit">Suchen</button>
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
