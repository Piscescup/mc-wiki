package io.github.piscescup.mcwiki.gui.format;

import io.github.piscescup.mcwiki.gui.WikiTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static WikiArticleDocument document(
        String fallbackText,
        String html,
        String pageUrl,
        String language
    ) {
        if (html.isBlank()) {
            return fallbackDocument(fallbackText, pageUrl, language);
        }

        try {
            List<WikiArticleDocument.Block> blocks = new ArrayList<>();
            blocks.add(new WikiArticleDocument.Block(
                Component.literal(WikiTexts.text(language, "abstract"))
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),
                0,
                0,
                6
            ));

            int[] sectionNumbers = new int[5];
            for (Element child : Jsoup.parse(html).body().children()) {
                appendElement(blocks, child, sectionNumbers, 0);
            }

            appendReference(blocks, pageUrl, language);
            if (blocks.stream().mapToInt(
                block -> block.text().getString().length()
            ).sum() < 20) {
                return fallbackDocument(fallbackText, pageUrl, language);
            }

            return new WikiArticleDocument(blocks, narration(blocks));
        } catch (RuntimeException exception) {
            return fallbackDocument(fallbackText, pageUrl, language);
        }
    }

    public static Component format(
        String fallbackText,
        String html,
        String pageUrl,
        String language
    ) {
        MutableComponent result = Component.empty();
        for (WikiArticleDocument.Block block
            : document(fallbackText, html, pageUrl, language).blocks()) {
            if (!result.getString().isEmpty()) {
                result.append("\n");
            }
            result.append(block.text());
        }
        return result;
    }

    private static void appendElement(
        List<WikiArticleDocument.Block> blocks,
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
            appendHeading(blocks, element, sectionNumbers);
            return;
        }
        if (tag.equals("p")) {
            appendParagraph(blocks, Component.literal(element.text()), 0, 0, 8);
            return;
        }
        if (tag.equals("ul") || tag.equals("ol")) {
            appendList(blocks, element, listDepth);
            return;
        }
        if (tag.equals("dl")) {
            appendDefinitionList(blocks, element);
            return;
        }

        for (Element child : element.children()) {
            appendElement(blocks, child, sectionNumbers, listDepth);
        }
    }

    private static void appendHeading(
        List<WikiArticleDocument.Block> blocks,
        Element heading,
        int[] sectionNumbers
    ) {
        int depth = Integer.parseInt(heading.tagName().substring(1)) - 2;
        depth = Math.clamp(depth, 0, sectionNumbers.length - 1);
        sectionNumbers[depth]++;
        Arrays.fill(sectionNumbers, depth + 1, sectionNumbers.length, 0);

        String headingText = normalize(heading.text());
        if (headingText.isEmpty()) {
            return;
        }

        blocks.add(new WikiArticleDocument.Block(
            Component.literal(sectionNumber(sectionNumbers, depth) + " " + headingText)
                .withStyle(headingStyle(depth)),
            0,
            10,
            4
        ));
    }

    private static void appendParagraph(
        List<WikiArticleDocument.Block> blocks,
        Component paragraph,
        int indent,
        int topMargin,
        int bottomMargin
    ) {
        String normalized = normalize(paragraph.getString());
        if (normalized.isEmpty()) {
            return;
        }

        blocks.add(new WikiArticleDocument.Block(
            Component.literal(normalized).withStyle(paragraph.getStyle()),
            indent,
            topMargin,
            bottomMargin
        ));
    }

    private static void appendList(
        List<WikiArticleDocument.Block> blocks,
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
            String marker = ordered ? itemNumber + ". " : "- ";
            MutableComponent line = Component.literal(marker)
                .withStyle(ordered
                    ? ChatFormatting.GOLD
                    : ChatFormatting.AQUA)
                .append(Component.literal(listItemText(item)));
            appendParagraph(blocks, line, depth * 14, 0, 3);

            for (Element child : item.children()) {
                if (child.tagName().equals("ul")
                    || child.tagName().equals("ol")) {
                    appendList(blocks, child, depth + 1);
                }
            }
        }
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
        List<WikiArticleDocument.Block> blocks,
        Element list
    ) {
        for (Element item : list.children()) {
            String text = normalize(item.text());
            if (text.isEmpty()) {
                continue;
            }

            if (item.tagName().equals("dt")) {
                blocks.add(new WikiArticleDocument.Block(
                    Component.literal(text)
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
                    0,
                    2,
                    2
                ));
            } else if (item.tagName().equals("dd")) {
                blocks.add(new WikiArticleDocument.Block(
                    Component.literal(text),
                    12,
                    0,
                    4
                ));
            }
        }
    }

    private static void appendReference(
        List<WikiArticleDocument.Block> blocks,
        String pageUrl,
        String language
    ) {
        if (pageUrl.isBlank()) {
            return;
        }

        blocks.add(new WikiArticleDocument.Block(
            Component.literal(WikiTexts.text(language, "reference"))
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),
            0,
            12,
            2
        ));
        blocks.add(new WikiArticleDocument.Block(
            Component.literal(pageUrl).withStyle(ChatFormatting.GRAY),
            0,
            0,
            0
        ));
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

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }

    private static WikiArticleDocument fallbackDocument(
        String text,
        String pageUrl,
        String language
    ) {
        List<WikiArticleDocument.Block> blocks = new ArrayList<>();
        blocks.add(new WikiArticleDocument.Block(
            Component.literal(WikiTexts.text(language, "abstract"))
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),
            0,
            0,
            6
        ));

        boolean previousBlank = false;
        for (String rawLine : text.split("\\R", -1)) {
            String line = normalize(rawLine);
            if (line.isEmpty()) {
                previousBlank = true;
                continue;
            }

            blocks.add(new WikiArticleDocument.Block(
                Component.literal(line),
                0,
                previousBlank ? 6 : 0,
                2
            ));
            previousBlank = false;
        }

        appendReference(blocks, pageUrl, language);
        return new WikiArticleDocument(blocks, narration(blocks));
    }

    private static Component narration(List<WikiArticleDocument.Block> blocks) {
        StringBuilder narration = new StringBuilder();
        for (WikiArticleDocument.Block block : blocks) {
            if (!narration.isEmpty()) {
                narration.append('\n');
            }
            narration.append(block.text().getString());
        }
        return Component.literal(narration.toString());
    }
}
