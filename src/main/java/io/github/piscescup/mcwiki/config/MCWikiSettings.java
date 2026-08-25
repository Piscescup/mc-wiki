package io.github.piscescup.mcwiki.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.piscescup.mcwiki.wiki.model.WikiSearchModeConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent client settings for the Minecraft Wiki browser.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class MCWikiSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SETTINGS_FILE =
        FabricLoader.getInstance().getConfigDir().resolve("mc-wiki.json");

    private String language = "en_us";
    private WikiSearchModeConfig searchMode = WikiSearchModeConfig.AUTO;
    private int searchResultLimit = 8;
    private int requestTimeoutSeconds = 10;

    @NotNull
    public static MCWikiSettings load() {
        if (!Files.isRegularFile(SETTINGS_FILE)) {
            return new MCWikiSettings();
        }

        try (Reader reader = Files.newBufferedReader(SETTINGS_FILE)) {
            MCWikiSettings settings = GSON.fromJson(reader, MCWikiSettings.class);
            return settings == null ? new MCWikiSettings() : settings.normalized();
        } catch (IOException | RuntimeException exception) {
            return new MCWikiSettings();
        }
    }

    public void save() throws IOException {
        Files.createDirectories(SETTINGS_FILE.getParent());
        try (Writer writer = Files.newBufferedWriter(SETTINGS_FILE)) {
            GSON.toJson(this, writer);
        }
    }

    @NotNull
    public String language() {
        return this.language;
    }

    public void setLanguage(@NotNull String language) {
        if (!language.equals("en_us") && !language.equals("zh_cn")) {
            throw new IllegalArgumentException("Supported languages: en_us, zh_cn");
        }
        this.language = language;
    }

    @NotNull
    public WikiSearchModeConfig searchMode() {
        return this.searchMode;
    }

    public int searchResultLimit() {
        return this.searchResultLimit;
    }

    public int requestTimeoutSeconds() {
        return this.requestTimeoutSeconds;
    }

    @NotNull
    private MCWikiSettings normalized() {
        if (!this.language.equals("en_us") && !this.language.equals("zh_cn")) {
            this.language = "en_us";
        }
        if (this.searchMode == null) {
            this.searchMode = WikiSearchModeConfig.AUTO;
        }
        this.searchResultLimit = Math.clamp(this.searchResultLimit, 1, 20);
        this.requestTimeoutSeconds = Math.clamp(this.requestTimeoutSeconds, 1, 60);
        return this;
    }
}
