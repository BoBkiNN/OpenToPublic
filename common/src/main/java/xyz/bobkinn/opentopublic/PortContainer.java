package xyz.bobkinn.opentopublic;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PortContainer implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    public static PortContainer self;
    public final Map<String, ArrayList<Integer>> upnpPorts;
    public Integer mainPort;
    public boolean fileExists;

    public PortContainer(Map<String, ArrayList<Integer>> upnpPorts, Integer mainPort) {
        if (self != null) throw new RuntimeException("Container filled");
        this.upnpPorts = upnpPorts;
        this.mainPort = mainPort;
    }

    public static PortContainer newEmpty() {
        HashMap<String, ArrayList<Integer>> addiPorts = new HashMap<>();
        addiPorts.put("tcp", new ArrayList<>());
        addiPorts.put("udp", new ArrayList<>());
        return new PortContainer(addiPorts, null);
    }

    public static void saveBackup(PortContainer container, File file) {
        if (container.isEmpty()) return;
        try {
            if (!file.getParentFile().exists())
                if (!file.getParentFile().mkdirs()) throw new IOException("Failed to create folders for " + file);
            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
            oos.writeObject(container);
            oos.close();
            PortContainer.self.fileExists = true;
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Failed to save container backup", e);
        }
    }

    public static PortContainer loadBackup(File file) {
        if (!file.exists()) return null;
        PortContainer res;
        try {
            if (!file.getParentFile().exists())
                if (!file.getParentFile().mkdirs()) throw new IOException("Failed to create folders for " + file);
            ObjectInputStream oos = new ObjectInputStream(Files.newInputStream(file.toPath()));
            res = (PortContainer) oos.readObject();
            oos.close();
            res.fileExists = true;
        } catch (IOException | ClassNotFoundException e) {
            OpenToPublic.LOGGER.error("Failed to load container backup", e);
            return null;
        }
        if (res.isEmpty()) {
            try {
                if (!file.delete()) throw new IOException("Failed to delete empty file " + file);
                res.fileExists = false;
            } catch (Exception e) {
                OpenToPublic.LOGGER.error("Failed to delete empty file", e);
            }
        }
        return res;
    }

    public boolean isEmpty() {
        return upnpPorts.get("tcp").isEmpty() && upnpPorts.get("udp").isEmpty() && mainPort == null;
    }

    @Override
    public String toString() {
        return "PortContainer{" + "upnpPorts=" + upnpPorts + ", mainPort=" + mainPort + '}';
    }

    public ArrayList<Integer> getTcpPorts() {
        return upnpPorts.get("tcp");
    }

    public ArrayList<Integer> getUdpPorts() {
        return upnpPorts.get("udp");
    }

    public void addTCP(int port) {
        if (mainPort != null) if (port == mainPort) return;
        ArrayList<Integer> ports = upnpPorts.get("tcp");
        if (ports.contains(port)) return;
        ports.add(port);
        upnpPorts.put("tcp", ports);
    }

    public void addUDP(int port) {
        ArrayList<Integer> ports = upnpPorts.get("udp");
        if (ports.contains(port)) return;
        ports.add(port);
        upnpPorts.put("udp", ports);
    }
}
