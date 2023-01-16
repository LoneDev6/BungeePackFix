

package dev.lone.bungeepackfix.bungee.packets.impl;

import dev.lone.bungeepackfix.bungee.packets.Packets;
import dev.lone.bungeepackfix.bungee.packets.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ServerboundResourcePackPacket extends ServerboundPacket
{
    public int status;

    //<editor-fold desc="Reflection initialization stuff">
    public static final LinkedHashMap<Integer, Integer> PACKET_MAP;
    static
    {
        PACKET_MAP = new LinkedHashMap<>();

        try
        {
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_8, 0x19);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_9, 0x16);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_12, 0x18);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_13, 0x1D);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_14, 0x1F);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_16, 0x21);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19, 0x23);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19_1, 0x24);
        }catch (Exception ignored)
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
                ServerboundResourcePackPacket::new,
                PACKET_MAP
        );
    }

    public ServerboundResourcePackPacket(int status)
    {
        this.status = status;
    }

    public ServerboundResourcePackPacket() {}

    public ServerboundResourcePackPacket(Status status)
    {
        this.status = status.ordinal();
    }

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        final PacketWrapper wrapper = new PacketWrapper(this, Unpooled.EMPTY_BUFFER);
        if (handler instanceof UpstreamBridge)
        {
            Packets.runHandlers(wrapper, Packets.getUserConnection((UpstreamBridge) handler));
        }
        else //sending this packet to the client? wtf
        {
            if (handler instanceof PacketHandler)
                ((PacketHandler) handler).handle(wrapper);
        }
    }

    @Override
    public void read(final ByteBuf buf)
    {
        this.status = readVarInt(buf);
    }

    @Override
    public void read(final ByteBuf buf, final ProtocolConstants.Direction direction, final int ProtocolConstants)
    {
        this.status = readVarInt(buf);
    }

    @Override
    public void write(final ByteBuf buf)
    {
        writeVarInt(status, buf);
    }

    @Override
    public void write(final ByteBuf buf, final ProtocolConstants.Direction direction, final int ProtocolConstants)
    {
        writeVarInt(status, buf);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        final ServerboundResourcePackPacket thisNut = (ServerboundResourcePackPacket) o;
        return Objects.equals(this.status, thisNut.status);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.status);
    }

    @Override
    public String toString()
    {
        return "ServerboundResourcePackPacket{status=" + Status.values()[this.status] + "}";
    }

    public enum Status
    {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}