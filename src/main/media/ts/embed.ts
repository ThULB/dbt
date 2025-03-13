// @ts-ignore
import videojs from "video.js";

import { Player } from "./_player/player";

export class EmbeddedPlayer {

    player: Player;

    baseURL: string;

    playerId: string;

    constructor(baseURL: string, playerId: string = "dbt-embeded-player") {
        this.baseURL = baseURL && baseURL + (baseURL.lastIndexOf("/") === baseURL.length - 1 ? "" : "/");
        this.playerId = playerId;

        this.init();
    }

    private init() {
        this.player = new Player(this.baseURL, this.playerId, true, this.parseLocation());
    }

    private parseLocation(): { [key: string]: string } {
        const vs: { [key: string]: string } = {};
        const up = location.href.split("?");

        if (up) {
            vs.id = up[0].match(new RegExp("^.*\\/(.*)$"))[1];

            const qs = up[1];
            if (qs) {
                const ps = qs.split("&");
                for (let i = 0; i < ps.length; i++) {
                    const p = ps[i].substring(0, ps[i].indexOf("="));
                    const v = ps[i].substring(ps[i].indexOf("=") + 1);
                    if (p != v) {
                        vs[p] = decodeURIComponent(v);
                    }
                }
            }
        }

        return vs;
    }

}

(<any>window).videojs = videojs;
(<any>window).embeddedPlayer = function (url: string, playerId: string | null) {
    return new EmbeddedPlayer(url, playerId);
};
