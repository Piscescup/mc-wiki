package io.github.piscescup.mcwiki.format;

import java.util.List;

/**
 * A table extracted from parsed MediaWiki HTML.
 */
public record WikiTable(
    String caption,
    List<Row> rows,
    List<WikiRecipe> recipes
) {
    public WikiTable {
        rows = List.copyOf(rows);
        recipes = List.copyOf(recipes);
    }

    public WikiTable(String caption, List<Row> rows) {
        this(caption, rows, List.of());
    }

    public record Row(List<String> cells, boolean header) {
        public Row {
            cells = List.copyOf(cells);
        }
    }
}
