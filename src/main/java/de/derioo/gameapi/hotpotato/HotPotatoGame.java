package de.derioo.gameapi.hotpotato;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.derioo.gameapi.utils.ConfigHandler;
import de.derioo.gameapi.utils.ItemBuilder;
import de.derioo.gameapi.utils.LocationUtils;
import de.derioo.gameapi.utils.Minigame;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class HotPotatoGame extends Minigame implements Listener {

    private ArrayList<UUID> players;
    private String bossbar;
    private int duration;

    private int maxTimePerRound;

    private Player potatoPlayer;

    private final Plugin plugin;

    private final Map<Player, BossBar> bossbars = new HashMap<>();

    public HotPotatoGame(Plugin plugin) {
        this.maxTimePerRound = ConfigHandler.get("timeConfig.maxTimePerRoundInTicks").getAsInt();
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void onLeave(Player p) {
        if (p.equals(potatoPlayer)) this.setNewPotatoPlayer();
        this.removePlayer(p);
    }

    @Override
    public void onJoin(Player p) {

    }

    @Override
    public void onStart(ArrayList<UUID> startPlayers) {
        this.players = startPlayers;
        this.duration = 0;
        this.bossbar = ConfigHandler.getMessage("bossbar").getAsString();
        this.setNewPotatoPlayer();
        ItemStack[] emptyItems = new ItemStack[0];

        this.players.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (!p.equals(this.potatoPlayer)) {
                p.getInventory().setArmorContents(emptyItems);
                p.getInventory().setContents(emptyItems);
                p.getInventory().setExtraContents(emptyItems);
            }
            p.teleportAsync(LocationUtils.getLocation(ConfigHandler.get("locations.spawn").getAsJsonObject()));
            this.updateBossbar(uuid);
        });

    }

    @Override
    public void onStop() {
        this.bossbars.forEach((p, bossBar) -> bossBar.removePlayer(p));
    }

    @Override
    public void onRunning() {
        this.duration += 10;
        if (this.maxTimePerRound - this.duration <= 0) {
            this.removePlayer(this.potatoPlayer);
            this.nextRound();
            return;
        }
        this.updateBossbar();

        this.players.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 1));
        });
        this.potatoPlayer.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, this.potatoPlayer.getLocation().clone().add(0, 2,0), 1);
        this.potatoPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 2));
    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUniqueId());
        ItemStack[] emptyItems = new ItemStack[0];

        player.getInventory().setContents(emptyItems);
        player.getInventory().setArmorContents(emptyItems);
        player.getInventory().setExtraContents(emptyItems);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text(ConfigHandler.getMessage("chatmessages.eliminated").getAsString().replace("{player}",player.getName())));
        }
        this.onPlayerDie(player);
    }

    private void nextRound() {
        this.maxTimePerRound = (this.maxTimePerRound - ConfigHandler.get("timeConfig.timeLossPerRoundInTicks").getAsInt()) <= 0 ? this.maxTimePerRound : (this.maxTimePerRound - ConfigHandler.get("timeConfig.timeLossPerRoundInTicks").getAsInt());
        if (this.players.size() <= 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text(ConfigHandler.getMessage("chatmessages.won").getAsString().replace("{player}", Bukkit.getPlayer(this.players.get(0)).getName())));
            }

            Bukkit.getPlayer(this.players.get(0)).teleportAsync(LocationUtils.getLocation(ConfigHandler.get("locations.win").getAsJsonObject()));
            this.stop();
            return;
        }

        this.setNewPotatoPlayer();
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player hitter)) return;
        if (!(e.getEntity() instanceof Player player)) return;

        if (hitter.equals(this.potatoPlayer)) {
            this.setPotatoPlayer(player);

            this.spawnFirework(player);
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

        Bukkit.getScheduler().runTaskLater(this.plugin, firework::detonate, 100);
    }


    private void updateBossbar(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            this.players.remove(uuid);
            return;
        }
        BossBar bossBar = this.bossbars.getOrDefault(p, Bukkit.createBossBar(this.bossbar.replace("{duration}", this.getFormattedTime()), BarColor.BLUE, BarStyle.SOLID));
        bossBar.setProgress((double) (this.maxTimePerRound - this.duration) / this.maxTimePerRound);
        bossBar.setTitle(this.bossbar.replace("{duration}", this.getFormattedTime()));
        this.bossbars.put(p, bossBar);
        bossBar.addPlayer(p);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ConfigHandler.getMessage("actionbar").getAsString().replace("{role}",this.getRole(uuid))));
    }

    private String getRole(UUID uuid) {
        JsonObject o = ConfigHandler.getMessage("roles").getAsJsonObject();
        return this.potatoPlayer.getUniqueId().equals(uuid) ? o.get("potato").getAsString() : o.get("player").getAsString();
    }


    private String getFormattedTime() {
        int ticks = this.maxTimePerRound - this.duration;
        int seconds = ticks / 20;
        return ConfigHandler.getMessage("formatted-time").getAsString().replace("{seconds}", String.valueOf(seconds));
    }

    private void setNewPotatoPlayer() {
        ArrayList<UUID> copiedList = new ArrayList<>(this.players);
        Collections.shuffle(copiedList);
        UUID newPlayer = copiedList.get(0);
        this.setPotatoPlayer(Bukkit.getPlayer(newPlayer));
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

        this.potatoPlayer.getInventory().setHelmet(ItemBuilder.fromJson(ConfigHandler.get("potato-player-inventory.armor.helmet").getAsJsonObject()).toItemStack());
        this.potatoPlayer.getInventory().setChestplate(ItemBuilder.fromJson(ConfigHandler.get("potato-player-inventory.armor.chestplate").getAsJsonObject()).toItemStack());
        this.potatoPlayer.getInventory().setLeggings(ItemBuilder.fromJson(ConfigHandler.get("potato-player-inventory.armor.leggins").getAsJsonObject()).toItemStack());
        this.potatoPlayer.getInventory().setBoots(ItemBuilder.fromJson(ConfigHandler.get("potato-player-inventory.armor.boots").getAsJsonObject()).toItemStack());


        JsonArray hotbar = ConfigHandler.get("potato-player-inventory.hotbar").getAsJsonArray();
        for (int i = 0; i < 9; i++) {
            if (hotbar.get(i).getAsJsonObject().size() == 0)continue;
            this.potatoPlayer.getInventory().setItem(i, ItemBuilder.fromJson(hotbar.get(i).getAsJsonObject()).toItemStack());
        }
    }


    private void onPlayerDie(Player p) {
        p.teleportAsync(LocationUtils.getLocation(ConfigHandler.get("locations.hub").getAsJsonObject()));
        this.spawnFirework(p);
    }

    private void updateBossbar() {
        new ArrayList<>(this.players).stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(this::updateBossbar);
    }
}
