const configuredApplicationBase = document
    .querySelector<HTMLMetaElement>(
        'meta[name="podcast-application-base-url"]'
    )
    ?.content

const normalizedApplicationBase =
    configuredApplicationBase?.endsWith('/')
        ? configuredApplicationBase
        : `${configuredApplicationBase || ''}/`

const applicationBaseUrl = new URL(
    normalizedApplicationBase,
    window.location.origin
)

export default function applicationUrl(path: string): string {
    return new URL(
        path.replace(/^\/+/, ''),
        applicationBaseUrl
    ).toString()
}
