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
    public static HashSet<TeleportQueue> teleportQueue = new HashSet<>();

    float delay = 5f;

    @AutoRunnable(delayTicks = 0L, periodTicks = 2L)
    public void teleportHander(){
        if (teleportQueue.isEmpty()){
            return;
        }

        for (TeleportQueue x : teleportQueue){
            Player player = x.getPlayer();
            if (x.getDelay() <= 0.1){
                player.teleport(x.getLocation());
                player.sendMessage(Constants.message.deserialize("<colour:green>Teleport Successful"));
                player.sendActionBar(Component.empty());
                teleportQueue.remove(x);
                continue;
            }

            x.decreaseDelay(0.1f);
            player.sendActionBar(Constants.message.deserialize("<color:#888888>Teleporting in <color:gold>" + NumberUtil.formatFloat(x.getDelay(), 1) + "s <color:#ff8888>Dont move!</color>"));
        }
    }

    public static void teleport(Player player, Location location){
        teleportQueue.add(new TeleportQueue(
                player,
                location,
                5f
        ));
    }

    public static boolean contains(Player player){
        for (TeleportQueue x : teleportQueue){
            if (x.getPlayer().equals(player)){
                return true;
            }
        }
        return false;
    }

    public static void removeIfContained(Player player){
        boolean removed = teleportQueue.removeIf(x -> x.getPlayer().equals(player));
        if (removed) {
            player.sendActionBar(Constants.message.deserialize("<color:red>Teleport Cancelled!"));
        }
    }
}
