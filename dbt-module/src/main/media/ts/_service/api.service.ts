import { HttpClient } from "../_http/client";
import { Sources } from "../_datamodel/sources";

export class ApiService {

    private http: HttpClient;

    baseURL: string;

    static filePathEncode(filePath: string): string {
        return filePath.split("/").map(encodeURIComponent).join("/");
    }

    constructor(baseURL: string) {
        this.baseURL = baseURL;
        this.http = new HttpClient();
    }

    getSources(id: string, callbackFn: (sources: Sources) => void) {
        this.http.get(this.baseURL + "rsc/media/sources/" + id, (response) => {
            callbackFn(response);
        }, (httpRequest) => {
            throw new Error("Source request failed with error code: " + httpRequest.status);
        });
    }

    getThumbs(id: string, callbackFn: (thumbs: Array<string>) => void) {
        this.http.get(this.baseURL + "rsc/media/thumbs/" + id, (response: Sources) => {
            const sources = [];

            if (response && response.source) {
                response.source.forEach((thumb) =>
                    sources.push(this.baseURL + "rsc/media/thumb/" + [id, ApiService.filePathEncode(thumb.src)].join("/"))
                );
            }

            callbackFn(sources);
        }, (httpRequest) => {
            throw new Error("Thumbnail request failed with error: " + httpRequest.status)
        });
    }

    getSubtitles(id: string, callbackFn: (subtitles: Sources) => void) {
        this.http.get(this.baseURL + "rsc/media/subtitles/" + id, (response: Sources) => {
            callbackFn(response);
        }, (httpRequest) => {
            throw new Error("Subtitle request failed with error: " + httpRequest.status)
        });
    }

    mediaSubtitleUrl(id: string, filename: string) {
        return `${this.baseURL}rsc/media/subtitle/${id}/${ApiService.filePathEncode(filename)}`;
    }
}
