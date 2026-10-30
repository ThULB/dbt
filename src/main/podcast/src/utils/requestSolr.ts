import applicationUrl from './applicationUrl';

export default async function requestSolr(params: URLSearchParams): Promise<string> {
  const url = new URL(applicationUrl('servlets/solr/find'));
  url.search = params.toString();

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  return response.text();
}
