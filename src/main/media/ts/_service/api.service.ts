import { HttpClient } from "../_http/client";
import { Sources } from "../_datamodel/sources";

export class ApiService {

  private http: HttpClient;

  baseURL: string;

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
        response.source.forEach((thumb) => sources.push(this.baseURL + "rsc/media/thumb/" + [id, thumb.src].join("/")));
      }

      callbackFn(sources);
    }, (httpRequest) => {
      throw new Error("Thumbnail request failed with error: " + httpRequest.status)
    });
  }
}