package uk.laykon.coral.commands.utility.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uk.laykon.coral.autoreg.AutoCommand;
import uk.laykon.coral.util.Constants;

public class NightVision {
    @AutoCommand(
            value = "nightvision",
            description = "gives self night vision",
            permission = "coral.nightvision",
            aliases = {
                    "nv"
            }
    )
    public void onCommand(CommandSender sender) {
        if (!(sender instanceof Player player)){
            return;
        }
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)){
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(Constants.message.deserialize("<color:red>Night Vision removed!"));
            return;
        }
        PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2);
        player.addPotionEffect(effect);
        player.sendMessage(Constants.message.deserialize("<color:green>Night Vision added!"));
    }
}
