package xyz.bobkinn.opentopublic;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class OtpPersistentState extends PersistentState {

    public static final String DATA_NAME = "lanOptions";

    private String motd = null;
    private Integer maxPlayers = null;
    private Boolean enablePvp = null;

    public static final Type<OtpPersistentState> TYPE = new Type<>(
            OtpPersistentState::new,
            OtpPersistentState::fromNbt,
            null
    );

    public static @NotNull OtpPersistentState fromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        var otp = new OtpPersistentState();
        if (tag.contains("motd")) otp.motd = tag.getString("motd");
        if (tag.contains("maxPlayers")) otp.maxPlayers = tag.getInt("maxPlayers");
        if (tag.contains("enablePvp")) otp.enablePvp = tag.getBoolean("enablePvp");
        return otp;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (motd != null) nbt.putString("motd", motd);
        if (maxPlayers != null) nbt.putInt("maxPlayers", maxPlayers);
        if (enablePvp != null) nbt.putBoolean("enablePvp", enablePvp);
        return nbt;
    }

}

