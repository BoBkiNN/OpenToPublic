package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface ServerMetadataAccessor {

    @Invoker("buildServerStatus")
    ServerStatus buildServerStatus();

    @Mutable
    @Accessor
    void setStatus(ServerStatus status);
}
