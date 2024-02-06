package xyz.bobkinn_.opentopublic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OtpPersistentState extends SavedData {

    private static final String DATA_NAME = "lanOptions";
    private CompoundTag data;

    public OtpPersistentState() {
        data = new CompoundTag();
    }

    /**
     * Get nbt stored in class
     * @return nbt
     */
    public CompoundTag getData() {
        return data;
    }

    /**
     * Set nbt compound to class
     * @param data nbt
     */
    public void setData(CompoundTag data){
        this.data = data;
    }

    /**
     * Get nbt data from main container
     * @param tag file data
     */
    public void fromNbt(@NotNull CompoundTag tag) {
        data = tag.getCompound("data");
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        return data;
    }

    /**
     * Save nbt file to world 'data' folder
     * @param world integrated server world
     */
    public void saveToFile(@NotNull ServerLevel world) {
        try {
            Path worldDir = world.getServer().getServerDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
            File dataFolder = new File(worldDir.toFile(), "data");
            if (!dataFolder.exists()) {
                Files.createDirectory(dataFolder.toPath());
            }
            File outputFile = new File(dataFolder, DATA_NAME+".dat");
            setDirty(true);
            save(outputFile);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not save data", e);
        }
    }

    /**
     * Load nbt from world 'data' folder
     * @param world integrated server world
     */
    public void loadFromFile(ServerLevel world) {
        try {
            Path worldDir = world.getServer().getServerDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
            File dataFolder = new File(worldDir.toFile(), "data");
            if (!dataFolder.exists()) {
                return;
            }
            File inputFile = new File(dataFolder, DATA_NAME+".dat");
            if (!inputFile.exists()) {
                return;
            }
            CompoundTag compressedTag = NbtIo.readCompressed(inputFile.toPath(), NbtAccounter.unlimitedHeap());
            CompoundTag tag = compressedTag.copy();
            tag.remove("DataVersion");
            fromNbt(tag);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not load data", e);
        }
    }
}

