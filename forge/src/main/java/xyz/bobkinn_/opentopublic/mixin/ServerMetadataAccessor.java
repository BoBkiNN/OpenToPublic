package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface ServerMetadataAccessor {

    @Mutable @Accessor("status")
    ServerStatus getStatusResponse();
}
