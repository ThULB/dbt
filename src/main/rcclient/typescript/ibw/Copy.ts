/// <reference path="Tag.ts" />

module ibw {
    export class CopyBackup {
        slotId: string;
        location: string;
        shelfmark: string;
        loanIndicator: string;
        isBundle: boolean;
        bundleEPN: string;
        migrate: boolean;

        public static parse(from: string): CopyBackup {
            var m: Array<string> = from.match(/(.*) (SSTold|RC) (.*)/);

            if (m && m.length == 4) {
                var backup: CopyBackup = new CopyBackup();
                backup.slotId = m[1];
                backup.migrate = m[2] === "SSTold";

                if (backup.migrate)
                    backup.slotId = backup.slotId.replaceAll(":", ".");

                var tmp = m[3];
                var exp: RegExp = /(.*) \\ c:(.*)/;
                if (exp.test(tmp)) {
                    var p: Array<string> = tmp.match(exp);
                    backup.isBundle = true;
                    backup.bundleEPN = p[2];
                    tmp = p[1];
                } else {
                    backup.isBundle = false;
                }

                var lsl = tmp.match(/!(.*)!(.*) @ (.*)/);
                backup.location = lsl[1];
                backup.shelfmark = lsl[2];
                backup.loanIndicator = lsl[3];

                return backup;
            }

            return null;
        }

        toString(): string {
            return "CopyBackup: [slotId={0}, location={1}, shelfmark={2}, indicator={3}, isBundle={4}, bundleEPN={5}, migrate={6}]".format(
                this.slotId, this.location, this.shelfmark, this.loanIndicator, this.isBundle, this.bundleEPN, this.migrate
            );
        }
    }

    export class Copy {
        num: number;
        type: string;
        ppn: string;
        epn: string;

        location: string;
        shelfmark: string;
        loanIndicator: string;
        isBundle: boolean;

        barcode: string;

        backup: Array<CopyBackup>;

        public static parse(from: string): Copy {
            var lines: Array<string> = from.split("\n");

            var copy: Copy = new Copy();
            var has7100: boolean = false;

            try {
                for (var i in lines) {
                    var tag: Tag = Tag.parse(lines[i]);
                    if (tag == null) continue;

                    if (tag.category.startsWith("70")) {
                        copy.num = parseInt(tag.category) - 7000;
                        copy.type = tag.content.match(/(.*) : (.*)/)[2];
                    } else {
                        switch (tag.category) {
                            case "4802":
                                var backup: CopyBackup = CopyBackup.parse(tag.content);
                                if (core.Utils.isValid(backup)) {
                                    (!core.Utils.isValid(copy.backup)) && (copy.backup = new Array<CopyBackup>());
                                    copy.backup.push(backup);
                                }
                                break;
                            case "7100":
                                has7100 = true;
                                var m: Array<string> = tag.content.match(/!(.*)!(.*) @ (.*)/);
                                copy.location = m[1];
                                copy.shelfmark = m[2];

                                var exp: RegExp = /(.*) \\ c/;

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
            } catch (e) {
                return null;
            }

            return has7100 ? copy : null;
        }

        hasRegistered(): boolean {
            return core.Utils.isValid(this.backup) && this.backup.length != 0;
        }

        getBackup(slotId: string): CopyBackup {
            if (core.Utils.isValid(this.backup)) {
                for (var i in this.backup) {
                    if (slotId == this.backup[i].slotId)
                        return this.backup[i];
                }
            }

            return null;
        }

        toString(): string {
            return "Copy: [num={0}, type={1}, ppn={2}, epn={3}, isBundle={4}, location={5}, shelfmark={6}, indicator={7}]".format(
                this.num, this.type, this.ppn, this.epn, this.isBundle, this.location, this.shelfmark, this.loanIndicator
            );
        }
    }
}