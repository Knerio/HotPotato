package de.derioo.gameapi.utils;

import de.derioo.gameapi.utils.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {

    private final List<Minigame> minigames;
    private final Plugin plugin;

    public GameManager(Plugin plugin, Minigame... minigames){
        this.minigames = List.of(minigames);
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            for (Minigame minigame : minigames) {
                if (minigame.isRunning())minigame.onRunning();
            }
        }, 10,10);
    }

    public void start(Minigame minigame, ArrayList<UUID> players){
        for (Minigame game : minigames) {
            if (!game.equals(minigame))continue;
            game.start(players);
        }
    }

    public void stopAll() {
        for (Minigame minigame : minigames) {
            if (!minigame.isRunning())continue;
            minigame.stop();
        }
    }
}
