package io.github.piscescup.mcwiki.client.gui.format;

import io.github.piscescup.mcwiki.client.WikiTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.Set;

/**
 * Restores article headings, paragraphs, and HTML list semantics.
 */
public final class WikiArticleHtmlFormatter {
    private static final Set<String> SKIPPED_CLASSES = Set.of(
        "mw-editsection",
        "navbox",
        "infobox",
        "gallery",
        "searchaux",
        "metadata",
        "noprint",
        "mw-empty-elt"
    );

    private WikiArticleHtmlFormatter() {
    }

    public static Component format(
        String fallbackText,
        String html,
        String pageUrl,
        String language
    ) {
        if (html.isBlank()) {
            return fallback(fallbackText, pageUrl, language);
        }

        try {
            MutableComponent result = Component.empty();
            result.append(Component.literal(
                WikiTexts.text(language, "abstract")
            ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
            result.append("\n");

            int[] sectionNumbers = new int[5];
            for (Element child : Jsoup.parse(html).body().children()) {
                appendElement(result, child, sectionNumbers, 0);
            }

            appendReference(result, pageUrl, language);
            if (result.getString().length() < 20) {
                return fallback(fallbackText, pageUrl, language);
            }
            return result;
        } catch (RuntimeException exception) {
            return fallback(fallbackText, pageUrl, language);
        }
    }

    private static void appendElement(
        MutableComponent result,
        Element element,
        int[] sectionNumbers,
        int listDepth
    ) {
        if (shouldSkip(element)) {
            return;
        }

        String tag = element.tagName();
        if (tag.equals("table")
            || tag.equals("script")
            || tag.equals("style")
            || tag.equals("link")) {
            return;
        }

        if (isHeading(tag)) {
            appendHeading(result, element, sectionNumbers);
            return;
        }
        if (tag.equals("p")) {
            appendParagraph(result, element.text());
            return;
        }
        if (tag.equals("ul") || tag.equals("ol")) {
            appendList(result, element, listDepth);
            return;
        }
        if (tag.equals("dl")) {
            appendDefinitionList(result, element);
            return;
        }

        for (Element child : element.children()) {
            appendElement(result, child, sectionNumbers, listDepth);
        }
    }

    private static void appendHeading(
        MutableComponent result,
        Element heading,
        int[] sectionNumbers
    ) {
        int depth = Integer.parseInt(heading.tagName().substring(1)) - 2;
        depth = Math.clamp(depth, 0, sectionNumbers.length - 1);
        sectionNumbers[depth]++;
        Arrays.fill(
            sectionNumbers,
            depth + 1,
            sectionNumbers.length,
            0
        );

        String headingText = heading.text().strip();
        if (headingText.isEmpty()) {
            return;
        }

        result.append("\n");
        result.append(Component.literal(
            sectionNumber(sectionNumbers, depth) + " " + headingText
        ).withStyle(headingStyle(depth)));
        result.append("\n");
    }

    private static void appendParagraph(
        MutableComponent result,
        String paragraph
    ) {
        String normalized = normalize(paragraph);
        if (!normalized.isEmpty()) {
            result.append(Component.literal(normalized));
            result.append("\n\n");
        }
    }

    private static void appendList(
        MutableComponent result,
        Element list,
        int depth
    ) {
        boolean ordered = list.tagName().equals("ol");
        int itemNumber = 0;

        for (Element item : list.children()) {
            if (!item.tagName().equals("li")) {
                continue;
            }

            itemNumber++;
            String marker = ordered ? itemNumber + ". " : "• ";
            result.append(Component.literal(
                "  ".repeat(depth) + marker
            ).withStyle(ordered
                ? ChatFormatting.GOLD
                : ChatFormatting.AQUA));

            String itemText = listItemText(item);
            result.append(Component.literal(itemText));
            result.append("\n");

            for (Element child : item.children()) {
                if (child.tagName().equals("ul")
                    || child.tagName().equals("ol")) {
                    appendList(result, child, depth + 1);
                }
            }
        }
        result.append("\n");
    }

    private static String listItemText(Element item) {
        Element copy = item.clone();
        for (Element child : copy.children()) {
            if (child.tagName().equals("ul")
                || child.tagName().equals("ol")) {
                child.remove();
            }
        }
        return normalize(copy.text());
    }

    private static void appendDefinitionList(
        MutableComponent result,
        Element list
    ) {
        for (Element item : list.children()) {
            String text = normalize(item.text());
            if (text.isEmpty()) {
                continue;
            }

            if (item.tagName().equals("dt")) {
                result.append(Component.literal(text)
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
            } else if (item.tagName().equals("dd")) {
                result.append(Component.literal("  " + text));
            }
            result.append("\n");
        }
        result.append("\n");
    }

    private static boolean shouldSkip(Element element) {
        for (String className : element.classNames()) {
            if (SKIPPED_CLASSES.contains(className)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHeading(String tag) {
        return tag.length() == 2
            && tag.charAt(0) == 'h'
            && tag.charAt(1) >= '2'
            && tag.charAt(1) <= '6';
    }

    private static String sectionNumber(int[] numbers, int depth) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index <= depth; index++) {
            if (index > 0) {
                result.append('.');
            }
            result.append(Math.max(1, numbers[index]));
        }
        return result.append('.').toString();
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

    private static void appendReference(
        MutableComponent result,
        String pageUrl,
        String language
    ) {
        if (pageUrl.isBlank()) {
            return;
        }
        result.append(Component.literal(
            WikiTexts.text(language, "reference") + "\n"
        ).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
        result.append(Component.literal(pageUrl)
            .withStyle(ChatFormatting.GRAY));
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }

    private static Component fallback(
        String text,
        String pageUrl,
        String language
    ) {
        return WikiTextFormatter.format(text, "", pageUrl, language);
    }
}
