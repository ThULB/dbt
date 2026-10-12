export default async function requestSolr(
    params: URLSearchParams
):Promise<string> {
    const response = await fetch(`/mir/servlets/solr/find?${params}`)

    if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`)
    }

    return response.text()
}
