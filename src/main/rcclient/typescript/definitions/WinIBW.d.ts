/// <reference path="XPCOM.d.ts" />

interface IError extends Error {
    fileName?: string;
    lineNumber?: number;
    columnNumber?: number;
    stack?: string;

    toSource?();
}

interface IActiveWindow extends nsISupports {
    materialCode: string;
    windowID: number;
    title: IEditControl;
    clipboard: string;
    codedData: boolean;
    noviceMode: boolean;
    caption: string;
    status: string;
    titleCopyFile: string;
    commandLine: string;
    messages: IMessageCollection;

    closeWindow();
    command(cmd: string, b: boolean);
    copyTitle(): string;
    pasteTitle();
    getVariable(name: string): string;
    setVariable(name: string, value: string);
    simulateIBWKey(key: string);
    getLastCommand(): string;
    processURL(url: string);
    pressButton(btn: string);
    showMessage(msg: string, type: number);
    appendMessage(msg: string, type: number);
    findString(str: string): boolean;
    findTagContent(tag: string, num: number, withCat: boolean): string;
}

interface IWindow extends nsISupports {
    windowID: number;
    visible: boolean;
    text: string;

    minimize();
    maximize();
    restore();
    close();
    activate();
}

interface IWindowCollection extends nsISupports {
    count: number;
    item(index: number): IWindow;
    getWindowSnapshot(): IWindowSnapshot;
    restoreWindowSnapshot(window: IWindowSnapshot);
}

interface IWindowSnapshot extends nsISupports {
    getSnapShot();
}

interface IMessage extends nsISupports {
    text: string;
    type: number;
}

interface IMessageCollection extends nsISupports {
    count: number;
    item(index: number): IMessage;
}

interface IApplication extends nsISupports {
    width: number;
    height: number;
    activeWindow: IActiveWindow;
    overwriteMode: boolean;
    windows: IWindowCollection;
    language: string;
    receivedMessageOnly: boolean;
    protectedColor: number;
    ignoredColor: number;
    newWindow: number;

    cascadeWindows();
    tileWindowsVertical();
    tileWindowsHorizontal();
    messageBox(title: string, msg: string, icon: string);
    callStdScriptFunction(script: string): boolean;
    getStdScriptFunctionList(): string;
    getCommandLineContent(): string;
    activateWindow(n: number): boolean;
    activateCommandLine();
    activateView();
    closeWindow(n: number): boolean;
    addSyntaxColor(a: string, b: string, n: number);
    removeSyntaxColor(a: string, b: string);
    getFolderPath(n: number): string;
    activate();
    shellExecute(a: string, n: number, b: string, c: string);
    connect(a: string, b: string): boolean;
    downloadToFile(a: string, b: string): boolean;
    reloadUserScripts();
    endUserScriptsLearning();
    writeProfileString(prefix: string, name: string, val: string): boolean;
    writeProfileInt(prefix: string, name: string, val: number): boolean;
    getProfileString(prefix: string, name: string, defaultValue: string): string;
    getProfileInt(prefix: string, name: string, defaultValue: number): number;
    addPasteReplacement(a: string, b: string);
    clearPasteReplacements();
    cleanUpCommandLineHistory();
    update();
    disableScreenUpdate(b: boolean);
    switchRTL();
    switchSpellCheck();
    writeUpdateDate(date: string);
    readUpdateDate(): string;
    getSIdLBS(): string;
    setSIdLBS(id: string);
}

interface IEditControl extends nsISupports {
    selStart: number;
    selEnd: number;
    canCutSelection: boolean;
    canCopySelection: boolean;
    canReplaceSelection: boolean;
    canPaste: boolean;
    canUndo: boolean;
    canRedo: boolean;
    currentField: string;
    currentLineNumber: number;
    tagAndSelection: string;
    selection: string;
    tag: string;
    changed: boolean;

    cut();
    copy();
    copyPlus();
    paste();
    pasteTitle();
    undo();
    redo();
    copyToFile(name: string): boolean;
    pasteFromFile(name: string): boolean;
    deleteSelection();
    deleteToEndOfLine();
    deleteLine(index: number);
    deleteWord(index: number);
    find(str: string, b1: boolean, b2: boolean, b3: boolean): boolean;
    replace(str: string);
    replace2(str: string);
    replaceAll(s1: string, s2: string, b1: boolean, b2: boolean);
    charLeft(count: number, b: boolean);
    charRight(count: number, b: boolean);
    lineDown(count: number, b: boolean);
    lineUp(count: number, b: boolean);
    pageDown(count: number, b: boolean);
    pageUp(count: number, b: boolean);
    endOfBuffer(b: boolean);
    endOfField(b: boolean);
    startOfBuffer(b: boolean);
    startOfField(b: boolean);
    wordLeft(count: number, b: boolean);
    wordRight(count: number, b: boolean);
    insertText(text: string);
    insertText2(text: string);
    joinLines();
    setSelection(s: number, e: number, b: boolean);
    selectAll();
    selectNone();
    refresh();
    addWord();
    getSpellCheckState(): string;
    getSpellCheckEnabled(): boolean;
    setSpellCheckEnabled(b: boolean);
    getRTLEnabled(): boolean;
    setRTLEnabled(b: boolean);
    getUserWantedLangCode(): string;
    setUserWantedLangCode(code: string);
    findTag(tag: string, num: number, withCat: boolean, jumpTo: boolean, b3: boolean): string;
    findTag2(tag: string, num: number, withCat: boolean, jumpTo: boolean, b3: boolean): string;
    hasFocus(): boolean;
    setFocus();
    getDOMElement(): Element;
    transferData();
}