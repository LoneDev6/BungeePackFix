package dev.lone.bungeepackfix;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.lone.bungeepackfix.configuration.Configuration;
import dev.lone.bungeepackfix.listeners.ServerResourcePackSendListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "bungeepackfix",
        name = "BungeePackFix (Velocity)",
        version = "1.0.8",
        description = "Avoid sending resourcepacks again if it's the same resourcepack. Useful when you switch servers.", authors = {"LoneDev", "YoSoyVillaa"}
)
public class BungeePackFixVelocity {

    private final Path pluginPath;
    private final Logger logger;
    private final ProxyServer proxy;
    private Configuration config;

    @Inject
    public BungeePackFixVelocity(ProxyServer proxy, @DataDirectory Path pluginPath, Logger logger){
        this.proxy = proxy;
        this.pluginPath = pluginPath;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event){
        Toml toml = loadConfig();
        if(toml == null) {
            logger.error("-- Cannot load Configuration --");
            logger.error("Disabling features");
            return;
        }
        this.config = new Configuration(toml);
        proxy.getEventManager().register(this, new ServerResourcePackSendListener(this));
    }

    public Logger getLogger() {
        return logger;
    }

    public Configuration getConfig() {
        return config;
    }

    private Toml loadConfig(){
        if(!Files.exists(pluginPath)){
            try {
                Files.createDirectory(pluginPath);
            } catch(IOException e){
                return null;
            }
        }
        Path configPath = pluginPath.resolve("config.toml");
        if(!Files.exists(configPath)){
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.toml")){
                Files.copy(in, configPath);
            } catch(IOException e){
                return null;
            }
        }
        try {
            return new Toml().read(Files.newInputStream(configPath));
        } catch (IOException e){
            return null;
        }
    }
}
