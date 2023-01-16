/*
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you,  right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.  Any license notice or attribution required by the License must also include this Commons Cause License Condition notice.
 *
 * Software: BungeePackFix
 * License: Apache 2.0
 * Licensor: LoneDev
 */
package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.bungee.libs.packetlistener.Packets;
import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.impl.ClientboundResourcePackPacket;
import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.impl.ServerboundResourcePackPacket;
import dev.lone.bungeepackfix.generic.PackUtility;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends Plugin implements Listener
{
    public static Logger logger;
    public Settings settings;

    HashMap<UUID, BungeePlayerPackCache> playersCache = new HashMap<>();

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        settings = new Settings(this);

        registerPackets();
        getProxy().getPluginManager().registerListener(this, this);

        //TODO fix metrics backend URL
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            new Metrics(this, 13008);
        });
    }

    @Override
    public void onDisable()
    {
        logger = null;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e)
    {
        playersCache.remove(e.getPlayer().getUniqueId());
    }

    private void registerPackets()
    {
        ClientboundResourcePackPacket.register();
        Packets.registerHandler(ClientboundResourcePackPacket.class, (packet, conn) -> {

            if(isIgnoredServer(conn))
                return false;

            BungeePlayerPackCache cache = playersCache.get(conn.getUniqueId());
            if(cache != null && cache.installedSuccessfully)
            {
                if(cache.isSamePack(
                        packet,
                        conn,
                        settings
                ))
                {
                    if(settings.log_ignored_respack)
                        logger.log(Level.WARNING, "Ignored already sent pack: " + conn.getName() + " " + cache.cachedPacket);
                    if(settings.ignored_pack_msg_enabled)
                        conn.sendMessage(settings.ignored_pack_msg);

                    handleIgnoredPacket(conn);
                    return true;
                }
                else
                {
                    playersCache.put(conn.getUniqueId(), new BungeePlayerPackCache(packet));
                }
            }
            else
            {
                playersCache.put(conn.getUniqueId(), new BungeePlayerPackCache(packet));
            }

            if(settings.log_sent_respack)
                logger.log(Level.WARNING, "Sending pack: " + conn.getName() + " " + packet);

            return false;
        });

        ServerboundResourcePackPacket.register();
        Packets.registerHandler(ServerboundResourcePackPacket.class, (packet, conn) -> {

            if(isIgnoredServer(conn))
                return false;

            if(settings.log_debug)
                logger.log(Level.WARNING, "RespackStatusPacketIn: " + conn.getName() + " " + ServerboundResourcePackPacket.Status.values()[packet.status]);

            if(packet.status == ServerboundResourcePackPacket.Status.SUCCESSFULLY_LOADED.ordinal())
            {
                BungeePlayerPackCache cache = playersCache.get(conn.getUniqueId());
                if(cache != null)
                    cache.installedSuccessfully = true;
            }
            else if(packet.status == ServerboundResourcePackPacket.Status.FAILED_DOWNLOAD.ordinal())
            {
                playersCache.remove(conn.getUniqueId());
            }
            return false;
        });
    }

    private boolean isIgnoredServer(UserConnection conn)
    {
        return (settings.ignored_servers.contains(conn.getServer().getInfo().getName()));
    }

    /**
     * Emulate the client behaviour to maintain compatibility with plugins that are waiting for the
     * Spigot "PlayerResourcePackStatusEvent"
     * @param conn Player connection.
     */
    private void handleIgnoredPacket(UserConnection conn)
    {
        Packets.sendPacketToServer(
                this,
                conn,
                new ServerboundResourcePackPacket(ServerboundResourcePackPacket.Status.SUCCESSFULLY_LOADED),
                PackUtility.DELAY_MS_FAKE_SUCCESS_PACKET
        );
    }
}
