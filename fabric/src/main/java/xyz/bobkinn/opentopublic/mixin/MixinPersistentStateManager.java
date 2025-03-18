package xyz.bobkinn.opentopublic.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DimensionDataStorage.class)
public class MixinPersistentStateManager {

    /**
     * Allows null DataFixTypes
     */
    @Redirect(method = "readTagFromDisk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag onTryDataFix(DataFixTypes instance, DataFixer dataFixer, CompoundTag nbt, int oldVersion, int newVersion) {
        if (instance == null) return nbt;
        return instance.update(dataFixer, nbt, oldVersion, newVersion);
    }

}
