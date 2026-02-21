package uk.laykon.coral.commands.utility.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.laykon.coral.autoreg.AutoCommand;

public class Fly {
    @AutoCommand(
            value = "fly",
            description = "toggles the flight",
            permission = "coral.fly"
    )
    public void fly(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        boolean enable = !player.getAllowFlight();

        player.setAllowFlight(enable);
        player.setFlying(enable);

        TextColor color = TextColor.fromHexString(enable ? "#44ff44" : "#ff44444");
        Component sentMessage = Component.text("Flight " + (enable ? "Enabled" : "Disabled"))
            .color(color);
        player.sendMessage(sentMessage);
    }
}
