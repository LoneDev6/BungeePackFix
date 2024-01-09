package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.bungee.packets.Packets;
import dev.lone.bungeepackfix.bungee.packets.impl.ClientboundResourcePackPacket;
import dev.lone.bungeepackfix.bungee.packets.impl.ClientboundResourcePackPopPacket;
import dev.lone.bungeepackfix.bungee.packets.impl.ServerboundResourcePackPacket;
import dev.lone.bungeepackfix.generic.PackUtility;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class EventsListener implements Listener
{
    private final HashMap<UUID, PlayersPackCache> playersCache = new HashMap<>();

    public void registerEvents()
    {
        Main.inst().getProxy().getPluginManager().registerListener(Main.inst(), this);
    }

    public void registerBungeePackets()
    {
        ClientboundResourcePackPacket.register();
        Packets.registerHandler(ClientboundResourcePackPacket.class, (packet, conn) -> {

            if(isIgnoredServer(conn))
                return false;

            PlayersPackCache cache = playersCache.get(conn.getUniqueId());
            if(cache != null && cache.installedSuccessfully)
            {
                if(cache.isSamePack(
                        packet,
                        conn,
                        Main.inst().settings
                ))
                {
                    if(Main.inst().settings.log_ignored_respack)
                        Main.logger.log(Level.WARNING, "Ignored already sent pack: " + conn.getName() + " " + cache.cachedPacket);
                    if(Main.inst().settings.ignored_pack_msg_enabled)
                        conn.sendMessage(Main.inst().settings.ignored_pack_msg);

                    handleIgnoredPacket(conn);
                    return true;
                }
                else
                {
                    playersCache.put(conn.getUniqueId(), new PlayersPackCache(packet));
                }
            }
            else
            {
                playersCache.put(conn.getUniqueId(), new PlayersPackCache(packet));
            }

            if(Main.inst().settings.log_sent_respack)
                Main.logger.log(Level.WARNING, "Sending pack: " + conn.getName() + " " + packet);

            return false;
        });

        ServerboundResourcePackPacket.register();
        Packets.registerHandler(ServerboundResourcePackPacket.class, (packet, conn) -> {

            if(isIgnoredServer(conn))
                return false;

            if(Main.inst().settings.log_debug)
                Main.logger.log(Level.WARNING, "RespackStatusPacketIn: " + conn.getName() + " " + ServerboundResourcePackPacket.Status.values()[packet.status]);

            if(packet.status == ServerboundResourcePackPacket.Status.SUCCESSFULLY_LOADED.ordinal())
            {
                PlayersPackCache cache = playersCache.get(conn.getUniqueId());
                if(cache != null)
                    cache.installedSuccessfully = true;
            }
            else if(packet.status == ServerboundResourcePackPacket.Status.FAILED_DOWNLOAD.ordinal())
            {
                playersCache.remove(conn.getUniqueId());
            }
            return false;
        });

        ClientboundResourcePackPopPacket.register();
        Packets.registerHandler(ClientboundResourcePackPopPacket.class, (packet, conn) -> {
            return Main.inst().settings.cancel_modern_resourcepack_remove_packet;
        });
    }

    private boolean isIgnoredServer(UserConnection conn)
    {
        return Main.inst().settings.isIgnoredServer(conn.getServer().getInfo().getName());
    }

    /**
     * Emulate the client behaviour to maintain compatibility with plugins that are waiting for the
     * Spigot "PlayerResourcePackStatusEvent"
     * @param conn Player connection.
     */
    private void handleIgnoredPacket(UserConnection conn)
    {
        Packets.sendPacketToServer(
                Main.inst(),
                conn,
                new ServerboundResourcePackPacket(ServerboundResourcePackPacket.Status.SUCCESSFULLY_LOADED),
                PackUtility.DELAY_MS_FAKE_SUCCESS_PACKET
        );
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e)
    {
        playersCache.remove(e.getPlayer().getUniqueId());
    }
}
