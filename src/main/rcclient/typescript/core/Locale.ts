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

        getString(aProperty: string, aArgs?): string {
            try {
                if (aArgs) {
                    aArgs = Array.prototype.slice.call(arguments, 1);
                    return this.stringBundle.formatStringFromName(aProperty, aArgs, aArgs.length);
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