import requestSolr from './requestSolr';
import type { SolrResponse } from '../types/types';

const CATEGORY_PREFIX = 'itunes-podcast:';

export default async function requestPodcastCategories(): Promise<Set<string>> {
  const params = new URLSearchParams({
    q: '*:*',
    fq: 'mods.genre:podcast',
    rows: '0',
    wt: 'json',
    facet: 'true',
    'facet.field': 'category',
    'facet.prefix': CATEGORY_PREFIX,
    'facet.mincount': '1',
    'facet.limit': '-1',
  });

  const response = JSON.parse(await requestSolr(params)) as SolrResponse;
  const facetValues = response.facet_counts?.facet_fields?.category ?? [];
  const categories = new Set<string>();

  // Solr returns facet fields as a flat list: [value, count, value, count, ...]
  for (let i = 0; i < facetValues.length; i += 2) {
    const value = facetValues[i];
    const count = Number(facetValues[i + 1]);

    if (typeof value === 'string' && value.startsWith(CATEGORY_PREFIX) && count > 0) {
      categories.add(value.slice(CATEGORY_PREFIX.length));
    }
  }

  return categories;
}
