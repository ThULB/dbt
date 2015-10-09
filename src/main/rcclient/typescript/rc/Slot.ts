module rc {
    export enum Status {
        /**
         * Stands for an free and unused slot.
         */
        FREE,

        /**
         * Stands for an reserved slot.
         */
        RESERVED,

        /**
         * Stands for a active slot.
         */
        ACTIVE,

        /**
         * Stands for a archived slot for late reactivation.
         */
        ARCHIVED,

        /**
         * Stands for a pending slot status change.
         */
        PENDING
    }

    export class Slot {
        id: string;
        status: Status;
        eOnly: boolean;
        title: string;
        entries: Array<Entry>;

        public static parseSlot(elm: Element): Slot {
            var slot: Slot = new Slot();

            slot.id = elm.getAttribute("id");
            slot.status = Status[elm.getAttribute("status").toUpperCase()];
            slot.eOnly = (elm.hasAttribute("onlineOnly") ? elm.getAttribute("onlineOnly") === "true" : false);

            slot.title = (<Text>elm.getElementsByTagName("title").item(0).firstChild).data;

            slot.entries = new Array<Entry>();
            var entries = elm.getElementsByTagName("entry");
            for (var i = 0; i < entries.length; i++) {
                var entry: Entry = Entry.parseEntry(<Element>entries.item(i));
                (entry != null) && slot.entries.push(entry);
            }

            return slot;
        }

        toString(): string {
            return "Slot [id=" + this.id + ", status=" + this.status + ", eOnly=" + this.eOnly + ", title=" + this.title + ", numEntries=" + this.entries.length + "]";
        }
    }
}
