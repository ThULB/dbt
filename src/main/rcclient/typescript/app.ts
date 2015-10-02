/// <reference path="definitions/XUL.d.ts" />
/// <reference path="rc/Client.ts" />

class IBWRCClient {
    private rcClient: rc.Client;

    constructor() {
        this.rcClient = new rc.Client("https://dbttest.thulb.uni-jena.de/mir");
        this.rcClient.addListener(rc.Client.SLOT_LIST_LOADED, this, this.onSlotListLoaded);
        this.rcClient.loadSlots();
    }

    onSlotListLoaded(delegate: rc.Client) {
        var slots: Array<rc.Slot> = delegate.getSlots();

        var mlRC: XULElement = <XULElement>document.getElementById("mlESA");

        mlRC.removeAllItems();

        for (var i in slots) {
            var slot: rc.Slot = slots[i];

            mlRC.appendItem(slot.title, slot.id);
        }
    }
}

function onLoad() {
    new IBWRCClient();
}