import itunesPodcastCategories from '../data/podcast-categories.json';

const categories = itunesPodcastCategories.flatMap((category) => [
  category,
  ...category.subcategories,
]);

export default function podcastCategoryValue(category: string): string | undefined {
  return categories.find((item) => item.value === category || item.label === category)?.value;
}
