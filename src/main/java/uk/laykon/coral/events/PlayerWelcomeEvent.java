package uk.laykon.coral.events;

import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerJoinEvent;
import uk.laykon.coral.autoreg.AutoEvent;
import uk.laykon.coral.util.Console;

public class PlayerWelcomeEvent {
    @AutoEvent
    public void onPlayerJoin(PlayerJoinEvent it) {
        it.joinMessage(Component.empty());
        if (it.getPlayer().hasPlayedBefore()) {
            Console.sendToAll("<color:#888888>Welcome back <color:gold>" + it.getPlayer().getName() + "!</color:#888888>");
            return;
        }

        Console.sendToAll("<color:#888888><b>Everybody welcome <color:gold>" + it.getPlayer().getName() + "!</color:#888888>");
    }

}
