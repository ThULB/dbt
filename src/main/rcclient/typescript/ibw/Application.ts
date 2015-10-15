/// <reference path="../definitions/XPCOM.d.ts" />
/// <reference path="../definitions/WinIBW.d.ts" />

/// <reference path="../core/Utils.ts" />
/// <reference path="Error.ts" />

module ibw {
    export class UserInfo {
        uid: string;
        name: string;
        libId: string;
        description: string;

        toString(): string {
            return "UserInfo: [uid={0}, name={1}, libId={2}, description={3}]".format(this.uid, this.name, this.libId, this.description);
        }
    }

    export class Tag {
        category: string;
        content: string;

        public static parseFrom(from: any): Tag {
            if (typeof from === "object" && from.hasOwnProperty("length")) {
                var tag: Tag = new Tag();

                tag.category = from[0];
                tag.content = from[1];

                return tag;
            } else if (typeof from === "string") {
                var t: string = (<string>from).trim();
                var s: Array<string> = t.match(/(\d{4})\s(.*)/);

                if (s != null && s.length == 3) {
                    return Tag.parseFrom(Array.prototype.slice.call(s, 1));
                }
            }

            return null;
        }

        toString(): string {
            return "Tag: [category={0}, content={1}]".format(this.category, this.content);
        }
    }

    export class Copy {
        num: number;
        type: string;
        epn: string;

        location: string;
        shelfmark: string;
        loanIndicator: string;
        isBundle: boolean;

        barcode: string;
        comment: string;

        public static parseFrom(from: string): Copy {
            var lines: Array<string> = from.split("\n");

            var copy: Copy = new Copy();

            for (var i in lines) {
                var tag: Tag = Tag.parseFrom(lines[i]);
                if (tag == null) continue;

                if (tag.category.startsWith("70")) {
                    copy.num = parseInt(tag.category) - 7000;
                    copy.type = tag.content.match(/(.*) : (.*)/)[2];
                } else {
                    switch (tag.category) {
                        case "4802":
                            copy.comment = tag.content;
                            break;
                        case "7100":
                            var m: Array<string> = tag.content.match(/!(.*)!(.*) @ (.*)/);
                            copy.location = m[1];
                            copy.shelfmark = m[2];

                            var exp: RegExp = new RegExp("/(.*) \\ c/");
                            if (exp.test(m[3])) {
                                copy.loanIndicator = m[3].match(exp)[1];
                                copy.isBundle = true;
                            } else {
                                copy.loanIndicator = m[3];
                                copy.isBundle = false;
                            }
                            break;
                        case "7800":
                            copy.epn = tag.content;
                            break;
                        case "8200":
                            copy.barcode = tag.content;
                            break;
                    }
                }
            }

            return copy;
        }

        toString(): string {
            return "Copy: [num={0}, type={1}, epn={2}, isBundle={3}]".format(
                this.num, this.type, this.epn, this.isBundle
            );
        }
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

    export function getCopys(title?: string): Array<Copy> {
        if (!core.Utils.isValid(title) && core.Utils.isValid(application.activeWindow.title))
            throw new ibw.Error(ibw.ErrorCode.ACTIVE_TTILE);

        title = title || application.activeWindow.copyTitle();

        var copys: Array<Copy> = new Array<Copy>();

        var c: Array<string> = title.match(/\n(70.+)\s(.*)\n/g);
        for (var i = 0; i < c.length; i++) {
            var copyTag: Tag = Tag.parseFrom(c[i]);
            if (copyTag == null) continue;

            var sOffset = title.indexOf(c[i].trim());
            var eOffset = (i < c.length - 1 ? title.indexOf(c[i + 1].trim()) : title.length);

            var copy: Copy = Copy.parseFrom(title.substring(sOffset, eOffset));
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
