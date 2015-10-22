module net {
    export class ErrorCode {
        public static UNKNOWN: number = 1;
        public static NSRESULT: number = 1 << 1;
        public static NOT_SUPPORTED: number = 1 << 2;
        public static OFFLINE: number = 1 << 3;
        public static HTTP_ERROR: number = 1 << 4;
    }
}