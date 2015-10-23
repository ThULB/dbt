/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static EVENT_ERROR = "CLIENT_ERROR";
        public static EVENT_LOGIN_SUCCESS = "CLIENT_LOGIN_SUCCESS";
        public static EVENT_SLOT_LIST_LOADED = "CLIENT_SLOT_LIST_LOADED";
        public static EVENT_SLOT_LOADED = "CLIENT_SLOT_LOADED";

        public statusText: string;

        private mURL: string;
        private mSlots: Array<Slot>;
        private mToken: string;

        constructor(aURL: string) {
            super();

            this.mURL = aURL;
        }

        /**
         * Method to login to RC servlet.
         */
        private doLogin() {
            this.statusText = core.Locale.getInstance().getString("client.status.doLogin");

            var request: net.HTTPRequest = new net.HTTPRequest(this.mURL + "/rcclient/token");
            request.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onLoginComplete);
            request.addListener(net.HTTPRequest.EVENT_ERROR, this, this.onError);
            request.addListener(net.HTTPRequest.EVENT_PROGRESS, this, (aRequest: net.HTTPRequest, aProgress: number, aProgressMax: number) => {
                this.dispatch(net.HTTPRequest.EVENT_PROGRESS, aProgress, aProgressMax);
            });
            request.execute();
        }

        /**
         * Loads Slots after successfully login.
         */
        loadSlots() {
            this.addListener(Client.EVENT_LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_LOGIN_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.loadSlots");

                aRequest.setMethod(net.HTTPRequest.METHOD_POST);
                aRequest.setData("token=" + this.mToken);
                
                aRequest.setURL(this.mURL + "/rcclient/list");
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotsComplete);
                aRequest.execute();
            });
            this.doLogin();
        }

        /**
         * Loads Slot after successfully login.
         * 
         * @param id the Slot id to load
         */
        loadSlot(id: string) {
            this.addListener(Client.EVENT_LOGIN_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_LOGIN_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.loadSlot");
                
                aRequest.setMethod(net.HTTPRequest.METHOD_POST);
                aRequest.setData("token=" + this.mToken);

                aRequest.setURL(this.mURL + "/rcclient/" + id);
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotComplete);
                aRequest.execute();
            });
            this.doLogin();
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
         * Callback method if a error was triggered.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aError the error object
         */
        private onError(aRequest: net.HTTPRequest, aError: net.Error) {
            this.dispatch(Client.EVENT_ERROR, aError);
        }

        /**
         * Callback method after a successfully login. Triggers EVENT_LOGIN_SUCCESS event.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onLoginComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            if (core.Utils.isValid(doc)) {
                var elm: Element = <Element>doc.getElementsByTagName("token").item(0);
                this.mToken = core.Utils.isValid(elm) ? elm.textContent : null;

                if (!this.mToken.isEmpty()) {
                    this.statusText = core.Locale.getInstance().getString("client.status.doLogin.done");
                    this.dispatch(Client.EVENT_LOGIN_SUCCESS, aRequest);
                    return;
                }
            }

            this.dispatch(Client.EVENT_ERROR, "blah");
        }

        /**
         * Callback method after successfully loaded Slot list.
         * Parse Slots from given XML data and triggers EVENT_SLOT_LIST_LOADED afterward.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onSlotsComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            this.mSlots = new Array<Slot>();
            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            if (core.Utils.isValid(doc)) {
                var slots: NodeList = doc.getElementsByTagName("slot");
                for (var c = 0; c < slots.length; c++) {
                    var slot: Slot = Slot.parse(<Element>slots.item(c));
                    this.mSlots.push(slot);
                }
            }

            this.statusText = core.Locale.getInstance().getString("client.status.loadSlots.done");
            this.dispatch(Client.EVENT_SLOT_LIST_LOADED);
        }

        /**
         * Callback method after successfully loaded Slot.
         * Parse entries from given XML data and triggers EVENT_SLOT_LOADED afterward. 
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onSlotComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

            var slot: Slot = null;

            if (core.Utils.isValid(doc)) {
                var elm: Element = <Element>doc.getElementsByTagName("slot").item(0);
                slot = Slot.parse(elm);
                this.setSlot(slot);
            }

            this.statusText = core.Locale.getInstance().getString("client.status.loadSlot.done");
            this.dispatch(Client.EVENT_SLOT_LOADED, slot);
        }
    }
}