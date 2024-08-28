package dev.lone.bungeepackfix.bungee.packets.impl;

import dev.lone.bungeepackfix.bungee.packets.ClientboundPacket;
import dev.lone.bungeepackfix.bungee.packets.Packet;
import dev.lone.bungeepackfix.bungee.packets.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// https://mappings.cephx.dev/1.20.3/net/minecraft/network/protocol/common/ClientboundResourcePackPopPacket.html
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ClientboundResourcePackPopPacket extends ClientboundPacket
{
    public Optional<UUID> id;

    //<editor-fold desc="Reflection initialization stuff">
    // https://github.com/dmulloy2/ProtocolLib/commits/master/src/main/java/com/comphenix/protocol/PacketType.java
    private static final LinkedHashMap<Integer, Integer> PACKET_MAP;
    static
    {
        PACKET_MAP = new LinkedHashMap<>();
        try
        {
            PACKET_MAP.put(Packet.versionIdByName("MINECRAFT_1_20_3"), 0x43);
        }
        catch (Exception ignored)
        {
            // Failed to find constant, probably Bungeecord is outdated.
        }
    }
    //</editor-fold>

    /**
     * Utility method to register this packet
     */
    public static void register()
    {
        Packets.registerPacket(
                ClientboundResourcePackPopPacket::new,
                PACKET_MAP
        );
    }

    public ClientboundResourcePackPopPacket() {}

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        PacketWrapper wrapper = Packet.newPacketWrapper(this, Unpooled.EMPTY_BUFFER, Protocol.STATUS);
        if (handler instanceof DownstreamBridge)
        {
            Packets.runHandlers(wrapper, Packets.getUserConnection((DownstreamBridge) handler));
        }
        else // Sending packet to the server? wtf, should I warn about that?
        {
            if (handler instanceof PacketHandler)
                ((PacketHandler) handler).handle(wrapper);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        return Objects.equals(id, ((ClientboundResourcePackPopPacket) o).id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public String toString()
    {
        return "ClientboundResourcePackPopPacket{id=" + id + "}";
    }

    @Override
    public void read(final ByteBuf buf)
    {
        if (buf.readBoolean()) {
            try {
                id = Optional.of(readUUID(buf));
            } catch (Throwable ignored) {
                id = Optional.empty();
            }
        } else {
            id = Optional.empty();
        }
    }

    @Override
    public void read(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        read(buf);
    }

    @Override
    public void write(final ByteBuf buf)
    {
        if(id.isPresent())
        {
            buf.writeBoolean(true);
            writeUUID(id.get(), buf);
        }
        else
        {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void write(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        write(buf);
    }
}
