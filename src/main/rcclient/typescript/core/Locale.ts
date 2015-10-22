/// <reference path="../definitions/XPCOM.d.ts" />

module core {
    export class Locale {
        private static INSTANCE: Locale;

        private stringBundle: nsIStringBundle;

        public static getInstance(propFile?: string): Locale {
            if (Locale.INSTANCE == null) {
                if (propFile == null)
                    throw "Couldn't init locale service.";
                else {
                    Locale.INSTANCE = new Locale(propFile);
                }
            }

            return Locale.INSTANCE;
        }

        constructor(propFile: string) {
            this.stringBundle = Components.classes["@mozilla.org/intl/stringbundle;1"].getService(Components.interfaces.nsIStringBundleService).createBundle(
                propFile);
        }

        getString(aProperty: string, ...aArgs: Array<any>): string {
            try {
                if (aArgs !== null && aArgs.length > 0) {
                    return this.stringBundle.GetStringFromName(aProperty).format(...aArgs);
                } else {
                    return this.stringBundle.GetStringFromName(aProperty);
                }
            } catch (ex) {
                return "???" + aProperty + "???";
            }
        }

        getCurrentLocale(): string {
            return Components.classes["@mozilla.org/chrome/chrome-registry;1"].getService(Components.interfaces.nsIXULChromeRegistry).getSelectedLocale("global");
        }
    }
}