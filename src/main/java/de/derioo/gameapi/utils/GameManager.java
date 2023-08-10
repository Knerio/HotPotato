package de.derioo.gameapi.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager implements Listener {

    private final List<Minigame> minigames;
    private final Plugin plugin;

    public GameManager(Plugin plugin, Minigame... minigames) {
        this.minigames = List.of(minigames);
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            for (Minigame minigame : this.minigames) {
                if (minigame.isRunning()) minigame.onRunning();
            }
        }, 10, 10);

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void start(Minigame minigame, ArrayList<UUID> players) {
        for (Minigame game : this.minigames) {
            if (!game.equals(minigame)) continue;
            game.start(players);
        }
    }

    public void stopAll() {
        for (Minigame minigame : this.minigames) {
            if (!minigame.isRunning()) continue;
            minigame.stop();
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (Minigame minigame : this.minigames) {
            minigame.onJoin(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        for (Minigame minigame : this.minigames) {
            minigame.onLeave(e.getPlayer());
        }
    }
}
