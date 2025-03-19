package xyz.bobkinn.opentopublic;

public enum OpenMode {
    LAN, MANUAL, UPNP;

    public OpenMode next() {
        var ord = ordinal() + 1;
        var values = values();
        if (ord >= values.length) {
            return values[0];
        }
        return values[ord];
    }
}
