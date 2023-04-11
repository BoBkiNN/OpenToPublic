package xyz.bobkinn_.opentopublic;

public enum OpenedStatus {
    MANUAL,
    UPNP,
    LAN;

    public static OpenedStatus current = null;
}
