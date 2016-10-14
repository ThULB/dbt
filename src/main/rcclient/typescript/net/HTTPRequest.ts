/// <reference path="../definitions/XPCOM.d.ts" />

/// <reference path="Error.ts" />
/// <reference path="ErrorCode.ts" />
/// <reference path="HTTPError.ts" />

module net {
    export class HTTPRequest extends core.EventDispatcher {
        public static EVENT_COMPLETE = "REQUEST_COMPLETE";
        public static EVENT_ERROR = "REQUEST_ERROR";
        public static EVENT_PROGRESS = "REQUEST_PROGRESS";

        public static METHOD_POST = "POST";
        public static METHOD_GET = "GET";
        public static METHOD_PUT = "PUT";

        private URL: string;
        private URI: nsIURI;
        private channel: nsIChannel;

        private options: HTTPOptions;

        constructor(URL: string, options?: IHTTPOptions) {
            super();

            this.setURL(URL);
            this.options = new HTTPOptions(options);
        }

        getURL(): string {
            return this.URL;
        }

        setURL(URL: string) {
            this.URL = URL;

            var ioService: nsIIOService = Components.classes["@mozilla.org/network/io-service;1"].getService(Components.interfaces.nsIIOService);
            this.URI = ioService.newURI(this.URL, String.prototype.defaultCharset, null);
            this.channel = ioService.newChannelFromURI(this.URI);
        }

        getChannel(): nsIChannel {
            return this.channel;
        }

        execute(options?: IHTTPOptions) {
            core.Utils.isValid(options) && (this.options = new HTTPOptions(options));

            if (this.options.bypassCache) {
                this.channel.loadFlags |= Components.interfaces.nsIRequest.LOAD_BYPASS_CACHE;
            }

            var httpChannel: nsIHttpChannel = this.channel.QueryInterface(Components.interfaces.nsIHttpChannel);
            httpChannel.referrer = this.URI;
            httpChannel.requestMethod = this.options.method;

            var data: String = new String(this.options.data || null);

            if (!data.isEmpty() && (this.options.method == HTTPRequest.METHOD_POST || this.options.method == HTTPRequest.METHOD_PUT)) {
                var inputStream: nsIStringInputStream = Components.classes["@mozilla.org/io/string-input-stream;1"].createInstance(Components.interfaces.nsIStringInputStream);
                inputStream.setData(data.toString(), data.length);

                var uploadChannel: nsIUploadChannel = this.channel.QueryInterface(Components.interfaces.nsIUploadChannel);
                uploadChannel.setUploadStream(inputStream, this.options.contentType, data.length);

                // order important - setUploadStream resets to PUT
                (this.options.method == HTTPRequest.METHOD_POST) && (httpChannel.requestMethod = this.options.method);
            }

            var listener: nsIStreamListener = new StreamListener(this, this.onComplete, this.onProgess);

            this.channel.notificationCallbacks = listener;
            this.channel.asyncOpen(listener, null);
        }

        private onComplete(aHTTPRequest: net.HTTPRequest, aData: any, aSuccess: boolean) {
            if (aSuccess)
                aHTTPRequest.dispatch(HTTPRequest.EVENT_COMPLETE, aData);
            else
                aHTTPRequest.dispatch(HTTPRequest.EVENT_ERROR, aData);
        }

        private onProgess(aHTTPRequest: net.HTTPRequest, aProgress: number, aProgressMax: number) {
            aHTTPRequest.dispatch(HTTPRequest.EVENT_PROGRESS, aProgress, aProgressMax);
        }
    }

    class StreamListener implements nsIInterfaceRequestor, nsIHttpEventSink, nsIProgressEventSink, nsIStreamListener {
        private mChannel: nsIChannel;
        private mData: string;

        private mClass: HTTPRequest;
        private mCompleteFunc: (aClass: any, aData: any, aSuccess: boolean) => void;
        private mProgressFunc: (aClass: any, aProgress: number, aProgressMax: number) => void;

        constructor(aClass: HTTPRequest, aCompleteFunc, aProgressFunc?) {
            this.mClass = aClass;
            this.mCompleteFunc = aCompleteFunc;
            this.mProgressFunc = aProgressFunc;
        }

        onStartRequest(aRequest: nsIRequest, aContext: nsISupports) {
            this.mData = "";
        }

        onDataAvailable(aRequest: nsIRequest, aContext: nsISupports, aStream: nsIInputStream, aSourceOffset: number, aLength: number) {
            var scriptableInputStream: nsIScriptableInputStream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
            scriptableInputStream.init(aStream);

            this.mData += scriptableInputStream.read(aLength);
        }

        onStopRequest(aRequest: nsIRequest, aContext: nsISupports, aStatus: number) {
            var error: Error;

            if (Components.isSuccessCode(aStatus) || aStatus == 0x80540008 || aStatus == 0x805D0021) {
                var channel: nsIChannel = this.mChannel || this.mClass.getChannel();
                // errors on protocol (HTTP) level give NS_OK on channel and
                // the error in a protocol-specific way
                if (channel instanceof Components.interfaces.nsIHttpChannel) {
                    var httpChannel: nsIHttpChannel = channel.QueryInterface(Components.interfaces.nsIHttpChannel);

                    /* nsIHttpChannel::responseStatus and nsIHttpChannel::responseStatusText
                     * throw an NS_ERROR_NOT_AVAILABLE if the channel did not even
                     * manage to connect to the remote server. */
                    try {
                        // HTTP 2xx is success
                        if (200 <= httpChannel.responseStatus &&
                            httpChannel.responseStatus < 300) {
                            this.mCompleteFunc(this.mClass, this.mData.toUnicode(), true);
                        } else {
                            error = new HTTPError(httpChannel.responseStatus, httpChannel.responseStatusText, channel.URI.spec);
                        }
                    } catch (e) {
                        error = new Error(ErrorCode.OFFLINE, channel.URI.spec);
                    }
                } else {
                    error = new Error(ErrorCode.NOT_SUPPORTED);
                }
            } else {
                error = new Error(ErrorCode.NSRESULT, aStatus.toString(16));
            }

            if (core.Utils.isValid(error))
                this.mCompleteFunc(this.mClass, error, false);
        }

        // nsIChannelEventSink
        onChannelRedirect(aOldChannel: nsIChannel, aNewChannel: nsIChannel, aFlags) {
            // if redirecting, store the new channel
            this.mChannel = aNewChannel;
        }

        // nsIHttpEventSink (not implementing will cause annoying exceptions)
        onRedirect(aOldChannel: nsIChannel, aNewChannel: nsIChannel) {
            this.mChannel = aNewChannel;
        }

        // nsIProgressEventSink (not implementing will cause annoying exceptions)
        onProgress(aRequest: nsIRequest, aContext: nsISupports, aProgress: number, aProgressMax: number) {
            this.mProgressFunc(this.mClass, aProgress, aProgressMax);
        }

        onStatus(aRequest: nsIRequest, aContext: nsISupports, aStatus: number, aStatusArg: string) {
            // ignore status because of false fails
            // if (aStatus == 0x804B0007) {
            //     this.mCompleteFunc(this.mClass, new Error(ErrorCode.OFFLINE, aStatusArg), false);
            // }
        }

        // nsIInterfaceRequestor
        getInterface(aIID) {
            try {
                return this.QueryInterface(aIID);
            } catch (e) {
                throw Components.results.NS_NOINTERFACE;
            }
        }

        // we are faking an XPCOM interface, so we need to implement QI
        QueryInterface(aIID) {
            if (aIID.equals(Components.interfaces.nsISupports) ||
                aIID.equals(Components.interfaces.nsIInterfaceRequestor) ||
                aIID.equals(Components.interfaces.nsIChannelEventSink) ||
                aIID.equals(Components.interfaces.nsIProgressEventSink) ||
                aIID.equals(Components.interfaces.nsIHttpEventSink) ||
                aIID.equals(Components.interfaces.nsIStreamListener))
                return this;

            throw Components.results.NS_NOINTERFACE;
        }
    }


    interface IHTTPData {
        toString(): string;
    }

    interface IHTTPOptions {
        method: string;
        data?: IHTTPData;
        contentType?: string;
        bypassCache?: boolean;
    }

    class HTTPOptions implements IHTTPOptions {
        method: string;
        data: IHTTPData;
        contentType: string;
        bypassCache: boolean;

        constructor(options?: IHTTPOptions) {
            this.method = options && options.method || HTTPRequest.METHOD_GET,
                this.data = options && options.data || null,
                this.contentType = options && options.contentType || "application/x-www-form-urlencoded",
                this.bypassCache = options && options.bypassCache || true
        }
    }

    interface IHTTPParameters<T> {
        [name: string]: T;
    }

    export class HTTPParameters<T> implements IHTTPData {
        private params: IHTTPParameters<T> = {};

        setParameter(name: string, value?: T) {
            this.params[name] = value;
        }

        getParameter(name: string): T {
            return this.params[name];
        }

        toString(): string {
            var arr: Array<string> = new Array<string>();

            for (var name in this.params) {
                arr.push(encodeURIComponent(name) + (this.params[name] && "=" + encodeURIComponent(this.params[name].toString()) || ""));
            }

            return arr.join("&");
        }
    }
}
