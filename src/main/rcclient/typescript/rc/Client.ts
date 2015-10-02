/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static SLOT_LIST_LOADED = "SLOT_LIST_LOADED";

        private mURL: string;
        private mSlots: Array<Slot>;

        constructor(aURL: string) {
            super();

            this.mURL = aURL;
        }

        loadSlots(): void {
            console.log("Loading slots...")
            var request: net.HTTPRequest = new net.HTTPRequest(this.mURL + "/rc?XSL.Style=xml");
            request.execute(this, this.onSlotsComplete);
        }

        getSlots(): Array<Slot> {
            return this.mSlots;
        }

        private onSlotsComplete(aData: string, aSuccess: boolean): void {
            if (!aSuccess) return;

            console.log("..loading done.");
            this.mSlots = new Array<Slot>();
            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            if (doc != null) {
                var slots: NodeList = doc.getElementsByTagName("slot");
                for (var c = 0; c < slots.length; c++) {
                    var slot: Slot = Slot.parseSlot(<Element>slots.item(c));
                    this.mSlots.push(slot);
                }
            }

            this.dispatch(Client.SLOT_LIST_LOADED);
        }
    }
}