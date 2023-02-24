package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface ServerMetadataAccessor {

    @Mutable @Accessor("metadata")
    ServerMetadata getMetadata();
}
