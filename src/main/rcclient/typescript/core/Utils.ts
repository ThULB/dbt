/// <reference path="../definitions/XPCOM.d.ts" />

module core {
    export class Utils {
        public static isValid(aObj: any): boolean {
            return void 0 !== aObj && null !== aObj && "undefined" !== typeof aObj && !("null" === aObj && "string" == typeof aObj);
        }

        public static toBoolean(aObj: any): boolean {
            return (typeof aObj == "string" && aObj == "true") || aObj == true ? true : false;
        }

        public static generateUUID(): string {
            var d: number = new Date().getTime();
            var uuid: string = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
                var r: number = (d + Math.random() * 16) % 16 | 0;
                d = Math.floor(d / 16);
                return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
            });
            return uuid;
        }
        
        /**
         * Get the directory for given property.
         * 
         * @param aPropName - the property name
         * @param aInterface - the nsIFile interface
         */
        public static getSpecialDir(aPropName: string, aInterface?: nsISupports): any {
            try {
                var file = Components.classes["@mozilla.org/file/directory_service;1"].getService(Components.interfaces.nsIProperties).get(aPropName,
                    aInterface != undefined ? aInterface : Components.interfaces.nsILocalFile);

                return file;
            } catch (ex) {
                return null;
            }
        }
    }
}