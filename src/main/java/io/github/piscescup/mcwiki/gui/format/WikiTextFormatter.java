package io.github.piscescup.mcwiki.gui.format;

import io.github.piscescup.mcwiki.gui.WikiTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Legacy plain-text formatter retained as a fallback path.
 */
public final class WikiTextFormatter {
    private static final Pattern HEADING =
        Pattern.compile("^(={2,6})\\s*(.+?)\\s*\\1$");

    private WikiTextFormatter() {
    }

    public static Component format(
        String text,
        String html,
        String pageUrl,
        String language
    ) {
        MutableComponent result = Component.empty();
        result.append(Component.literal(
            WikiTexts.text(language, "abstract")
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
        result.append("\n");

        int[] sectionNumbers = new int[5];
        appendArticle(result, text, sectionNumbers);
        appendTables(
            result,
            WikiHtmlTableParser.parse(html),
            ++sectionNumbers[0],
            language
        );
        appendReference(result, pageUrl, language);
        return result;
    }

    private static void appendArticle(
        MutableComponent result,
        String text,
        int[] sectionNumbers
    ) {
        boolean previousLineBlank = false;

        for (String rawLine : text.split("\\R", -1)) {
            String line = rawLine.strip();
            Matcher heading = HEADING.matcher(line);

            if (heading.matches()) {
                int depth = heading.group(1).length() - 2;
                sectionNumbers[depth]++;
                Arrays.fill(
                    sectionNumbers,
                    depth + 1,
                    sectionNumbers.length,
                    0
                );

                result.append("\n");
                result.append(Component.literal(
                    sectionNumber(sectionNumbers, depth)
                        + " " + heading.group(2)
                ).withStyle(headingStyle(depth)));
                result.append("\n");
                previousLineBlank = false;
                continue;
            }

            if (line.isEmpty()) {
                if (!previousLineBlank) {
                    result.append("\n");
                }
                previousLineBlank = true;
                continue;
            }

            result.append(Component.literal(line));
            result.append("\n");
            previousLineBlank = false;
        }
    }

    private static void appendTables(
        MutableComponent result,
        List<WikiTable> tables,
        int sectionNumber,
        String language
    ) {
        if (tables.isEmpty()) {
            return;
        }

        result.append("\n");
        result.append(Component.literal(
            sectionNumber + ". " + WikiTexts.text(language, "tables")
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        result.append("\n");

        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            WikiTable table = tables.get(tableIndex);
            String caption = table.caption().isBlank()
                ? WikiTexts.text(language, "untitled_table")
                : table.caption();

            result.append("\n");
            result.append(Component.literal(
                WikiTexts.text(language, "table") + " "
                    + (tableIndex + 1) + ". " + caption
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
            result.append("\n");

            String rule = "-".repeat(tableRuleWidth(table));
            result.append(Component.literal(rule).withStyle(ChatFormatting.GRAY));
            result.append("\n");

            int lastHeaderRow = lastHeaderRow(table);
            for (int rowIndex = 0; rowIndex < table.rows().size(); rowIndex++) {
                WikiTable.Row row = table.rows().get(rowIndex);
                MutableComponent rowText = Component.literal(
                    String.join("    ", row.cells())
                );
                if (row.header()) {
                    rowText.withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
                }
                result.append(rowText);
                result.append("\n");

                if (rowIndex == lastHeaderRow) {
                    result.append(Component.literal(rule).withStyle(ChatFormatting.GRAY));
                    result.append("\n");
                }
            }

            result.append(Component.literal(rule).withStyle(ChatFormatting.GRAY));
            result.append("\n");
        }
    }

    private static int lastHeaderRow(WikiTable table) {
        int lastHeader = -1;
        for (int index = 0; index < table.rows().size(); index++) {
            if (!table.rows().get(index).header()) {
                break;
            }
            lastHeader = index;
        }
        return lastHeader >= 0 ? lastHeader : 0;
    }

    private static int tableRuleWidth(WikiTable table) {
        int longestRow = table.rows().stream()
            .mapToInt(row -> {
                String rowText = String.join("    ", row.cells());
                return rowText.codePointCount(0, rowText.length());
            })
            .max()
            .orElse(24);
        return Math.clamp(longestRow, 24, 56);
    }

    private static void appendReference(
        MutableComponent result,
        String pageUrl,
        String language
    ) {
        if (pageUrl.isBlank()) {
            return;
        }

        result.append("\n");
        result.append(Component.literal(
            WikiTexts.text(language, "reference") + "\n"
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
        result.append(Component.literal(pageUrl)
            .withStyle(ChatFormatting.GRAY));
    }

    private static String sectionNumber(int[] numbers, int depth) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index <= depth; index++) {
            if (index > 0) {
                result.append('.');
            }
            result.append(Math.max(1, numbers[index]));
        }
        result.append('.');
        return result.toString();
    }

    private static ChatFormatting[] headingStyle(int depth) {
        return switch (depth) {
            case 0 -> new ChatFormatting[] {
                ChatFormatting.BOLD,
                ChatFormatting.GOLD
            };
            case 1 -> new ChatFormatting[] {
                ChatFormatting.BOLD,
                ChatFormatting.YELLOW
            };
            default -> new ChatFormatting[] {
                ChatFormatting.BOLD,
                ChatFormatting.AQUA
            };
        };
    }
}
