package de.derioo.td.listeners;

import de.derioo.td.Main;
import de.derioo.td.utils.DataBase;
import de.derioo.td.utils.TowerDefenceHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Consumer;

public class ConnectionListeners implements Listener {
    TowerDefenceHandler handler = Main.getInstance().getHandler();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->{
            handler.joinPlayer(e.getPlayer().getUniqueId());
        });
        e.getPlayer().teleport(handler.getHub());
    }

}
