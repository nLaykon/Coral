package uk.laykon.coral.autoreg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public record CommandContext(JavaPlugin plugin, CommandSender sender, Command command, String label, String[] args) {
}
