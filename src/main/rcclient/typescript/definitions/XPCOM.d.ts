interface Components {
    classes: [nsISupports];
    interfaces: any;
    results: any;

    isSuccessCode(aStatus): boolean;
}
declare var Components: Components;

interface nsIInterfaceRequestor {
    getInterface(uuid: any, result: any);
}

interface nsISupports extends nsIInterfaceRequestor {
    QueryInterface(uuid: any, result?: any);
}

interface nsISimpleEnumerator extends nsISupports {
    getNext(): nsISupports;
    hasMoreElements(): boolean;
}

interface nsIURI extends nsISupports {
    asciiHost: string;
    asciiSpec: string;
    hasRef: boolean;
    host: string;
    hostPort: string;
    originCharset: string;
    password: string;
    path: string;
    port: number;
    prePath: string;
    ref: string;
    scheme: string;
    spec: string;
    specIgnoringRef: string;
    username: string;
    userPass: string;

    clone(): nsIURI;
    cloneIgnoringRef(): nsIURI;
    equals(aURI: nsIURI): boolean;
    equalsExceptRef(aURI: nsIURI): boolean;
    resolve(aRelativePath: string): string;
    schemeIs(scheme: string): boolean;
}

interface nsIProtocolHandler extends nsISupports {
    // constants
    URI_STD: number;
    URI_NORELATIVE: number;
    URI_NOAUTH: number;
    URI_INHERITS_SECURITY_CONTEXT: number;
    URI_FORBIDS_AUTOMATIC_DOCUMENT_REPLACEMENT: number;
    URI_LOADABLE_BY_ANYONE: number;
    URI_DANGEROUS_TO_LOAD: number;
    URI_IS_UI_RESOURCE: number;
    URI_IS_LOCAL_FILE: number;
    URI_LOADABLE_BY_SUBSUMERS: number;
    URI_NON_PERSISTABLE: number;
    URI_DOES_NOT_RETURN_DATA: number;
    URI_IS_LOCAL_RESOURCE: number;
    URI_OPENING_EXECUTES_SCRIPT: number;
    ALLOWS_PROXY: number;
    ALLOWS_PROXY_HTTP: number;

    defaultPort: number;
    protocolFlags: number;
    scheme: string;

    allowPort(port: number, scheme: string): boolean;
    newChannel(aURI: nsIURI): nsIChannel;
    newURI(aSpec: string, aOriginCharset: string, aBaseURI: nsIURI): nsIURI;
}

interface nsIInputStream extends nsISupports {
    available(): number;
    close();
    isNonBlocking(): boolean;
}

interface nsIScriptableInputStream extends nsIInputStream {
    init(aInputStream: nsIInputStream);
    read(aCount: number): string;
    readBytes(aCount: number): string;
}

interface nsIStringInputStream extends nsIScriptableInputStream {
    setData(data: string, dataLen: number);
}

interface nsIScriptableUnicodeConverter extends nsISupports {
    charset: string;

    ConvertFromUnicode(aSrc: string): string;
    Finish(): string;
    ConvertToUnicode(aSrc: string): string;
    convertFromByteArray(aData: any, aCount: number): string;
    convertToByteArray(aString: string, aLen?: number): any;
    convertToInputStream(aString: string): nsIInputStream;
}

interface nsIRequestObserver extends nsISupports {
    onStartRequest(aRequest: nsIRequest, aContext: nsISupports);
    onStopRequest(aRequest: nsIRequest, aContext: nsISupports, aStatusCode: number);
}

interface nsIStreamListener extends nsIRequestObserver {
    onDataAvailable(aRequest: nsIRequest, aContext: nsISupports, aInputStream: nsIInputStream, aOffset: number, aCount: number);
}

declare var nsIStreamListener: {
    prototype: nsIStreamListener;
    new (): nsIStreamListener;
}

interface nsIProgressEventSink extends nsISupports {
    onProgress(aRequest: nsIRequest, aContext: nsISupports, aProgress: number, aProgressMax: number);
    onStatus(aRequest: nsIRequest, aContext: nsISupports, aStatus: number, aStatusArg: string);
}

interface nsIHttpEventSink extends nsISupports {
    onRedirect(httpChannel: nsIHttpChannel, newChannel: nsIChannel);
}

interface nsILoadGroup extends nsISupports {
    activeCount: number;
    defaultLoadRequest: nsIRequest;
    groupObserver: nsIRequestObserver;
    notificationCallbacks: nsIInterfaceRequestor;
    requests: nsISimpleEnumerator;

    addRequest(aRequest: nsIRequest, aContext: nsISupports);
    removeRequest(aRequest: nsIRequest, aContext: nsISupports, aStatus: number);
}

interface nsIRequest extends nsISupports {
    // constants
    LOAD_NORMAL: number;
    LOAD_BACKGROUND: number;
    INHIBIT_CACHING: number;
    INHIBIT_PERSISTENT_CACHING: number;
    LOAD_BYPASS_CACHE: number;
    LOAD_FROM_CACHE: number;
    VALIDATE_ALWAYS: number;
    VALIDATE_NEVER: number;
    VALIDATE_ONCE_PER_SESSION: number;
    LOAD_ANONYMOUS: number;

    loadFlags: number;
    loadGroup: nsILoadGroup;
    name: string;
    status: number;

    cancel(aStatus: number);
    isPending(): boolean;
    resume();
    suspend();
}

interface nsIAsyncVerifyRedirectCallback extends nsISupports {
    onRedirectVerifyCallback(result: number);
}

interface nsIChannelEventSink extends nsISupports {
    // constants
    REDIRECT_TEMPORARY: number;
    REDIRECT_PERMANENT: number;
    REDIRECT_INTERNAL: number;

    asyncOnChannelRedirect(oldChannel: nsIChannel, newChannel: nsIChannel, flags: number, callback: nsIAsyncVerifyRedirectCallback);
}

interface nsIChannel extends nsIRequest {
    // constants
    LOAD_DOCUMENT_URI: number;
    LOAD_RETARGETED_DOCUMENT_URI: number;
    LOAD_REPLACE: number;
    LOAD_INITIAL_DOCUMENT_URI: number;
    LOAD_TARGETED: number;
    LOAD_CALL_CONTENT_SNIFFERS: number;
    LOAD_CLASSIFY_URI: number;

    contentCharset: string;
    contentLength: number;
    contentType: string;
    notificationCallbacks: nsIInterfaceRequestor;
    originalURI: nsIURI;
    owner: nsISupports;
    securityInfo: nsISupports;
    URI: nsIURI;

    asyncOpen(aListener: nsIStreamListener, aContext: nsISupports);
    open(): nsIInputStream;
}


interface nsIHttpHeaderVisitor extends nsISupports {
    visitHeader(aHeader: string, aValue: string);
}

interface nsIHttpChannel extends nsIChannel {
    // constants
    REFERRER_POLICY_NO_REFERRER_WHEN_DOWNGRADE: number;
    REFERRER_POLICY_NO_REFERRER: number;
    REFERRER_POLICY_ORIGIN: number;
    REFERRER_POLICY_ORIGIN_WHEN_XORIGIN: number;
    REFERRER_POLICY_UNSAFE_URL: number;

    allowPipelining: boolean;
    redirectionLimit: number;
    referrer: nsIURI;
    requestMethod: string;
    requestSucceeded: boolean;
    responseStatus: number;
    responseStatusText: string;
    referrerPolicy?: number;

    getRequestHeader(aHeader: string): string;
    getResponseHeader(aHeader: string): string;
    isNoCacheResponse(): boolean;
    isNoStoreResponse(): boolean;
    redirectTo(aNewURI: nsIURI);
    setRequestHeader(aHeader: string, aValue: string, aMerge: boolean);
    setResponseHeader(aHeader: string, aValue: string, aMerge: boolean);
    visitRequestHeaders(aVisitor: nsIHttpHeaderVisitor);
    visitResponseHeaders(aVisitor: nsIHttpHeaderVisitor);
}

interface nsIUploadChannel extends nsISupports {
    uploadStream: nsIInputStream;

    setUploadStream(aStream: nsIInputStream, aContentType: string, aContentLength: number);
}

interface nsIIOService extends nsISupports {
    offline: boolean;

    allowPort(aPort: number, aScheme: string): boolean;
    extractScheme(urlString: string): string;
    getProtocolFlags(aScheme: string): number;
    getProtocolHandler(aScheme: string): nsIProtocolHandler;
    newChannel(aSpec: string, aOriginCharset: string, aBaseURI: nsIURI): nsIChannel;
    newChannelFromURI(aURI: nsIURI): nsIChannel;
    newFileURI(aFile: nsIFile): nsIURI;
    newURI(aSpec: string, aOriginCharset: string, aBaseURI: nsIURI): nsIURI;
}

interface nsIFile extends nsISupports {
    // constants
    NORMAL_FILE_TYPE: number
    DIRECTORY_TYPE: number;
    DELETE_ON_CLOSE: number;

    directoryEntries: nsISimpleEnumerator;
    diskSpaceAvailable: number;
    fileSize: number;
    fileSizeOfLink: number;
    followLinks: boolean;
    lastModifiedTime: number
    lastModifiedTimeOfLink: number;
    leafName: string;
    parent: nsIFile;
    path: string;
    permissions: number;
    permissionsOfLink: number;
    persistentDescriptor: string;
    target: string;

    append(node: string);
    appendRelativeNativePath(relativeFilePath: string);
    appendRelativePath(relativeFilePath: string);
    clone(): nsIFile;
    contains(inFile: nsIFile): boolean;
    copyTo(newParentDir: nsIFile, newName: string);
    copyToFollowingLinks(newParentDir: nsIFile, newName: string);
    create(aType: number, permissions: number);
    createUnique(aType: number, permissions: number);
    equals(inFile: nsIFile): boolean;
    exists(): boolean;
    getRelativeDescriptor(fromFile: nsIFile): string;
    initWithFile(aFile: nsIFile);
    initWithNativePath(filePath: string);
    initWithPath(filePath: string);
    isDirectory(): boolean;
    isExecutable(): boolean;
    isFile(): boolean;
    isHidden(): boolean;
    isReadable(): boolean;
    isSpecial(): boolean;
    isSymlink(): boolean;
    isWritable(): boolean;
    launch();
    moveTo(newParentDir: nsIFile, newName: string);
    normalize();
    renameTo(newParentDir: nsIFile, newName: string);
    remove(recursive: boolean);
    reveal();
    setRelativeDescriptor(fromFile: nsIFile, relativeDesc: string);
}

interface nsIController extends nsISupports {
    isCommandEnabled(command: string): boolean;
    supportsCommand(command: string): boolean;
    doCommand(command: string);
    onEvent(eventName: string);
}

interface nsIBoxObject extends nsISupports {
    element: Element;
    parentBox: Element;
    firstChild: Element;
    lastChild: Element;
    nextSibling: Element;
    previousSibling: Element;
    x: number;
    y: number;
    screenX: number;
    screenY: number;
    width: number;
    height: number;

    getPropertyAsSupports(propertyName: string): nsISupports;
    setPropertyAsSupports(propertyName: string, value: nsISupports);
    getProperty(propertyName: string): string;
    setProperty(propertyName: string, propertyValue: string);
    removeProperty(propertyName: string);
}

interface nsIObserver extends nsISupports {
    observe(aSubject: nsISupports, aTopic: string, aData: string);
}

interface nsIStringBundle extends nsISupports {
    formatStringFromID(aID: number, params: string, length: number): string;
    formatStringFromName(aName: string, params: Array<any>, length: number): string;
    getSimpleEnumeration(): nsISimpleEnumerator;
    GetStringFromID(aID: number): string;
    GetStringFromName(aName: string): string;
}

interface nsIStringBundleService extends nsISupports {
    createBundle(aURLSpec: string): nsIStringBundle;
    createExtensibleBundle(aRegistryKey: string): nsIStringBundle;
    flushBundles();
    formatStatusMessage(aStatus: number, aStatusArg: string): string;
}