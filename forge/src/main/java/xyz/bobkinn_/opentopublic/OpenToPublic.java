package xyz.bobkinn_.opentopublic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.bobkinn_.opentopublic.upnp.UpnpThread;

import java.io.*;
import java.nio.file.Path;

@Mod("opentopublic")
public class OpenToPublic {
    public static final String MOD_ID = "opentopublic";
    public static Path modConfigPath = null;
    public static File backupFile = null;

    public static boolean lanOpening = false;
    public static final ThreeLean<Boolean, Boolean, String> openPublic = ThreeLean.newBBS("upnp");
    public static int customPort = 25565;
    public static boolean onlineMode = true;
    public static int maxPlayers = 8;
    public static boolean enablePvp = true;
    public static boolean upnpSuccess = false;
    public static String upnpIp = null;
    public static boolean isLan = false;
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
                e.printStackTrace();
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

    public OpenToPublic(){
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
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
