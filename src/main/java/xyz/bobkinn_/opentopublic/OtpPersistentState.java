package xyz.bobkinn_.opentopublic;

import net.minecraft.SharedConstants;
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

    public OtpPersistentState() {
        super(DATA_NAME);
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
     * get class instance
     * @param world integrated server world
     * @return class instance
     */
    public static OtpPersistentState get(@NotNull ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(OtpPersistentState::new, DATA_NAME);
    }

    /**
     * Get nbt data from main container
     * @param tag file data
     */
    @Override
    public void fromTag(@NotNull NbtCompound tag) {
        data = tag.getCompound("data");
    }

    @Override
    public NbtCompound writeNbt(@NotNull NbtCompound tag) {
        tag.put("data", data);
        return tag;
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
            NbtCompound tag = new NbtCompound();
            writeNbt(tag);
            NbtCompound compressedTag = tag.copy();
            compressedTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
            NbtIo.writeCompressed(compressedTag, outputFile);
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
            fromTag(tag);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not load data", e);
        }
    }
}

