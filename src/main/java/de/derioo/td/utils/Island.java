package de.derioo.td.utils;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Island {

    @Getter
    private final Location center;

    @Getter
    private final JsonObject profile;

    @Setter
    @Getter
    private boolean generated;

    public static final List<Island> islands = new ArrayList<>();

    public Island(Location center, JsonObject profile, boolean generated) {
        this.center = center;
        this.profile = profile;
        this.generated = generated;
        islands.add(this);
    }
}
