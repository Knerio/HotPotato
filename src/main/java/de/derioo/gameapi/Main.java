package de.derioo.gameapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.gameapi.commands.StartPotatoCommand;
import de.derioo.gameapi.hotpotato.HotPotato;
import de.derioo.gameapi.listeners.ConnectionListener;
import de.derioo.gameapi.utils.FileUtils;
import de.derioo.gameapi.utils.GameManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin  {

    @Getter
    private static Main instance;

    @Getter
    @Setter
    private JsonObject jsonConfig;

    @Getter
    private HotPotato hotPotato;

    @Getter
    private GameManager manager;

    @Override
    public void onEnable() {
        instance = this;

        this.getDataFolder().mkdirs();

        File configFile = new File(this.getDataFolder().getPath(), "config.json");
        if (configFile.exists()) {
            try {
                jsonConfig = JsonParser.parseString(FileUtils.readFile(configFile)).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(Component.text("Default config wird geladen..."));
            try {
                configFile.createNewFile();
                FileUtils.copyFile(this.getResource("config.json"), configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                jsonConfig = JsonParser.parseString(FileUtils.readFile(configFile)).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);

        this.hotPotato = new HotPotato(this);

        this.manager = new GameManager(this, this.hotPotato);


        new StartPotatoCommand();
    }

    @Override
    public void onDisable() {
        manager.stopAll();
    }
}
