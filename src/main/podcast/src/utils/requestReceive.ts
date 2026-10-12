export default async function requestReceive(id: string): Promise<string> {
    const response = await fetch(
        `/mir/receive/${id}?XSL.Style=podcast`
    )

    if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`)
    }

    return response.text()
}