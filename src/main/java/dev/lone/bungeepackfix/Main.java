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
package dev.lone.bungeepackfix;

import dev.lone.bungeepackfix.libs.packetlistener.Packets;
import dev.lone.bungeepackfix.libs.packetlistener.packets.RespackSendPacketOut;
import dev.lone.bungeepackfix.libs.packetlistener.packets.RespackStatusPacketIn;
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

    HashMap<UUID, PlayerPackCache> playersPacks = new HashMap<>();

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        settings = new Settings(this);

        registerPackets();
        getProxy().getPluginManager().registerListener(this, this);

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            Metrics metrics = new Metrics(this, 13008);
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
        playersPacks.remove(e.getPlayer().getUniqueId());
    }

    private void registerPackets()
    {
        RespackSendPacketOut.register();
        Packets.registerHandler(RespackSendPacketOut.class, (packet, conn) -> {
            PlayerPackCache cache = playersPacks.get(conn.getUniqueId());
            if(cache != null && cache.installedSuccessfully)
            {
                if(cache.matches(packet))
                {
                    if(settings.log_ignored_respack)
                        logger.log(Level.WARNING, "Ignored already sent packet: " + conn.getName() + " " + cache.cachedPacket);
                    if(settings.ignored_pack_msg_enabled)
                        conn.sendMessage(settings.ignored_pack_msg);
                    handleIgnoredPacket(conn);
                    return true;
                }
                else
                {
                    playersPacks.put(conn.getUniqueId(), new PlayerPackCache(packet));
                }
            }
            else
            {
                playersPacks.put(conn.getUniqueId(), new PlayerPackCache(packet));
            }

            if(settings.log_sent_respack)
                logger.log(Level.WARNING, "Sending packet: " + conn.getName() + " " + packet);

            return false;
        });

        RespackStatusPacketIn.register();
        Packets.registerHandler(RespackStatusPacketIn.class, (packet, conn) -> {
            if(settings.log_debug)
                logger.log(Level.WARNING, "RespackStatusPacketIn: " + conn.getName() + " " + RespackStatusPacketIn.Status.values()[packet.status]);

            if(packet.status == RespackStatusPacketIn.Status.SUCCESSFULLY_LOADED.ordinal())
            {
                PlayerPackCache cache = playersPacks.get(conn.getUniqueId());
                if(cache != null)
                    cache.installedSuccessfully = true;
            }
            else if(packet.status == RespackStatusPacketIn.Status.FAILED_DOWNLOAD.ordinal())
            {
                playersPacks.remove(conn.getUniqueId());
            }
            return false;
        });
    }

    /**
     * Emulate the client behaviour to maintain compatibility with plugins that are waiting for the
     * Spigot "PlayerResourcePackStatusEvent"
     * @param conn Player connection.
     */
    private void handleIgnoredPacket(UserConnection conn)
    {
//        PacketsRegister.sendProxiedPacket(
//                this,
//                e.getPlayer(),
//                new RespackStatusPacketIn(RespackStatusPacketIn.Status.ACCEPTED),
//                500L
//        );
//        PacketsRegister.sendProxiedPacket(
//                this,
//                e.getPlayer(),
//                new RespackStatusPacketIn(RespackStatusPacketIn.Status.SUCCESSFULLY_LOADED),
//                1500L
//        );
        Packets.sendPacketToServer(
                this,
                conn,
                new RespackStatusPacketIn(RespackStatusPacketIn.Status.SUCCESSFULLY_LOADED),
                200L
        );
    }
}
