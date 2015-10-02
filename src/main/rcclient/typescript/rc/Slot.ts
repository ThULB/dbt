module rc {
    export class Slot {
        id: string;
        status: string;
        eOnly: boolean;
        title: string;
        entries: Array<Entry>;

        public static parseSlot(elm: Element): Slot {
            var slot: Slot = new Slot();

            slot.id = elm.getAttribute("id");
            slot.status = elm.getAttribute("status");
            slot.eOnly = (elm.hasAttribute("onlineOnly") ? elm.getAttribute("onlineOnly") === "true" : false);

            slot.title = (<Text>elm.getElementsByTagName("title").item(0).firstChild).data;

            return slot;
        }

        toString(): string {
            return "Slot [id=" + this.id + ", " + this.status + ", eOnly=" + this.eOnly + ", title=" + this.title + "]";
        }
    }
}
