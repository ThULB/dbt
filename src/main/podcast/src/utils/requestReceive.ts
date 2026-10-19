import applicationUrl from './applicationUrl'

export default async function requestReceive(id: string): Promise<string> {
    const url = new URL(
        applicationUrl(`receive/${encodeURIComponent(id)}`)
    )
    url.searchParams.set('XSL.Style', 'podcast')

    const response = await fetch(url)

    if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`)
    }

    return response.text()
}
