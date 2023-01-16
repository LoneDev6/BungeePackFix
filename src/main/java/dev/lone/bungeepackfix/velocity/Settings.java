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

package dev.lone.bungeepackfix.velocity;

import com.moandjiezana.toml.Toml;
import dev.lone.bungeepackfix.generic.AbstractSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Settings extends AbstractSettings<Component>
{
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public Settings(Path pluginPath)
    {
        Toml config = loadConfig(pluginPath);
        if (config == null)
            throw new RuntimeException("Cannot load config.yml file.");

        equal_pack_attributes_hash = config.getBoolean("equal_pack_attributes.hash", true);
        equal_pack_attributes_forced = config.getBoolean("equal_pack_attributes.forced", true);
        equal_pack_attributes_prompt_message = config.getBoolean("equal_pack_attributes.prompt_message", true);

        ignore_hash_in_url = config.getBoolean("ignore_hash_in_url", true);
        main_server_name = config.getString("main_server_name", "server_1");
        ignored_servers = config.getList("ignored_servers", new ArrayList<>());

        log_debug = config.getBoolean("log.debug", false);
        log_ignored_respack = config.getBoolean("log.ignored_respack", false);
        log_sent_respack = config.getBoolean("log.sent_respack", false);
        ignored_pack_msg_enabled = config.getBoolean("messages.ignored_pack.enabled", true);
        ignored_pack_msg = MINI_MESSAGE.deserialize(config.getString("messages.ignored_pack.message", "<gold>Skipped resourcepack installation (you already loaded it)."));
    }

    private Toml loadConfig(Path pluginPath)
    {
        if (!Files.exists(pluginPath))
        {
            try
            {
                Files.createDirectory(pluginPath);
            }
            catch (IOException e)
            {
                return null;
            }
        }

        Path configPath = pluginPath.resolve("config.toml");
        if (!Files.exists(configPath))
        {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.toml"))
            {
                //noinspection ConstantConditions
                Files.copy(in, configPath);
            }
            catch (IOException e)
            {
                return null;
            }
        }

        //TODO: automatically add missing properties added between updates

        try
        {
            return new Toml().read(Files.newInputStream(configPath));
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
