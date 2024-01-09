package dev.lone.bungeepackfix.bungee.packets;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class Packet extends DefinedPacket
{
    public static boolean hasRwComponent;

    static Constructor<PacketWrapper> legacyConstructor;
    static
    {
        try
        {
            //noinspection JavaReflectionMemberAccess
            legacyConstructor = PacketWrapper.class.getConstructor(DefinedPacket.class, ByteBuf.class);
        }
        catch (NoSuchMethodException ignored) { }

        try
        {
            DefinedPacket.class.getMethod("readBaseComponent", ByteBuf.class, int.class);
            hasRwComponent = true;
        }
        catch (NoSuchMethodException e)
        {
            hasRwComponent = false;
        }
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

    public static int versionIdByName(String name) throws NoSuchFieldException, IllegalAccessException
    {
        return (int) ProtocolConstants.class.getDeclaredField(name).get(null);
    }
}
