package de.derioo.gameapi.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Minigame {

    @Getter
    @Setter
    private boolean isRunning;

    public Minigame() {
        this.isRunning = false;
    }


    public abstract void onLeave(Player p);

    public abstract void onJoin(Player p);

    public abstract void onStart(ArrayList<UUID> startPlayers);

    public abstract void onStop();

    public abstract void onRunning();

    public void start(ArrayList<UUID> players) {
        this.isRunning = true;
        this.onStart(players);
    }

    public void stop() {
        this.isRunning = false;
        this.onStop();
    }
}
