module rc {
    export class ErrorCode {
        public static NO_TOKEN: number = 1;
        public static PARSE_ERROR_SLOTS: number = 1 << 1;
        public static PARSE_ERROR_SLOT: number = 1 << 2;
    }
}