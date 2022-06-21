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
package dev.lone.bungeepackfix.libs.packetlistener.packets;

import dev.lone.bungeepackfix.libs.packetlistener.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.LinkedHashMap;
import java.util.Objects;

public class RespackStatusPacketIn extends PacketIn
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
                RespackStatusPacketIn::new,
                PACKET_MAP
        );
    }

    public RespackStatusPacketIn(int status)
    {
        this.status = status;
    }

    public RespackStatusPacketIn() {}

    public RespackStatusPacketIn(Status status)
    {
        this.status = status.ordinal();
    }

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        PacketWrapper wrapper = new PacketWrapper(this, Unpooled.EMPTY_BUFFER);
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

        final RespackStatusPacketIn thizNuts = (RespackStatusPacketIn) o;
        return Objects.equals(this.status, thizNuts.status);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.status);
    }

    @Override
    public String toString()
    {
        return "RespackStatusPacketIn{status=" + Status.values()[this.status] + "}";
    }

    public enum Status
    {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}