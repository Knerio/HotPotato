package de.derioo.gameapi.hotpotato;

import com.google.gson.JsonObject;
import de.derioo.gameapi.utils.ItemBuilder;
import de.derioo.gameapi.utils.LocationUtils;
import de.derioo.gameapi.Main;
import de.derioo.gameapi.utils.Minigame;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class HotPotato extends Minigame implements Listener {

    private ArrayList<UUID> players;
    private String bossbar;
    private int duration;

    private int maxTimePerRound;

    private Player potatoPlayer;

    private final Plugin plugin;

    private final Map<Player, BossBar> bossbars = new HashMap<>();

    public HotPotato(Plugin plugin) {
        this.maxTimePerRound = Main.getInstance().getJsonConfig().get("timeConfig").getAsJsonObject().get("maxTimePerRoundInTicks").getAsInt();
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void onLeave(Player p) {
        if (p.equals(potatoPlayer)) setNewPotatoPlayer();
        this.removePlayer(p);
    }

    @Override
    public void onJoin(Player p) {

    }

    @Override
    public void onStart(ArrayList<UUID> startPlayers) {
        this.players = startPlayers;
        this.duration = 0;
        this.bossbar = Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("bossbar").getAsString();
        setNewPotatoPlayer();
        ItemStack[] emptyItems = new ItemStack[0];

        players.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (!p.equals(this.potatoPlayer)) {
                p.getInventory().setArmorContents(emptyItems);
                p.getInventory().setContents(emptyItems);
                p.getInventory().setExtraContents(emptyItems);
            }
            p.teleportAsync(LocationUtils.getLocation(Main.getInstance().getJsonConfig().get("locations").getAsJsonObject().get("spawn").getAsJsonObject()));
            updateBossbar(uuid);
        });

    }

    @Override
    public void onStop() {
        bossbars.forEach((p, bossBar) -> bossBar.removePlayer(p));
    }

    @Override
    public void onRunning() {
        this.duration += 10;
        if (this.maxTimePerRound - this.duration <= 0) {
            this.removePlayer(this.potatoPlayer);
            this.nextRound();
            return;
        }
        updateBossbar();
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        ItemStack[] emptyItems = new ItemStack[0];

        player.getInventory().setContents(emptyItems);
        player.getInventory().setArmorContents(emptyItems);
        player.getInventory().setExtraContents(emptyItems);


        new ArrayList<>(players).stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(Component.text(Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("chatmessages").getAsJsonObject().get("eliminated").getAsString().replace("{player}",player.getName())));
        });
        this.onPlayerDie(player);
    }

    private void nextRound() {
        this.maxTimePerRound = (this.maxTimePerRound - Main.getInstance().getJsonConfig().get("timeConfig").getAsJsonObject().get("timeLossPerRoundInTicks").getAsInt()) <= 0 ? this.maxTimePerRound : (this.maxTimePerRound - Main.getInstance().getJsonConfig().get("timeConfig").getAsJsonObject().get("timeLossPerRoundInTicks").getAsInt());
        if (players.size() <= 1) {
            new ArrayList<>(players).stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                p.sendMessage(Component.text( Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("chatmessages").getAsJsonObject().get("won").getAsString().replace("{player}", Bukkit.getPlayer(players.get(0)).getName())));
            });
            this.stop();
            return;
        }

        setNewPotatoPlayer();
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player hitter)) return;
        if (!(e.getEntity() instanceof Player player)) return;

        if (hitter.equals(this.potatoPlayer)) {
            setPotatoPlayer(player);

            spawnFirework(player);
        }
    }

    private void spawnFirework(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(FireworkEffect.builder()
                .trail(true)
                .withColor(Color.RED)
                .build());
        firework.setFireworkMeta(meta);

        Bukkit.getScheduler().runTaskLater(plugin, firework::detonate, 100);
    }


    private void updateBossbar(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            players.remove(uuid);
            return;
        }
        BossBar bossBar = bossbars.getOrDefault(p, Bukkit.createBossBar(this.bossbar.replace("{duration}", getFormattedTime()), BarColor.BLUE, BarStyle.SOLID));
        bossBar.setProgress((double) (this.maxTimePerRound - this.duration) / this.maxTimePerRound);
        bossBar.setTitle(this.bossbar.replace("{duration}", getFormattedTime()));
        bossbars.put(p, bossBar);
        bossBar.addPlayer(p);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("actionbar").getAsString().replace("{role}",getRole(uuid))));
    }

    private String getRole(UUID uuid) {
        JsonObject o = Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("roles").getAsJsonObject();
        return this.potatoPlayer.getUniqueId().equals(uuid) ? o.get("potato").getAsString() : o.get("player").getAsString();
    }


    private String getFormattedTime() {
        int ticks = this.maxTimePerRound - this.duration;
        int seconds = (int) ticks / 20;
        return seconds + " seconds";
    }

    private void setNewPotatoPlayer() {
        ArrayList<UUID> copiedList = new ArrayList<>(players);
        Collections.shuffle(copiedList);
        UUID newPlayer = copiedList.get(0);
        setPotatoPlayer(Bukkit.getPlayer(newPlayer));
    }

    private void setPotatoPlayer(Player newPlayer) {
        ItemStack[] emptyItems = new ItemStack[0];

        if (this.potatoPlayer != null) {
            this.potatoPlayer.getInventory().setArmorContents(emptyItems);
            this.potatoPlayer.getInventory().setContents(emptyItems);
            this.potatoPlayer.getInventory().setExtraContents(emptyItems);


        }

        this.potatoPlayer = newPlayer;

        this.potatoPlayer.getInventory().setArmorContents(emptyItems);
        this.potatoPlayer.getInventory().setContents(emptyItems);
        this.potatoPlayer.getInventory().setExtraContents(emptyItems);

        this.potatoPlayer.getInventory().setItem(4, new ItemBuilder(Material.POTATO).setName("Gib die Kartoffel ab!").toItemStack());
        this.potatoPlayer.getInventory().setHelmet(new ItemBuilder(Material.PLAYER_HEAD).setCustomSkullWithValue(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Y0NjI0ZWJmN2Q0MTlhMTFlNDNlZDBjMjAzOGQzMmNkMDlhZDFkN2E2YzZlMjBmNjMzOWNiY2ZlMzg2ZmQxYyJ9fX0="
        ).toItemStack());


    }


    private void onPlayerDie(Player p) {
        p.teleportAsync(LocationUtils.getLocation(Main.getInstance().getJsonConfig().get("locations").getAsJsonObject().get("hub").getAsJsonObject()));
        spawnFirework(p);
    }

    private void updateBossbar() {
        new ArrayList<>(players).stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(this::updateBossbar);
    }
}
