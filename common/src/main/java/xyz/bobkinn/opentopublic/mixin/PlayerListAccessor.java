package xyz.bobkinn.opentopublic.mixin;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    @Mutable @Accessor("maxPlayers")
    void setMaxPlayers(int maxPlayers);
}
