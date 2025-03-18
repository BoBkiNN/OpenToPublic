package xyz.bobkinn.opentopublic;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class OtpPersistentState extends SavedData {

    public static final String DATA_NAME = "lanOptions";

    private String motd = null;
    private Integer maxPlayers = null;
    private Boolean enablePvp = null;

    public static final Factory<OtpPersistentState> TYPE = new Factory<>(
            OtpPersistentState::new,
            OtpPersistentState::fromNbt,
            null
    );

    public static @NotNull OtpPersistentState fromNbt(@NotNull CompoundTag tag, HolderLookup.Provider lookup) {
        var otp = new OtpPersistentState();
        if (tag.contains("motd")) otp.motd = tag.getString("motd");
        if (tag.contains("maxPlayers")) otp.maxPlayers = tag.getInt("maxPlayers");
        if (tag.contains("enablePvp")) otp.enablePvp = tag.getBoolean("enablePvp");
        return otp;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (motd != null) nbt.putString("motd", motd);
        if (maxPlayers != null) nbt.putInt("maxPlayers", maxPlayers);
        if (enablePvp != null) nbt.putBoolean("enablePvp", enablePvp);
        return nbt;
    }

}

