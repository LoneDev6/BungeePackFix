package dev.lone.bungeepackfix.velocity;

import dev.lone.bungeepackfix.generic.AbstractSettings;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Path;

public class Settings extends AbstractSettings<Component>
{
    public Settings(Path dataDirectory) throws IOException
    {
        super(dataDirectory);
    }

    @Override
    protected void runPlatformDependentCode()
    {
        ignored_pack_msg = SERIALIZER.deserialize(config.getString("messages.ignored_pack.message"));
    }
}
