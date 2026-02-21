package uk.laykon.coral;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import uk.laykon.coral.autoreg.AutoRegistrar;

public final class Main extends JavaPlugin {
    private static JavaPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        AutoRegistrar.registerAll(this, "uk.laykon.coral");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }


    public static JavaPlugin getInstance() {
        return instance;
    }
}
