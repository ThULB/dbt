/// <reference path="definitions/WinIBW.d.ts" />
/// <reference path="definitions/XPCOM.d.ts" />

function RCClient() {
    var application: IApplication = Components.classes["@oclcpica.nl/kitabapplication;1"].getService(Components.interfaces.IApplication);
    
    var chromeFilePath: string = "chrome://IBWRCClient/";

    var ww = Components.classes["@mozilla.org/embedcomp/window-watcher;1"].getService(Components.interfaces.nsIWindowWatcher);

    if (!ww) {
        return false;
    }

    ww.openWindow(ww.activeWindow, chromeFilePath + "content/xul/IBWRCClient.xul", "", "chrome,dialog=yes, modal,centerscreen", null);
    
    application.activate();
}
