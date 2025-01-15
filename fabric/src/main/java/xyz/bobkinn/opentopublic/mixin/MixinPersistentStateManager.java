package xyz.bobkinn.opentopublic.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentStateManager.class)
public class MixinPersistentStateManager {

    /**
     * Allows null DataFixTypes
     */
    @Redirect(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/datafixer/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/NbtCompound;II)Lnet/minecraft/nbt/NbtCompound;"))
    private NbtCompound onTryDataFix(DataFixTypes instance, DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion) {
        if (instance == null) return nbt;
        return instance.update(dataFixer, nbt, oldVersion, newVersion);
    }

}
