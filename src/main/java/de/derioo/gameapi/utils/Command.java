package de.derioo.gameapi.utils;


import de.derioo.gameapi.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Command implements CommandExecutor, TabCompleter {
    private final String permission;
    private final String name;
    private final boolean requiresPlayer;
    private final List<String> tabComplete;

    public Command(String name, @Nullable String permission, boolean requiresPlayer) {
        this.permission = permission;
        this.name = name;
        this.requiresPlayer = requiresPlayer;
        this.tabComplete = new ArrayList<>();

        PluginCommand command = Main.getInstance().getCommand(name);
        if (command == null) {
            System.out.println("konnte nicht geladen werden (" + this.name + ")");
        } else {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    public String getPermission() {
        return this.permission;
    }

    public Command getCMD() {
        return this;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequiresPlayer() {
        return this.requiresPlayer;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (!Objects.equals(permission, "")) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(Component.text(ConfigHandler.getMessage("no-perm").getAsString()));
                return false;
            }
        }
        if (requiresPlayer && !(sender instanceof Player)) {
            sender.sendMessage(Component.text(ConfigHandler.getMessage("need-player").getAsString()));
            return false;
        }

        if (requiresPlayer) {
            this.execute((Player) sender, args);
            return false;
        }

        this.execute(sender, args);

        return false;
    }

    public void execute(CommandSender sender, String[] args) {
    }


    public void execute(Player p, String[] args) {
    }


    public void onTabComplete(CommandSender sender, String[] args, List<String> list) {
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        tabComplete.clear();
        onTabComplete(sender, args, tabComplete);

        List<String> completeList = new ArrayList<>();
        String currentarg = args[args.length - 1].toLowerCase();
        for (String s : tabComplete) {
            try {
                String s1 = s.toLowerCase();
                if (s1.startsWith(currentarg)) {
                    completeList.add(s);
                }
            } catch (Exception ignored) {}
        }
        return completeList;
    }
}
