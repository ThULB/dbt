/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static EVENT_SLOT_LIST_LOADED = "SLOT_LIST_LOADED";
        public static EVENT_SLOT_LOADED = "SLOT_LOADED";

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

        /**
         * Method to login to RC servlet.
         */
        private login() {
            var data = "action=login&real=local&uid=" + this.mUID + "&pwd=" + this.mPWD;
            var request: net.HTTPRequest = new net.HTTPRequest(this.mURL + "/servlets/MCRLoginServlet?action=login", net.HTTPRequest.METHOD_POST, data);
            request.execute(this, this.onLoginComplete);
        }

        /**
         * Loads Slots after successfully login.
         */
        loadSlots() {
            this.addListener(Client.EVENT_LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_LOGIN_SUCCESS);

                aRequest.setURL(this.mURL + "/rc?XSL.Style=xml");
                aRequest.execute(this, this.onSlotsComplete);
            });
            this.login();
        }

        /**
         * Loads Slot after successfully login.
         * 
         * @param id the Slot id to load
         */
        loadSlot(id: string) {
            this.addListener(Client.EVENT_LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_LOGIN_SUCCESS);

                aRequest.setURL(this.mURL + "/rc/" + id + "?XSL.Style=xml");
                aRequest.execute(this, this.onSlotComplete);
            });
            this.login();
        }

        /**
         * Returns an Slot array.
         * 
         * @return a Slot array
         */
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

        /**
         * Setter for a new or already exists Slot object.
         * 
         * @param slot the Slot to add or to overwrite with new data 
         */
        setSlot(slot: Slot) {
            if (!core.Utils.isValid(slot)) return;

            for (var i in this.mSlots) {
                if (this.mSlots[i].id == slot.id) {
                    this.mSlots[i] = slot;
                    return;
                }
            }

            this.mSlots.push(slot);
        }

        /**
         * Callback method after a successfully login. Triggers EVENT_LOGIN_SUCCESS event.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         * @param aSuccess the status of the HTTPRequest
         */
        private onLoginComplete(aRequest: net.HTTPRequest, aData: string, aSuccess: boolean) {
            if (!aSuccess) return;

            this.dispatch(Client.EVENT_LOGIN_SUCCESS, aRequest);
        }

        /**
         * Callback method after successfully loaded Slot list.
         * Parse Slots from given XML data and triggers EVENT_SLOT_LIST_LOADED afterward.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         * @param aSuccess the status of the HTTPRequest
         */
        private onSlotsComplete(aRequest: net.HTTPRequest, aData: string, aSuccess: boolean) {
            if (!aSuccess) return;

            this.mSlots = new Array<Slot>();
            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            if (core.Utils.isValid(doc)) {
                var slots: NodeList = doc.getElementsByTagName("slot");
                for (var c = 0; c < slots.length; c++) {
                    var slot: Slot = Slot.parse(<Element>slots.item(c));
                    this.mSlots.push(slot);
                }
            }

            this.dispatch(Client.EVENT_SLOT_LIST_LOADED);
        }

        /**
         * Callback method after successfully loaded Slot.
         * Parse entries from given XML data and triggers EVENT_SLOT_LOADED afterward. 
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         * @param aSuccess the status of the HTTPRequest
         */
        private onSlotComplete(aRequest: net.HTTPRequest, aData: string, aSuccess: boolean): void {
            if (!aSuccess) return;

            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            var slot: Slot = null;

            if (core.Utils.isValid(doc)) {
                var elm: Element = <Element>doc.getElementsByTagName("slot").item(0);
                slot = Slot.parse(elm);
                this.setSlot(slot);
            }

            this.dispatch(Client.EVENT_SLOT_LOADED, slot);
        }
    }
}