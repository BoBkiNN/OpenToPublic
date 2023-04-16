package xyz.bobkinn_.opentopublic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OtpPersistentState extends PersistentState {

    private static final String DATA_NAME = "lanOptions";
    private NbtCompound data;
    // I am lazy to register this to PersistentStateManager

    public OtpPersistentState() {
        data = new NbtCompound();
    }

    /**
     * Get nbt stored in class
     * @return nbt
     */
    public NbtCompound getData() {
        return data;
    }

    /**
     * Set nbt compound to class
     * @param data nbt
     */
    public void setData(NbtCompound data){
        this.data = data;
    }

    /**
     * Get nbt data from main container
     * @param tag file data
     */
    public void fromNbt(@NotNull NbtCompound tag) {
        data = tag.getCompound("data");
    }

    @Override
    public NbtCompound writeNbt(@NotNull NbtCompound tag) {
        return data;
    }

    /**
     * Save nbt file to world 'data' folder
     * @param world integrated server world
     */
    public void saveToFile(@NotNull ServerWorld world) {
        try {
            if (world.getServer().getRunDirectory() == null) return;
            Path worldDir = world.getServer().getRunDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
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
    public void loadFromFile(@NotNull ServerWorld world) {
        try {
            if (world.getServer().getRunDirectory() == null) return;
            Path worldDir = world.getServer().getRunDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
            File dataFolder = new File(worldDir.toFile(), "data");
            if (!dataFolder.exists()) {
                return;
            }
            File inputFile = new File(dataFolder, DATA_NAME+".dat");
            if (!inputFile.exists()) {
                return;
            }
            NbtCompound compressedTag = NbtIo.readCompressed(inputFile);
            NbtCompound tag = compressedTag.copy();
            tag.remove("DataVersion");
            fromNbt(tag);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not load data", e);
        }
    }
}

