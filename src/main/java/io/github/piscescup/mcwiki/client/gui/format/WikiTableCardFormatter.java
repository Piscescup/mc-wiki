package io.github.piscescup.mcwiki.client.gui.format;

import io.github.piscescup.mcwiki.client.WikiTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formats a table as responsive field cards instead of fixed-width columns.
 */
public final class WikiTableCardFormatter {
    private WikiTableCardFormatter() {
    }

    public static Component format(
        WikiTable table,
        int tableIndex,
        int tableCount,
        String language
    ) {
        MutableComponent result = Component.empty();
        String caption = table.caption().isBlank()
            ? WikiTexts.text(language, "untitled_table")
            : table.caption();

        result.append(Component.literal(
            WikiTexts.text(language, "table") + " "
                + (tableIndex + 1) + "/" + tableCount + " · " + caption
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        result.append("\n\n");

        if (!table.recipes().isEmpty()) {
            appendRecipes(result, table.recipes(), language);
        } else if (isKeyValueTable(table)) {
            appendKeyValueTable(result, table);
        } else {
            appendRecordCards(result, table, language);
        }
        return result;
    }

    private static void appendRecipes(
        MutableComponent result,
        List<WikiRecipe> recipes,
        String language
    ) {
        for (int recipeIndex = 0;
             recipeIndex < recipes.size();
             recipeIndex++) {
            WikiRecipe recipe = recipes.get(recipeIndex);
            result.append(Component.literal(
                WikiTexts.text(language, "recipe") + " "
                    + (recipeIndex + 1)
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
            result.append("\n\n");

            Map<List<String>, Character> symbols = ingredientSymbols(recipe);
            for (List<WikiRecipe.Slot> row : recipe.grid()) {
                for (WikiRecipe.Slot slot : row) {
                    char symbol = slot.isEmpty()
                        ? ' '
                        : symbols.get(slot.alternatives());
                    result.append(Component.literal("[ " + symbol + " ] ")
                        .withStyle(slot.isEmpty()
                            ? ChatFormatting.DARK_GRAY
                            : ChatFormatting.AQUA));
                }
                result.append("\n");
            }

            result.append("\n");
            for (Map.Entry<List<String>, Character> ingredient
                : symbols.entrySet()) {
                result.append(Component.literal(
                    ingredient.getValue() + " = "
                ).withStyle(ChatFormatting.YELLOW));
                result.append(Component.literal(
                    alternativesText(ingredient.getKey(), language)
                ));
                result.append("\n");
            }

            result.append(Component.literal(
                WikiTexts.text(language, "output") + ": "
            ).withStyle(ChatFormatting.GOLD));
            result.append(Component.literal(
                recipe.output() + " ×" + recipe.outputCount()
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN));
            result.append("\n\n");
        }
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
            + " " + WikiTexts.text(language, "and_more")
            + " (" + alternatives.size() + ")";
    }

    private static boolean isKeyValueTable(WikiTable table) {
        long keyValueRows = table.rows().stream()
            .filter(row -> row.header() && row.cells().size() == 2)
            .count();
        return keyValueRows * 2 >= table.rows().size();
    }

    private static void appendKeyValueTable(
        MutableComponent result,
        WikiTable table
    ) {
        for (WikiTable.Row row : table.rows()) {
            if (row.cells().isEmpty()) {
                continue;
            }

            appendField(result, row.cells().getFirst(),
                row.cells().size() > 1
                    ? String.join(" · ", row.cells().subList(1, row.cells().size()))
                    : "");
        }
    }

    private static void appendRecordCards(
        MutableComponent result,
        WikiTable table,
        String language
    ) {
        int headerIndex = findHeaderRow(table);
        List<String> headers = table.rows().get(headerIndex).cells();
        int recordNumber = 0;

        for (int rowIndex = headerIndex + 1;
             rowIndex < table.rows().size();
             rowIndex++) {
            WikiTable.Row row = table.rows().get(rowIndex);
            if (row.cells().isEmpty()) {
                continue;
            }

            if (row.header()) {
                result.append(Component.literal(
                    String.join(" · ", row.cells())
                ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
                result.append("\n\n");
                continue;
            }

            recordNumber++;
            String recordTitle = row.cells().getFirst().isBlank()
                ? WikiTexts.text(language, "entry") + " " + recordNumber
                : row.cells().getFirst();
            result.append(Component.literal(
                recordNumber + ". " + recordTitle
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
            result.append("\n");

            for (int column = 1; column < row.cells().size(); column++) {
                String label = column < headers.size()
                    ? headers.get(column)
                    : WikiTexts.text(language, "field") + " " + (column + 1);
                appendField(result, label, row.cells().get(column));
            }
            result.append("\n");
        }
    }

    private static int findHeaderRow(WikiTable table) {
        for (int index = 0; index < table.rows().size(); index++) {
            if (table.rows().get(index).header()) {
                return index;
            }
        }
        return 0;
    }

    private static void appendField(
        MutableComponent result,
        String label,
        String value
    ) {
        result.append(Component.literal(label + ": ")
            .withStyle(ChatFormatting.YELLOW));
        result.append(Component.literal(value));
        result.append("\n");
    }
}
