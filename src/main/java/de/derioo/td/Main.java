package de.derioo.td;

import de.derioo.td.commands.JoinProfileCommand;
import de.derioo.td.listeners.ConnectionListeners;
import de.derioo.td.utils.DataBase;
import de.derioo.td.utils.Logger;
import de.derioo.td.utils.TowerDefenceHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;


    public static int SAVE_INTERVALL = 20;

    @Getter
    private TowerDefenceHandler handler;



    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getWorld("world") == null){
            Logger.log(Logger.ERROR, "Void Welt exestiert nicht!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!DataBase.setup())return;

        createWorlds();

        handler = new TowerDefenceHandler();

        registerListeners();
        initCommands();


    }

    private void initCommands() {
        new JoinProfileCommand();
    }

    private void registerListeners() {
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new ConnectionListeners(), this);
    }

    @Override
    public void onDisable() {
        DataBase.getInstance().save();
    }

    private void createWorlds(){
        if (Bukkit.getWorld("hub") == null) {
            Bukkit.createWorld(new WorldCreator("hub").copy(Bukkit.getWorld("world")));
        }
        if (Bukkit.getWorld("islands") == null) {
            Bukkit.createWorld(new WorldCreator("islands").copy(Bukkit.getWorld("world")));
        }
    }



}
