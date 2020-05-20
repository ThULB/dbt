export enum ShareType {
  EMBED_CODE = "embed",
  EMBED_URL = "embedUrl",
  DIRECT = "direct"
}

export interface ShareOptions {
  width?: number;
  height?: number;
}

export interface ShareTranslation {
  title: string;
  embedCode: string;
  directLink: string;
  copy: string;
  copied: string;
}

export interface ShareTranslations {
  [lang: string]: ShareTranslation;
}

export const DefaultShareTranslation = {
  "de": {
    title: "Teilen",
    embedCode: "HTML Code",
    directLink: "Direkt Link",
    copy: "Kopieren",
    copied: "Kopiert"
  },
  "en": {
    title: "Share",
    embedCode: "HTML Code",
    directLink: "Direct Link",
    copy: "Copy",
    copied: "Copied"
  }
};
