package dev.lone.bungeepackfix.velocity.configuration;

import com.moandjiezana.toml.Toml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Configuration {

    private final Messages messages;
    private final EqualPackAttributes equalPackAttributes;
    private final Log log;

    public Configuration(@NotNull Toml toml) {
        this.messages = new Messages(toml.getTable("messages"));
        this.equalPackAttributes = new EqualPackAttributes(toml.getTable("equal_pack_attributes"));
        this.log = new Log(toml.getTable("log"));
    }

    public Messages getMessages() {
        return messages;
    }

    public EqualPackAttributes getEqualPackAttributes() {
        return equalPackAttributes;
    }

    public Log getLog() {
        return log;
    }

    public static class Messages {
        private final boolean enabled;
        private final String message;

        public Messages(@Nullable Toml toml) {
            if (toml != null) {
                this.enabled = toml.getBoolean("enable", true);
                this.message = toml.getString("message", "<gold>Skipped resourcepack installation (you already loaded it).");
            } else {
                this.enabled = true;
                this.message = "<gold>Skipped resourcepack installation (you already loaded it).";
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class EqualPackAttributes {
        private final boolean hash;
        private final boolean forced;
        private final boolean promptMessage;

        public EqualPackAttributes(@Nullable Toml toml) {
            if (toml != null) {
                this.hash = toml.getBoolean("hash", true);
                this.forced = toml.getBoolean("forced", true);
                this.promptMessage = toml.getBoolean("prompt_message", true);
            } else {
                this.hash = true;
                this.forced = true;
                this.promptMessage = true;
            }
        }

        public boolean isHash() {
            return hash;
        }

        public boolean isForced() {
            return forced;
        }

        public boolean isPromptMessage() {
            return promptMessage;
        }
    }

    public static class Log {
        private final boolean ignoreRespack;
        private final boolean sentRespack;
        private final boolean debug;

        public Log(@Nullable Toml toml) {
            if (toml != null) {
                this.ignoreRespack = toml.getBoolean("ignored_respack", true);
                this.sentRespack = toml.getBoolean("sent_respack", true);
                this.debug = toml.getBoolean("debug", true);
            } else {
                this.ignoreRespack = true;
                this.sentRespack = true;
                this.debug = true;
            }
        }

        public boolean isIgnoreRespack() {
            return ignoreRespack;
        }

        public boolean isSentRespack() {
            return sentRespack;
        }

        public boolean isDebug() {
            return debug;
        }
    }
}
