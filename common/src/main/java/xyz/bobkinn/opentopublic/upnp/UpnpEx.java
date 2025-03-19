package xyz.bobkinn.opentopublic.upnp;

import lombok.Getter;

@Getter
public class UpnpEx extends RuntimeException {
    private final UpnpEnum type;
    private final Exception ex;

    public UpnpEx(UpnpEnum type, Exception ex) {
        this.type = type;
        this.ex = ex;
    }

}
