package de.derioo.td.commands;

import com.google.gson.JsonObject;
import de.derioo.td.Main;
import de.derioo.td.utils.Island;
import de.derioo.td.utils.Profile;
import de.derioo.td.utils.TowerDefenceHandler;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class JoinProfileCommand extends Command{

    TowerDefenceHandler handler = Main.getInstance().getHandler();

    public JoinProfileCommand() {
        super("joinprofile","", true);
    }

    @Override
    public void execute(Player p, String[] args) {

        if (!TowerDefenceHandler.users.containsKey(p.getUniqueId())){
            handler.joinPlayer(p.getUniqueId());
        }
        JsonObject data = TowerDefenceHandler.users.get(p.getUniqueId());

        Island island = handler.getIsland(Profile.getProfile(data.get("profiles").getAsJsonArray().get(0).getAsJsonObject()));

        if (!island.isGenerated()){
            handler.generateIsland(island);
        }

    }
}
