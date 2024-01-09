package dev.lone.bungeepackfix.bungee.packets.impl;

import dev.lone.bungeepackfix.bungee.Main;
import dev.lone.bungeepackfix.bungee.Settings;
import dev.lone.bungeepackfix.bungee.packets.ClientboundPacket;
import dev.lone.bungeepackfix.bungee.packets.Packet;
import dev.lone.bungeepackfix.bungee.packets.Packets;
import dev.lone.bungeepackfix.generic.PackUtility;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.*;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

// https://mappings.cephx.dev/1.20.2/net/minecraft/network/protocol/common/ClientboundResourcePackPacket.html
// https://mappings.cephx.dev/1.20.3/net/minecraft/network/protocol/common/ClientboundResourcePackPushPacket.html
public class ClientboundResourcePackPacket extends ClientboundPacket
{
    public UUID id = UUID.nameUUIDFromBytes("".getBytes()); // 1.20.4+
    public String url;
    public String hash;
    public boolean required;
    public Object promptMessage;

    //<editor-fold desc="Reflection initialization stuff">
    // https://github.com/dmulloy2/ProtocolLib/commits/master/src/main/java/com/comphenix/protocol/PacketType.java
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
            PACKET_MAP.put(Packet.versionIdByName("MINECRAFT_1_20_3"), 0x44);
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

    // 1.20.4+
    public ClientboundResourcePackPacket(final UUID id, final String url, final String hash)
    {
        this.id = id != null ? id : UUID.nameUUIDFromBytes("".getBytes());
        this.url = url;
        this.hash = hash;
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
        // WARNING: this doesn't take 1.20.3+ uuid field into account!
        // I'm not even sure this is actually called.
        this.url = readString(buf);
        try
        {
            this.hash = readString(buf, 40);
        }
        catch (IndexOutOfBoundsException ignored)
        {
            //null hash?
        }
    }

    @Override
    public void read(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        if (Packets.is1_20_3OrGreater(clientVersion))
            id = readUUID(buf);

        read(buf);
        if (clientVersion >= ProtocolConstants.MINECRAFT_1_17)
        {
            required = buf.readBoolean();
            final boolean hasPromptMessage = buf.readBoolean();
            if (hasPromptMessage)
            {
                if(hasRwComponent)
                    promptMessage = readBaseComponent(buf, clientVersion);
                else
                    promptMessage = readString(buf); // Idk if this shit works.
            }
        }
    }

    @Override
    public void write(final ByteBuf buf)
    {
        // WARNING: this doesn't take 1.20.3+ uuid field into account!
        // I'm not even sure this is actually called.
        writeString(url, buf);
        writeString(hash == null ? "" : hash, buf);
    }

    @Override
    public void write(final ByteBuf buf, final ProtocolConstants.Direction direction, final int clientVersion)
    {
        if (Packets.is1_20_3OrGreater(clientVersion))
            writeUUID(id, buf);
        this.write(buf);
        if (clientVersion >= ProtocolConstants.MINECRAFT_1_17)
        {
            buf.writeBoolean(this.required);
            if (this.promptMessage != null)
            {
                buf.writeBoolean(true);
                if(hasRwComponent)
                    writeBaseComponent((BaseComponent) this.promptMessage, buf, clientVersion);
                else
                    writeString((String) this.promptMessage, buf);
            }
            else
            {
                buf.writeBoolean(false);
            }
        }
    }

    public String getUrlHashtag()
    {
        return PackUtility.getUrlHashtag(url);
    }

    public boolean isSamePack(ClientboundResourcePackPacket newPack)
    {
        if (this == newPack)
            return true;

        if (newPack == null || this.getClass() != newPack.getClass())
            return false;

        Settings settings = Main.inst().settings;
        final String newUrl = PackUtility.removeHashtag(settings.ignore_hash_in_url, newPack.url);
        final String prevUrl = PackUtility.removeHashtag(settings.ignore_hash_in_url, this.url);

        return (!settings.equal_pack_attribute_modern_uuid || Objects.equals(this.id, newPack.id)) &&
                Objects.equals(prevUrl, newUrl) &&
                (!settings.equal_pack_attributes_hash || Objects.equals(this.hash, newPack.hash)) &&
                (!settings.equal_pack_attributes_forced || Objects.equals(this.required, newPack.required)) &&
                (!settings.equal_pack_attributes_prompt_message || Objects.equals(this.promptMessage, newPack.promptMessage))
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
        return Objects.equals(this.id, thisNut.id) &&
                Objects.equals(this.url, thisNut.url) &&
                Objects.equals(this.hash, thisNut.hash) &&
                Objects.equals(this.required, thisNut.required) &&
                Objects.equals(this.promptMessage, thisNut.promptMessage)
                ;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.url, this.hash, this.required, this.promptMessage);
    }

    @Override
    public String toString()
    {
        return "ClientboundResourcePackPacket{id=" + id + ", url='" + this.url + '\'' + ", hash=" + this.hash + ", forced=" + this.required + ", promptMessage=" + this.promptMessage + '}';
    }
}
