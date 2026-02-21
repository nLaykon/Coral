package uk.laykon.coral.commands.utility.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.laykon.coral.Main;
import uk.laykon.coral.autoreg.AutoCommand;
import uk.laykon.coral.runnables.TeleportManager;
import uk.laykon.coral.util.Constants;

public class RTP {
    @AutoCommand(
            value = "rtp",
            description = "Teleports player to random location",
            permission = "coral.rtp"
    )
    public static void rtp(CommandSender sender) {
        if (!(sender instanceof Player player)){
            return;
        }
        World world = player.getWorld();

        int newX = Constants.random.nextInt(-2000, 2000);
        int newZ = Constants.random.nextInt(-2000, 2000);

        world.getChunkAtAsync(newX, newZ, true).thenAccept(chunk -> {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                Location newLoc = new Location(world, newX, (world.getHighestBlockAt(newX, newZ).getY()+1), newZ);
                TeleportManager.teleport(player, newLoc);
            });
        });
    }

}
