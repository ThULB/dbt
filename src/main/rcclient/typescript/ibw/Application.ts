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
     * Returns the activeWindow interface.
     * 
     * @return the activeWindow interface
     */
    export function getActiveWindow(): IActiveWindow {
        return application.activeWindow;
    }

    /**
     * Returns the user informations.
     * 
     * @return the user information
     */
    export function getUserInfo(): UserInfo {
        if (core.Utils.isValid(application.activeWindow.title))
            throw new ibw.Error(ibw.ErrorCode.ACTIVE_TTILE);

        application.activeWindow.command("s ben", false);
        if (application.activeWindow.status.toUpperCase() == "OK") {
            application.activeWindow.simulateIBWKey("F7");
            if (application.activeWindow.status.toUpperCase() == "OK") {
                var userInfo: UserInfo = new UserInfo();

                userInfo.uid = application.activeWindow.getVariable("P3VU1");
                userInfo.name = application.activeWindow.getVariable("P3VU6");
                userInfo.libId = application.activeWindow.getVariable("P3VU3");
                userInfo.description = application.activeWindow.getVariable("P3VUS");

                application.activeWindow.simulateIBWKey("FE");
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
     * Shows error message from given error object.
     * 
     * @param error the error object
     */
    export function showError(error: IError) {
        var msg: string = core.Locale.getInstance().getString("error.message", error.name, error.message);
        core.Utils.isValid(error.fileName) && (msg += " " + core.Locale.getInstance().getString("error.message.file", error.fileName));
        core.Utils.isValid(error.lineNumber) && (msg += " " + core.Locale.getInstance().getString("error.message.line", error.lineNumber));

        // TODO enable messageBox isn't on WINE
        // application.messageBox(error.name, msg, "error-icon");
        alert(msg);
    }
}
