package dev.lone.bungeepackfix.velocity;

import dev.lone.bungeepackfix.generic.AbstractSettings;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Settings extends AbstractSettings<Component>
{
    public Settings(Path dataDirectory, InputStream defaultConfigStream) throws IOException
    {
        super(dataDirectory, defaultConfigStream);
    }

    @Override
    protected void runPlatformDependentCode()
    {
        ignored_pack_msg = SERIALIZER.deserialize(config.getString("messages.ignored_pack.message"));
    }
}
