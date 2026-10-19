import DOMPurify from 'dompurify'

import type {
    PodcastEpisode,
    PodcastPlaylist
} from '../types/types.ts'

const ITUNES_NS =
    'http://www.itunes.com/dtds/podcast-1.0.dtd'

function getText(
    element: Element,
    tagName: string
): string {
    return (
        element
            .getElementsByTagName(tagName)[0]
            ?.textContent
            ?.trim() ?? ''
    )
}

function getItunesText(
    element: Element,
    tagName: string
): string {
    return (
        element
            .getElementsByTagNameNS(
                ITUNES_NS,
                tagName
            )[0]
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
        element
            .getElementsByTagNameNS(
                ITUNES_NS,
                tagName
            )[0]
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

    const channel =
        xml.getElementsByTagName('channel')[0]

    if (!channel) {
        throw new Error(
            'RSS channel wurde nicht gefunden'
        )
    }

    const categories =
        channel.getElementsByTagNameNS(
            ITUNES_NS,
            'category'
        )

    const category =
        categories[0]
            ?.getAttribute('text') ?? ''

    const subcategory =
        categories[1]
            ?.getAttribute('text') ?? ''

    const owner =
        channel.getElementsByTagNameNS(
            ITUNES_NS,
            'owner'
        )[0]

    const ownerName =
        owner
            ? getItunesText(owner, 'name')
            : ''

    const ownerEmail =
        owner
            ? getItunesText(owner, 'email')
            : ''

    const items = Array.from(
        channel.getElementsByTagName('item')
    )

    const episodes: PodcastEpisode[] =
        items.map((item) => {
            const enclosure =
                item.getElementsByTagName(
                    'enclosure'
                )[0]

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
