// src/i18n/index.ts
import { createI18n } from 'vue-i18n';

import deSource from '../../../resources/config/dbt/messages_de.properties?raw';
import enSource from '../../../resources/config/dbt/messages_en.properties?raw';

import { parseNamespacedProperties } from './properties.ts';

export type SupportedLocale = 'de' | 'en';

function supportedLocale(value: string | null): SupportedLocale | null {
  const language = value?.toLowerCase();

  if (language?.startsWith('en')) {
    return 'en';
  }

  if (language?.startsWith('de')) {
    return 'de';
  }

  return null;
}

function getCurrentLocale(): SupportedLocale {
  const queryLocale = supportedLocale(new URLSearchParams(window.location.search).get('lang'));
  const configuredLocale = supportedLocale(
    document.querySelector<HTMLMetaElement>('meta[name="podcast-locale"]')?.content ?? null,
  );

  return (
    queryLocale ??
    supportedLocale(window.currentLang ?? null) ??
    configuredLocale ??
    supportedLocale(document.documentElement.lang) ??
    'de'
  );
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: getCurrentLocale(),
  fallbackLocale: 'de',
  messages: {
    de: parseNamespacedProperties(deSource, 'podcast'),
    en: parseNamespacedProperties(enSource, 'podcast'),
  },
});
