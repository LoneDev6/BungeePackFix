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
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.LinkedHashMap;
import java.util.Objects;

public class RespackSendPacketOut extends PacketOut
{
    public String url;
    public String hash;
    public boolean forced;
    public String promptMessage;

    //<editor-fold desc="Reflection initialization stuff">
    public static final LinkedHashMap<Integer, Integer> PACKET_MAP;
    static
    {
        PACKET_MAP = new LinkedHashMap<>();

        try
        {
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_8, 0x48);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_9, 0x32);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_12, 0x34);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_13_1, 0x37);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_14, 0x39);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_15, 0x3A);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_16, 0x39);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_16_2, 0x38);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_17, 0x3C);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19, 0x3A);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19_1, 0x3D);
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
                RespackSendPacketOut::new,
                PACKET_MAP
        );
    }

    public RespackSendPacketOut(final String url, final String hash)
    {
        this.url = url;
        this.hash = hash;
    }

    public RespackSendPacketOut(final String url, final String hash, final boolean forced, final String promptMessage)
    {
        this(url, hash);
        this.forced = forced;
        this.promptMessage = promptMessage;
    }

    public RespackSendPacketOut() {}

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        PacketWrapper wrapper = new PacketWrapper(this, Unpooled.EMPTY_BUFFER);
        if (handler instanceof DownstreamBridge)
        {
            Packets.runHandlers(wrapper, Packets.getUserConnection((DownstreamBridge) handler));
        }
        else // Sending "resourcepack apply" packet to the server? wtf
        {
            if (handler instanceof PacketHandler)
                ((PacketHandler) handler).handle(wrapper);
        }
    }

    @Override
    public void read(final ByteBuf buf)
    {
        this.url = readString(buf);
        try
        {
            this.hash = readString(buf);
        }
        catch (IndexOutOfBoundsException ignored)
        {
            //null hash?
        }
    }

    @Override
    public void read(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        read(buf);
        if (clientVersion >= ProtocolConstants.MINECRAFT_1_17)
        {
            forced = buf.readBoolean();
            final boolean hasPromptMessage = buf.readBoolean();
            if (hasPromptMessage)
                promptMessage = readString(buf);
        }
    }

    @Override
    public void write(final ByteBuf buf)
    {
        writeString(url, buf);
        writeString(hash == null ? "" : hash, buf);
    }

    @Override
    public void write(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        this.write(buf);
        if (clientVersion >= ProtocolConstants.MINECRAFT_1_17)
        {
            buf.writeBoolean(this.forced);
            if (this.promptMessage != null)
            {
                buf.writeBoolean(true);
                writeString(this.promptMessage, buf);
            }
            else
            {
                buf.writeBoolean(false);
            }
        }
    }

    public boolean equals(RespackSendPacketOut o,
                          boolean checkHash,
                          boolean checkForced,
                          boolean checkMsg)
    {
        if (this == o)
        return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        return (Objects.equals(this.url, o.url)) &&
                (!checkHash || Objects.equals(this.hash, o.hash)) &&
                (!checkForced || Objects.equals(this.forced, o.forced)) &&
                (!checkMsg || Objects.equals(this.promptMessage, o.promptMessage))
                ;
    }
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        final RespackSendPacketOut thizNuts = (RespackSendPacketOut) o;
        return Objects.equals(this.url, thizNuts.url) &&
                Objects.equals(this.hash, thizNuts.hash) &&
                Objects.equals(this.forced, thizNuts.forced) &&
                Objects.equals(this.promptMessage, thizNuts.promptMessage)
                ;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.url, this.hash, this.forced, this.promptMessage);
    }

    @Override
    public String toString()
    {
        return "RespackSendPacketOut{url='" + this.url + '\'' + ", hash=" + this.hash + ", forced=" + this.forced + ", promptMessage=" + this.promptMessage + '}';
    }
}
