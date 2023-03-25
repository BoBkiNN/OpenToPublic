package xyz.bobkinn_.opentopublic.upnp;

public class UpnpEx extends RuntimeException{
    private final UpnpEnum type;
    private final Exception e;
    public UpnpEx(UpnpEnum type, Exception e){
        this.type = type;
        this.e = e;
    }

    public UpnpEnum getType() {
        return type;
    }

    public Exception getEx() {
        return e;
    }
}
