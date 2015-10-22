interface String {
    defaultCharset: string;

    format: (...args: Array<any>) => string;
    isEmpty: () => boolean;
    startsWith: (searchString: string, position?: number) => boolean;
    toUnicode: () => string;
}

String.prototype.defaultCharset = "UTF-8";

String.prototype.trim = function(): string {
    return this.replace(/^\s+|\s+$/gm, '');
};

String.prototype.format = function(...args: Array<any>): string {
    var formatted = this;
    (args.length == 1 && typeof args[0] == "object") && (args = args[0]);
    for (var i = 0; i < args.length; i++) {
        formatted = formatted.replace(
            RegExp("\\{" + i + "\\}", 'g'), args[i]);
    }
    return formatted;
};

String.prototype.isEmpty = function(): boolean {
    return this == null || this.length == 0;
};

String.prototype.startsWith = function(searchString: string, position?: number): boolean {
    position = position || 0;
    return this.indexOf(searchString, position) === position;
};

String.prototype.toUnicode = function(): string {
    try {
        var converter: nsIScriptableUnicodeConverter = Components.classes["@mozilla.org/intl/scriptableunicodeconverter"].createInstance(Components.interfaces.nsIScriptableUnicodeConverter);
        converter.charset = String.prototype.defaultCharset;
        return converter.ConvertToUnicode(this);
    } catch (ex) {
        return this;
    }
}