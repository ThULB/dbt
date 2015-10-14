module ibw {
    export class ErrorCode {
        public static UNKNOWN: number = 0;
        public static NO_LOGIN: number = 1 << 1;
        public static ACTIVE_TTILE: number = 1 << 2;
    }
}