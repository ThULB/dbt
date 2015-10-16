/// <reference path="Tag.ts" />

module ibw {
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
}