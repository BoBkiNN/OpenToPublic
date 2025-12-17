package xyz.bobkinn.opentopublic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

@Getter
@Setter
public class OtpPersistentState extends SavedData {

    public static final String DATA_NAME = "lanOptions";

    private String motd = null;
    private Integer maxPlayers = null;
    private Boolean enablePvp = null;

    public static final Codec<OtpPersistentState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("motd", null).forGetter(OtpPersistentState::getMotd),
            Codec.INT.optionalFieldOf("maxPlayers", null).forGetter(OtpPersistentState::getMaxPlayers),
            Codec.BOOL.optionalFieldOf("enablePvp", null).forGetter(OtpPersistentState::getEnablePvp)
    ).apply(inst, (m, mp, pvp) -> {
        var r = new OtpPersistentState();
        r.motd = m;
        r.maxPlayers = mp;
        r.enablePvp = pvp;
        return r;
    }));

    public static final SavedDataType<OtpPersistentState> TYPE = new SavedDataType<>(DATA_NAME,
            OtpPersistentState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_INDEX);

}

