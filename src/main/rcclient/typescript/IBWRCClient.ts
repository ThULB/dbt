/// <reference path="definitions/XUL.d.ts" />

/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />
/// <reference path="ibw/Application.ts" />

class IBWRCClient {
    public static VERSION: string = "@@VERSION";
    public static REVISION: string = "@@REVISION";

    public static LOCAL_URI_PREFIX: string = "chrome://IBWRCClient/";

    private static FORMAT_7100: string = "!{0}!{1} @ {2}\n";
    private static FORMAT_4801: string = "4801 Band im Semesterapparat <a href=\"{0}\" target=\"_blank\">{1}</a>.\n";
    private static FORMAT_4802: string = "4802 {0} RC {1}\n";

    private rcClient: rc.Client;
    
    // preference based or default variables
    private clientURL: string = "http://127.0.0.1:8291/mir"; //"http://dbttest.thulb.uni-jena.de/mir";
    private defaultIndicator: string = "i";

    private slot: rc.Slot;
    private userInfo: ibw.UserInfo;
    private copys: Array<ibw.Copy>;

    private elementStates = {
        "cbShelfMark": { disabled: true, hidden: true },
        "cbPresence": { disabled: true, hidden: false },
        "tbShelfMark": { disabled: true, hidden: false },
        "tbLocation": { disabled: true, hidden: false },
        "btnRegister": { disabled: true, hidden: false },
        "btnDeregister": { disabled: true, hidden: false },
        "boxBundle": { disabled: true, hidden: true },
        "boxShelfMark": { disabled: true, hidden: true }
    };

    constructor() {
        core.Locale.getInstance(IBWRCClient.LOCAL_URI_PREFIX + "locale/ibwrcclient.properties");

        try {
            this.userInfo = ibw.getUserInfo();
            if (core.Utils.isValid(this.userInfo)) {
                this.rcClient = new rc.Client(this.clientURL);
                this.rcClient.addListener(rc.Client.EVENT_ERROR, this, this.onError);
                this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
                this.rcClient.addListener(net.HTTPRequest.EVENT_PROGRESS, this, this.onProgress);
                this.rcClient.loadSlots();

                this.addCommandListener("mlSlots|mlPPN|mlEPN|btnRegister|btnDeregister".split("|"));
            }
        } catch (e) {
            ibw.showError(e);
            window.close();
        }
    }

    private clearMenuList(target: any, disabled: boolean) {
        var ml: XULMenuListElement = typeof target == "string" ? <any>document.getElementById(target) : target;
        ml.removeAllItems();
        ml.appendItem(core.Locale.getInstance().getString("defaultValues.pleaseSelect"), null);
        ml.selectedIndex = 0;
        ml.disabled = disabled;
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
                case "mlPPN":
                    this.onSelectPPN(<XULCommandEvent>ev);
                    break;
                case "mlEPN":
                    this.onSelectEPN(<XULCommandEvent>ev);
                    break;
                case "btnRegister":
                    this.onRegister(<XULCommandEvent>ev);
                    break;
                case "btnDeregister":
                    this.onDeregister(<XULCommandEvent>ev);
                    break;
            }
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

        for (var i in slot.entries) {
            var entry: rc.Entry = slot.entries[i];
            mlPPN.appendItem(entry.ppn, entry.ppn);
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

        this.clearMenuList("mlPPN", true);
        this.clearMenuList("mlEPN", true);

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

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        if (!PPN.isEmpty()) {
            ibw.getActiveWindow().command("f ppn " + PPN, false);
            this.copys = ibw.getCopys();

            var entry = this.slot.getEntryForPPN(PPN);

            this.clearMenuList(mlEPN, false);

            var selectedItem: XULSelectControlItemElement = null;

            for (var i in this.copys) {
                var copy: ibw.Copy = this.copys[i];

                if (copy == null || !copy.type.startsWith("k")) continue;

                var item = mlEPN.appendItem("({0}) {1}".format(copy.epn, copy.shelfmark || ""), i);

                if (entry != null && entry.epn == copy.epn) {
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
                        disabled = this.slot.getEntryForEPN(copy.epn) == null;
                        break;
                    case "cbPresence":
                        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
                        cbPresence.checked = disabled = copy.hasRegistered();
                        break;
                    case "cbShelfMark":
                        disabled = this.slot.getEntryForEPN(copy.epn) == null && !copy.hasRegistered();
                        break;
                    case "boxBundle":
                        hidden = !copy.isBundle;
                        break;
                    case "boxShelfMark":
                        hidden = false; // this.slot.getEntryForEPN(copy.epn) != null || copy.hasRegistered();
                        break;
                    case "tbLocation":
                        disabled = copy.hasRegistered();
                        break;
                    case "tbShelfMark":
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
        } else {
            for (var e in this.elementStates) {
                this.setDisabledState(e, true);
                this.setHiddenState(e, this.elementStates[e].hidden);
            }
        }
    }

    /**
     * Event handler for register button.
     * 
     * @param ev the command event
     */
    onRegister(ev: XULCommandEvent) {
        var btn: XULMenuListElement = <any>ev.currentTarget;

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value) || mlEPN.selectedIndex != 0 && mlEPN.selectedIndex - 1];

        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
        var cbShelfMark: XULCheckboxElement = <any>document.getElementById("cbShelfMark");
        var tbLocation: XULTextBoxElement = <any>document.getElementById("tbLocation");
        var tbShelfMark: XULTextBoxElement = <any>document.getElementById("tbShelfMark");

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
                        tbLocation.value,
                        cbPresence.checked || !cbShelfMark.checked ? copy.shelfmark : tbShelfMark.value,
                        this.defaultIndicator + (copy.isBundle ? " \\ c" : "")
                    )
                );
                if (!cbPresence.checked)
                    ibw.getTitle().insertText(IBWRCClient.FORMAT_4801.format(this.clientURL + "/rc/" + this.slot.id, this.slot.id));
                ibw.getTitle().insertText(IBWRCClient.FORMAT_4802.format(this.slot.id, cat7100));
            }

            // save title
            if (ibw.simulateKey("FR")) {
                this.rcClient.addListener(rc.Client.EVENT_COPY_REGISTERED, this, this.onRegisterComplete)
                this.rcClient.registerCopy(this.slot.id, this.slot.getEntryForPPN(copy.ppn).id, copy.epn);
            }
        }
    }

    /**
     * Callback method after successfully registration of copy.
     * 
     * @param delegate the delegating rc.CLient
     */
    onRegisterComplete(delegate: rc.Client) {
        delegate.clearListenersByEvent(rc.Client.EVENT_COPY_REGISTERED);
        this.updateStatusbar(delegate.statusText);

        ibw.messageBox("Info", core.Locale.getInstance().getString("client.status.registerCopy.done"), ibw.MESSAGE_INFO);

        var mlSlots: XULMenuListElement = <any>document.getElementById("mlSlots");
        mlSlots.doCommand();
    }
       
    /**
     * Event handler for deregister button.
     * 
     * @param ev the command event
     */
    onDeregister(ev: XULCommandEvent) {
        var btn: XULMenuListElement = <any>ev.currentTarget;

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value)];

        if (ibw.command("k e" + copy.num)) {
            var backup = copy.getBackup(this.slot.id);
            if (backup != null) {
                var cat7100 = IBWRCClient.FORMAT_7100.format(backup.location, backup.shelfmark, backup.loanIndicator);

                if (ibw.getTitle().find(IBWRCClient.FORMAT_4801.format(this.clientURL + "/rc/" + this.slot.id, this.slot.id).trim(), true, false, false))
                    ibw.getTitle().deleteToEndOfLine();

                if (ibw.getTitle().find(IBWRCClient.FORMAT_4802.format(this.slot.id, cat7100).trim(), true, false, false))
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
    * Callback method after successfully deregistration of copy.
    * 
    * @param delegate the delegating rc.CLient
    */
    onDeregisterComplete(delegate: rc.Client) {
        delegate.clearListenersByEvent(rc.Client.EVENT_COPY_DEREGISTERED);
        this.updateStatusbar(delegate.statusText);

        ibw.messageBox("Info", core.Locale.getInstance().getString("client.status.deregisterCopy.done"), ibw.MESSAGE_INFO);

        var mlSlots: XULMenuListElement = <any>document.getElementById("mlSlots");
        mlSlots.doCommand();
    }
}

function onLoad() {
    try {
        new IBWRCClient();
    } catch (e) {
        ibw.showError(e);
    }
}