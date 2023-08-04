package de.derioo.td.utils;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.td.Main;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataBase {

    @Getter
    private static DataBase instance;

    @Getter
    private final Connection connection;



    public static boolean setup() {
        File credentials = new File(Main.getInstance().getDataFolder().getPath(), "mysql.json");
        new File(Main.getInstance().getDataFolder().getPath()).mkdirs();
        try {
            if (credentials.createNewFile() || FileUtils.readFile(credentials).isEmpty()) {
                FileUtils.copyFile(Main.getInstance().getResource("mysql.json"), credentials);
                Logger.log(Logger.ERROR, "Please put the MySQL credentials in the mysql.json");
                Bukkit.getPluginManager().disablePlugin(Main.getInstance());
                return false;
            }

            JsonObject object = JsonParser.parseString(FileUtils.readFile(credentials)).getAsJsonObject();
            new DataBase(object.get("host").getAsString(), object.get("port").getAsInt(), object.get("db").getAsString(), object.get("username").getAsString(), object.get("password").getAsString());
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DataBase(String url, int port, String database, String username, String password) {
        instance = this;
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + url + ":" + port + "/" + database + "?autoReconnect=true", username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        createTables();
    }

    public void save() {
        // TODO: 03.08.2023
    }

    public void updateUserData(UUID uuid, String newData){
        try {
            PreparedStatement st = null;
            st = this.connection.prepareStatement("INSERT INTO userdatas (uuid, json) VALUES ('" + uuid.toString() + "', ?) ON DUPLICATE KEY UPDATE json = ?");
            st.setString(1, newData);
            st.setString(2, newData);
            st.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return;
    }
    public String getUserData(UUID uuid){
        try {
            PreparedStatement st = this.connection.prepareStatement("SELECT * FROM userdatas WHERE uuid = '" + uuid.toString() + "'");
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("json");
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    private void createTables() {
        try {
            PreparedStatement st;
            st = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS userdatas (uuid VARCHAR(40)," +
                    "json TEXT)");
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO: 03.08.2023
    }


}
