package xyz.bobkinn.opentopublic;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Config {
    private final List<Integer> tcp;
    private final List<Integer> udp;
    private final boolean hideIps;
    private final boolean setWindowTitle;

    public Config() {
        this.tcp = new ArrayList<>();
        this.udp = new ArrayList<>();
        this.hideIps = false;
        this.setWindowTitle = false; //I think this should be set to false by default, there's no real need for it, but feel free to edit this..
    }

}


