/// <reference path="definitions/XUL.d.ts" />
/// <reference path="libs/String.ts" />

/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />
/// <reference path="ibw/Application.ts" />

class IBWRCClient {
    public static VERSION: string = "@@VERSION";
    public static REVISION: string = "@@REVISION";

    public static CFG_PREFIX: string = "RC";
    public static LOCAL_URI_PREFIX: string = "chrome://IBWRCClient/";

    public static FORMAT_7100: string = "!{0}!{1} @ {2}\n";
    public static FORMAT_4801: string = "4801 Band im Semesterapparat <a href=\"{0}\" target=\"_blank\">{1}</a>.\n";
    public static FORMAT_4802: string = "4802 {0} RC {1}\n";
    public static FORMAT_4802_MIGRATE: string = "4802 {0} SSTold {1}\n";

    private rcClient: rc.Client;

    // preference based or default variables
    private clientURL: string = "http://www.db-thueringen.de";
    private defaultIndicator: string = "i";
    private defaultPresence: boolean = false;
    private shelfmarkChangeable: boolean = true;

    private slot: rc.Slot;
    private userInfo: ibw.UserInfo;
    private copys: Array<ibw.Copy>;

    private elementStates = {
        "cbShelfMark": { hidden: false },
        "cbPresence": { hidden: false },
        "tbShelfMark": { hidden: false },
        "tbLocation": { hidden: false },
        "tbBundleEPN": { hidden: false },
        "tbBarcode": { hidden: false },
        "tbBarcodes": { hidden: false },
        "btnRegister": { hidden: false },
        "btnDeregister": { hidden: false },
        "boxBundle": { hidden: true },
        "boxShelfMark": { hidden: true }
    };

    constructor() {
        core.Locale.getInstance(IBWRCClient.LOCAL_URI_PREFIX + "locale/ibwrcclient.properties");

        try {
            var version: XULLabelElement = <any>document.getElementById("version");
            version.value = "{0} r{1}".format(IBWRCClient.VERSION, IBWRCClient.REVISION);

            this.clientURL = ibw.application.getProfileString(IBWRCClient.CFG_PREFIX, "URL", this.clientURL);
            this.defaultIndicator = ibw.application.getProfileString(IBWRCClient.CFG_PREFIX, "LoanIndicator", this.defaultIndicator);
            this.defaultPresence = ibw.application.getProfileInt(IBWRCClient.CFG_PREFIX, "Presence", this.defaultPresence ? 1 : 0) == 1;
            this.shelfmarkChangeable = ibw.application.getProfileInt(IBWRCClient.CFG_PREFIX, "ShelfmarkChangeable", this.shelfmarkChangeable ? 1 : 0) == 1;

            this.userInfo = ibw.getUserInfo();
            if (core.Utils.isValid(this.userInfo)) {
                this.rcClient = new rc.Client(this.clientURL);
                this.rcClient.addListener(rc.Client.EVENT_ERROR, this, this.onError);
                this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
                this.rcClient.addListener(net.HTTPRequest.EVENT_PROGRESS, this, this.onProgress);
                this.rcClient.loadSlots();

                this.addCommandListener("mlSlots|mlSlotsBar|mlPPN|mlEPN|btnBarcode|btnRegister|btnDeregister|btnDeregisterAll|miSettings|miAbout".split("|"));

                document.getElementById("tbBarcode").addEventListener("keyup", (ev: KeyboardEvent) => {
                    if (ev.keyCode == 13) {
                        this.onBarcodeEntered((<XULTextBoxElement>ev.currentTarget).value);
                    }
                }, true);
                document.getElementById("tbBarcodes").addEventListener("keyup", (ev: KeyboardEvent) => {
                    this.setDisabledState("btnDeregisterAll", (<XULTextBoxElement>ev.currentTarget).value.isEmpty())
                }, true);
            }
        } catch (e) {
            ibw.showError(e);
            window.close();
        }
    }

    private clearMenuList(target: any, disabled: boolean): XULMenuListElement {
        var ml: XULMenuListElement = typeof target == "string" ? <any>document.getElementById(target) : target;
        ml.removeAllItems();
        ml.appendItem(core.Locale.getInstance().getString("defaultValues.pleaseSelect"), null);
        ml.selectedIndex = 0;
        ml.disabled = disabled;
        return ml;
    }

    private clearTextBox(target: any, disabled: boolean): XULTextBoxElement {
        var tb: XULTextBoxElement = typeof target == "string" ? <any>document.getElementById(target) : target;
        tb.value = "";
        tb.disabled = disabled;
        return tb;
    }

    private setDisabledState(target: any, disabled: boolean) {
        var elm: XULControlElement = typeof target == "string" ? <any>document.getElementById(target) : target;
        elm.disabled = disabled;
    }

    private setHiddenState(target: any, hidden: boolean) {
        var elm: XULControlElement = typeof target == "string" ? <any>document.getElementById(target) : target;
        if (elm.hidden != hidden) {
            elm.hidden = hidden;
            (<XULWindow>window).sizeToContent();
        }
    }

    private addCommandListener(elms: Array<string>) {
        for (var e in elms) {
            var elm: XULControlElement = <any>document.getElementById(elms[e]);
            if (core.Utils.isValid(elm)) {
                elm.addEventListener("command", this, false);
            }
        }
    }

    private updateStatusbar(status: string, progress: number = 100, progressMax: number = 100) {
        var statusText: XULTextBoxElement = <any>document.getElementById("sbStatusText");
        statusText.value = status;

        var progressMeter: XULProgressMeterElement = <any>document.getElementById("sbProgress");
        progressMeter.max = progressMax;
        progressMeter.value = progress.toString();
    }

    /**
     * Dispatches all Events.
     * 
     * @param ev the Event
     */
    handleEvent(ev: Event) {
        if (ev.type === "command") {
            switch ((<XULElement>ev.currentTarget).getAttribute("id")) {
                case "mlSlots":
                    this.onSelectSlot(<XULCommandEvent>ev);
                    break;
                case "mlSlotsBar":
                    this.onSelectSlot(<XULCommandEvent>ev);
                    break;
                case "mlPPN":
                    this.onSelectPPN(<XULCommandEvent>ev);
                    break;
                case "mlEPN":
                    this.onSelectEPN(<XULCommandEvent>ev);
                    break;
                case "btnBarcode":
                    this.onBarcode(<XULCommandEvent>ev);
                    break;
                case "btnRegister":
                    this.onRegister(<XULCommandEvent>ev);
                    break;
                case "btnDeregister":
                    this.onDeregister(<XULCommandEvent>ev);
                    break;
                case "btnDeregisterAll":
                    this.onDeregisterAll(<XULCommandEvent>ev);
                    break;
                case "miSettings":
                    this.onSettings();
                    break;
            }
        }
    }

    private onSettings() {
        (<XULWindow>window).openDialog("IBWRCClientSettings.xul", "_blank", "chrome,close,modal,centerscreen", this);
    }

    /**
     * Callback method used on load of settings dialog.
     * 
     * @param window the dialog window
     */
    onSettingsLoad(window: XULWindow) {
        var document = window.document;

        var tbURL: XULTextBoxElement = <any>document.getElementById("tbURL");
        var tbIndicator: XULTextBoxElement = <any>document.getElementById("tbIndicator");
        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");

        tbURL.value = this.clientURL;
        tbIndicator.value = this.defaultIndicator;
        cbPresence.checked = this.defaultPresence;
    }

    /**
     * Callback method of settings dialog if "Ok" was pressed.
     * 
     * @param window the dialog window
     */
    onSettingsAccept(window: XULWindow) {
        try {
            var document = window.document;

            var tbURL: XULTextBoxElement = <any>document.getElementById("tbURL");
            var tbIndicator: XULTextBoxElement = <any>document.getElementById("tbIndicator");
            var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
            var cbShelfmark: XULCheckboxElement = <any>document.getElementById("cbShelfmark");

            this.clientURL = tbURL.value;
            this.defaultIndicator = tbIndicator.value;
            this.defaultPresence = cbPresence.checked;
            this.shelfmarkChangeable = cbShelfmark.checked;

            ibw.application.writeProfileString(IBWRCClient.CFG_PREFIX, "URL", this.clientURL);
            ibw.application.writeProfileString(IBWRCClient.CFG_PREFIX, "LoanIndicator", this.defaultIndicator);
            ibw.application.writeProfileInt(IBWRCClient.CFG_PREFIX, "Presence", this.defaultPresence ? 1 : 0);
            ibw.application.writeProfileInt(IBWRCClient.CFG_PREFIX, "ShelfmarkChangeable", this.shelfmarkChangeable ? 1 : 0);

            this.rcClient.setURL(this.clientURL);
            this.rcClient.loadSlots();

            return true;
        } catch (e) {
            this.onError(this, e);
            return false;
        }
    }

    /**
     * Callback method if a error was triggered.
     * 
     * @param delegate the delegating HTTPRequest
     * @param error the error object
     */
    private onError(delegate: any, error: Error) {
        ibw.showError(error);
    }

    /**
     * Callback method to listen on HTTPRequest progress event.
     * 
     * @param delegate the delegating rc.Client
     * @param progress the current progress value
     * @param progressMax the progress max value
     */
    onProgress(delegate: rc.Client, progress: number, progressMax: number) {
        this.updateStatusbar(delegate.statusText, progress, progressMax);
    }

    /**
     * Callback method of loaded Slot list. Used to display Slots in MenuListElement.
     * 
     * @param delegate the delegating rc.Client
     */
    onSlotListLoaded(delegate: rc.Client) {
        this.updateStatusbar(delegate.statusText);

        this.clearMenuList("mlPPN", true);
        this.clearMenuList("mlEPN", true);

        for (var e in this.elementStates) {
            this.setDisabledState(e, true);
            this.setHiddenState(e, this.elementStates[e].hidden);
        }

        var elms: string[] = ["mlSlots", "mlSlotsBar"];
        var slots: Array<rc.Slot> = delegate.getSlots();

        for (var i in elms) {
            var ml: XULMenuListElement = <any>document.getElementById(elms[i]);

            ml.removeAllItems();
            ml.appendItem(core.Locale.getInstance().getString("defaultValues.pleaseSelect"), null);
            ml.selectedIndex = 0;

            for (var i in slots) {
                var slot: rc.Slot = slots[i];

                if (slot.eOnly || !slot.id.startsWith(this.userInfo.libId)) continue;
                ml.appendItem("({0}|{1}) {2}".format(slot.id, core.Locale.getInstance().getString("slot.status." + rc.Status[slot.status]), slot.title), slot.id);
            }

            ml.disabled = false;
        }
    }

    /**
     * Callback method of loaded Slot. Used to display Entry (OPC records) in MenuListElement.
     * 
     * @param delegate the delegating rc.CLient
     * @param slot the loaded slot
     */
    onSlotLoaded(delegate: rc.Client, slot: rc.Slot) {
        delegate.clearListenersByEvent(rc.Client.EVENT_SLOT_LOADED);
        this.updateStatusbar(delegate.statusText);

        var mlPPN: XULMenuListElement = <any>document.getElementById("mlPPN");

        this.clearMenuList(mlPPN, false);
        this.clearMenuList("mlEPN", true);

        var elms: string[] = ["btnBarcode", "tbBarcode", "tbBarcodes"];
        for (var i in elms) {
            this.setDisabledState(elms[i], false);
        }

        for (var i in slot.entries) {
            var entry: rc.Entry = slot.entries[i];
            mlPPN.appendItem("(" + entry.ppn + ") " + (core.Utils.isValid(entry.title) ? entry.title : ""), entry.ppn);
        }

        this.slot = slot;
    }

    /**
     * Event handler for Slot selection. Also used to trigger Entry loading.
     * 
     * @param ev the command event
     */
    onSelectSlot(ev: XULCommandEvent) {
        var mlSlots: XULMenuListElement = <any>ev.currentTarget;
        var slotId = mlSlots.selectedItem.value;

        var otherElm: XULMenuListElement = null;
        if (mlSlots.getAttribute("id") == "mlSlots") {
            otherElm = <any>document.getElementById("mlSlotsBar");
        } else {
            otherElm = <any>document.getElementById("mlSlots");
        }

        if (core.Utils.isValid(otherElm)) {
            otherElm.selectedIndex = mlSlots.selectedIndex;
        }

        this.clearMenuList("mlPPN", true);
        this.clearMenuList("mlEPN", true);
        this.clearTextBox("tbBarcode", true);
        this.clearTextBox("tbBarcodes", true);

        for (var e in this.elementStates) {
            this.setDisabledState(e, true);
            this.setHiddenState(e, this.elementStates[e].hidden);
        }

        if (!slotId.isEmpty()) {
            this.rcClient.addListener(rc.Client.EVENT_SLOT_LOADED, this, this.onSlotLoaded);
            this.rcClient.loadSlot(slotId);
        }
    }

    /**
     * Event handler for PPN selection. Also used to trigger EPN loading.
     * 
     * @param ev the command event
     */
    onSelectPPN(ev: XULCommandEvent) {
        var mlPPN: XULMenuListElement = <any>ev.currentTarget;
        var PPN = mlPPN.selectedItem.value;

        for (var e in this.elementStates) {
            this.setDisabledState(e, true);
            this.setHiddenState(e, this.elementStates[e].hidden);
        }

        var tbBarcode: XULTextBoxElement = <any>document.getElementById("tbBarcode");
        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        if (!PPN.isEmpty()) {
            ibw.getActiveWindow().command("f ppn " + PPN, false);
            this.copys = ibw.getCopys();

            var barcode = tbBarcode.value;

            var entry = this.slot.getEntryForPPN(PPN);

            this.clearMenuList(mlEPN, false);

            var selectedItem: XULSelectControlItemElement = null;

            for (var i in this.copys) {
                var copy: ibw.Copy = this.copys[i];

                if (copy == null || copy.epn.isEmpty() || copy.type === "d") continue;

                var item = mlEPN.appendItem("({0}|{1}) {2}".format(copy.epn, copy.backup == null ? "-" : "v", copy.shelfmark || ""), i);

                if (entry != null && entry.epn == copy.epn || !barcode.isEmpty() && barcode == copy.barcode) {
                    selectedItem = item;
                    selectedItem.value = i;
                }
            }

            if (selectedItem != null) {
                mlEPN.selectedItem = selectedItem;
                mlEPN.doCommand();
            }
        } else {
            this.clearMenuList(mlEPN, true);
            this.clearTextBox(tbBarcode, false);
        }
    }

    /**
     * Event handler for EPN selection.
     * 
     * @param ev the command event
     */
    onSelectEPN(ev: XULCommandEvent) {
        var mlEPN: XULMenuListElement = <any>ev.currentTarget;
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value)];

        if (core.Utils.isValid(copy)) {
            for (var e in this.elementStates) {
                var disabled: boolean = false;
                var hidden: boolean = this.elementStates[e].hidden;

                switch (e) {
                    case "btnRegister":
                        disabled = this.slot.getEntryForEPN(copy.epn) != null && copy.hasRegistered();
                        break;
                    case "btnDeregister":
                        disabled = this.slot.getEntryForEPN(copy.epn) == null && !copy.hasRegistered();
                        break;
                    case "cbPresence":
                        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
                        disabled = copy.hasRegistered();
                        cbPresence.checked = copy.hasRegistered() || this.defaultPresence;
                        break;
                    case "cbShelfMark":
                        hidden = disabled = copy.hasRegistered() || !this.shelfmarkChangeable;
                        break;
                    case "boxBundle":
                        hidden = !copy.isBundle;
                        break;
                    case "boxShelfMark":
                        hidden = !this.shelfmarkChangeable;
                        break;
                    case "tbLocation":
                        disabled = copy.hasRegistered();
                        break;
                    case "tbShelfMark":
                        disabled = copy.hasRegistered();
                        break;
                    case "tbBundleEPN":
                        disabled = copy.hasRegistered();
                        break;
                }

                this.setDisabledState(e, disabled);
                this.setHiddenState(e, hidden);
            }

            var tbShelfMark: XULTextBoxElement = <any>document.getElementById("tbShelfMark");
            tbShelfMark.value = copy.shelfmark;
            var tbLocation: XULTextBoxElement = <any>document.getElementById("tbLocation");
            tbLocation.value = copy.location;
            var tbBundleEPN: XULTextBoxElement = <any>document.getElementById("tbBundleEPN");
            tbBundleEPN.value = copy.hasRegistered() ? copy.backup[0].bundleEPN : "";
        } else {
            for (var e in this.elementStates) {
                this.setDisabledState(e, true);
                this.setHiddenState(e, this.elementStates[e].hidden);
            }
        }
    }

    /**
     * Event handler for barcode button.
     * 
     * @param ev the event
     */
    onBarcode(ev?: XULCommandEvent) {
        var boxEPN: XULElement = <any>document.getElementById("boxEPN");
        var boxBarcode: XULElement = <any>document.getElementById("boxBarcode");

        boxEPN.hidden = boxBarcode.hidden;
        boxBarcode.hidden = !boxBarcode.hidden;
    }

    private onBarcodeEntered(barcode: string) {
        if (!barcode.isEmpty()) {
            if (ibw.command("f bar " + barcode)) {
                var PPN: string = ibw.getActiveWindow().getVariable("P3GPP");

                if (this.slot.getEntryForPPN(PPN) != null) {
                    // FIXME if u know how to use itemCount on XULMenuListElement
                    for (var i = 0; i < this.slot.entries.length; i++) {
                        if (this.slot.entries[i].ppn == PPN) {
                            this.onBarcode();

                            var mlPPN: XULMenuListElement = <any>document.getElementById("mlPPN");
                            mlPPN.selectedIndex = i + 1;
                            mlPPN.selectedItem.value = PPN;
                            mlPPN.doCommand();
                            break;
                        }
                    }
                }
            }
        }
    }

    private register(copy: ibw.Copy, presence: boolean, bundleEPN: string, location?: string, shelfmark?: string) {
        if (ibw.command("k e" + copy.num)) {
            if (copy.hasRegistered()) {
                ibw.getTitle().findTag("4802", copy.backup.length - 1, true, true, false);
                ibw.getTitle().lineDown(1, false);
                ibw.getTitle().insertText(
                    IBWRCClient.FORMAT_4802.format(this.slot.id,
                        IBWRCClient.FORMAT_7100.format(
                            copy.backup[0].location,
                            copy.backup[0].shelfmark,
                            copy.backup[0].loanIndicator + (copy.backup[0].isBundle ? " \\ c:" + copy.backup[0].bundleEPN : "")
                        )
                    )
                );
            } else {
                var cat7100: string = ibw.getTitle().findTag("7100", 0, false, true, false);

                ibw.getTitle().insertText(
                    IBWRCClient.FORMAT_7100.format(
                        location || copy.location,
                        shelfmark || copy.shelfmark,
                        this.defaultIndicator + (copy.isBundle ? " \\ c" : "")
                    )
                );
                if (!presence)
                    ibw.getTitle().insertText(IBWRCClient.FORMAT_4801.format(this.clientURL + "/rc/" + this.slot.id, this.slot.id));
                ibw.getTitle().insertText(IBWRCClient.FORMAT_4802.format(this.slot.id, cat7100 + (copy.isBundle ? ":" + bundleEPN : "")));
            }

            // save title
            if (ibw.simulateKey("FR")) {
                this.rcClient.addListener(rc.Client.EVENT_COPY_REGISTERED, this, this.onRegisterComplete)
                this.rcClient.registerCopy(this.slot.id, this.slot.getEntryForPPN(copy.ppn).id, copy.epn);
            }
        }
    }

    /**
     * Event handler for register button.
     * 
     * @param ev the command event
     */
    onRegister(ev: XULCommandEvent) {
        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value)];

        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
        var cbShelfMark: XULCheckboxElement = <any>document.getElementById("cbShelfMark");
        var tbLocation: XULTextBoxElement = <any>document.getElementById("tbLocation");
        var tbShelfMark: XULTextBoxElement = <any>document.getElementById("tbShelfMark");
        var tbBundleEPN: XULTextBoxElement = <any>document.getElementById("tbBundleEPN");

        if (copy.isBundle && tbBundleEPN.value.isEmpty()) {
            var locale = core.Locale.getInstance();
            ibw.messageBox(locale.getString("msg.title.error"), locale.getString("error.noBundleEPN"), ibw.MESSAGE_ERROR);
            return;
        }

        if (cbShelfMark.checked)
            this.register(copy, cbPresence.checked, tbBundleEPN.value, tbLocation.value, tbShelfMark.value);
        else
            this.register(copy, cbPresence.checked, tbBundleEPN.value);
    }

    /**
     * Callback method after successfully registration of copy.
     * 
     * @param delegate the delegating rc.CLient
     */
    onRegisterComplete(delegate: rc.Client) {
        delegate.clearListenersByEvent(rc.Client.EVENT_COPY_REGISTERED);
        this.updateStatusbar(delegate.statusText);

        ibw.messageBox(core.Locale.getInstance().getString("client.status.registerCopy.done"), ibw.MESSAGE_INFO);

        var mlSlots: XULMenuListElement = <any>document.getElementById("mlSlots");
        mlSlots.doCommand();
    }

    private deregister(copy: ibw.Copy) {
        if (ibw.command("k e" + copy.num)) {
            var clientURL = this.clientURL || this.rcClient.getURL();
            var backup = copy.getBackup(this.slot.id);

            if (backup != null) {
                var cat7100 = IBWRCClient.FORMAT_7100.format(
                    backup.location,
                    backup.shelfmark,
                    backup.loanIndicator + (backup.isBundle ? " \\ c" : "")
                );

                var f4802 = IBWRCClient.FORMAT_4802;
                var slotId = this.slot.id.escapeRegExp();
                if (backup.migrate) {
                    slotId = this.slot.id.replaceAll(".", ":");
                    f4802 = IBWRCClient.FORMAT_4802_MIGRATE;
                }

                if (ibw.titleFindRegExp("4801", new RegExp(IBWRCClient.FORMAT_4801.replaceAll(".", "\.").format(".*" + "\/rc\/" + slotId, slotId).trim() + "|" + IBWRCClient.FORMAT_4801.replaceAll(".", "\.").format(".*" + "\/esa\/" + slotId.replaceAll(".", ":"), slotId.replaceAll(".", ":")).trim()), true, true))
                    ibw.getTitle().deleteToEndOfLine();

                if (ibw.titleFindRegExp("4802", new RegExp(f4802.format(slotId, cat7100.escapeRegExp()).trim()), true, true))
                    ibw.getTitle().deleteToEndOfLine();

                if (copy.backup.length == 1) {
                    ibw.getTitle().findTag("7100", 0, false, true, false);
                    ibw.getTitle().deleteToEndOfLine();

                    ibw.getTitle().insertText(cat7100);
                }
            }

            // save title
            if (ibw.simulateKey("FR")) {
                this.rcClient.addListener(rc.Client.EVENT_COPY_DEREGISTERED, this, this.onDeregisterComplete);
                this.rcClient.deregisterCopy(this.slot.id, this.slot.getEntryForPPN(copy.ppn).id, copy.epn);
            }
        }
    }

    /**
     * Event handler for deregister button.
     * 
     * @param ev the command event
     */
    onDeregister(ev: XULCommandEvent) {
        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value)];

        this.deregister(copy);
    }

    /**
     * Callback method after successfully deregistration of copy.
     * 
     * @param delegate the delegating rc.CLient
     */
    onDeregisterComplete(delegate: rc.Client) {
        delegate.clearListenersByEvent(rc.Client.EVENT_COPY_DEREGISTERED);
        this.updateStatusbar(delegate.statusText);

        ibw.messageBox(core.Locale.getInstance().getString("client.status.deregisterCopy.done"), ibw.MESSAGE_INFO);

        var mlSlots: XULMenuListElement = <any>document.getElementById("mlSlots");
        mlSlots.doCommand();
    }

    /**
     * Event handler for deregister all button.
     * 
     * @param ev the command event
     */
    onDeregisterAll(ev: XULCommandEvent) {
        var tbBarcodes: XULTextBoxElement = <any>document.getElementById("tbBarcodes");
        var barcodes: Array<string> = tbBarcodes.value.trim().split("\n");

        var chain = new DeregisterChain(this.rcClient, this.slot, this.deregister, barcodes);
        chain.addListener(DeregisterChain.COMPLETE, this, this.onDeregisterChainComplete);
        chain.addListener(DeregisterChain.PROGRESS, this, this.onProgress);
        chain.execute();
    }

    /**
     * Callback method after successfully deregistration of multiple copys.
     * 
     * @param delegate the delegating DeregisterChain
     */
    onDeregisterChainComplete(chain: DeregisterChain) {
        chain.clearListenersByScope(this);
        chain.destroy();

        if (chain.copys.length == chain.completed.length)
            ibw.messageBox(core.Locale.getInstance().getString("client.status.deregisterCopys.done"), ibw.MESSAGE_INFO);
        else
            ibw.messageBox(core.Locale.getInstance().getString("client.status.deregisterCopys.failure"), ibw.MESSAGE_WARNING);

        var mlSlots: XULMenuListElement = <any>document.getElementById("mlSlotsBar");
        mlSlots.doCommand();
    }
}

class DeregisterChain extends core.EventDispatcher {
    public static PROGRESS: string = "DEREGISTER_CHAIN_PROGRESS";
    public static COMPLETE: string = "DEREGISTER_CHAIN_COMPLETE";

    private numDeregistered: number = 0;

    public statusText: string;
    public copys: Array<ibw.Copy> = [];
    public completed: Array<ibw.Copy> = [];

    constructor(public rcClient: rc.Client, public slot: rc.Slot, public deregister: (copy: ibw.Copy) => void, public barcodes: Array<string>) {
        super();

        this.rcClient.addListener(rc.Client.EVENT_ERROR, this, this.onError);
    }

    execute() {
        this.numDeregistered = 0;

        if (this.barcodes.length == 0)
            this.incrementNumDeregistered();
        else
            this.deregisterNext(this.numDeregistered);
    }

    destroy() {
        this.rcClient.clearListenersByScope(this);
    }

    onError(delegate: rc.Client, error: Error) { }

    onDeregisterComplete(delegate: rc.Client, job: any) {
        delegate.clearListenersByEvent(rc.Client.EVENT_COPY_DEREGISTERED);

        for (var i in this.copys) {
            var copy = this.copys[i];
            if (job.epn == copy.epn) {
                this.completed.push(copy);
            }
        }

        this.statusText = core.Locale.getInstance().getString("client.status.deregisterCopy.done");
        this.dispatch(DeregisterChain.PROGRESS, this.numDeregistered + (this.barcodes.length == 0 ? 0 : 1), this.barcodes.length);
        this.incrementNumDeregistered();
    }

    private deregisterNext(index: number) {
        var barcode: string = this.barcodes[index].trim();

        if (!barcode.isEmpty()) {
            if (ibw.command("f bar " + barcode)) {
                var PPN: string = ibw.getActiveWindow().getVariable("P3GPP");

                if (this.slot.getEntryForPPN(PPN) != null) {
                    for (var i = 0; i < this.slot.entries.length; i++) {
                        var entry = this.slot.entries[i];
                        if (entry.ppn == PPN) {
                            var copys = ibw.getCopys();
                            for (var c in copys) {
                                if (core.Utils.isValid(copys[c]) && copys[c].barcode == barcode) {
                                    this.copys.push(copys[c]);
                                    this.deregister.apply(this, [copys[c]]);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        this.incrementNumDeregistered();
    }

    private incrementNumDeregistered() {
        this.numDeregistered++;

        this.numDeregistered < this.barcodes.length && this.deregisterNext(this.numDeregistered);
        this.numDeregistered >= this.barcodes.length && this.dispatch(DeregisterChain.COMPLETE);
    }
}

function onLoad() {
    new IBWRCClient();
}