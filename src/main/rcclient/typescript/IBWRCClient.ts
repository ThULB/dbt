/// <reference path="definitions/XUL.d.ts" />
/// <reference path="definitions/WinIBW.d.ts" />
/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />

class IBWRCClient {
    private rcClient: rc.Client;
    private activeWindow: IActiveWindow;

    constructor() {
        this.rcClient = new rc.Client("https://dbttest.thulb.uni-jena.de/mir");
        this.rcClient.addListener(rc.Client.EVENT_SLOT_LIST_LOADED, this, this.onSlotListLoaded);
        this.rcClient.loadSlots();
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
     * Returns the active WinIBW window.
     * 
     * @return the active WinIBW window
     */
    getActiveWindow(): IActiveWindow {
        if (!core.Utils.isValid(this.activeWindow)) {
            var ibw: IApplication = Components.classes["@oclcpica.nl/kitabapplication;1"].getService(Components.interfaces.IApplication);
            this.activeWindow = ibw.activeWindow;
        }

        return this.activeWindow;
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

                if (slot.eOnly) continue;

                mlRC.appendItem("(" + slot.id + "|" + core.Locale.getInstance().getString("slot.status." + rc.Status[slot.status]) + ") " + slot.title, slot.id);
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

        mlPPN.removeAllItems();
        mlPPN.appendItem(core.Locale.getInstance().getString("defaultValues.pleaseSelect"), null);
        mlPPN.selectedIndex = 0;
        mlPPN.disabled = false;

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

        if (core.Utils.isValid(slotId)) {
            this.rcClient.addListener(rc.Client.EVENT_SLOT_LOADED, this, this.onSlotLoaded);
            this.rcClient.loadSlot(slotId);
        } else {
            var mlPPN: XULMenuListElement = <any>document.getElementById("mlPPN");
            mlPPN.disabled = true;
        }
    }

    /**
     * Callback method of selected PPN. Used to trigger EPN loading.
     * 
     * @param ev the command event
     */
    onSelectPPN(ev: XULCommandEvent) {
        var mlPPN: XULMenuListElement = <any>ev.currentTarget;
        var ppn = mlPPN.selectedItem.value;

        if (core.Utils.isValid(ppn)) {
            this.getActiveWindow().command("f ppn " + ppn, false);
        }
    }
}

function onLoad() {
    core.Locale.getInstance("chrome://IBWRCClient/locale/ibwrcclient.properties");
    new IBWRCClient();
}