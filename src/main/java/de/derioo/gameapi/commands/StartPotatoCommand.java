package de.derioo.gameapi.commands;

import com.google.gson.JsonParser;
import de.derioo.gameapi.Main;
import de.derioo.gameapi.utils.Command;
import de.derioo.gameapi.utils.FileUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartPotatoCommand extends Command {
    public StartPotatoCommand() {
        super("potato", "potato.start", true);
    }

    @Override
    public void execute(Player p, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                Main.getInstance().setJsonConfig(JsonParser.parseString(FileUtils.readFile(new File(Main.getInstance().getDataFolder().getPath(), "config.json"))).getAsJsonObject());
            } catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(Component.text(Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("reload-failed").getAsString()));
                return;
            }
            p.sendMessage(Component.text(Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("reload").getAsString()));
            return;
        }
        Main.getInstance().getManager().start(Main.getInstance().getHotPotato(), new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList()));
        p.sendMessage(Component.text(Main.getInstance().getJsonConfig().get("messages").getAsJsonObject().get("started").getAsString()));
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> list) {
        list.add("reload");
    }
}
