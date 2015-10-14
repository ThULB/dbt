/// <reference path="../definitions/XPCOM.d.ts" />
/// <reference path="../definitions/WinIBW.d.ts" />

module ibw {
    export class UserInfo {
        uid: string;
        name: string;
        libId: string;
        description: string;
    }

    export function Application(): IApplication {
        return Components.classes["@oclcpica.nl/kitabapplication;1"].getService(Components.interfaces.IApplication)
    }

    export function getUserInfo(): UserInfo {
        var activeWindow: IActiveWindow = ibw.Application().activeWindow;

        if (core.Utils.isValid(activeWindow.title))
            throw new ibw.Error(ibw.ErrorCode.ACTIVE_TTILE);

        activeWindow.command("s ben", false);
        if (activeWindow.status.toUpperCase() == "OK") {
            activeWindow.simulateIBWKey("F7");
            if (activeWindow.status.toUpperCase() == "OK") {
                var userInfo: UserInfo = new UserInfo();

                userInfo.uid = activeWindow.getVariable("P3VU1");
                userInfo.name = activeWindow.getVariable("P3VU6");
                userInfo.libId = activeWindow.getVariable("P3VU3");
                userInfo.description = activeWindow.getVariable("P3VUS");

                activeWindow.simulateIBWKey("FE");
                return userInfo;
            }
        }

        throw new ibw.Error(ibw.ErrorCode.NO_LOGIN);
    }
}
