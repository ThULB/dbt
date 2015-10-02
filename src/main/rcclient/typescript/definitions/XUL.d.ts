interface XULElement extends HTMLElement {
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