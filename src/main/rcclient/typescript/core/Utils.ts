module core {
    export class Utils {
        public static isValid(aObj: any): boolean {
            return void 0 !== aObj && null !== aObj && "undefined" !== typeof aObj && !("null" === aObj && "string" == typeof aObj);
        }

        public static toBoolean(aObj: any): boolean {
            return (typeof aObj == "string" && aObj == "true") || aObj == true ? true : false;
        }

        public static uniqueId(): string {
            var idstr = String.fromCharCode(Math.floor((Math.random() * 25) + 65));
            do {
                // between numbers and characters (48 is 0 and 90 is Z (42-48 = 90)
                var ascicode = Math.floor((Math.random() * 42) + 48);
                if (ascicode < 58 || ascicode > 64) {
                    // exclude all chars between : (58) and @ (64)
                    idstr += String.fromCharCode(ascicode);
                }
            } while (idstr.length < 16);

            return idstr;
        }
    }
}