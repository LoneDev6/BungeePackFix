package dev.lone.bungeepackfix.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.lone.bungeepackfix.velocity.listeners.ServerResourcePackSendListener;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "bungeepackfix",
        name = "BungeePackFix (Velocity)",
        version = "1.1.0-r3",
        description = "Avoid sending resourcepacks again if it's the same resourcepack. Useful when you switch servers.", authors = {"LoneDev", "YoSoyVillaa"}
)
public class BungeePackFixVelocity
{
    private final Path dataDirectory;
    private final Logger logger;
    private final ProxyServer proxy;
    public Settings settings;

    @Inject
    public BungeePackFixVelocity(ProxyServer proxy, @DataDirectory Path dataDirectory, Logger logger)
    {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public ProxyServer getProxy()
    {
        return proxy;
    }

    public void runDelayedTask(Runnable runnable, long delayMs)
    {
        getProxy().getScheduler().buildTask(this, runnable)
                .delay(delayMs, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e)
    {
        try
        {
            this.settings = new Settings(dataDirectory, getClass().getClassLoader().getResourceAsStream("config.yml"));
            proxy.getEventManager().register(this, new ServerResourcePackSendListener(this));
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            logger.error("Disabling plugin.");
            return;
        }

        //TODO add metrics
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onPlayerDisconnect(DisconnectEvent e)
    {
        VelocityPlayerPackCache.playersCache.remove(e.getPlayer().getUniqueId());
    }
}
