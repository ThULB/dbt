/// <reference path="../definitions/XPCOM.d.ts" />

module net {
    export class HTTPRequest {
        public static METHOD_POST = "POST";
        public static METHOD_GET = "GET";
        public static METHOD_PUT = "PUT";

        private mURL: string;
        private mMethod: string;
        private mData: string;

        private mURI: nsIURI;
        private mChannel: nsIChannel;

        private mCallbackClass: any;
        private mCompleteCallback: (aClass: HTTPRequest, aData: any, aSuccess: boolean) => void;
        private mProgressCallback: (aClass: HTTPRequest, aProgress: number, aProgressMax: number) => void;

        constructor(aURL: string, aMethod?: string, aData?: string, aCache?: boolean) {
            this.mMethod = aMethod != null ? aMethod.toUpperCase() : HTTPRequest.METHOD_GET;
            this.mData = aData;

            this.setURL(aURL);

            if (aCache == null || aCache == false) {
                try {
                    // bypass cache
                    this.mChannel.loadFlags |= Components.interfaces.nsIRequest.LOAD_BYPASS_CACHE;
                } catch (ex) {
                    // ignore Exception
                }
            }
        }

        setURL(aURL: string) {
            this.mURL = aURL;

            var ioService: nsIIOService = Components.classes["@mozilla.org/network/io-service;1"].getService(Components.interfaces.nsIIOService);
            this.mURI = ioService.newURI(this.mURL, null, null);

            this.mChannel = ioService.newChannelFromURI(this.mURI);
            this.mChannel.contentCharset = "UTF-8";
        }

        getChannel(): nsIChannel {
            return this.mChannel;
        }

        execute(aCallbackClass: any, aCompleteCallback, aProgressCallback?): void {
            if (this.mMethod == HTTPRequest.METHOD_POST || this.mMethod == HTTPRequest.METHOD_PUT) {
                var httpChannel: nsIHttpChannel = this.mChannel.QueryInterface(Components.interfaces.nsIHttpChannel);
                httpChannel.referrer = this.mURI;

                var inputStream: nsIStringInputStream = Components.classes["@mozilla.org/io/string-input-stream;1"].createInstance(Components.interfaces.nsIStringInputStream);
                inputStream.setData(this.mData, this.mData.length);

                var uploadChannel: nsIUploadChannel = this.mChannel.QueryInterface(Components.interfaces.nsIUploadChannel);
                uploadChannel.setUploadStream(inputStream, "application/x-www-form-urlencoded", -1);

                // order important - setUploadStream resets to PUT
                (this.mMethod == HTTPRequest.METHOD_POST) && (httpChannel.requestMethod = this.mMethod);
            }

            this.mCallbackClass = aCallbackClass;
            this.mCompleteCallback = aCompleteCallback;
            this.mProgressCallback = aProgressCallback;

            var listener: nsIStreamListener = new StreamListener(this, this.onComplete, this.onProgess);
            this.mChannel.notificationCallbacks = listener;

            this.mChannel.asyncOpen(listener, null);
        }

        private onComplete(aClass: net.HTTPRequest, aData: any, aSuccess: boolean): void {
            var cbClass: any = aClass.mCallbackClass;
            var callback: Function = aClass.mCompleteCallback;

            (callback != null) && callback.call(cbClass, aClass, aData, aSuccess);
        }

        private onProgess(aClass: net.HTTPRequest, aProgress: number, aProgressMax: number): void {
            var cbClass: any = aClass.mCallbackClass;
            var callback: Function = aClass.mProgressCallback;

            (callback != null) && callback.call(cbClass, aClass, aProgress, aProgressMax);
        }
    }

    class StreamListener implements nsIStreamListener, nsIHttpEventSink, nsIProgressEventSink {
        private mChannel: any;
        private mData: string;

        private mClass: any;
        private mCompleteFunc: (aClass: any, aData: string, aSuccess: boolean) => void;
        private mProgressFunc: (aClass: any, aProgress: number, aProgressMax: number) => void;

        constructor(aClass: any, aCompleteFunc, aProgressFunc?) {
            this.mClass = aClass;
            this.mCompleteFunc = aCompleteFunc;
            this.mProgressFunc = aProgressFunc;
        }

        onStartRequest(aRequest: nsIRequest, aContext): void {
            this.mData = "";
        }

        onDataAvailable(aRequest: nsIRequest, aContext: nsISupports, aStream: nsIInputStream, aSourceOffset: number, aLength: number): void {
            var scriptableInputStream: nsIScriptableInputStream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
            scriptableInputStream.init(aStream);

            this.mData += scriptableInputStream.read(aLength);
        }

        onStopRequest(aRequest: nsIRequest, aContext: nsISupports, aStatus: number): void {
            if (Components.isSuccessCode(aStatus)) {
                // request was successfull
                this.mCompleteFunc(this.mClass, this.mData, true);
            } else {
                // request failed
                this.mCompleteFunc(this.mClass, null, false);
            }

            this.mChannel = null;
        }

        onChannelRedirect(aOldChannel: nsIChannel, aNewChannel: nsIChannel, aFlags): void {
            // if redirecting, store the new channel
            this.mChannel = aNewChannel;
        }
        
        // nsIInterfaceRequestor
        getInterface(aIID): any {
            try {
                return this.QueryInterface(aIID);
            } catch (e) {
                throw Components.results.NS_NOINTERFACE;
            }
        }

        // nsIProgressEventSink (not implementing will cause annoying exceptions)
        onProgress(aRequest: nsIRequest, aContext: nsISupports, aProgress: number, aProgressMax: number): void {
            (this.mProgressFunc != null) && this.mProgressFunc(this.mClass, aProgress, aProgressMax);
        }
        onStatus(aRequest: nsIRequest, aContext: nsISupports, aStatus: number, aStatusArg: string): void { }

        // nsIHttpEventSink (not implementing will cause annoying exceptions)
        onRedirect(aOldChannel: nsIChannel, aNewChannel: nsIChannel): void { }

        // we are faking an XPCOM interface, so we need to implement QI
        QueryInterface(aIID): any {
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
}
