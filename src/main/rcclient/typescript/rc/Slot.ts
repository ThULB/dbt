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
        /**
         *  The Slot Id
         */
        id: string;
        
        /**
         * The Slot status
         */
        status: Status;
        
        /**
         *  The eOnly state
         */
        eOnly: boolean;
        
        /**
         * The Slot title
         */
        title: string;
        
        /**
         * The Slot entries (only OPCRecord)
         */
        entries: Array<Entry>;

        /**
         * Parse Slot from given Element.
         * 
         * @param elm the Slot element to parse
         */
        public static parse(elm: Element): Slot {
            var slot: Slot = new Slot();

            slot.id = elm.getAttribute("id");
            slot.status = Status[elm.getAttribute("status").toUpperCase()];
            slot.eOnly = (elm.hasAttribute("onlineOnly") ? core.Utils.toBoolean(elm.getAttribute("onlineOnly")) : false);

            slot.title = (<Text>elm.getElementsByTagName("title").item(0).firstChild).data;

            slot.entries = new Array<Entry>();
            var entries = elm.getElementsByTagName("entry");
            for (var i = 0; i < entries.length; i++) {
                var entry: Entry = Entry.parse(<Element>entries.item(i));
                (entry != null) && slot.entries.push(entry);
            }

            return slot;
        }

        public getEntryForPPN(ppn: string): Entry {
            for (var i in this.entries) {
                if (this.entries[i].ppn == ppn)
                    return this.entries[i];
            }
            
            return null;
        }

        /**
         * String presentation of the Slot object.
         * 
         * @return the string 
         */
        toString(): string {
            return "Slot [id=" + this.id + ", status=" + this.status + ", eOnly=" + this.eOnly + ", title=" + this.title + ", numEntries=" + this.entries.length + "]";
        }
    }
}
