/// <reference path="XPCOM.d.ts" />

interface nsIController extends nsISupports {
    isCommandEnabled(command: string): boolean;
    supportsCommand(command: string): boolean;
    doCommand(command: string);
    onEvent(eventName: string);
}

interface XULCommandDispatcher extends nsISupports {
    focusedElement: Element;
    focusedWindow: Window;
    suppressFocusScroll: boolean;

    addCommandUpdater(updater: Element, events: string, targets: string);
    removeCommandUpdater(updater: Element);
    updateCommands(eventName: string);
    getControllerForCommand(command: string): nsIController;
    getControllers();
    advanceFocus();
    rewindFocus();
    advanceFocusIntoSubtree(elt: Element);
}

interface XULDocument extends nsISupports, Document {
    popupNode: Node;
    popupRangeParent: Node;
    popupRangeOffset: number;
    tooltipNode: Node;
    commandDispatcher: XULCommandDispatcher;
    width: number;
    height: number;

    addBroadcastListenerFor(broadcaster: Element, observer: Element, attr: string);
    removeBroadcastListenerFor(broadcaster: Element, observer: Element, attr: string);
    persist(id: string, attr: string);
    getBoxObjectFor(elt: Element): nsIBoxObject;
    loadOverlay(url: string, aObserver: nsIObserver);
}

interface XULElement extends Element {
    className: string;
    align: string;
    dir: string;
    flex: string;
    flexGroup: string;
    ordinal: string;
    orient: string;
    pack: string;
    hidden: boolean;
    collapsed: boolean;
    observes: boolean;
    menu: string;
    contextMenu: string;
    tooltip: string;
    width: string;
    height: string;
    minWidth: string;
    minHeight: string;
    maxWidth: string;
    maxHeight: string;
    persist: string;
    left: string;
    top: string;
    datasources: string;
    ref: string;
    tooltipText: string;
    statusText: string;
    allowEvents: boolean;

    style: CSSStyleDeclaration;

    focus();
    blur();
    click();
    doCommand();

    getElementsByAttribute(name: string, value: string): NodeList;
    getElementsByAttributeNS(namespaceURI: string, name: string, value: string): NodeList;

    appendItem(label: string, value?: string, description?: string): XULElement;
    contains(item: XULElement): boolean;
    getIndexOfItem(item: XULElement): number;
    getItemAtIndex(index: number): XULElement;
    insertItemAt(index: number, label: string, value?: string);
    removeAllItems();
    removeItemAt(index: number): XULElement;
    select();
}

interface XULControlElement extends XULElement {
    disabled: boolean;
    tabIndex: number;
}

interface XULSelectControlItemElement extends XULElement {
    disabled: boolean;
    crop: string;
    image: string;
    label: string;
    accessKey: string;
    command: string;
    value: string;
    selected: boolean;
    control: XULSelectControlElement;
}

interface XULSelectControlElement extends XULControlElement {
    selectedItem: XULSelectControlItemElement;
    selectedIndex: number;
    value: string;
    itemCount: number;

    appendItem(label: string, value: string): XULSelectControlItemElement;
    insertItemAt(index: number, label: string, value: string): XULSelectControlItemElement;
    removeItemAt(index: number): XULSelectControlItemElement;
    getIndexOfItem(item: XULSelectControlItemElement): number;
    getItemAtIndex(index: number): XULSelectControlItemElement;
}

interface XULMenuListElement extends XULSelectControlElement {
    editable: boolean;
    open: boolean;
    label: string;
    crop: string;
    image: string;
    inputField: Node;
}