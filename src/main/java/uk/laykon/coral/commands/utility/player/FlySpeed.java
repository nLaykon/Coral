package uk.laykon.coral.commands.utility.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.laykon.coral.autoreg.AutoCommand;
import uk.laykon.coral.util.Constants;

public class FlySpeed {
    @AutoCommand(
            value = "flyspeed",
            description = "Sets self's fly speed",
            permission = "coral.flyspeed",
            aliases = {
                    "fs",
                    "speed"
            }
    )
    public void flyspeed(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)){
            return;
        }
        if (args.length == 0){
            return;
        }

        float rangeMin = 0.1f;
        float rangeMax = 10.0f;

        float selection;

        try  {
            selection = Float.parseFloat(args[0]);
        }catch (Exception e){
            player.sendMessage(MiniMessage.miniMessage().deserialize("<color:red>Invalid selection! <0.1-10>"));
            return;
        }

        if (selection < rangeMin || selection > rangeMax){
            player.sendMessage(Constants.message.deserialize("<color:red>Invalid selection! <0.1-10>"));
            return;
        }

        player.setFlySpeed(selection/10);

        player.sendMessage(Constants.message.deserialize("<color:green>Fly speed set to: <color:gold>" + selection));
    }

}
