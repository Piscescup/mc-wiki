package io.github.piscescup.mcwiki.client.gui.format;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Parses MediaWiki content tables using jsoup's HTML5 DOM parser.
 */
public final class WikiHtmlTableParser {
    private WikiHtmlTableParser() {
    }

    public static List<WikiTable> parse(String html) {
        if (html.isBlank()) {
            return List.of();
        }

        try {
            Document document = Jsoup.parse(html);
            List<WikiTable> tables = new ArrayList<>();

            for (Element tableElement : document.getElementsByTag("table")) {
                if (!isContentTable(tableElement)
                    || hasTableAncestor(tableElement)) {
                    continue;
                }

                WikiTable table = parseTable(tableElement);
                if (!table.rows().isEmpty()) {
                    tables.add(table);
                }
            }

            return List.copyOf(tables);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private static WikiTable parseTable(Element tableElement) {
        Element captionElement = directChild(tableElement, "caption");
        String caption = captionElement == null
            ? ""
            : normalize(captionElement.text());

        List<WikiTable.Row> rows = new ArrayList<>();
        for (Element child : tableElement.children()) {
            if (child.tagName().equals("tr")) {
                addRow(rows, child);
                continue;
            }

            if (child.tagName().equals("thead")
                || child.tagName().equals("tbody")
                || child.tagName().equals("tfoot")) {
                for (Element row : child.children()) {
                    if (row.tagName().equals("tr")) {
                        addRow(rows, row);
                    }
                }
            }
        }

        return new WikiTable(caption, rows, parseRecipes(tableElement));
    }

    private static List<WikiRecipe> parseRecipes(Element tableElement) {
        List<WikiRecipe> recipes = new ArrayList<>();
        for (Element recipeElement
            : tableElement.getElementsByClass("mcui-Crafting_Table")) {
            Element input = recipeElement
                .getElementsByClass("mcui-input")
                .first();
            Element output = recipeElement
                .getElementsByClass("mcui-output")
                .first();
            if (input == null || output == null) {
                continue;
            }

            List<List<WikiRecipe.Slot>> grid = new ArrayList<>();
            for (Element rowElement : input.getElementsByClass("mcui-row")) {
                List<WikiRecipe.Slot> row = new ArrayList<>();
                for (Element child : rowElement.children()) {
                    if (child.hasClass("invslot")) {
                        row.add(parseSlot(child));
                    }
                }
                if (!row.isEmpty()) {
                    grid.add(row);
                }
            }

            WikiRecipe.Slot outputSlot = parseSlot(output);
            if (grid.isEmpty() || outputSlot.isEmpty()) {
                continue;
            }

            String count = output
                .getElementsByClass("invslot-item-count")
                .text();
            recipes.add(new WikiRecipe(
                grid,
                outputSlot.alternatives().getFirst(),
                count.isBlank() ? "1" : count
            ));
        }
        return List.copyOf(recipes);
    }

    private static WikiRecipe.Slot parseSlot(Element slotElement) {
        LinkedHashSet<String> alternatives = new LinkedHashSet<>();
        for (Element item
            : slotElement.getElementsByClass("invslot-item")) {
            String title = normalize(item.attr("title"));
            if (!title.isBlank()) {
                alternatives.add(title);
            }
        }
        return new WikiRecipe.Slot(List.copyOf(alternatives));
    }

    private static void addRow(
        List<WikiTable.Row> rows,
        Element rowElement
    ) {
        List<String> cells = new ArrayList<>();
        boolean header = false;

        for (Element cell : rowElement.children()) {
            if (!cell.tagName().equals("th")
                && !cell.tagName().equals("td")) {
                continue;
            }

            cells.add(normalize(cell.text()));
            header |= cell.tagName().equals("th");
        }

        if (!cells.isEmpty()
            && cells.stream().anyMatch(value -> !value.isBlank())) {
            rows.add(new WikiTable.Row(cells, header));
        }
    }

    private static Element directChild(Element parent, String tagName) {
        for (Element child : parent.children()) {
            if (child.tagName().equals(tagName)) {
                return child;
            }
        }
        return null;
    }

    private static boolean isContentTable(Element table) {
        if (table.hasClass("wikitable")) {
            return true;
        }
        return table.classNames().stream()
            .anyMatch(className -> className.startsWith("infobox"));
    }

    private static boolean hasTableAncestor(Element element) {
        for (Element parent : element.parents()) {
            if (parent.tagName().equals("table")) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }
}
