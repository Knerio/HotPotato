package de.derioo.td.utils;

import org.bukkit.Bukkit;

public class Logger {

    public static int MSG = 0;
    public static int WARN = 1;
    public static int ERROR = 2;

    public static void log(int msgType, String msg ) {
        switch (msgType) {
            case 0 -> msg = "§b[TD] | §r " + msg;
            case 1 -> msg = "§e[TD] WARN: " + msg;
            case 2 -> msg = "§4[TD] ERROR: §c" + msg;
            default -> {
                return;
            }
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void error(StackTraceElement[] error){
        for (StackTraceElement e : error) {
            log(ERROR, e.toString());
        }
    }

    public static void log(int type,StackTraceElement[] error){
        for (StackTraceElement e : error) {
            log(type, e.toString());
        }
    }


    public static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("§b[DerioLog]§r "+msg);
    }
}
