package io.github.piscescup;

import io.github.piscescup.util.validation.NullCheck;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The constants of the {@code Minecraft Wiki} Mod.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class References {
    /**
     * The unique {@link Identifier} of the {@code Minecraft Wiki} Mod.
     */
    public static final String MOD_ID = "mc-wiki";

    /**
     * The visual name of the {@code Minecraft Wiki} Mod.
     */
    public static final String MOD_NAME = "Minecraft Wiki";

    /**
     * The {@link Logger} of the {@code Minecraft Wiki} Mod.
     */
    public static final Logger MOD_LOGGER = LogManager.getLogger(MOD_NAME);

    /**
     * Create a {@link Identifier} from the given path, using the {@code mc-wiki} as {@code namespace}.
     * @param path the path of the {@link Identifier}.
     * @return a {@link Identifier} from the given path
     * @throws NullPointerException if the {@code path} is {@code null}
     */
    public static @NotNull Identifier ofPath(@NotNull String path) {
        NullCheck.requireNonNull(path);

        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
