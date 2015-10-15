/// <reference path="../definitions/WinIBW.d.ts" />

module ibw {
    export class Error implements IError {
        name: string = "IBWError";
        message: string;
        fileName: string;
        lineNumber: number;
        columnNumber: number;

        errorCode: number;
        attributes: Array<any>;

        constructor(errorCode: number, ...argArray: Array<any>) {
            this.errorCode = errorCode;
            this.attributes = argArray;

            this.message = this.toString();
        }

        toString(): string {
            return core.Locale.getInstance().getString("IBW.errorCode." + this.errorCode, this.attributes);
        }
    }
}