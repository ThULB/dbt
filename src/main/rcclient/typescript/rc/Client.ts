/// <reference path="../definitions/crypto-js.d.ts" />

/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static EVENT_ERROR = "CLIENT_ERROR";
        public static EVENT_SESSION_SUCCESS = "CLIENT_SESSION_SUCCESS";
        public static EVENT_SLOT_LIST_LOADED = "CLIENT_SLOT_LIST_LOADED";
        public static EVENT_SLOT_LOADED = "CLIENT_SLOT_LOADED";

        public statusText: string;
        private numTries: number = 0;

        private mURL: string;
        private mSlots: Array<Slot>;

        private mToken: string;
        private mSessionToken: string;

        constructor(aURL: string) {
            super();

            this.mURL = aURL;
        }

        /**
         * Method to login to RC servlet.
         */
        private requestToken() {
            this.statusText = core.Locale.getInstance().getString("client.status.registerSession");

            var request: net.HTTPRequest = new net.HTTPRequest(this.mURL + "/rcclient/token");
            request.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onRequestTokenComplete);
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
            this.addListener(Client.EVENT_SESSION_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_SESSION_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.loadSlots");

                aRequest.setURL(this.mURL + "/rcclient/list");
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotsComplete);

                aRequest.execute();
            });
            this.requestToken();
        }

        /**
         * Loads Slot after successfully login.
         * 
         * @param id the Slot id to load
         */
        loadSlot(id: string) {
            this.addListener(Client.EVENT_SESSION_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_SESSION_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.loadSlot");

                aRequest.setURL(this.mURL + "/rcclient/" + id);
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotComplete);
                aRequest.execute();
            });
            this.requestToken();
        }

        /**
         * Returns an Slot array.
         * 
         * @return a Slot array
         */
        getSlots(): Array<Slot> {
            return this.mSlots;
        }

        /**
         * Returns a Slot by given id.
         * 
         * @param id the slot id
         * @return the slot or <code>null</code> if nothing was found
         */
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
            if (aError instanceof net.HTTPError) {
                switch (aError.errorCode) {
                    case 401:
                        if (this.numTries < 3) {
                            this.requestToken();
                            this.numTries++;
                            return;
                        }
                        break;
                }
            }
            this.dispatch(Client.EVENT_ERROR, aError);
        }

        /**
         * Callback method after a successfully retrieve of token.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onRequestTokenComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            var token: string = CryptoJS.enc.Base64.parse(aData).toString(CryptoJS.enc.Utf8);

            if (!token.isEmpty()) {
                this.numTries = 0;
                this.mToken = token;

                this.mSessionToken = core.Utils.generateUUID();

                aRequest.setURL(this.mURL + "/rcclient/session");
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onRegisterSessionComplete);

                aRequest.execute({ method: net.HTTPRequest.METHOD_POST, data: ClientData.encrypt(this.mToken, this.mSessionToken), contentType: "text/plain" });

                return;
            }

            this.dispatch(Client.EVENT_ERROR, ErrorCode.NO_TOKEN);
        }

        /**
         * Callback method after successfully register client session.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onRegisterSessionComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            this.statusText = core.Locale.getInstance().getString("client.status.registerSession.done");
            this.dispatch(Client.EVENT_SESSION_SUCCESS, aRequest);
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

            aData = ClientData.decrypt(this.mSessionToken, aData);

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

            aData = ClientData.decrypt(this.mSessionToken, aData);

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

    class ClientData {
        private static KEY_SIZE: number = 128;
        private static ITERATIONS: number = 100;

        public static encrypt(passphrase: string, data: string): string {
            var salt = CryptoJS.lib.WordArray.random(ClientData.KEY_SIZE / 32);
            var key = CryptoJS.PBKDF2(passphrase, salt, { keySize: ClientData.KEY_SIZE / 32, iterations: ClientData.ITERATIONS });
            var iv = CryptoJS.MD5(passphrase);

            var encrypted = CryptoJS.AES.encrypt(data, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });

            return CryptoJS.lib.WordArray.create().concat(salt).concat(encrypted.ciphertext).toString(CryptoJS.enc.Base64);
        }

        public static decrypt(passphrase: string, encrypted: string) {
            var ciphertext = CryptoJS.enc.Base64.parse(encrypted);

            var ciphertextWords = ciphertext.words;

            var salt = CryptoJS.lib.WordArray.create(ciphertextWords.slice(0, ClientData.KEY_SIZE / 32 / 4));
            ciphertext = CryptoJS.lib.WordArray.create(ciphertextWords.slice(ClientData.KEY_SIZE / 32 / 4));

            var key = CryptoJS.PBKDF2(passphrase, salt, { keySize: ClientData.KEY_SIZE / 32, iterations: ClientData.ITERATIONS });
            var iv = CryptoJS.MD5(passphrase);

            var decrypt = CryptoJS.AES.decrypt(CryptoJS.lib.CipherParams.create({ ciphertext: ciphertext }), key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });

            return decrypt.toString(CryptoJS.enc.Utf8);
        }
    }
}