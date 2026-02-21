package uk.laykon.coral.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import uk.laykon.coral.autoreg.AutoEvent;
import uk.laykon.coral.util.Constants;

public class PlayerMessageHandler {
    @AutoEvent
    public void onPlayerMessage(AsyncChatEvent it){
        Player player = it.getPlayer();
        Component message = it.message();
        Component parsedMessage;

        if (player.hasPermission("coral.message.format")){
            String raw = PlainTextComponentSerializer.plainText().serialize(message);
            parsedMessage = Constants.message.deserialize(raw);
        }else {
            parsedMessage = Component.text(PlainTextComponentSerializer.plainText().serialize(message)).color(TextColor.fromHexString("#ffffff"));
        }

        Component finalMessage = parsedMessage;

        it.renderer((source, sourcDisplayName, msg, viewer) -> {
            Component rank = Component.text("[Member] ",  NamedTextColor.YELLOW);

            return rank
                    .append(sourcDisplayName.color(NamedTextColor.YELLOW))
                    .append(Component.text(" » ",  NamedTextColor.GRAY))
                    .append(finalMessage).color(TextColor.fromHexString("#ffffff"));
        });
    }
}
