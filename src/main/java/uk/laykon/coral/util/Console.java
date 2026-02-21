package uk.laykon.coral.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.laykon.coral.Main;

import java.util.logging.Level;

public class Console {
    private static final Plugin plugin = Main.getInstance();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final String codeName = "<gold>Coral</gold>";
    private static final CommandSender sender = Bukkit.getConsoleSender();

    private static Component formatMessage(String colorTag, String message) {
        String raw = "<" + colorTag + ">[" + codeName + "<" + colorTag + ">] " + message;
        return miniMessage.deserialize(raw);
    }

    public static void error(String error) {
        sender.sendMessage(formatMessage("red", error));
    }

    public static void log(String log) {
        sender.sendMessage(formatMessage("gray", log));
    }

    public static void warn(String warn) {
        sender.sendMessage(formatMessage("yellow", warn));
    }

    public static void success(String success) {
        sender.sendMessage(formatMessage("green", success));
    }

    public static void execute(String command) {
        Bukkit.dispatchCommand(sender, command);
    }

    public static void announceError(String error) {
        plugin.getLogger().log(Level.SEVERE, error);
        Component msg = formatMessage("red", error);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void announceLog(String log) {
        plugin.getLogger().log(Level.INFO, log);
        Component msg = formatMessage("gray", log);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void announceWarn(String warn) {
        plugin.getLogger().log(Level.WARNING, warn);
        Component msg = formatMessage("yellow", warn);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void announceSuccess(String success) {
        plugin.getLogger().log(Level.INFO, success);
        Component msg = formatMessage("green", success);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void announce(String announcement) {
        plugin.getLogger().log(Level.INFO, announcement);
        Component msg = miniMessage.deserialize("<aqua>[" + codeName + "]</aqua> <gray><bold>" + announcement + "</bold></gray>");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void sendToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Constants.message.deserialize(message));
        }
    }
}
