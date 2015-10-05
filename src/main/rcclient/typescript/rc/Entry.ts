module rc {
    export class Entry {
        id: string;
        ppn: string;
        epn: string;

        public static parseEntry(elm: Element): Entry {
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

            return entry;
        }

        toString(): string {
            return "Entry [id=" + this.id + ", ppn=" + this.ppn + ", epn=" + this.epn + "]";
        }
    }
}