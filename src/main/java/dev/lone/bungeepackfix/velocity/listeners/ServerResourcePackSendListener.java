package dev.lone.bungeepackfix.velocity.listeners;

import com.google.common.io.BaseEncoding;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerResourcePackSendEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import dev.lone.bungeepackfix.generic.PackUtility;
import dev.lone.bungeepackfix.velocity.BungeePackFixVelocity;
import dev.lone.bungeepackfix.velocity.Settings;
import dev.lone.bungeepackfix.velocity.VelocityPlayerPackCache;

public class ServerResourcePackSendListener
{
    private final BungeePackFixVelocity plugin;
    private final Settings settings;

    public ServerResourcePackSendListener(BungeePackFixVelocity plugin)
    {
        this.plugin = plugin;
        this.settings = plugin.settings;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerResourcePackSend(ServerResourcePackSendEvent e)
    {
        final Player player = e.getServerConnection().getPlayer();
        final ResourcePackInfo serverPack = e.getProvidedResourcePack();

        if (settings.log_debug)
            plugin.getLogger().warn("ServerResourcePackSendEvent: " + player.getUsername() + " " + readablePacket(serverPack));

        if (settings.isIgnoredServer(e.getServerConnection().getServerInfo().getName()))
        {
            plugin.getLogger().warn("Skipping ignored server: " + player.getUsername() + " " + readablePacket(serverPack));
            return;
        }

        if (player.getAppliedResourcePack() == null)
        {
            if (settings.log_sent_respack)
                plugin.getLogger().warn("Sending pack: " + player.getUsername() + " " + readablePacket(serverPack));
            return;
        }

        final ResourcePackInfo playerPack = player.getAppliedResourcePack();

        if (VelocityPlayerPackCache.isSamePack(
                settings,
                e.getServerConnection(),
                playerPack,
                serverPack,
                settings.ignore_hash_in_url,
                settings.equal_pack_attributes_hash,
                settings.equal_pack_attributes_forced,
                settings.equal_pack_attributes_prompt_message
        ))
        {
            if (plugin.settings.log_ignored_respack)
                plugin.getLogger().warn("Ignored already sent pack: " + player.getUsername() + " " + readablePacket(serverPack));
            if (plugin.settings.ignored_pack_msg_enabled)
                player.sendMessage(plugin.settings.ignored_pack_msg);

            e.setResult(ResultedEvent.GenericResult.denied());
            handleIgnoredPacket(e.getServerConnection(), serverPack);
            return;
        }

        if (plugin.settings.log_sent_respack)
            plugin.getLogger().warn("Sending pack: " + player.getUsername() + " " + readablePacket(serverPack));
    }

    private String readablePacket(ResourcePackInfo pack)
    {
        return "ResourcePackInfo{url='" + pack.getUrl() + '\'' + ", hash=" + hashToString(pack.getHash()) + ", forced=" + pack.getShouldForce() + ", promptMessage=" + pack.getPrompt() + '}';
    }

    private String hashToString(byte[] hash)
    {
        if(hash == null)
            return "null";
        return BaseEncoding.base16().lowerCase().encode(hash);
    }

    /**
     * Emulate the client behaviour to maintain compatibility with plugins that are waiting for the
     * Spigot "PlayerResourcePackStatusEvent"
     *
     * @param conn Player connection.
     * @param pack The resourcepack info.
     */
    private void handleIgnoredPacket(ServerConnection conn, ResourcePackInfo pack)
    {
        plugin.runDelayedTask(() -> {
            plugin.getProxy().getEventManager().fireAndForget(new PlayerResourcePackStatusEvent(
                    conn.getPlayer(),
                    PlayerResourcePackStatusEvent.Status.SUCCESSFUL,
                    pack
            ));
        }, PackUtility.DELAY_MS_FAKE_SUCCESS_PACKET);
    }
}
