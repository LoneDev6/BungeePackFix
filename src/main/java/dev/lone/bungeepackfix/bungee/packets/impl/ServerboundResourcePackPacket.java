

package dev.lone.bungeepackfix.bungee.packets.impl;

import dev.lone.bungeepackfix.bungee.packets.Packet;
import dev.lone.bungeepackfix.bungee.packets.Packets;
import dev.lone.bungeepackfix.bungee.packets.ServerboundPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

// https://mappings.cephx.dev/1.20.2/net/minecraft/network/protocol/common/ServerboundResourcePackPacket.html
// https://mappings.cephx.dev/1.20.3/net/minecraft/network/protocol/common/ServerboundResourcePackPacket.html
public class ServerboundResourcePackPacket extends ServerboundPacket
{
    public UUID id = UUID.nameUUIDFromBytes("".getBytes()); // 1.20.4+
    public int status;

    //<editor-fold desc="Reflection initialization stuff">
    // https://github.com/dmulloy2/ProtocolLib/commits/master/src/main/java/com/comphenix/protocol/PacketType.java
    private static final LinkedHashMap<Integer, Integer> PACKET_MAP;
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
            PACKET_MAP.put(Packet.versionIdByName("MINECRAFT_1_20_3"), 0x28);
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
                ServerboundResourcePackPacket::new,
                PACKET_MAP
        );
    }

    public ServerboundResourcePackPacket(final UUID id, final int status)
    {
        this.id = id != null ? id : UUID.nameUUIDFromBytes("".getBytes());
        this.status = status;
    }

    public ServerboundResourcePackPacket(int status)
    {
        this(null, status);
    }

    public ServerboundResourcePackPacket() {}

    public ServerboundResourcePackPacket(final UUID id, Status status)
    {
        this.id = id != null ? id : UUID.nameUUIDFromBytes("".getBytes());
        this.status = status.ordinal();
    }

    public ServerboundResourcePackPacket(Status status)
    {
        this(null, status);
    }

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        PacketWrapper wrapper = Packet.newPacketWrapper(this, Unpooled.EMPTY_BUFFER, Protocol.STATUS);
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
        // WARNING: this doesn't take 1.20.3+ uuid field into account!
        // I'm not even sure this is actually called.
        this.status = readVarInt(buf);
    }

    @Override
    public void read(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        if (Packets.is1_20_3OrGreater(clientVersion))
            id = readUUID(buf);
        this.status = readVarInt(buf);
    }

    @Override
    public void write(final ByteBuf buf)
    {
        // WARNING: this doesn't take 1.20.3+ uuid field into account!
        // I'm not even sure this is actually called.
        writeVarInt(status, buf);
    }

    @Override
    public void write(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        if (Packets.is1_20_3OrGreater(clientVersion))
            writeUUID(id, buf);
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
        return Objects.equals(this.id, thisNut.id) && Objects.equals(this.status, thisNut.status);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.status);
    }

    @Override
    public String toString()
    {
        return "ServerboundResourcePackPacket{id=" + id + ", status=" + Status.values()[this.status] + "}";
    }

    public enum Status
    {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED,
        DOWNLOADED, // 1.20.3+
        INVALID_URL, // 1.20.3+
        FAILED_RELOAD, // 1.20.3+
        DISCARDED, // 1.20.3+
    }
}