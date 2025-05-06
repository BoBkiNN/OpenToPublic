package xyz.bobkinn.opentopublic;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
@Setter
public class OtpPersistentState extends SavedData {

    public static final String DATA_NAME = "lanOptions";
    public static final Function<CompoundTag, OtpPersistentState> FACTORY = OtpPersistentState::fromNbt;

    private String motd = null;
    private Integer maxPlayers = null;
    private Boolean enablePvp = null;

    public static @NotNull OtpPersistentState fromNbt(@NotNull CompoundTag tag) {
        var otp = new OtpPersistentState();
        if (tag.contains("motd")) otp.motd = tag.getString("motd");
        if (tag.contains("maxPlayers")) otp.maxPlayers = tag.getInt("maxPlayers");
        if (tag.contains("enablePvp")) otp.enablePvp = tag.getBoolean("enablePvp");
        return otp;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        if (motd != null) nbt.putString("motd", motd);
        if (maxPlayers != null) nbt.putInt("maxPlayers", maxPlayers);
        if (enablePvp != null) nbt.putBoolean("enablePvp", enablePvp);
        return nbt;
    }

}

