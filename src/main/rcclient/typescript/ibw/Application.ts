/// <reference path="../definitions/XPCOM.d.ts" />
/// <reference path="../definitions/WinIBW.d.ts" />

/// <reference path="../core/Utils.ts" />
/// <reference path="Copy.ts" />
/// <reference path="Error.ts" />
/// <reference path="Tag.ts" />
/// <reference path="UserInfo.ts" />

module ibw {
    var application: IApplication = Components.classes["@oclcpica.nl/kitabapplication;1"].getService(Components.interfaces.IApplication);

    /**
     * Runs command on activeWindow and returns <code>true</code> on success.
     * 
     * @param cmd the comamnd to run
     * @return <code>true</code> on success otherwise <code>false</code>
     */
    export function command(cmd: string): boolean {
        if (core.Utils.isValid(application.activeWindow.title))
            throw new ibw.Error(ibw.ErrorCode.ACTIVE_TTILE);

        application.activeWindow.command(cmd, false);
        return application.activeWindow.status.toUpperCase() == "OK";
    }

    /**
     * Simulates a keypress of given key.
     * 
     * @param key the key to press
     * @return <code>true</code> on success otherwise <code>false</code>
     */
    export function simulateKey(key: string): boolean {
        application.activeWindow.simulateIBWKey(key);
        return application.activeWindow.status.toUpperCase() == "OK";
    }
    
    /**
     * Returns the activeWindow interface.
     * 
     * @return the activeWindow interface
     */
    export function getActiveWindow(): IActiveWindow {
        return application.activeWindow;
    }
    
    /**
     * Returns the title from activeWindow.
     * 
     * @return the active title
     */
    export function getTitle(): IEditControl {
        return application.activeWindow.title;
    }

    /**
     * Returns the user informations.
     * 
     * @return the user information
     */
    export function getUserInfo(): UserInfo {
        if (ibw.command("s ben")) {
            if (ibw.simulateKey("F7")) {
                var userInfo: UserInfo = new UserInfo();

                userInfo.uid = application.activeWindow.getVariable("P3VU1");
                userInfo.name = application.activeWindow.getVariable("P3VU6");
                userInfo.libId = application.activeWindow.getVariable("P3VU3");
                userInfo.description = application.activeWindow.getVariable("P3VUS");

                ibw.simulateKey("FE");
                return userInfo;
            }
        }

        throw new ibw.Error(ibw.ErrorCode.NO_LOGIN);
    }

    /**
     * Returns a list of parsed copys.
     * 
     * @return a list of copys
     */
    export function getCopys(title?: string): Array<Copy> {
        if (!core.Utils.isValid(title) && core.Utils.isValid(application.activeWindow.title))
            throw new ibw.Error(ibw.ErrorCode.ACTIVE_TTILE);

        title = title || application.activeWindow.copyTitle();

        var copys: Array<Copy> = new Array<Copy>();

        var c: Array<string> = title.match(/\n(70.+)\s(.*)\n/g);
        for (var i = 0; i < c.length; i++) {
            var copyTag: Tag = Tag.parse(c[i]);
            if (copyTag == null) continue;

            var sOffset = title.indexOf(c[i].trim());
            var eOffset = (i < c.length - 1 ? title.indexOf(c[i + 1].trim()) : title.length);

            var copy: Copy = Copy.parse(title.substring(sOffset, eOffset));
            copys.push(copy);
        }

        return copys;
    }

    /**
     * Search the tag in the given title and returns the category content.
     * 
     * @param title the title copy
     * @param tag the tag to search
     * @param occurrence the occurrence of the tag. by default the first
     * @return the category content
     */
    export function findTag(title: string, tag: string, occurrence: number = 0) {
        var lines: Array<string> = title.split("\n");
        var occ: number = 0;

        for (var i in lines) {
            if (occ > occurrence) break;

            var exp: RegExp = new RegExp(tag + "\\s(.*)");
            if (lines[i].match(exp)) {
                if (occurrence == occ) {
                    var match = exp.exec(lines[i]);
                    return match[1];
                }
                occ++;
            }
        }

        return null;
    }

    /**
     * Shows error message from given error object.
     * 
     * @param error the error object
     */
    export function showError(error: IError) {
        var msg: string = core.Locale.getInstance().getString("error.message", error.name, error.message);
        core.Utils.isValid(error.fileName) && (msg += " " + core.Locale.getInstance().getString("error.message.file", error.fileName));
        core.Utils.isValid(error.lineNumber) && (msg += " " + core.Locale.getInstance().getString("error.message.line", error.lineNumber));

        // TODO on WINE add *mfc71, *msvcp71 and *msvcr71 to use as native, before buildin
        application.messageBox(error.name, msg, "error-icon");
    }
}
