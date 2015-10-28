/// <reference path="../definitions/WinIBW.d.ts" />

module net {
    export class HTTPError implements IError {
        name: string = "HTTPError";
        message: string;
        fileName: string;
        lineNumber: number;
        columnNumber: number;

        errorCode: number;
        attributes: Array<any>;
        statusText: string;
        url: string;

        constructor(errorCode: number, statusText: string, url?: string) {
            this.errorCode = errorCode;
            this.statusText = statusText;
            this.url = url;

            this.message = this.toString();
        }

        toString(): string {
            // TODO refactor error message
            return "{0} {1}".format(this.errorCode, this.statusText);
        }
    }
}