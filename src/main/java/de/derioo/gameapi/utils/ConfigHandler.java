package de.derioo.gameapi.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.derioo.gameapi.Main;

import java.util.ArrayList;
import java.util.Arrays;

public class ConfigHandler {

    private static JsonObject config;

    public ConfigHandler(JsonObject config){
        ConfigHandler.config = config;
    }

    public static JsonElement getMessage(String message){
        if (!message.contains("."))return config.get("messages").getAsJsonObject().get(message);

        JsonObject currentObject = config.get("messages").getAsJsonObject();

        ArrayList<String> split = new java.util.ArrayList<>(Arrays.stream(message.split("\\.")).toList());
        split.remove(split.size()-1);
        for (String s : split) {
            currentObject = currentObject.get(s).getAsJsonObject();
        }

        return currentObject.get(message.split("\\.")[message.split("\\.").length-1]);
    }


    public static JsonElement get(String location){
        if (!location.contains("."))return config.get(location);

        JsonObject currentObject = config;

        ArrayList<String> split = new java.util.ArrayList<>(Arrays.stream(location.split("\\.")).toList());
        split.remove(split.size()-1);
        for (String s : split) {
            currentObject = currentObject.get(s).getAsJsonObject();
        }

        return currentObject.get(location.split("\\.")[location.split("\\.").length-1]);
    }

}
