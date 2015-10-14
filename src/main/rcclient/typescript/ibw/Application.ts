/// <reference path="../definitions/XPCOM.d.ts" />
/// <reference path="../definitions/WinIBW.d.ts" />

/// <reference path="Error.ts" />

module ibw {
    export class UserInfo {
        uid: string;
        name: string;
        libId: string;
        description: string;
    }

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

    export function showError(error: Error) {
        // TODO enable messageBox isn't on WINE
        // application.messageBox(error.name, error.message, "error-icon");
        alert(error);
    }
}
