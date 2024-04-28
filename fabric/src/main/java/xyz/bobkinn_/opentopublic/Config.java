package xyz.bobkinn_.opentopublic;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final List<Integer> tcp;
    private final List<Integer> udp;
    private final boolean hideIps;

    public Config(){
        this.tcp = new ArrayList<>();
        this.udp = new ArrayList<>();
        this.hideIps = false;
    }

    public List<Integer> getTcp() {
        return tcp;
    }

    public List<Integer> getUdp() {
        return udp;
    }

    public boolean isHideIps() {
        return hideIps;
    }
}


