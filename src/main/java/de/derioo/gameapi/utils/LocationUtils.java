package de.derioo.gameapi.utils;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    public static Location getLocation(JsonObject o){
        return new Location(Bukkit.getWorld(o.get("world").getAsString()), o.get("x").getAsDouble(), o.get("y").getAsDouble(), o.get("z").getAsDouble(), o.get("yaw").getAsFloat(), o.get("pitch").getAsFloat());
    }

}
