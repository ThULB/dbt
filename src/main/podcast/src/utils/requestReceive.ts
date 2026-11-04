import applicationUrl from './applicationUrl';

export class ReceiveRequestError extends Error {
  readonly status: number;

  constructor(status: number) {
    super(`Request failed with status ${status}`);
    this.name = 'ReceiveRequestError';
    this.status = status;
  }
}

export default async function requestReceive(id: string): Promise<string> {
  const url = new URL(applicationUrl(`receive/${encodeURIComponent(id)}`));
  url.searchParams.set('XSL.Style', 'podcast');

  const response = await fetch(url);

  if (!response.ok) {
    throw new ReceiveRequestError(response.status);
  }

  return response.text();
}
