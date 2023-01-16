package dev.lone.bungeepackfix.generic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractSettings<T>
{
    protected ComponentSerializer<Component, ?, String> SERIALIZER;

    public boolean equal_pack_attributes_hash;
    public boolean equal_pack_attributes_forced;
    public boolean equal_pack_attributes_prompt_message;

    public boolean ignore_hash_in_url;
    public String main_server_name;
    public List<String> ignored_servers;

    public boolean log_debug;
    public boolean log_ignored_respack;
    public boolean log_sent_respack;
    public boolean ignored_pack_msg_enabled;

    public boolean minimessage_support;

    public T ignored_pack_msg;

    protected YamlConfig config;

    public AbstractSettings(Path dataDirectory) throws IOException
    {
        config = new YamlConfig(new File(dataDirectory.toFile(), "config.yml").toPath());
        config.load();

        equal_pack_attributes_hash = config.getBoolean("equal_pack_attributes.hash", true);
        equal_pack_attributes_forced = config.getBoolean("equal_pack_attributes.forced", true);
        equal_pack_attributes_prompt_message = config.getBoolean("equal_pack_attributes.prompt_message", true);

        ignore_hash_in_url = config.getBoolean("ignore_hash_in_url", true);
        main_server_name = config.getString("main_server_name", "server_1");
        ignored_servers = config.getStringList("ignored_servers");

        log_debug = config.getBoolean("log.debug", false);
        log_ignored_respack = config.getBoolean("log.ignored_respack", false);
        log_sent_respack = config.getBoolean("log.sent_respack", false);
        ignored_pack_msg_enabled = config.getBoolean("messages.ignored_pack.enabled", true);

        minimessage_support = config.getBoolean("messages.minimessage_support", false);

        if(minimessage_support)
            SERIALIZER = MiniMessage.miniMessage();
        else
            SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

        runPlatformDependentCode();
    }

    protected abstract void runPlatformDependentCode();
}