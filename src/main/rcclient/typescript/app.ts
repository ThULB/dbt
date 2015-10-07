/// <reference path="definitions/XUL.d.ts" />
/// <reference path="core/Locale.ts" />
/// <reference path="rc/Client.ts" />

class IBWRCClient {
    private rcClient: rc.Client;

    constructor() {
        this.rcClient = new rc.Client("https://dbttest.thulb.uni-jena.de/mir");
        this.rcClient.addListener(rc.Client.SLOT_LIST_LOADED, this, this.onSlotListLoaded);
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
            }
        }
    }

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
                mlRC.appendItem(slot.title, slot.id);
            }
        }
    }

    onSlotLoaded(delegate: rc.Client, slot: rc.Slot) {
        delegate.clearListenersByEvent(rc.Client.SLOT_LOADED);

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

    onSelectSlot(ev: XULCommandEvent) {
        var mlRC: XULMenuListElement = <any>ev.currentTarget;
        var slotId = mlRC.selectedItem.value;

        if (slotId != null) {
            this.rcClient.addListener(rc.Client.SLOT_LOADED, this, this.onSlotLoaded);
            this.rcClient.loadSlot(slotId);
        } else {
            var mlPPN: XULMenuListElement = <any>document.getElementById("mlPPN");
            mlPPN.disabled = true;
        }
    }
}

function onLoad() {
    core.Locale.getInstance("chrome://IBWRCClient/locale/ibwrcclient.properties");
    new IBWRCClient();
}