package dev.lone.bungeepackfix.bungee.packets.impl;

import dev.lone.bungeepackfix.bungee.packets.ClientboundPacket;
import dev.lone.bungeepackfix.bungee.packets.Packet;
import dev.lone.bungeepackfix.bungee.packets.Packets;
import dev.lone.bungeepackfix.generic.PackUtility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ClientboundResourcePackPacket extends ClientboundPacket
{
    public String url;
    public String hash;
    public boolean forced;
    public String promptMessage;

    //<editor-fold desc="Reflection initialization stuff">
    private static final LinkedHashMap<Integer, Integer> PACKET_MAP;
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
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19_3, 0x3C);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_19_4, 0x40);
            PACKET_MAP.put(ProtocolConstants.MINECRAFT_1_20_2, 0x06);
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
                ClientboundResourcePackPacket::new,
                PACKET_MAP
        );
    }

    public ClientboundResourcePackPacket(final String url, final String hash)
    {
        this.url = url;
        this.hash = hash;
    }

    public ClientboundResourcePackPacket(final String url, final String hash, final boolean forced, final String promptMessage)
    {
        this(url, hash);
        this.forced = forced;
        this.promptMessage = promptMessage;
    }

    public ClientboundResourcePackPacket() {}

    @Override
    public void handle(final AbstractPacketHandler handler) throws Exception
    {
        PacketWrapper wrapper = Packet.newPacketWrapper(this, Unpooled.EMPTY_BUFFER, Protocol.STATUS);
        if (handler instanceof DownstreamBridge)
        {
            Packets.runHandlers(wrapper, Packets.getUserConnection((DownstreamBridge) handler));
        }
        else // Sending "resourcepack apply" packet to the server? wtf, should I warn about that?
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

    @Nullable
    public String getUrlHashtag()
    {
        return PackUtility.getUrlHashtag(url);
    }

    public boolean isSamePack(ClientboundResourcePackPacket newPack,
                              boolean ignoreHashtagInUrl,
                              boolean checkHash,
                              boolean checkForced,
                              boolean checkMsg)
    {
        if (this == newPack)
            return true;

        if (newPack == null || this.getClass() != newPack.getClass())
            return false;

        final String newUrl = PackUtility.removeHashtag(ignoreHashtagInUrl, newPack.url);
        final String prevUrl = PackUtility.removeHashtag(ignoreHashtagInUrl, this.url);

        return (Objects.equals(prevUrl, newUrl)) &&
                (!checkHash || Objects.equals(this.hash, newPack.hash)) &&
                (!checkForced || Objects.equals(this.forced, newPack.forced)) &&
                (!checkMsg || Objects.equals(this.promptMessage, newPack.promptMessage))
                ;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        final ClientboundResourcePackPacket thisNut = (ClientboundResourcePackPacket) o;
        return Objects.equals(this.url, thisNut.url) &&
                Objects.equals(this.hash, thisNut.hash) &&
                Objects.equals(this.forced, thisNut.forced) &&
                Objects.equals(this.promptMessage, thisNut.promptMessage)
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
        return "ClientboundResourcePackPacket{url='" + this.url + '\'' + ", hash=" + this.hash + ", forced=" + this.forced + ", promptMessage=" + this.promptMessage + '}';
    }
}
