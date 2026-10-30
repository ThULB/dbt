const LANGUAGE_LINK_SELECTOR = '.language-select a.language';
const SUPPORTED_LANGUAGES = ['de', 'en'] as const;

function languageFromLink(link: HTMLAnchorElement): string | null {
  return SUPPORTED_LANGUAGES.find((language) => link.classList.contains(language)) ?? null;
}

export function syncLanguageLinks(): void {
  document.querySelectorAll<HTMLAnchorElement>(LANGUAGE_LINK_SELECTOR).forEach((link) => {
    const language = languageFromLink(link);

    if (!language) {
      return;
    }

    const url = new URL(window.location.href);
    url.searchParams.set('lang', language);
    link.href = url.href;
  });
}
