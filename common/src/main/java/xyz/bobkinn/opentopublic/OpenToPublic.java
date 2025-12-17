package xyz.bobkinn.opentopublic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;

import java.io.*;
import java.nio.file.Path;

public abstract class OpenToPublic {
    public static final String MOD_ID = "opentopublic";
    public static final Logger LOGGER = LogManager.getLogger("OpenToPublic");
    public static OpenMode selectedMode = OpenMode.LAN;
    public static OpenMode openedMode = null;
    public static Path modConfigPath = null;
    public static File backupFile = null;
    public static boolean lanOpening = false;
    public static int customPort = 25565;
    public static String upnpIp = null;
    public static boolean serverStopped = false;
    public static Config cfg = null;

    public OpenToPublic() {
        modConfigPath = getConfigsFolder().resolve(OpenToPublic.MOD_ID);

        // if world not closed correctly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (OpenToPublic.serverStopped && upnpIp != null && !PortContainer.INSTANCE.isEmpty()) {
                LOGGER.info("Closing ports at shutdown..");
                UpnpThread.runClose();
            }
        }));
    }

    public static void saveConfig(Path to) {
        var cfg = OpenToPublic.cfg;
        if (cfg == null) return;
        File folder = to.getParent().toFile();
        if (!folder.exists()) {
            if (!mkdirs(folder)) return;
        }
        var file = to.toFile();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(GSON.toJson(cfg, Config.class));
            bw.close();
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Config.class, (InstanceCreator<Config>) type -> new Config())
            .create();

    public static void updateConfig(Path path) {
        File folder = path.getParent().toFile();
        if (!folder.exists()) {
            if (!mkdirs(folder)) return;
        }
        File file = path.toFile();
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    LOGGER.error("Failed to create config file");
                    return;
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(GSON.toJson(new Config(), Config.class));
                bw.close();
            } catch (IOException e) {
                LOGGER.error("Failed to write config file", e);
            }
            return;
        }
        try {
            FileReader fr = new FileReader(file);
            Config cfg = GSON.fromJson(fr, Config.class);
            cfg.getTcp().forEach((p) -> PortContainer.INSTANCE.addTCP(p));
            cfg.getUdp().forEach((p) -> PortContainer.INSTANCE.addUDP(p));
            OpenToPublic.cfg = cfg;
            // save config to fill missing entries
            FileWriter fw = new FileWriter(file);
            GSON.toJson(OpenToPublic.cfg, Config.class, fw);
            fw.close();
        } catch (IOException e) {
            LOGGER.error("Failed to update config.json", e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean mkdirs(File folder) {
        boolean created = folder.mkdirs();
        if (!created) {
            LOGGER.error("Failed to create config folder");
            return false;
        }
        return true;
    }

    public abstract Path getConfigsFolder();
}
