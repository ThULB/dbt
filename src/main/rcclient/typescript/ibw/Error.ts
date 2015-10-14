module ibw {
    export class Error implements Error {
        name: string = "IBWError";
        message: string;

        errorCode: number;
        attributes: Array<any>;

        constructor(errorCode: number, ...argArray: Array<any>) {
            this.errorCode = errorCode;
            this.attributes = argArray;

            this.message = this.toLocalizedString();
        }

        toLocalizedString(): string {
            return core.Locale.getInstance().getString("IBW.errorCode." + this.errorCode, this.attributes)
        }

        toString(): string {
            return this.name + ": " + this.message;
        }
    }
}