package io.github.piscescup.mcwiki.gui.format;

import java.util.List;

/**
 * A crafting recipe extracted from a MediaWiki crafting widget.
 */
public record WikiRecipe(
    List<List<Slot>> grid,
    String output,
    String outputCount
) {
    public WikiRecipe {
        grid = grid.stream()
            .map(List::copyOf)
            .toList();
    }

    public record Slot(List<String> alternatives) {
        public Slot {
            alternatives = List.copyOf(alternatives);
        }

        public boolean isEmpty() {
            return this.alternatives.isEmpty();
        }
    }
}
