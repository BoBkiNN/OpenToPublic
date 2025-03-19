package xyz.bobkinn.opentopublic;

public enum OpenedStatus {
    MANUAL, UPNP, LAN;

    public static OpenedStatus current = null;
}
