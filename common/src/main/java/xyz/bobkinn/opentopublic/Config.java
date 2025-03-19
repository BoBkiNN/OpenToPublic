package xyz.bobkinn.opentopublic;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Config {
    private final List<Integer> tcp;
    private final List<Integer> udp;
    private final boolean hideIps;

    public Config() {
        this.tcp = new ArrayList<>();
        this.udp = new ArrayList<>();
        this.hideIps = false;
    }

}


