/// <reference path="Utils.ts" />

module core {
    export class NSObject {
        // namespace cache
        private _ns: string;
        
        public getNS(container?: Object, ns?: string): string {
            if (this.constructor === null)
                return null;
                
            (container == null) && (container = window);

            var names = Object.getOwnPropertyNames(container);
            for (var i in names) {
                var name = names[i];
                if (container[name] === undefined) continue;

                if (this.constructor === container[name]) {
                    return ns !== undefined ? [ns, name].join(".") : name;
                } else if (name !== "prototype" && container[name] !== null && container[name].constructor === Object) {
                    var ret = this.getNS(container[name], name);
                    if (ret != null)
                        return ret;
                }
            }

            return null;
        }

        public getClassName(): string {
            (this._ns == null) && (this._ns = this.getNS());

            return this._ns.split(".").reverse()[0];
        }
    }
}