/// <reference path="definitions/XUL.d.ts" />

/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />
/// <reference path="ibw/Application.ts" />

class IBWRCClient {
    public static VERSION: string = "@@VERSION";
    public static REVISION: string = "@@REVISION";

    public static localURIPrefix: string = "chrome://IBWRCClient/";

    private rcClient: rc.Client;
    
    // preference based or default variables
    private clientURL: string = "http://141.24.167.11:8291/mir";
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
        "btnUnRegister": { disabled: true, hidden: false },
        "boxBundle": { disabled: true, hidden: true },
        "boxShelfMark": { disabled: true, hidden: true }
    };

    constructor() {
        core.Locale.getInstance(IBWRCClient.localURIPrefix + "locale/ibwrcclient.properties");

        try {
            this.userInfo = ibw.getUserInfo();
            if (core.Utils.isValid(this.userInfo)) {
                this.rcClient = new rc.Client(this.clientURL);
                this.rcClient.addListener(rc.Client.EVENT_ERROR, this, this.onError);
                this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
                this.rcClient.addListener(net.HTTPRequest.EVENT_PROGRESS, this, this.onProgress);
                this.rcClient.loadSlots();

                this.addCommandListener("mlSlots|mlPPN|mlEPN|btnRegister|btnUnRegister".split("|"));
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
            var mlRC: XULMenuListElement = <any>document.getElementById(elms[i]);

            mlRC.removeAllItems();
            mlRC.appendItem(core.Locale.getInstance().getString("defaultValues.pleaseSelect"), null);
            mlRC.selectedIndex = 0;

            for (var i in slots) {
                var slot: rc.Slot = slots[i];

                if (slot.eOnly || !slot.id.startsWith(this.userInfo.libId)) continue;
                mlRC.appendItem("({0}|{1}) {2}".format(slot.id, core.Locale.getInstance().getString("slot.status." + rc.Status[slot.status]), slot.title), slot.id);
            }
        }
    }

    /**
     * Callback method of loaded Slot. Used to display Entry (OPC records) in MenuListElement.
     * 
     * @param delegate the delegating rc.CLient
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
     * Callback method of selected Slot. Used to trigger Entry loading.
     * 
     * @param ev the command event
     */
    onSelectSlot(ev: XULCommandEvent) {
        var mlRC: XULMenuListElement = <any>ev.currentTarget;
        var slotId = mlRC.selectedItem.value;

        if (!slotId.isEmpty()) {
            this.rcClient.addListener(rc.Client.EVENT_SLOT_LOADED, this, this.onSlotLoaded);
            this.rcClient.loadSlot(slotId);
        } else {
            this.clearMenuList("mlPPN", true);
            this.clearMenuList("mlEPN", true);

            for (var e in this.elementStates) {
                this.setDisabledState(e, this.elementStates[e].disabled);
                this.setHiddenState(e, this.elementStates[e].hidden);
            }
        }
    }

    /**
     * Callback method of selected PPN. Used to trigger EPN loading.
     * 
     * @param ev the command event
     */
    onSelectPPN(ev: XULCommandEvent) {
        var mlPPN: XULMenuListElement = <any>ev.currentTarget;
        var PPN = mlPPN.selectedItem.value;

        for (var e in this.elementStates) {
            this.setDisabledState(e, this.elementStates[e].disabled);
            this.setHiddenState(e, this.elementStates[e].hidden);
        }

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        if (!PPN.isEmpty()) {
            ibw.getActiveWindow().command("f ppn " + PPN, false);
            this.copys = ibw.getCopys();
            var entry = this.slot.getEntryForPPN(PPN);

            this.clearMenuList(mlEPN, false);
            var selectedIndex: number = 0;

            for (var i in this.copys) {
                var copy: ibw.Copy = this.copys[i];

                if (!copy.type.startsWith("k")) continue;

                if (entry != null && entry.epn == copy.epn)
                    selectedIndex = parseInt(i) + 1;

                mlEPN.appendItem("({0}) {1}".format(copy.epn, copy.shelfmark || ""), i);
            }

            if (selectedIndex != 0) {
                mlEPN.selectedIndex = selectedIndex;
                mlEPN.doCommand();
            }
        } else {
            this.clearMenuList(mlEPN, true);
        }
    }
    
    /**
     * Callback method of selected EPN.
     * 
     * @param ev the command event
     */
    onSelectEPN(ev: XULCommandEvent) {
        var mlEPN: XULMenuListElement = <any>ev.currentTarget;
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value) || mlEPN.selectedIndex != 0 && mlEPN.selectedIndex - 1];

        if (core.Utils.isValid(copy)) {
            for (var e in this.elementStates) {
                var disabled: boolean = false;
                var hidden: boolean = this.elementStates[e].hidden;

                switch (e) {
                    case "btnRegister":
                        disabled = this.slot.getEntryForEPN(copy.epn) != null;
                        break;
                    case "btnUnRegister":
                        disabled = this.slot.getEntryForEPN(copy.epn) == null;
                        break;
                    case "cbShelfMark":
                        disabled = this.slot.getEntryForEPN(copy.epn) == null && !copy.hasRegistered();
                        break;
                    case "boxBundle":
                        hidden = !copy.isBundle;
                        break;
                    case "boxShelfMark":
                        hidden = this.slot.getEntryForEPN(copy.epn) != null || copy.hasRegistered();
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
                this.setDisabledState(e, this.elementStates[e].disabled);
                this.setHiddenState(e, this.elementStates[e].hidden);
            }
        }
    }

    onRegister(ev: XULCommandEvent) {
        var btn: XULMenuListElement = <any>ev.currentTarget;

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        var copy: ibw.Copy = this.copys[parseInt(mlEPN.selectedItem.value) || mlEPN.selectedIndex != 0 && mlEPN.selectedIndex - 1];

        var cbPresence: XULCheckboxElement = <any>document.getElementById("cbPresence");
        var cbShelfMark: XULCheckboxElement = <any>document.getElementById("cbShelfMark");
        var tbLocation: XULTextBoxElement = <any>document.getElementById("tbLocation");
        var tbShelfMark: XULTextBoxElement = <any>document.getElementById("tbShelfMark");

        if (ibw.command("k e" + copy.num)) {
            if (core.Utils.isValid(copy.backup) && copy.backup.length != 0) {
                // TODO make me work
            } else {
                var oldCat: string = ibw.getTitle().findTag("7100", 0, false, true, false);
                var newCat: string = "!" + tbLocation.value + "!"
                    + (cbPresence.checked || !cbShelfMark.checked ? copy.shelfmark : tbShelfMark.value) + " @ " + this.defaultIndicator
                    + (copy.isBundle ? " \\ c" : "");

                ibw.getTitle().insertText(newCat + "\n");
                if (!cbPresence.checked)
                    ibw.getTitle().insertText("4801 Band im Semesterapparat <a href=\"" + this.clientURL + "/rc/" + this.slot.id + "\" target=\"_blank\">" + this.slot.id + "</a>.\n");
                ibw.getTitle().insertText("4802 " + this.slot.id + " SSTold " + oldCat);
            }
        }
    }
}

function onLoad() {
    try {
        new IBWRCClient();
    } catch (e) {
        ibw.showError(e);
    }
}