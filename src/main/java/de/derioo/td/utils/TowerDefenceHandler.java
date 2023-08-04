package de.derioo.td.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.td.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        if (!isRegistered(uuid)) register(uuid);
        users.put(uuid, JsonParser.parseString(DataBase.getInstance().getUserData(uuid)).getAsJsonObject());

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
        Location location = new Location(Bukkit.getWorld("islands"), 0, 100, 10000 * islands);

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

        users.put(uuid, o);
    }

    public void generateIsland(Island island){
        JsonObject world = island.getProfile().get("world_settings").getAsJsonObject();

        JsonObject format = world.get("world_settings").getAsJsonObject().get("format").getAsJsonObject();

        Map<String, BlockData> blocks = new HashMap<>();

        int height = format.get("height").getAsInt();
        int length = format.get("length").getAsInt();



        for (int i = 0; i < height; i++) {

            for (int j = 0; j < length; j++) {
                blocks.put(i+":"+j,
                        world.get("world").getAsJsonArray().get(i + (j*length)).getAsString().equals("-") ?
                                Material.GOLD_BLOCK.createBlockData()
                                :
                                Material.DIAMOND_BLOCK.createBlockData());
            }
        }


        blocks.forEach((s, blockData) -> {

        });

    }

    /*TODO
     *     "world":
     *       [
     *       "F", "F", "F", "F", "F", "F",
     *       "-", "-", "-", "-", "F", "F",
     *       "F", "F", "F", "-", "F", "F",
     *       "F", "F", "F", "-", "-", "-",
     *       "F", "F", "F", "F", "F", "F"
     *     ],
     *     "world_settings": {
     *       "format": {
     *         "length": 6,
     *         "height": 5
     *       }
     *     }
     */


    public Location getHub() {
        return LocationUtils.getLocation(config.get("hub").getAsJsonObject());
    }
}
