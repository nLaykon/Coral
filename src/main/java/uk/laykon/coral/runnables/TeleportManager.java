package uk.laykon.coral.runnables;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import uk.laykon.coral.Types.TeleportQueue;
import uk.laykon.coral.autoreg.AutoRunnable;
import uk.laykon.coral.util.Constants;
import uk.laykon.coral.util.NumberUtil;

import java.util.HashSet;

public class TeleportManager {
    public static HashSet<TeleportQueue> queue = new HashSet<>();

    @AutoRunnable(delayTicks = 0L, periodTicks = 2L, async = false)
    public void teleportHander(){
        if (queue.isEmpty()){
            return;
        }

        for (TeleportQueue x : queue){
            Player player = x.getPlayer();
            if (x.getDelay() <= 0.1){
                player.teleport(x.getLocation());
                player.sendMessage(Constants.message.deserialize("<colour:green>Teleport Successful"));
                player.sendActionBar(Component.empty());
                queue.remove(x);
                continue;
            }

            x.decreaseDelay(0.1f);
            player.sendActionBar(Constants.message.deserialize(
                    "<color:#888888>Teleporting in <color:gold>" +
                    NumberUtil.formatFloat(x.getDelay(), 1) +
                    "s <color:#ff8888>Dont move!</color>")
            );
        }
    }

    public static void teleport(Player player, Location location){
        queue.add(new TeleportQueue(
                player,
                location,
                5f
        ));
    }
    public static void teleport(Player player, Location location, float delay){
        queue.add(new TeleportQueue(
                player,
                location,
                delay
        ));
    }

    public static boolean contains(Player player){
        for (TeleportQueue x : queue){
            if (x.getPlayer().equals(player)){
                return true;
            }
        }
        return false;
    }

    public static void removeIfContained(Player player){
        boolean removed = queue.removeIf(x -> x.getPlayer().equals(player));
        if (removed) {
            player.sendActionBar(Constants.message.deserialize("<color:red>Teleport Cancelled!"));
        }
    }
}
