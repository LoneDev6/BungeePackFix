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
package dev.lone.bungeepackfix.libs;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;

public class YamlConfig
{
    private final File configFile;
    private final String fileName;
    private final Plugin plugin;
    private static Configuration config;

    public YamlConfig(String fileName, Plugin plugin)
    {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public Configuration getConfig()
    {
        return config;
    }

    public void saveDefaultConfig()
    {
        if (!this.configFile.getParentFile().exists())
        {
            this.configFile.getParentFile().mkdirs();
        }
        try
        {
            if (!this.configFile.exists())
            {
                Files.copy(this.plugin.getResourceAsStream(this.fileName), this.configFile.toPath(), new CopyOption[0]);
            }
            loadConfig();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void loadConfig() throws IOException
    {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
    }

    public void saveConfig() throws IOException
    {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, this.configFile);
    }
}
