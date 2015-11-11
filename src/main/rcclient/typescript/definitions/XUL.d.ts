/// <reference path="XPCOM.d.ts" />

interface XULWindow extends nsISupports, Window {
    sizeToContent();
    openDialog(url: string, name: string, features: string, ...args): XULWindow;
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

interface XULCommandEvent extends Event {
    ctrlKey: boolean;
    shiftKey: boolean;
    altKey: boolean;
    metaKey: boolean;
    sourceEvent: Event;

    initCommandEvent(typeArg: string, canBubbleArg: boolean, cancelableArg: boolean, viewArg: Window, detailArg: number, ctrlKeyArg: boolean, altKeyArg: boolean, shiftKeyArg: boolean, metaKeyArg: boolean, sourceEvent: Event)
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
    boxObject: nsIBoxObject;
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

interface XULLabeledControlElement extends XULControlElement {
    crop: string;
    image: string;
    label: string;
    accessKey: string;
    command: string;
}

interface XULCheckboxElement extends XULLabeledControlElement {
    // constants
    CHECKSTATE_UNCHECKED: number;
    CHECKSTATE_CHECKED: number;
    CHECKSTATE_MIXED: number;

    checked: boolean;
    checkState: number;
    autoCheck: boolean;
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

interface XULTextBoxElement extends XULControlElement {
    inputField: Node;
    textLength: number;
    maxLength: number;
    size: number;
    selectionStart: number;
    selectionEnd: number;
    value: string;
    type: string;

    select();
    setSelectionRange(selectionStart: number, selectionEnd: number);
}

interface XULProgressMeterElement extends XULControlElement {
    accessibleType: number;
    max: number;
    mode: string;
    value: string;
}

interface XULDescriptionElement extends XULElement {
    disabled: boolean;
    crop: boolean;
    value: string;
}

interface XULLabelElement extends XULDescriptionElement {
    accessKey: string;
    control: string;
}
