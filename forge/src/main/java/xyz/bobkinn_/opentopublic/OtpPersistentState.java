package xyz.bobkinn_.opentopublic;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.SharedConstants;
import net.minecraft.world.storage.WorldSavedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OtpPersistentState extends WorldSavedData {

    private static final String DATA_NAME = "lanOptions";
    private CompoundNBT data;

    public OtpPersistentState() {
        super(DATA_NAME);
        data = new CompoundNBT();
    }

    /**
     * Get nbt stored in class
     * @return nbt
     */
    public CompoundNBT getData() {
        return data;
    }

    /**
     * Set nbt compound to class
     * @param data nbt
     */
    public void setData(CompoundNBT data){
        this.data = data;
    }

    /**
     * get class instance
     * @param world integrated server world
     * @return class instance
     */
    public static OtpPersistentState get(ServerWorld world) {
        return world.getSavedData().getOrCreate(OtpPersistentState::new, DATA_NAME);
    }

    /**
     * Get nbt data from main container
     * @param tag file data
     */
    @Override
    public void read(CompoundNBT tag) {
        data = tag.getCompound("data");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("data", data);
        return tag;
    }

    /**
     * Save nbt file to world 'data' folder
     * @param world integrated server world
     */
    public void saveToFile(ServerWorld world) {
        try {
            Path worldDir = world.getServer().getDataDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
            File dataFolder = new File(worldDir.toFile(), "data");
            if (!dataFolder.exists()) {
                Files.createDirectory(dataFolder.toPath());
            }
            File outputFile = new File(dataFolder, DATA_NAME+".dat");
            CompoundNBT tag = new CompoundNBT();
            write(tag);
            CompoundNBT compressedTag = tag.copy();
            compressedTag.putInt("DataVersion", SharedConstants.getVersion().getWorldVersion());
            CompressedStreamTools.writeCompressed(compressedTag, outputFile);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not save data", e);
        }
    }

    /**
     * Load nbt from world 'data' folder
     * @param world integrated server world
     */
    public void loadFromFile(ServerWorld world) {
        try {
            Path worldDir = world.getServer().getDataDirectory().toPath().resolve(Util.savesFolder).resolve(Util.getLevelName(world));
            File dataFolder = new File(worldDir.toFile(), "data");
            if (!dataFolder.exists()) {
                return;
            }
            File inputFile = new File(dataFolder, DATA_NAME+".dat");
            if (!inputFile.exists()) {
                return;
            }
            CompoundNBT compressedTag = CompressedStreamTools.readCompressed(inputFile);
            CompoundNBT tag = compressedTag.copy();
            tag.remove("DataVersion");
            read(tag);
        } catch (IOException e) {
            OpenToPublic.LOGGER.error("Could not load data", e);
        }
    }
}

