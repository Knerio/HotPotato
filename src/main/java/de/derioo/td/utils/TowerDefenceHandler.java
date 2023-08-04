package de.derioo.td.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.td.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TowerDefenceHandler {

    public final static Map<UUID, JsonObject> users = new HashMap<>();
    private final Map<Profile, Island> generatedIslands = new HashMap<>();

    private final JsonObject config;

    public TowerDefenceHandler() {
        try {
            File config = new File(Main.getInstance().getDataFolder(), "config.json");
            if (config.createNewFile()) {
                FileUtils.copyFile(Main.getInstance().getResource("config.json"), config);
                Logger.log(Logger.ERROR, "Config wurde noch nicht aufgesetzt, default config wurde angewendet");
            }
            this.config = JsonParser.parseString(FileUtils.readFile(new File(Main.getInstance().getDataFolder().getPath(), "config.json"))).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinPlayer(UUID uuid) {
        if (!isRegistered(uuid)) {
            register(uuid);
        } else users.put(uuid, JsonParser.parseString(DataBase.getInstance().getUserData(uuid)).getAsJsonObject());

    }


    public boolean isRegistered(UUID uuid) {
        String data = DataBase.getInstance().getUserData(uuid);
        return data != null;
    }

    public Island getIsland(Profile profile) {
        if (generatedIslands.containsKey(profile)) {
            return generatedIslands.get(profile);
        }
        int islands = Island.islands.size();
        Location location = new Location(Bukkit.getWorld("islands"), 10000, 100, 10000 * islands);

        return new Island(location, profile.getProfile(), false);
    }


    public void register(UUID uuid) {

        JsonObject o = new JsonObject();

        JsonArray profiles = new JsonArray();
        JsonObject firstProfile = new JsonObject();

        firstProfile.add("world_settings", this.config.get("default_world").getAsJsonObject());
        firstProfile.addProperty("creation", System.currentTimeMillis());
        firstProfile.addProperty("last_save", System.currentTimeMillis());
        firstProfile.addProperty("name", "Empty Profile 1");
        firstProfile.addProperty("profile_index", 1);
        firstProfile.addProperty("uuid", UUID.randomUUID().toString());

        profiles.add(firstProfile);
        o.add("profiles", profiles);

        new Profile(firstProfile);

        users.put(uuid, o);
    }

    public void generateIsland(Island island) {
        JsonObject world = island.getProfile().get("world_settings").getAsJsonObject();

        JsonObject format = world.get("world_settings").getAsJsonObject().get("format").getAsJsonObject();

        Map<String, BlockData> blocks = new HashMap<>();

        int height = format.get("height").getAsInt();
        int length = format.get("length").getAsInt();


        for (int i = 0; i < height; i += 1) {

            for (int j = 0; j < length; j += 1) {
                blocks.put(i * 16 + ":" + j * 16,
                        world.get("world").getAsJsonArray().get(j + (i * length)).getAsString().equals("-") ?
                                Material.GOLD_BLOCK.createBlockData()
                                :
                                Material.DIAMOND_BLOCK.createBlockData());
            }
        }

        World w = Bukkit.getWorld("islands");

        if (w == null) throw new IllegalStateException("123");


        blocks.forEach((s, blockData) -> {
            int x = Integer.parseInt(s.split(":")[0]);
            int y = Integer.parseInt(s.split(":")[1]);

            Chunk chunk = w.getChunkAt(new Location(w, x, 100, island.getCenter().getZ() + y));

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            for (x = chunkX * 16; x < (chunkX + 1) * 16; x++) {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++) {
                    Block block = w.getBlockAt(x, 100, z);
                    block.setBlockData(blockData);
                    w.getBlockAt(x, 101, z).setBlockData(Material.LIGHT.createBlockData());
                    if (blockData.getMaterial().equals(Material.GOLD_BLOCK)) {
                        w.getBlockAt(x, 99, z).setBlockData(Material.DIRT.createBlockData());
                    }
                }
            }

        });


    }


    public List<Chunk> getChunks(Chunk centerChunk, int radius) {
        List<Chunk> chunks = new ArrayList<>();
        for (int x = centerChunk.getX() - radius; x < centerChunk.getX() + radius; x++) {
            for (int z = centerChunk.getZ() - radius; z < centerChunk.getZ() + radius; z++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public Location getHub() {
        return LocationUtils.getLocation(config.get("hub").getAsJsonObject());
    }
}
