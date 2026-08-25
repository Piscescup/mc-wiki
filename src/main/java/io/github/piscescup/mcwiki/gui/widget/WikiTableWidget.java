package io.github.piscescup.mcwiki.gui.widget;

import io.github.piscescup.mcwiki.gui.WikiTexts;
import io.github.piscescup.mcwiki.gui.format.WikiRecipe;
import io.github.piscescup.mcwiki.gui.format.WikiTable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scrollable table view with custom layouts for infoboxes, recipes, and data tables.
 */
public final class WikiTableWidget extends AbstractTextAreaWidget {
    private static final int BLOCK_GAP = 8;
    private static final int CELL_GAP = 6;
    private static final int CELL_PADDING = 6;
    private static final int RECIPE_SLOT = 22;

    private final Font font;
    private final WikiTable table;
    private final Component title;
    private final String language;

    public WikiTableWidget(
        int x,
        int y,
        int width,
        int height,
        Font font,
        WikiTable table,
        int tableIndex,
        int tableCount,
        String language
    ) {
        super(
            x,
            y,
            width,
            height,
            title(table, tableIndex, tableCount, language),
            AbstractScrollArea.defaultSettings(9)
        );
        this.font = font;
        this.table = table;
        this.language = language;
        this.title = title(table, tableIndex, tableCount, language);
    }

    @Override
    protected int getInnerHeight() {
        return layoutTable(null);
    }

    @Override
    protected void extractContents(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float deltaTicks
    ) {
        layoutTable(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.title);
    }

    private int layoutTable(GuiGraphicsExtractor graphics) {
        int contentWidth = Math.max(140, getWidth() - totalInnerPadding());
        int y = getInnerTop();

        if (graphics != null) {
            graphics.textWithWordWrap(
                this.font,
                this.title,
                getInnerLeft(),
                y,
                contentWidth,
                0xFFF7D774
            );
        }
        y += Math.max(this.font.lineHeight, this.font.wordWrapHeight(this.title, contentWidth));
        y += 10;

        if (!this.table.recipes().isEmpty()) {
            y = layoutRecipes(graphics, y, contentWidth);
        } else if (isKeyValueTable(this.table)) {
            y = layoutKeyValueRows(graphics, y, contentWidth);
        } else if (maxColumns(this.table) <= 4) {
            y = layoutGridRows(graphics, y, contentWidth);
        } else {
            y = layoutRecordCards(graphics, y, contentWidth);
        }

        return y - getInnerTop();
    }

    private int layoutRecipes(
        GuiGraphicsExtractor graphics,
        int y,
        int contentWidth
    ) {
        for (int recipeIndex = 0; recipeIndex < this.table.recipes().size(); recipeIndex++) {
            WikiRecipe recipe = this.table.recipes().get(recipeIndex);
            Component heading = Component.literal(
                WikiTexts.text(this.language, "recipe") + " " + (recipeIndex + 1)
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);

            if (graphics != null) {
                graphics.text(this.font, heading, getInnerLeft(), y, 0xFFA9E3FF);
            }
            y += this.font.lineHeight + 6;

            Map<List<String>, Character> symbols = ingredientSymbols(recipe);
            int gridWidth = recipe.grid().stream()
                .mapToInt(row -> row.size())
                .max()
                .orElse(3) * (RECIPE_SLOT + 4);
            int drawLeft = getInnerLeft() + Math.max(0, (contentWidth - gridWidth) / 2);

            for (List<WikiRecipe.Slot> row : recipe.grid()) {
                int x = drawLeft;
                for (WikiRecipe.Slot slot : row) {
                    if (graphics != null) {
                        graphics.fill(x, y, x + RECIPE_SLOT, y + RECIPE_SLOT, 0xA0192433);
                        graphics.outline(x, y, RECIPE_SLOT, RECIPE_SLOT, 0xFF46617C);
                        if (!slot.isEmpty()) {
                            graphics.centeredText(
                                this.font,
                                String.valueOf(symbols.get(slot.alternatives())),
                                x + RECIPE_SLOT / 2,
                                y + 7,
                                0xFFE8F4FF
                            );
                        }
                    }
                    x += RECIPE_SLOT + 4;
                }
                y += RECIPE_SLOT + 4;
            }

            y += 4;
            for (Map.Entry<List<String>, Character> entry : symbols.entrySet()) {
                String legend = entry.getValue() + " = "
                    + alternativesText(entry.getKey(), this.language);
                if (graphics != null) {
                    graphics.textWithWordWrap(
                        this.font,
                        Component.literal(legend),
                        getInnerLeft(),
                        y,
                        contentWidth,
                        0xFFF4F8FC
                    );
                }
                y += wrappedHeight(legend, contentWidth) + 2;
            }

            String outputText = WikiTexts.text(this.language, "output")
                + ": "
                + recipe.output()
                + " x"
                + recipe.outputCount();
            if (graphics != null) {
                graphics.textWithWordWrap(
                    this.font,
                    Component.literal(outputText).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN),
                    getInnerLeft(),
                    y,
                    contentWidth,
                    0xFF9CF095
                );
            }
            y += wrappedHeight(outputText, contentWidth);
            y += BLOCK_GAP + 4;
        }

        return y;
    }

    private int layoutKeyValueRows(
        GuiGraphicsExtractor graphics,
        int y,
        int contentWidth
    ) {
        int labelWidth = Math.clamp(contentWidth / 3, 110, 180);
        int valueWidth = contentWidth - labelWidth - CELL_GAP;

        for (WikiTable.Row row : this.table.rows()) {
            if (row.cells().isEmpty()) {
                continue;
            }

            if (row.cells().size() == 1) {
                y = layoutSectionHeading(graphics, y, contentWidth, row.cells().getFirst());
                continue;
            }

            String label = row.cells().getFirst();
            String value = joinCells(row.cells().subList(1, row.cells().size()));
            int rowHeight = Math.max(
                wrappedHeight(label, labelWidth - CELL_PADDING * 2),
                wrappedHeight(value, valueWidth - CELL_PADDING * 2)
            ) + CELL_PADDING * 2;

            if (graphics != null) {
                int left = getInnerLeft();
                int right = left + labelWidth + CELL_GAP;

                graphics.fill(left, y, left + labelWidth, y + rowHeight, 0xC61B3650);
                graphics.fill(right, y, right + valueWidth, y + rowHeight, 0xC6141F2D);
                graphics.outline(left, y, labelWidth, rowHeight, 0xFF3E648D);
                graphics.outline(right, y, valueWidth, rowHeight, 0xFF2C4A69);

                graphics.textWithWordWrap(
                    this.font,
                    Component.literal(label).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW),
                    left + CELL_PADDING,
                    y + CELL_PADDING,
                    labelWidth - CELL_PADDING * 2,
                    0xFFF7D774
                );
                graphics.textWithWordWrap(
                    this.font,
                    Component.literal(value),
                    right + CELL_PADDING,
                    y + CELL_PADDING,
                    valueWidth - CELL_PADDING * 2,
                    0xFFF4F8FC
                );
            }

            y += rowHeight + CELL_GAP;
        }

        return y;
    }

    private int layoutGridRows(
        GuiGraphicsExtractor graphics,
        int y,
        int contentWidth
    ) {
        int columnCount = Math.max(1, maxColumns(this.table));
        int[] widths = columnWidths(contentWidth, columnCount);
        int left = getInnerLeft();
        int rowIndex = 0;

        for (WikiTable.Row row : this.table.rows()) {
            if (row.cells().isEmpty()) {
                continue;
            }

            if (row.header() && row.cells().size() == 1) {
                y = layoutSectionHeading(graphics, y, contentWidth, row.cells().getFirst());
                continue;
            }

            int rowHeight = CELL_PADDING * 2;
            for (int column = 0; column < columnCount; column++) {
                String cell = column < row.cells().size() ? row.cells().get(column) : "";
                rowHeight = Math.max(
                    rowHeight,
                    wrappedHeight(cell, widths[column] - CELL_PADDING * 2) + CELL_PADDING * 2
                );
            }

            int x = left;
            for (int column = 0; column < columnCount; column++) {
                String cell = column < row.cells().size() ? row.cells().get(column) : "";
                int cellWidth = widths[column];

                if (graphics != null) {
                    int fill = row.header()
                        ? 0xD01E4F70
                        : rowIndex % 2 == 0
                            ? 0xA0151F2B
                            : 0xA01A2634;
                    int outline = row.header() ? 0xFF5D90BD : 0xFF314B65;

                    graphics.fill(x, y, x + cellWidth, y + rowHeight, fill);
                    graphics.outline(x, y, cellWidth, rowHeight, outline);
                    graphics.textWithWordWrap(
                        this.font,
                        Component.literal(cell).withStyle(
                            row.header()
                                ? ChatFormatting.BOLD
                                : ChatFormatting.WHITE
                        ),
                        x + CELL_PADDING,
                        y + CELL_PADDING,
                        cellWidth - CELL_PADDING * 2,
                        0xFFF4F8FC
                    );
                }

                x += cellWidth + CELL_GAP;
            }

            y += rowHeight + CELL_GAP;
            rowIndex++;
        }

        return y;
    }

    private int layoutRecordCards(
        GuiGraphicsExtractor graphics,
        int y,
        int contentWidth
    ) {
        int headerIndex = findHeaderRow(this.table);
        List<String> headers = this.table.rows().get(headerIndex).cells();
        int recordNumber = 0;

        for (int rowIndex = headerIndex + 1; rowIndex < this.table.rows().size(); rowIndex++) {
            WikiTable.Row row = this.table.rows().get(rowIndex);
            if (row.cells().isEmpty()) {
                continue;
            }

            if (row.header()) {
                y = layoutSectionHeading(graphics, y, contentWidth, joinCells(row.cells()));
                continue;
            }

            recordNumber++;
            String titleText = row.cells().getFirst().isBlank()
                ? WikiTexts.text(this.language, "entry") + " " + recordNumber
                : row.cells().getFirst();

            int cardHeight = CELL_PADDING * 2
                + wrappedHeight(titleText, contentWidth - CELL_PADDING * 2)
                + 8;
            for (int column = 1; column < row.cells().size(); column++) {
                String label = column < headers.size()
                    ? headers.get(column)
                    : WikiTexts.text(this.language, "field") + " " + (column + 1);
                String value = row.cells().get(column);
                cardHeight += wrappedHeight(label + ": " + value, contentWidth - CELL_PADDING * 2) + 4;
            }

            if (graphics != null) {
                int left = getInnerLeft();
                graphics.fill(left, y, left + contentWidth, y + cardHeight, 0xB2162330);
                graphics.outline(left, y, contentWidth, cardHeight, 0xFF35506A);
                graphics.textWithWordWrap(
                    this.font,
                    Component.literal(recordNumber + ". " + titleText)
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
                    left + CELL_PADDING,
                    y + CELL_PADDING,
                    contentWidth - CELL_PADDING * 2,
                    0xFF8EEAFF
                );
            }

            int lineY = y + CELL_PADDING
                + wrappedHeight(recordNumber + ". " + titleText, contentWidth - CELL_PADDING * 2)
                + 8;
            for (int column = 1; column < row.cells().size(); column++) {
                String label = column < headers.size()
                    ? headers.get(column)
                    : WikiTexts.text(this.language, "field") + " " + (column + 1);
                MutableComponent field = Component.literal(label + ": ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(row.cells().get(column)));

                if (graphics != null) {
                    graphics.textWithWordWrap(
                        this.font,
                        field,
                        getInnerLeft() + CELL_PADDING,
                        lineY,
                        contentWidth - CELL_PADDING * 2,
                        0xFFF4F8FC
                    );
                }
                lineY += Math.max(
                    this.font.lineHeight,
                    this.font.wordWrapHeight(field, contentWidth - CELL_PADDING * 2)
                ) + 4;
            }

            y += cardHeight + BLOCK_GAP;
        }

        return y;
    }

    private int layoutSectionHeading(
        GuiGraphicsExtractor graphics,
        int y,
        int contentWidth,
        String text
    ) {
        Component heading = Component.literal(text)
            .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
        int height = Math.max(this.font.lineHeight, this.font.wordWrapHeight(heading, contentWidth));

        if (graphics != null) {
            graphics.textWithWordWrap(
                this.font,
                heading,
                getInnerLeft(),
                y,
                contentWidth,
                0xFFA9E3FF
            );
        }

        return y + height + 6;
    }

    private int wrappedHeight(String text, int width) {
        return Math.max(
            this.font.lineHeight,
            this.font.wordWrapHeight(Component.literal(text), Math.max(20, width))
        );
    }

    private static int findHeaderRow(WikiTable table) {
        for (int index = 0; index < table.rows().size(); index++) {
            if (table.rows().get(index).header()) {
                return index;
            }
        }
        return 0;
    }

    private static int maxColumns(WikiTable table) {
        return table.rows().stream()
            .mapToInt(row -> row.cells().size())
            .max()
            .orElse(1);
    }

    private static boolean isKeyValueTable(WikiTable table) {
        long keyValueRows = table.rows().stream()
            .filter(row -> row.cells().size() == 2)
            .count();
        return keyValueRows * 2 >= table.rows().size();
    }

    private static String joinCells(List<String> cells) {
        return String.join(" / ", cells);
    }

    private static int[] columnWidths(int contentWidth, int columnCount) {
        int[] widths = new int[columnCount];
        int remaining = contentWidth - (columnCount - 1) * CELL_GAP;

        if (columnCount == 2) {
            widths[0] = Math.clamp(remaining / 3, 110, 180);
            widths[1] = remaining - widths[0];
            return widths;
        }

        int base = remaining / columnCount;
        int extra = remaining % columnCount;
        for (int index = 0; index < columnCount; index++) {
            widths[index] = base + (index < extra ? 1 : 0);
        }
        return widths;
    }

    private static Map<List<String>, Character> ingredientSymbols(
        WikiRecipe recipe
    ) {
        Map<List<String>, Character> symbols = new LinkedHashMap<>();
        for (List<WikiRecipe.Slot> row : recipe.grid()) {
            for (WikiRecipe.Slot slot : row) {
                if (!slot.isEmpty() && !symbols.containsKey(slot.alternatives())) {
                    symbols.put(
                        slot.alternatives(),
                        (char) ('A' + symbols.size())
                    );
                }
            }
        }
        return symbols;
    }

    private static String alternativesText(
        List<String> alternatives,
        String language
    ) {
        if (alternatives.size() <= 3) {
            return String.join(" / ", alternatives);
        }
        return String.join(" / ", alternatives.subList(0, 3))
            + " "
            + WikiTexts.text(language, "and_more")
            + " ("
            + alternatives.size()
            + ")";
    }

    private static Component title(
        WikiTable table,
        int tableIndex,
        int tableCount,
        String language
    ) {
        String caption = table.caption().isBlank()
            ? WikiTexts.text(language, "untitled_table")
            : table.caption();
        return Component.literal(
            WikiTexts.text(language, "table")
                + " "
                + (tableIndex + 1)
                + "/"
                + tableCount
                + " - "
                + caption
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
    }
}
