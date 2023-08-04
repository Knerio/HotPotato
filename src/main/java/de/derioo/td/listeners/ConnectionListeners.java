package de.derioo.td.listeners;

import de.derioo.td.Main;
import de.derioo.td.utils.DataBase;
import de.derioo.td.utils.TowerDefenceHandler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.function.Consumer;

public class ConnectionListeners implements Listener {

    TowerDefenceHandler handler;

    public ConnectionListeners() {
        handler = Main.getInstance().getHandler();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            handler.joinPlayer(e.getPlayer().getUniqueId());
        });
        e.getPlayer().teleport(handler.getHub());
    }
/*
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!e.getWorld().getName().equals("islands"))return;
        if (e.isNewChunk()) {
            Chunk chunk = e.getChunk();

            int x = chunk.getX() << 4;
            int z = chunk.getZ() << 4;
            for (int xx = x; xx < x + 16; xx++) {
                for (int zz = z; zz < z + 16; zz++) {
                    for (int yy = -64; yy < 310; yy++) {
                        Block block = e.getWorld().getBlockAt(xx, yy, zz);
                        if (!block.getType().equals(Material.AIR))
                            block.setBlockData(Material.LIGHT.createBlockData(), false);
                    }
                }
            }
        }
    }

 */
}
