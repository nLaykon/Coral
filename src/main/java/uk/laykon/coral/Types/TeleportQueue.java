package uk.laykon.coral.Types;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportQueue {
    Player player;
    Location location;
    float delay;
    public TeleportQueue(Player player, Location location, float delay) {
        this.player = player;
        this.location = location;
        this.delay = delay;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    public Player getPlayer() {
        return player;
    }
    public Location getLocation() {
        return location;
    }

    public void decreaseDelay(float delay) {
        this.delay -= delay;
    }
}
