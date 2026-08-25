package io.github.piscescup.mcwiki.gui.format;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Structured article content used by the custom Wiki article widget.
 */
public record WikiArticleDocument(
    @NotNull List<Block> blocks,
    @NotNull Component narration
) {
    public WikiArticleDocument {
        blocks = List.copyOf(blocks);
    }

    public record Block(
        @NotNull Component text,
        int indent,
        int topMargin,
        int bottomMargin
    ) {
    }
}
