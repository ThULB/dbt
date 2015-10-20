/// <reference path="Tag.ts" />

module ibw {
    export class Copys {
        private copys: Array<Copy>;

        constructor(copys: Array<Copy>) {
            this.copys = copys;
        }

        length(): number {
            return this.copys.length;
        }

        item(index: number): Copy {
            return this.copys[index];
        }

        hasRegistered(): boolean {
            for (var i in this.copys) {
                if (core.Utils.isValid(this.copys[i].backup))
                    return true;
            }
            return false;
        }
    }

    export class CopyBackup {
        slotId: string;
        location: string;
        shelfmark: string;
        loanIndicator: string;
        isBundle: boolean;
        bundleEPN: string;

        public static parse(from: string): CopyBackup {
            var m: Array<string> = from.match(/(.*) SSTold (.*)/);

            if (m && m.length == 3) {
                var backup: CopyBackup = new CopyBackup();
                backup.slotId = m[1];

                var tmp = m[2];
                var exp: RegExp = /(.*) \\ c:(.*)/;
                if (exp.test(tmp)) {
                    var p: Array<string> = tmp.match(exp);
                    backup.isBundle = true;
                    backup.bundleEPN = p[2];
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
            return "CopyBackup: [slotId={0}, location={1}, shelfmark={2}, indicator={3}, isBundle={4}, bundleEPN={5}]".format(
                this.slotId, this.location, this.shelfmark, this.loanIndicator, this.isBundle, this.bundleEPN
            );
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

        backup: CopyBackup;

        public static parse(from: string): Copy {
            var lines: Array<string> = from.split("\n");

            var copy: Copy = new Copy();

            for (var i in lines) {
                var tag: Tag = Tag.parse(lines[i]);
                if (tag == null) continue;

                if (tag.category.startsWith("70")) {
                    copy.num = parseInt(tag.category) - 7000;
                    copy.type = tag.content.match(/(.*) : (.*)/)[2];
                } else {
                    switch (tag.category) {
                        case "4802":
                            copy.backup = CopyBackup.parse(tag.content);
                            break;
                        case "7100":
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

            return copy;
        }

        toString(): string {
            return "Copy: [num={0}, type={1}, epn={2}, isBundle={3}]".format(
                this.num, this.type, this.epn, this.isBundle
            );
        }
    }
}