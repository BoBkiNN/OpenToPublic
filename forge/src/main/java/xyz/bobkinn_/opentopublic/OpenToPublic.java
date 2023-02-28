package xyz.bobkinn_.opentopublic;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("opentopublic")
public class OpenToPublic {
    public static boolean lanOpening = false;
    public static boolean openPublic = true;
    public static int customPort = 25565;
    public static boolean onlineMode = true;
    public static int maxPlayers = 8;
    public static boolean enablePvp = true;

    public static Logger LOGGER = LogManager.getLogger("OpenToPublic");

    public OpenToPublic() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }


}
