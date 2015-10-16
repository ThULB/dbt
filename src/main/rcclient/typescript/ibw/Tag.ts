module ibw {
    export class Tag {
        category: string;
        content: string;

        public static parse(from: any): Tag {
            if (typeof from === "object" && from.hasOwnProperty("length")) {
                var tag: Tag = new Tag();

                tag.category = from[0];
                tag.content = from[1];

                return tag;
            } else if (typeof from === "string") {
                var t: string = (<string>from).trim();
                var s: Array<string> = t.match(/(\d{4})\s(.*)/);

                if (s != null && s.length == 3) {
                    return Tag.parse(Array.prototype.slice.call(s, 1));
                }
            }

            return null;
        }

        toString(): string {
            return "Tag: [category={0}, content={1}]".format(this.category, this.content);
        }
    }
}