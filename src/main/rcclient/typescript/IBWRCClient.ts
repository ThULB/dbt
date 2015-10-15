/// <reference path="definitions/XUL.d.ts" />

/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />
/// <reference path="ibw/Application.ts" />

class IBWRCClient {
    public static localURIPrefix: string = "chrome://IBWRCClient/";

    private rcClient: rc.Client;
    private userInfo: ibw.UserInfo;

    constructor() {
        core.Locale.getInstance(IBWRCClient.localURIPrefix + "locale/ibwrcclient.properties");

        try {
            this.userInfo = ibw.getUserInfo();
            if (core.Utils.isValid(this.userInfo)) {
                //this.rcClient = new rc.Client("https://dbttest.thulb.uni-jena.de/mir");
                this.rcClient = new rc.Client("http://141.24.167.11:8291/mir");
                this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
                this.rcClient.loadSlots();
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
            mlRC.addEventListener("command", this, false);

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
        mlPPN.addEventListener("command", this, false);

        this.clearMenuList(mlPPN, false);
        this.clearMenuList("mlCopy", true);

        for (var i in slot.entries) {
            var entry: rc.Entry = slot.entries[i];
            mlPPN.appendItem(entry.ppn, entry.ppn);
        }
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
            this.clearMenuList("mlCopy", true);
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

        var mlCopy: XULMenuListElement = <any>document.getElementById("mlCopy");
        if (!PPN.isEmpty()) {
            ibw.getActiveWindow().command("f ppn " + PPN, false);
            var copys: Array<ibw.Copy> = ibw.getCopys();

            mlCopy.addEventListener("command", this, false);

            this.clearMenuList(mlCopy, false);

            for (var i in copys) {
                var copy: ibw.Copy = copys[i];
                mlCopy.appendItem("({0}) {1}".format(copy.epn, copy.shelfmark || ""), "k e {0}".format(copy.num));
            }
        } else {
            this.clearMenuList(mlCopy, true);
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