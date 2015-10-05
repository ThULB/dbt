/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static SLOT_LIST_LOADED = "SLOT_LIST_LOADED";
        public static SLOT_LOADED = "SLOT_LOADED";

        private mURL: string;
        private mUID: string;
        private mPWD: string;

        private mSlots: Array<Slot>;

        constructor(aURL: string) {
            super();

            this.mURL = aURL;
            this.mUID = "rcclient";
            this.mPWD = "G?4(<#EeTqn*Tu)F";
        }

        private login() {
            console.log("Logon on server...");
            var data = "action=login&real=local&uid=" + this.mUID + "&pwd=" + this.mPWD;
            var request: net.HTTPRequest = new net.HTTPRequest(this.mURL + "/servlets/MCRLoginServlet?action=login", net.HTTPRequest.METHOD_POST, data);
            request.execute(this, this.onLoginComplete);
        }

        loadSlots() {
            this.addListener(Client.LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.LOGIN_SUCCESS);

                console.log("Loading slots...")
                aRequest.setURL(this.mURL + "/rc?XSL.Style=xml");
                aRequest.execute(this, this.onSlotsComplete);
            });
            this.login();
        }

        loadSlot(id: string) {
            this.addListener(Client.LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.LOGIN_SUCCESS);

                console.log("Loading slot with id \"" + id + "\"...")
                aRequest.setURL(this.mURL + "/rc/" + id + "?XSL.Style=xml");
                aRequest.execute(this, this.onSlotComplete);
            });
            this.login();
        }

        getSlots(): Array<Slot> {
            return this.mSlots;
        }

        getSlot(id: string): Slot {
            for (var i in this.mSlots) {
                if (this.mSlots[i].id == id)
                    return this.mSlots[i];
            }

            return null;
        }

        setSlot(slot: Slot) {
            if (slot == null) return;

            for (var i in this.mSlots) {
                if (this.mSlots[i].id == slot.id) {
                    this.mSlots[i] = slot;
                    return;
                }
            }

            this.mSlots.push(slot);
        }

        private onLoginComplete(aRequest: net.HTTPRequest, aData: string, aSuccess: boolean) {
            this.dispatch(Client.LOGIN_SUCCESS, aRequest);
        }

        private onSlotsComplete(aClass: net.HTTPRequest, aData: string, aSuccess: boolean) {
            if (!aSuccess) return;

            console.log("..loading slots done.");
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

        private onSlotComplete(aClass: net.HTTPRequest, aData: string, aSuccess: boolean): void {
            if (!aSuccess) return;

            console.log("..loading slot done.");
            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            var slot: Slot = null;

            if (doc != null) {
                var elm: Element = <Element>doc.getElementsByTagName("slot").item(0);
                slot = Slot.parseSlot(elm);
                this.setSlot(slot);
            }

            this.dispatch(Client.SLOT_LOADED, slot);
        }
    }
}