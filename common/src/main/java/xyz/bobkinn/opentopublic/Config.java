package xyz.bobkinn.opentopublic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Config {
    private final List<Integer> tcp;
    private final List<Integer> udp;
    private boolean hideIps;

    public Config() {
        this.tcp = new ArrayList<>();
        this.udp = new ArrayList<>();
        this.hideIps = false;
    }

}


