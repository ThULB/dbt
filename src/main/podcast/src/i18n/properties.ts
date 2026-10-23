export type Messages = Record<string, string>
export type MessageTree = {
    [key: string]: string | MessageTree
}

function unescapeProperty(value: string): string {
    let result = ''

    for (let i = 0; i < value.length; i++) {
        if (value[i] !== '\\') {
            result += value[i]
            continue
        }

        const next = value[++i]

        switch (next) {
            case 't':
                result += '\t'
                break
            case 'r':
                result += '\r'
                break
            case 'n':
                result += '\n'
                break
            case 'f':
                result += '\f'
                break
            case 'u': {
                const code = value.slice(i + 1, i + 5)

                if (/^[0-9a-fA-F]{4}$/.test(code)) {
                    result += String.fromCharCode(parseInt(code, 16))
                    i += 4
                } else {
                    result += 'u'
                }

                break
            }
            default:
                result += next ?? '\\'
        }
    }

    return result
}

function getLogicalLines(source: string): string[] {
    const result: string[] = []
    let current = ''

    for (const physicalLine of source.split(/\r?\n/)) {
        const line = current
            ? physicalLine.replace(/^\s+/, '')
            : physicalLine

        current += line

        const trailingBackslashes =
            current.match(/\\+$/)?.[0].length ?? 0

        if (trailingBackslashes % 2 === 1) {
            current = current.slice(0, -1)
        } else {
            result.push(current)
            current = ''
        }
    }

    if (current) {
        result.push(current)
    }

    return result
}

export function parseProperties(source: string): Messages {
    const messages: Messages = {}

    for (const line of getLogicalLines(source)) {
        const trimmed = line.trimStart()

        if (!trimmed || trimmed.startsWith('#') || trimmed.startsWith('!')) {
            continue
        }

        let separator = -1
        let escaped = false

        for (let i = 0; i < trimmed.length; i++) {
            const character = trimmed[i]

            if (character === '\\') {
                escaped = !escaped
                continue
            }

            if (
                !escaped &&
                (character === '=' || character === ':' || /\s/.test(character))
            ) {
                separator = i
                break
            }

            escaped = false
        }

        if (separator === -1) {
            messages[unescapeProperty(trimmed)] = ''
            continue
        }

        const rawKey = trimmed.slice(0, separator)
        let valueStart = separator

        while (/\s/.test(trimmed[valueStart] ?? '')) {
            valueStart++
        }

        if (trimmed[valueStart] === '=' || trimmed[valueStart] === ':') {
            valueStart++
        }

        while (/\s/.test(trimmed[valueStart] ?? '')) {
            valueStart++
        }

        messages[unescapeProperty(rawKey)] =
            unescapeProperty(trimmed.slice(valueStart))
    }

    return messages
}

export function parseNamespacedProperties(
    source: string,
    namespace: string
): MessageTree {
    const messages = parseProperties(source)
    const result: MessageTree = {}
    const prefix = `${namespace}.`

    for (const [key, value] of Object.entries(messages)) {
        if (!key.startsWith(prefix)) {
            continue
        }

        const path = key.split('.')
        let target = result

        for (let index = 0; index < path.length - 1; index++) {
            const segment = path[index]
            const existing = target[segment]

            if (typeof existing === 'string') {
                throw new Error(
                    `Conflicting translation key: ${path.slice(0, index + 1).join('.')}`
                )
            }

            if (!existing) {
                target[segment] = {}
            }

            target = target[segment] as MessageTree
        }

        target[path[path.length - 1]] = value
    }

    return result
}
