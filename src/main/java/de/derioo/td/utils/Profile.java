package de.derioo.td.utils;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Profile {

    @Getter
    private final JsonObject profile;

    private static final List<Profile> profiles = new ArrayList<>();

    public Profile(JsonObject profile){
        this.profile = profile;
        profiles.add(this);
    }

    public static Profile getProfile(JsonObject profile){
        for (Profile p : profiles) {
            if (p.getProfile().get("uuid").getAsString().equals(profile.get("uuid").getAsString()))return p;
        }
        return null;
    }

}
