package de.derioo.gameapi.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ConfigHandler {

    private static JsonObject config;

    public ConfigHandler(JsonObject config){
        ConfigHandler.config = config;
    }

    public static JsonElement getMessage(String message){
        if (!message.contains("."))return ConfigHandler.config.get("messages").getAsJsonObject().get(message);

        JsonObject currentObject = ConfigHandler.config.get("messages").getAsJsonObject();

        return getElement(message, currentObject);
    }


    public static JsonElement get(String location){
        if (!location.contains("."))return ConfigHandler.config.get(location);

        JsonObject currentObject = ConfigHandler.config;

        return getElement(location, currentObject);
    }

    private static JsonElement getElement(String location, JsonObject currentObject) {
        ArrayList<String> split = new ArrayList<>(Arrays.stream(location.split("\\.")).toList());
        split.remove(split.size()-1);
        for (String s : split) {
            currentObject = currentObject.get(s).getAsJsonObject();
        }

        return currentObject.get(location.split("\\.")[location.split("\\.").length-1]);
    }

}
