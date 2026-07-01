import { Player } from "./_player/player";

(<any>window).mediaPlayer = function (url: string, playerId: string | null, options: { [key: string]: string }) {
  return new Player(url, playerId, false, options);
};