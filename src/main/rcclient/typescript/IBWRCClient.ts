/// <reference path="definitions/XUL.d.ts" />

/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />
/// <reference path="ibw/Application.ts" />

class IBWRCClient {
    public static VERSION: string = "@@VERSION";
    public static REVISION: string = "@@REVISION";

    public static localURIPrefix: string = "chrome://IBWRCClient/";

    private rcClient: rc.Client;
    private userInfo: ibw.UserInfo;

    private slot: rc.Slot;
    private copys: ibw.Copys;

    constructor() {
        core.Locale.getInstance(IBWRCClient.localURIPrefix + "locale/ibwrcclient.properties");

        try {
            this.userInfo = ibw.getUserInfo();
            if (core.Utils.isValid(this.userInfo)) {
                //this.rcClient = new rc.Client("https://dbttest.thulb.uni-jena.de/mir");
                this.rcClient = new rc.Client("http://141.24.167.11:8291/mir");
                this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
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

    private addCommandListener(elms: Array<string>) {
        for (var e in elms) {
            var elm: XULControlElement = <any>document.getElementById(elms[e]);
            if (core.Utils.isValid(elm)) {
                elm.addEventListener("command", this, false);
            }
        }
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
            }
        }
    }

    /**
     * Callback method of loaded Slot list. Used to display Slots in MenuListElement.
     * 
     * @param delegate the delegating rc.Client
     */
    onSlotListLoaded(delegate: rc.Client) {
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

        var elms: Array<string> = "cbShelfMark|tbShelfMark|cbPresence|tbLocation|btnRegister|btnUnRegister".split("|");
        for (var e in elms) {
            this.setDisabledState(elms[e], true);
        }

        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");
        if (!PPN.isEmpty()) {
            ibw.getActiveWindow().command("f ppn " + PPN, false);
            this.copys = new ibw.Copys(ibw.getCopys());
            var entry = this.slot.getEntryForPPN(PPN);

            this.clearMenuList(mlEPN, false);
            var selectedIndex: number = 0;

            for (var i = 0; i < this.copys.length(); i++) {
                var copy: ibw.Copy = this.copys.item(i);

                if (!copy.type.startsWith("k")) continue;

                if (entry != null && entry.epn == copy.epn)
                    selectedIndex = i + 1;

                mlEPN.appendItem("({0}) {1}".format(copy.epn, copy.shelfmark || ""), i.toString());
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
        var copy: ibw.Copy = this.copys.item(parseInt(mlEPN.selectedItem.value) || mlEPN.selectedIndex != 0 && mlEPN.selectedIndex - 1);

        var elms: Array<string> = "cbShelfMark|tbShelfMark|cbPresence|tbLocation|btnRegister|btnUnRegister".split("|");
        var mlEPN: XULMenuListElement = <any>document.getElementById("mlEPN");

        if (core.Utils.isValid(copy)) {
            for (var e in elms) {
                var state: boolean = false;
                if (("btnRegister" == elms[e] && this.slot.getEntryForEPN(copy.epn) != null) ||
                    ("btnUnRegister" == elms[e] && this.slot.getEntryForEPN(copy.epn) == null) ||
                    // disable checkbox "presence" if selected epn not matching one within slot and any copy has already registered 
                    ("cbPresence" == elms[e] && this.slot.getEntryForEPN(copy.epn) == null && this.copys.hasRegistered()))
                    state = true;

                this.setDisabledState(elms[e], state);
            }

            var tbShelfMark: XULTextBoxElement = <any>document.getElementById("tbShelfMark");
            tbShelfMark.value = copy.shelfmark;
            var tbLocation: XULTextBoxElement = <any>document.getElementById("tbLocation");
            tbLocation.value = copy.location;
        } else {
            for (var e in elms) {
                this.setDisabledState(elms[e], true);
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