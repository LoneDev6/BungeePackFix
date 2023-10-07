package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.generic.Metrics;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

public final class Main extends Plugin implements Listener
{
    public static Logger logger;
    private BungeeAudiences adventure;
    public Settings settings;
    private static Main instance;

    public static Main inst()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        logger = this.getLogger();
        adventure = BungeeAudiences.create(this);
        try
        {
            settings = new Settings(this.getDataFolder().toPath(), getResourceAsStream("config.yml"));
        }
        catch (Throwable ex)
        {
            logger.severe("Failed to load settings.");
            ex.printStackTrace();
            logger.severe("Disabling plugin.");
            return;
        }

        EventsListener eventsListener = new EventsListener();
        eventsListener.registerBungeePackets();
        eventsListener.registerEvents();

        getProxy().getPluginManager().registerListener(this, this);

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            new Metrics(this, 9);
        });
    }

    @Override
    public void onDisable()
    {
        logger = null;
        if (this.adventure != null)
        {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @NonNull
    public BungeeAudiences adventure()
    {
        if (this.adventure == null)
        {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }
}
