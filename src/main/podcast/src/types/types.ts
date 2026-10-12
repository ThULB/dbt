export interface SolrResponse {
    responseHeader: SolrResponseHeader
    response: SolrResult
    facet_counts: SolrFacetCounts
}

export interface SolrResponseHeader {
    zkConnected: boolean
    status: number
    QTime: number
    params: Record<string, string | string[]>
}

export interface SolrResult {
    numFound: number
    start: number
    maxScore: number
    numFoundExact: boolean
    docs: SolrDocument[]
}

export interface SolrDocument {
    objectKind: string
    id: string
    returnId: string
    objectProject: string
    objectType: string

    link: string[]

    modified: string
    created: string
    modifiedby: string
    createdby: string

    state: string

    derCount: number
    derivates: string[]

    worldReadable: boolean
    worldReadableComplete: boolean

    category: string[]
    'category.top': string[]

    'mods.title': string[]
    'mods.title.main': string

    'mods.name': string[]
    'mods.name.top': string[]

    'mods.author': string[]

    'mods.publisher'?: string[]

    'mods.genre': string[]

    'mods.abstract': string[]

    'mods.dateIssued': string
    'mods.yearIssued': number
    'mods.type': string

    search_result_link_text: string

    'mods.pindexname': string[]
    'mods.pindexname.published': string[]

    'mods.nameByRole.personal.aut': string[]
    'mods.nameByRole.personal.own'?: string[]

    'mods.abstract.result': string
    'mods.abstract.result.de': string[]

    'mir.toc.title': string
    'mir.toc.authors': string

    _version_: number
    score: number
}

export interface SolrFacetCounts {
    facet_queries: Record<string, unknown>

    facet_fields: {
        'mods.genre': (string | number)[]
        worldReadableComplete: (string | number)[]
    }

    facet_ranges: Record<string, unknown>
    facet_intervals: Record<string, unknown>
    facet_heatmaps: Record<string, unknown>
}

export interface PodcastEpisode {
    title: string
    link: string
    author: string[]
    description: string
    image: string
    audioUrl: string
    audioLength: number
    audioType: string
    guid: string
    pubDate: string
    duration: string
}

export interface PodcastPlaylist {
    title: string
    link: string
    language: string
    explicit: boolean
    author: string[]
    description: string
    ownerName: string
    ownerEmail: string
    image: string
    category: string
    subcategory: string
    episodes: PodcastEpisode[]
}
