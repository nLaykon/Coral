package uk.laykon.coral.events;

import org.bukkit.event.player.PlayerMoveEvent;
import uk.laykon.coral.autoreg.AutoEvent;
import uk.laykon.coral.runnables.TeleportManager;

public class TeleportCanceller {
    @AutoEvent
    public void onMove(PlayerMoveEvent it){
        TeleportManager.removeIfContained(it.getPlayer());
    }
}
