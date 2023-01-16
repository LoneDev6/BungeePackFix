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
package dev.lone.bungeepackfix.bungee;

import dev.lone.bungeepackfix.bungee.libs.YamlConfig;
import dev.lone.bungeepackfix.generic.AbstractSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

public class Settings extends AbstractSettings<TextComponent>
{
    public Settings(Plugin plugin)
    {
        YamlConfig config = new YamlConfig("config.yml", plugin.getDataFolder().toPath());
        config.saveDefaultConfig();

        equal_pack_attributes_hash = config.getConfig().getBoolean("equal_pack_attributes.hash", true);
        equal_pack_attributes_forced = config.getConfig().getBoolean("equal_pack_attributes.forced", true);
        equal_pack_attributes_prompt_message = config.getConfig().getBoolean("equal_pack_attributes.prompt_message", true);

        ignore_hash_in_url = config.getConfig().getBoolean("ignore_hash_in_url", true);
        main_server_name = config.getConfig().getString("main_server_name", "server_1");
        ignored_servers = config.getConfig().getStringList("ignored_servers");

        log_debug = config.getConfig().getBoolean("log.debug", false);
        log_ignored_respack = config.getConfig().getBoolean("log.ignored_respack", false);
        log_sent_respack = config.getConfig().getBoolean("log.sent_respack", false);
        ignored_pack_msg_enabled = config.getConfig().getBoolean("messages.ignored_pack.enabled", true);
        ignored_pack_msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("messages.ignored_pack.message", "Ignored pack")));
    }
}