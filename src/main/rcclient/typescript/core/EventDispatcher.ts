/// <reference path="Utils.ts" />

module core {
    interface EventTarget {
        scope: any;
        listener: any;
    }

    export class EventDispatcher {
        private _messageListeners: Object = {};

        public addListener(event: string, scope: any, listener: any) {
            core.Utils.isValid(this._messageListeners[event]) || (this._messageListeners[event] = []);
            this._messageListeners[event].push(<EventTarget>{ scope: scope, listener: listener });
        }

        public clearListeners() {
            this._messageListeners = {};
        }

        public clearListenersByEvent(event: string) {
            delete this._messageListeners[event];
        }

        public clearListenersByScope(scope: any) {
            var d = null, f = [];
            for (var ev in this._messageListeners) {
                if (this._messageListeners.hasOwnProperty(ev) && core.Utils.isValid(this._messageListeners[ev])) {
                    do {
                        d = null;
                        for (var c = 0, l = this._messageListeners[ev].length; c < l; c++)
                            if ((<EventTarget>this._messageListeners[ev][c]).scope == scope) {
                                d = c;
                                break;
                            }
                        null !== d && this._messageListeners[ev].splice(d, 1);
                    } while (null !== d);
                    0 == this._messageListeners[ev].length && f.push(ev);
                }
            }
            for (var c = 0, fl = f.length; c < fl; c++)
                delete this._messageListeners[f[c]];
        }

        public dispatch(event: string, ...argArray: any[]) {
            if (core.Utils.isValid(this._messageListeners[event])) {
                var ev = this._messageListeners[event].slice(0);
                var args = [this];
                args = args.concat(argArray);

                for (var c = 0, l = ev.length; c < l; c++) {
                    (<EventTarget>ev[c]).listener.apply((<EventTarget>ev[c]).scope, args);
                }
            }
        }
    }
}