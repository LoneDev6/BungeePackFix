package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.generic.AbstractSettings;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Settings extends AbstractSettings<BaseComponent[]>
{
    public Settings(Path dataDirectory, InputStream defaultConfigStream) throws IOException
    {
        super(dataDirectory, defaultConfigStream);
    }

    @Override
    protected void runPlatformDependentCode()
    {
        this.ignored_pack_msg = BungeeComponentSerializer.get().serialize(
                SERIALIZER.deserialize(config.getString("messages.ignored_pack.message"))
        );
    }
}