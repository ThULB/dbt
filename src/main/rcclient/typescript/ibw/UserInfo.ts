module ibw {
    export class UserInfo {
        uid: string;
        name: string;
        libId: string;
        description: string;

        toString(): string {
            return "UserInfo: [uid={0}, name={1}, libId={2}, description={3}]".format(this.uid, this.name, this.libId, this.description);
        }
    }
}