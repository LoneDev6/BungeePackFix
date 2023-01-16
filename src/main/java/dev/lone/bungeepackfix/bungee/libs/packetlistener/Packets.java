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
package dev.lone.bungeepackfix.bungee.libs.packetlistener;

import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.Packet;
import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.ServerboundPacket;
import dev.lone.bungeepackfix.bungee.libs.packetlistener.packets.ClientboundPacket;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Static abuse magic.
 *
 * WARNING: for now it supports only Protocol.GAME packets.
 */
public class Packets
{
    private static final HashMap<Class<? extends DefinedPacket>, List<Handler<? extends Packet>>> packetsHandlers = new HashMap<>();

    //<editor-fold desc="Reflection initialization stuff">
    private static boolean legacyRegister;
    private static final HashMap<ServerConnection, ChannelWrapper> channelWrappers = new HashMap<>();

    private static Field field_DownStreamBridge_con;
    private static Field field_UptreamBridge_con;
    private static Field field_channel;
    private static Method method_map;
    private static Method method_regPacket;
    private static Class<?> class_protocolMapping;
    private static Object TO_CLIENT;
    private static Object TO_SERVER;

    static
    {
        Class<?> class_directionData;
        try
        {
            class_directionData = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
            class_protocolMapping = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolMapping");

            Field f_toClient = Protocol.class.getDeclaredField("TO_CLIENT");
            f_toClient.setAccessible(true);
            TO_CLIENT = f_toClient.get(Protocol.GAME);

            Field f_toServer = Protocol.class.getDeclaredField("TO_SERVER");
            f_toServer.setAccessible(true);
            TO_SERVER = f_toServer.get(Protocol.GAME);

            (method_map = Protocol.class.getDeclaredMethod("map", int.class, int.class)).setAccessible(true);
            (field_channel = ServerConnection.class.getDeclaredField("ch")).setAccessible(true);
            (field_DownStreamBridge_con = DownstreamBridge.class.getDeclaredField("con")).setAccessible(true);
            (field_UptreamBridge_con = UpstreamBridge.class.getDeclaredField("con")).setAccessible(true);

            for (Method method : class_directionData.getDeclaredMethods())
            {
                if (method.getName().equals("registerPacket"))
                {
                    legacyRegister = method.getParameters().length == 2;
                    method_regPacket = method;
                    method_regPacket.setAccessible(true);

                    // I don't break because the legacy method may be added by forks
                    if(legacyRegister)
                        break;
                }
            }
        }
        catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    /**
     * Obtain the ChannelWrapper instance from a ServerConnectedEvent.
     * @param e The event when the player connects to a server.
     * @return the ChannelWrapper instance from a ServerConnectedEvent.
     */
    public static ChannelWrapper getChannelWrapper(ServerConnectedEvent e)
    {
        return getChannelWrapper((ServerConnection) e.getServer());
    }

    /**
     * Obtain the ChannelWrapper instance from a ServerConnection.
     * @param serverConnection A ServerConnection.
     * @return the ChannelWrapper instance from a ServerConnection.
     */
    public static ChannelWrapper getChannelWrapper(ServerConnection serverConnection)
    {
        if (channelWrappers.containsKey(serverConnection))
            return channelWrappers.get(serverConnection);

        ChannelWrapper channelWrapper;
        try
        {
            channelWrapper = (ChannelWrapper) field_channel.get(serverConnection);
            channelWrappers.put(serverConnection, channelWrapper);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        return channelWrapper;
    }

    /**
     * Registers a custom or vanilla packet.
     * @param packetConstructor The generic constructor of this packet. Example: MyPacket::new)
     * @param packetMap Version of the client and packetId.
     */
    public static void registerPacket(Supplier<? extends DefinedPacket> packetConstructor, LinkedHashMap<Integer, Integer> packetMap)
    {
        Object dir = null;
        if(packetConstructor.get() instanceof ServerboundPacket)
            dir = TO_SERVER;
        else if(packetConstructor.get() instanceof ClientboundPacket)
            dir = TO_CLIENT;

        if(dir == null)
        {
            throw new IllegalArgumentException(
                    String.format("packetConstructor %s doesn't implement PacketIn nor PacketOut.", packetConstructor.get().getClass())
            );
        }

        Object[] array = (Object[]) Array.newInstance(class_protocolMapping, packetMap.size());
        try
        {
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : packetMap.entrySet())
            {
                array[i] = method_map.invoke(null, entry.getKey(), entry.getValue());
                i++;
            }

            if (legacyRegister)
                method_regPacket.invoke(dir, packetConstructor.get().getClass(), array);
            else
                method_regPacket.invoke(dir, packetConstructor.get().getClass(), packetConstructor, array);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Register a function to modify a packet.
     * @param packetType The packet type.
     * @param handler The function to be executed for the provided packet type.
     * @param <T> The packet type.
     */
    public static <T extends Packet> void registerHandler(Class<T> packetType, Handler<T> handler)
    {
        packetsHandlers.computeIfAbsent(packetType, aClass -> new ArrayList<>()).add(handler);
    }

    /**
     * Run the registered functions for this packet (if any function is registered).
     * @param wrapper The packet involved.
     * @param userConnection The user connection.
     */
    @SuppressWarnings({"rawtypes", "unchecked"}) //fug off
    public static void runHandlers(PacketWrapper wrapper, UserConnection userConnection)
    {
        List<Handler<? extends Packet>> handlers = packetsHandlers.get(wrapper.packet.getClass());
        if(handlers != null)
        {
            for (Handler handler : handlers)
            {
                if(handler.run(wrapper.packet, userConnection))
                    Packets.cancelPacket();
            }
        }
    }

    /**
     * Utility method to cancel execution of a packet.
     */
    public static void cancelPacket()
    {
        throw CancelSendSignal.INSTANCE;
    }

    /**
     * Gets {@link UserConnection} from a {@link DownstreamBridge}.
     * @param downstreamBridge The DownstreamBridge.
     * @return The UserConnection.
     * @throws IllegalAccessException Throws {@link IllegalAccessException} when the "con" field failed to be found.
     */
    public static UserConnection getUserConnection(DownstreamBridge downstreamBridge) throws IllegalAccessException
    {
        return (UserConnection) field_DownStreamBridge_con.get(downstreamBridge);
    }

    /**
     * Gets {@link UserConnection} from a {@link UpstreamBridge}.
     * @param upstreamBridge The UpstreamBridge.
     * @return The UserConnection.
     * @throws IllegalAccessException Throws {@link IllegalAccessException} when the "con" field failed to be found.
     */
    public static UserConnection getUserConnection(UpstreamBridge upstreamBridge) throws IllegalAccessException
    {
        return (UserConnection) field_UptreamBridge_con.get(upstreamBridge);
    }

    /**
     * Send a packet to the server as a player.
     * @param player The player to emulate.
     * @param packet The packet you want to send to the server (warning: it must be a TO_SERVER packet).
     * @param <T> The packet type.
     * @return Returns true if successfully sent, false if the player is offline and can't send the packet.
     */
    public static <T extends DefinedPacket> boolean sendPacketToServer(ProxiedPlayer player, T packet)
    {
        if(player.isConnected())
        {
            ChannelWrapper channelWrapper = Packets.getChannelWrapper((ServerConnection) player.getServer());
            channelWrapper.write(packet);
            return true;
        }
        return false;
    }

    /**
     * Send a packet to the server as a player.
     * @param plugin The Bungeecord plugin calling this method.
     * @param player The player to emulate.
     * @param packet The packet you want to send to the server (warning: it must be a TO_SERVER packet).
     * @param delayMs Delay in milliseconds before sending the packet.
     * @param <T> The packet type.
     * @return Returns true if successfully sent, false if the player is offline and can't send the packet.
     */
    public static <T extends DefinedPacket> boolean sendPacketToServer(Plugin plugin, ProxiedPlayer player, T packet, long delayMs)
    {
        if(player.isConnected())
        {
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                ChannelWrapper channelWrapper = Packets.getChannelWrapper((ServerConnection) player.getServer());
                channelWrapper.write(packet);
            }, delayMs, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    /**
     * Function called when a packet is received or sent.
     * @param <T> The packet type.
     */
    @FunctionalInterface
    public interface Handler<T extends DefinedPacket>
    {
        /**
         * Function to be executed.
         * @param packet The packet involved provided to this function.
         * @param conn The user connection involved in this packet.
         * @return Return true if the packet must be cancelled and not sent to the client/server, return false if the
         * packet must be sent to the client/server.
         */
        boolean run(T packet, UserConnection conn);
    }
}

