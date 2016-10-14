module rc {
    export class Entry {
        /**
         * The Entry Id 
         */
        id: string;

        /**
         * The PPN
         */
        ppn: string;

        /**
         * The EPN
         */
        epn: string;

        /**
         * The title
         */
        title: string;

        /**
         * Parse Entry from given Element.
         * 
         * @param elm the Entry element to parse
         */
        public static parse(elm: Element): Entry {
            var record: NodeList = elm.getElementsByTagName("opcrecord");

            if (record.length == 0)
                return null;

            var picaRecord: NodeList = (<Element>record.item(0)).getElementsByTagNameNS("http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd", "record");
            if (picaRecord.length == 0)
                return null;

            var entry: Entry = new Entry();

            entry.id = elm.getAttribute("id");
            entry.epn = (<Element>record.item(0)).getAttribute("epn");
            entry.ppn = (<Element>picaRecord.item(0)).getAttribute("ppn");

            var fields: NodeList = (<Element>picaRecord.item(0)).getElementsByTagNameNS("http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd", "field");
            for (var i = 0; i < fields.length; i++) {
                if (fields.item(i).attributes.getNamedItem("tag").value == "021A" || fields.item(i).attributes.getNamedItem("tag").value == "036C") {
                    var subfields = fields.item(i).childNodes;
                    for (var j = 0; j < subfields.length; j++) {
                        if (subfields.item(j).nodeName == "pica:subfield" && subfields.item(j).attributes.getNamedItem("code").value == "a") {
                            entry.title = subfields.item(j).textContent.toUnicode();
                            break;
                        }
                    }
                }

                if (core.Utils.isValid(entry.title)) break;
            }

            return entry;
        }

        /**
         * String presentation of the Entry object.
         * 
         * @return the string 
         */
        toString(): string {
            return "Entry [id=" + this.id + ", ppn=" + this.ppn + ", epn=" + this.epn + ", title=" + this.title + "]";
        }
    }
}