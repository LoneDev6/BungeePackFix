package dev.lone.bungeepackfix.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerResourcePackSendEvent;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import dev.lone.bungeepackfix.BungeePackFixVelocity;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Objects;

public class ServerResourcePackSendListener {

    private final BungeePackFixVelocity plugin;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ServerResourcePackSendListener(BungeePackFixVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerResourcePackSend(ServerResourcePackSendEvent event) {
        if (plugin.getConfig().getLog().isDebug())
            plugin.getLogger().warn(
                    "ServerResourcePackSendEvent: " +
                            event.getServerConnection().getPlayer().getUsername());

        if (event.getServerConnection().getPlayer().getAppliedResourcePack() == null) {
            event.setResult(ResultedEvent.GenericResult.allowed());
            if (plugin.getConfig().getLog().isSentRespack())
                plugin.getLogger().warn(
                        "Sending ResourcePack: " +
                                event.getServerConnection().getPlayer().getUsername());
            return;
        }

        ResourcePackInfo playerPack = event.getServerConnection().getPlayer().getAppliedResourcePack();
        ResourcePackInfo serverPack = event.getProvidedResourcePack();

        if (playerPack == serverPack || (serverPack.getUrl().equals(playerPack.getUrl()) &&
                (!plugin.getConfig().getEqualPackAttributes().isHash() || Objects.equals(playerPack.getHash(), serverPack.getHash())) &&
                (!plugin.getConfig().getEqualPackAttributes().isForced() || playerPack.getShouldForce() == serverPack.getShouldForce()) &&
                (!plugin.getConfig().getEqualPackAttributes().isPromptMessage() || Objects.equals(playerPack.getPrompt(), serverPack.getPrompt())))) {
            event.setResult(ResultedEvent.GenericResult.denied());
            if (plugin.getConfig().getMessages().isEnabled())
                event.getServerConnection().getPlayer().sendMessage(miniMessage.deserialize(plugin.getConfig().getMessages().getMessage()));
            if (plugin.getConfig().getLog().isIgnoreRespack())
                plugin.getLogger().warn(
                        "Ignoring ResourcePack: " +
                                event.getServerConnection().getPlayer().getUsername());
            return;
        }

        if (plugin.getConfig().getLog().isSentRespack())
            plugin.getLogger().warn(
                    "Sending ResourcePack: " +
                            event.getServerConnection().getPlayer().getUsername());
    }
}
