export interface Source {
  src: string;
  type: string;
}

export interface Sources {
  id: string;
  source: Array<Source>;
}