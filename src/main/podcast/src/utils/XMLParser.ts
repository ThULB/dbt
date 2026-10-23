import DOMPurify from 'dompurify'

import type {
    PodcastEpisode,
    PodcastPlaylist
} from '../types/types.ts'

const ITUNES_NS =
    'http://www.itunes.com/dtds/podcast-1.0.dtd'

function getDirectChildren(
    element: Element,
    tagName: string,
    namespaceUri: string | null
): Element[] {
    return Array.from(element.children).filter(child =>
        child.localName === tagName &&
        child.namespaceURI === namespaceUri
    )
}

function getDirectChild(
    element: Element,
    tagName: string,
    namespaceUri: string | null
): Element | undefined {
    return getDirectChildren(
        element,
        tagName,
        namespaceUri
    )[0]
}

function getText(
    element: Element,
    tagName: string
): string {
    return (
        getDirectChild(
            element,
            tagName,
            element.namespaceURI
        )
            ?.textContent
            ?.trim() ?? ''
    )
}

function getItunesText(
    element: Element,
    tagName: string
): string {
    return (
        getDirectChild(
            element,
            tagName,
            ITUNES_NS
        )
            ?.textContent
            ?.trim() ?? ''
    )
}

function getItunesAuthors(
    element: Element
): string[] {
    return getItunesText(element, 'author')
        .split(';')
        .map(author => author.trim())
        .filter(Boolean)
}

function getSafeHtml(
    element: Element,
    tagName: string
): string {
    return DOMPurify.sanitize(
        getText(element, tagName)
    )
}

function getItunesAttribute(
    element: Element,
    tagName: string,
    attributeName: string
): string {
    return (
        getDirectChild(
            element,
            tagName,
            ITUNES_NS
        )
            ?.getAttribute(attributeName) ?? ''
    )
}

function parsePodcastXml(
    xmlString: string
): PodcastPlaylist {
    const parser = new DOMParser()

    const xml = parser.parseFromString(
        xmlString,
        'application/xml'
    )

    const parserError =
        xml.querySelector('parsererror')

    if (parserError) {
        throw new Error(
            'Podcast XML konnte nicht verarbeitet werden'
        )
    }

    const root = xml.documentElement
    const channel = getDirectChild(
        root,
        'channel',
        root.namespaceURI
    )

    if (!channel) {
        throw new Error(
            'RSS channel wurde nicht gefunden'
        )
    }

    const categoryElement = getDirectChild(
        channel,
        'category',
        ITUNES_NS
    )

    const category =
        categoryElement
            ?.getAttribute('text') ?? ''

    const subcategoryElement =
        categoryElement
            ? getDirectChild(
                categoryElement,
                'category',
                ITUNES_NS
            )
            : undefined

    const subcategory =
        subcategoryElement
            ?.getAttribute('text') ?? ''

    const owner = getDirectChild(
        channel,
        'owner',
        ITUNES_NS
    )

    const ownerName =
        owner
            ? getItunesText(owner, 'name')
            : ''

    const ownerEmail =
        owner
            ? getItunesText(owner, 'email')
            : ''

    const items = getDirectChildren(
        channel,
        'item',
        channel.namespaceURI
    )

    const episodes: PodcastEpisode[] =
        items.map((item) => {
            const enclosure = getDirectChild(
                item,
                'enclosure',
                item.namespaceURI
            )

            return {
                title:
                    getText(item, 'title'),

                link:
                    getText(item, 'link'),

                author:
                    getItunesAuthors(item),

                description:
                    getSafeHtml(
                        item,
                        'description'
                    ),

                image:
                    getItunesAttribute(
                        item,
                        'image',
                        'href'
                    ),

                audioUrl:
                    enclosure
                        ?.getAttribute('url') ?? '',

                audioLength:
                    Number(
                        enclosure
                            ?.getAttribute(
                                'length'
                            ) ?? 0
                    ),

                audioType:
                    enclosure
                        ?.getAttribute('type') ?? '',

                guid:
                    getText(item, 'guid'),

                pubDate:
                    getText(
                        item,
                        'pubDate'
                    ),

                duration:
                    getItunesText(
                        item,
                        'duration'
                    )
            }
        })

    return {
        title:
            getText(channel, 'title'),

        link:
            getText(channel, 'link'),

        language:
            getText(
                channel,
                'language'
            ),

        explicit:
            getItunesText(
                channel,
                'explicit'
            ).toLowerCase() === 'true',

        author:
            getItunesAuthors(channel),

        description:
            getSafeHtml(
                channel,
                'description'
            ),

        ownerName,

        ownerEmail,

        image:
            getItunesAttribute(
                channel,
                'image',
                'href'
            ),

        category,

        subcategory,

        episodes
    }
}

export default parsePodcastXml
