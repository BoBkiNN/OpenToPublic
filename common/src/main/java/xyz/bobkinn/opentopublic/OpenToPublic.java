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
    public static Path modConfigPath = null;
    public static File backupFile = null;

    public static boolean lanOpening = false;
    public static final ThreeLean<Boolean, Boolean, String> openPublic = ThreeLean.newBBS("upnp");
    public static int customPort = 25565;
    public static String upnpIp = null;
    public static boolean serverStopped = false;
    public static Config cfg = null;

    public static final Logger LOGGER = LogManager.getLogger("OpenToPublic");

    public static void updateConfig(Path path){
        File folder = OpenToPublic.modConfigPath.toFile();
        if (!folder.exists()){
            boolean created = folder.mkdirs();
            if (!created) {
                LOGGER.error("Failed to create config folder");
                return;
            }
        }
        File file = path.toFile();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Config.class, (InstanceCreator<Config>) type -> new Config())
                .create();
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    LOGGER.error("Failed to create config file");
                    return;
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(gson.toJson(new Config(), Config.class));
                bw.close();
            } catch (IOException e) {
                LOGGER.error("Failed to write config file", e);
            }
            return;
        }
        try {
            FileReader fr = new FileReader(file);
            Config cfg = gson.fromJson(fr, Config.class);
            cfg.getTcp().forEach((p) -> PortContainer.self.addTCP(p));
            cfg.getUdp().forEach((p) -> PortContainer.self.addUDP(p));
            OpenToPublic.cfg = cfg;
            // save config to fill missing entries
            FileWriter fw = new FileWriter(file);
            gson.toJson(OpenToPublic.cfg, Config.class, fw);
            fw.close();
        } catch (IOException e) {
            LOGGER.error("Failed to update config.json", e);
        }
    }

    public abstract Path getConfigsFolder();

    public OpenToPublic(){
        modConfigPath = getConfigsFolder().resolve(OpenToPublic.MOD_ID);
        openPublic.setCurrentState(2);


        // if world not closed correctly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (OpenToPublic.serverStopped && upnpIp != null && !PortContainer.self.isEmpty()) {
                LOGGER.info("Closing ports at shutdown..");
                UpnpThread.runClose();
            }
        }));
    }
}
