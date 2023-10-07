package dev.lone.bungeepackfix.bungee.packets;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class Packet extends DefinedPacket
{
    static Constructor<PacketWrapper> legacyConstructor;
    static
    {
        try
        {
            //noinspection JavaReflectionMemberAccess
            legacyConstructor = PacketWrapper.class.getConstructor(DefinedPacket.class, ByteBuf.class);
        }
        catch (NoSuchMethodException ignored) { }
    }

    public static PacketWrapper newPacketWrapper(DefinedPacket packet, ByteBuf buf, Protocol protocol)
    {
        PacketWrapper wrapper;
        if(legacyConstructor != null)
        {
            try
            {
                wrapper = legacyConstructor.newInstance(packet, buf);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
            wrapper = new PacketWrapper(packet, buf, protocol);
        return wrapper;
    }
}
