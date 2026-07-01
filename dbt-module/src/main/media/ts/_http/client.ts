export class HttpClient {

  private static defaultHeaders: { [key: string]: string } = { "Accept": "application/json" };


  private setRequestHeaders(httpRequest: XMLHttpRequest, headers: { [key: string]: string } = {}) {
    const h = { ...HttpClient.defaultHeaders, ...headers };

    for (const name in h) {
      if (h.hasOwnProperty(name)) {
        httpRequest.setRequestHeader(name, h[name]);
      }
    }
  }

  request(url: string, method: string, complete: (data: any) => void, error: (httpRequest: XMLHttpRequest) => void = null) {
    const httpRequest = new XMLHttpRequest();

    if (!httpRequest) {
      return false;
    }

    httpRequest.onreadystatechange = function () {
      if (httpRequest.readyState === XMLHttpRequest.DONE) {
        if (httpRequest.status === 200) {
          complete(JSON.parse(httpRequest.responseText) || httpRequest.responseText);
        } else {
          if (error) {
            error(httpRequest);
          } else {
            throw new Error("There was a problem with the request.");
          }
        }
      }
    };

    httpRequest.open(method, url);
    this.setRequestHeaders(httpRequest);
    httpRequest.send();
  }

  get(url: string, complete: (data: any) => void, error: (httpRequest: XMLHttpRequest) => void = null) {
    this.request(url, "GET", complete, error);
  }

}