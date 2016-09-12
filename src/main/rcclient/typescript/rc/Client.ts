/// <reference path="../definitions/crypto-js.d.ts" />
/// <reference path="../libs/JSON2.ts" />

/// <reference path="../core/EventDispatcher.ts" />
/// <reference path="../net/HTTPRequest.ts" />

/// <reference path="Entry.ts" />
/// <reference path="Error.ts" />
/// <reference path="ErrorCode.ts" />
/// <reference path="Slot.ts" />

module rc {
    export class Client extends core.EventDispatcher {
        public static EVENT_ERROR = "CLIENT_ERROR";
        public static EVENT_SESSION_SUCCESS = "CLIENT_SESSION_SUCCESS";
        public static EVENT_SLOT_LIST_LOADED = "CLIENT_SLOT_LIST_LOADED";
        public static EVENT_SLOT_LOADED = "CLIENT_SLOT_LOADED";
        public static EVENT_COPY_REGISTERED = "CLIENT_COPY_REGISTERED";
        public static EVENT_COPY_DEREGISTERED = "CLIENT_COPY_DEREGISTERED";

        public statusText: string;
        private numTries: number = 0;

        private URL: string;
        private slots: Array<Slot>;

        private token: string;
        private sessionToken: string;

        constructor(aURL: string) {
            super();

            this.URL = aURL;
        }

        getURL(): string {
            return this.URL;
        }

        setURL(aURL: string) {
            this.URL = aURL;
        }

        /**
         * Method to login to RC servlet.
         */
        private requestToken() {
            this.statusText = core.Locale.getInstance().getString("client.status.registerSession");

            var request: net.HTTPRequest = new net.HTTPRequest(this.URL + "/rcclient/token");
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

                aRequest.setURL(this.URL + "/rcclient/list");
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotsComplete);

                aRequest.execute({ method: net.HTTPRequest.METHOD_GET });
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

                aRequest.setURL(this.URL + "/rcclient/" + id);
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onSlotComplete);
                aRequest.execute({ method: net.HTTPRequest.METHOD_GET });
            });
            this.requestToken();
        }

        /**
         * Registers copy with given parameters.
         * 
         * @param slotId the Slot id
         * @param entryId the entry id
         * @param epn the EPN to register on entry 
         */
        registerCopy(slotId: string, entryId: string, epn: string) {
            this.addListener(Client.EVENT_SESSION_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_SESSION_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.registerCopy");

                aRequest.setURL(this.URL + "/rcclient/" + slotId);
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onRegisterCopyComplete);

                var job = { action: "register", entryId: entryId, epn: epn };
                aRequest.execute({ method: net.HTTPRequest.METHOD_POST, data: ClientData.encrypt(this.sessionToken, JSON2.stringify(job)), contentType: "text/plain" });
            });
            this.requestToken();
        }

        /**
         * Deregisters copy with given parameters.
         * 
         * @param slotId the Slot id
         * @param entryId the entry id
         * @param epn the EPN to register on entry 
         */
        deregisterCopy(slotId: string, entryId: string, epn: string) {
            this.addListener(Client.EVENT_SESSION_SUCCESS, this, (aDelegate: Client, aRequest: net.HTTPRequest) => {
                aDelegate.clearListenersByEvent(Client.EVENT_SESSION_SUCCESS);

                aDelegate.statusText = core.Locale.getInstance().getString("client.status.deregisterCopy");

                aRequest.setURL(this.URL + "/rcclient/" + slotId);
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onDeregisterCopyComplete);

                var job = { action: "deregister", entryId: entryId, epn: epn };
                aRequest.execute({ method: net.HTTPRequest.METHOD_POST, data: ClientData.encrypt(this.sessionToken, JSON2.stringify(job)), contentType: "text/plain" });
            });
            this.requestToken();
        }

        /**
         * Returns an Slot array.
         * 
         * @return a Slot array
         */
        getSlots(): Array<Slot> {
            return this.slots;
        }

        /**
         * Returns a Slot by given id.
         * 
         * @param id the slot id
         * @return the slot or <code>null</code> if nothing was found
         */
        getSlot(id: string): Slot {
            for (var i in this.slots) {
                if (this.slots[i].id == id)
                    return this.slots[i];
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

            for (var i in this.slots) {
                if (this.slots[i].id == slot.id) {
                    this.slots[i] = slot;
                    return;
                }
            }

            this.slots.push(slot);
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
                this.token = token;

                this.sessionToken = core.Utils.generateUUID();

                aRequest.setURL(this.URL + "/rcclient/session");
                aRequest.addListener(net.HTTPRequest.EVENT_COMPLETE, this, this.onRegisterSessionComplete);

                aRequest.execute({ method: net.HTTPRequest.METHOD_POST, data: ClientData.encrypt(this.token, this.sessionToken), contentType: "text/plain" });

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

            try {
                aData = ClientData.decrypt(this.sessionToken, aData);

                this.slots = new Array<Slot>();
                var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

                if (core.Utils.isValid(doc)) {
                    var slots: NodeList = doc.getElementsByTagName("slot");
                    for (var c = 0; c < slots.length; c++) {
                        var slot: Slot = Slot.parse(<Element>slots.item(c));
                        this.slots.push(slot);
                    }

                    this.slots.sort(Slot.compare);
                }
            } catch (e) {
                this.dispatch(Client.EVENT_ERROR, new rc.Error(rc.ErrorCode.PARSE_ERROR_SLOTS, e));
                return;
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

            try {
                aData = ClientData.decrypt(this.sessionToken, aData);

                var doc: Document = new DOMParser().parseFromString(aData, "text/xml");

                var slot: Slot = null;

                if (core.Utils.isValid(doc)) {
                    var elm: Element = <Element>doc.getElementsByTagName("slot").item(0);
                    slot = Slot.parse(elm);
                    this.setSlot(slot);
                }
            } catch (e) {
                this.dispatch(Client.EVENT_ERROR, new rc.Error(rc.ErrorCode.PARSE_ERROR_SLOT, e));
                return;
            }

            this.statusText = core.Locale.getInstance().getString("client.status.loadSlot.done");
            this.dispatch(Client.EVENT_SLOT_LOADED, slot);
        }

        /**
         * Callback method after successfully registered a copy.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onRegisterCopyComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            aData = ClientData.decrypt(this.sessionToken, aData);

            this.statusText = core.Locale.getInstance().getString("client.status.registerCopy.done");
            this.dispatch(Client.EVENT_COPY_REGISTERED, JSON2.parse(aData));
        }

        /**
         * Callback method after successfully deregistered a copy.
         * 
         * @param aRequest the delegating HTTPRequest
         * @param aData the response
         */
        private onDeregisterCopyComplete(aRequest: net.HTTPRequest, aData: string) {
            aRequest.clearListenersByEvent(net.HTTPRequest.EVENT_COMPLETE);

            aData = ClientData.decrypt(this.sessionToken, aData);

            this.statusText = core.Locale.getInstance().getString("client.status.deregisterCopy.done");
            this.dispatch(Client.EVENT_COPY_DEREGISTERED, JSON2.parse(aData));
        }
    }

    class ClientData {
        private static ENCRYPT_ENABLED = false;
        private static KEY_SIZE: number = 128;
        private static ITERATIONS: number = 100;

        public static encrypt(passphrase: string, data: string): string {
            if (ClientData.ENCRYPT_ENABLED) {
                var salt = CryptoJS.lib.WordArray.random(ClientData.KEY_SIZE / 32);
                var key = CryptoJS.PBKDF2(passphrase, salt, { keySize: ClientData.KEY_SIZE / 32, iterations: ClientData.ITERATIONS });
                var iv = CryptoJS.MD5(passphrase);

                var encrypted = CryptoJS.AES.encrypt(data, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });

                return CryptoJS.lib.WordArray.create().concat(salt).concat(encrypted.ciphertext).toString(CryptoJS.enc.Base64);
            } else {
                return window.btoa(data);
            }
        }

        public static decrypt(passphrase: string, encrypted: string) {
            if (ClientData.ENCRYPT_ENABLED) {
                var ciphertext = CryptoJS.enc.Base64.parse(encrypted);

                var ciphertextWords = ciphertext.words;

                var salt = CryptoJS.lib.WordArray.create(ciphertextWords.slice(0, ClientData.KEY_SIZE / 32 / 4));
                ciphertext = CryptoJS.lib.WordArray.create(ciphertextWords.slice(ClientData.KEY_SIZE / 32 / 4));

                var key = CryptoJS.PBKDF2(passphrase, salt, { keySize: ClientData.KEY_SIZE / 32, iterations: ClientData.ITERATIONS });
                var iv = CryptoJS.MD5(passphrase);

                var decrypt = CryptoJS.AES.decrypt(CryptoJS.lib.CipherParams.create({ ciphertext: ciphertext }), key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });

                return decrypt.toString(CryptoJS.enc.Utf8);
            } else {
                return window.atob(encrypted);
            }
        }
    }
}