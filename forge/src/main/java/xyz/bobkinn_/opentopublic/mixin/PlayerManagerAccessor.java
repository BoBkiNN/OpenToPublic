package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerList.class)
public interface PlayerManagerAccessor {
    @Mutable @Accessor("maxPlayers")
    void setMaxPlayers(int maxPlayers);
}
