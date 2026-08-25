package io.github.piscescup.mcwiki.gui.format;


import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Extracts content tables while ignoring MediaWiki navigation boxes.
 */
public final class WikiTableParser {
    private WikiTableParser() {
    }

    public static List<WikiTable> parse(String html) {
        if (html.isBlank()) {
            return List.of();
        }

        TableCallback callback = new TableCallback();
        try {
            new ParserDelegator().parse(
                new StringReader(html),
                callback,
                true
            );
            return List.copyOf(callback.tables);
        } catch (IOException exception) {
            return List.of();
        }
    }

    private static final class TableCallback
        extends HTMLEditorKit.ParserCallback {

        private final List<WikiTable> tables = new ArrayList<>();
        private int tableDepth;
        private TableBuilder table;

        @Override
        public void handleStartTag(
            HTML.Tag tag,
            MutableAttributeSet attributes,
            int position
        ) {
            if (tag == HTML.Tag.TABLE) {
                if (this.tableDepth == 0 && isContentTable(attributes)) {
                    this.table = new TableBuilder();
                }
                this.tableDepth++;
                return;
            }

            if (this.table == null || this.tableDepth != 1) {
                return;
            }

            if (tag == HTML.Tag.TR) {
                this.table.startRow();
            } else if (tag == HTML.Tag.TH) {
                this.table.startCell(true);
            } else if (tag == HTML.Tag.TD) {
                this.table.startCell(false);
            } else if (tag == HTML.Tag.CAPTION) {
                this.table.startCaption();
            } else if (tag == HTML.Tag.BR) {
                this.table.appendText(" ");
            }
        }

        @Override
        public void handleSimpleTag(
            HTML.Tag tag,
            MutableAttributeSet attributes,
            int position
        ) {
            if (tag == HTML.Tag.BR
                && this.table != null
                && this.tableDepth == 1) {
                this.table.appendText(" ");
            }
        }

        @Override
        public void handleText(char[] data, int position) {
            if (this.table != null && this.tableDepth == 1) {
                this.table.appendText(new String(data));
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            if (tag == HTML.Tag.TABLE) {
                this.tableDepth--;
                if (this.tableDepth == 0 && this.table != null) {
                    WikiTable completed = this.table.build();
                    if (!completed.rows().isEmpty()) {
                        this.tables.add(completed);
                    }
                    this.table = null;
                }
                return;
            }

            if (this.table == null || this.tableDepth != 1) {
                return;
            }

            if (tag == HTML.Tag.TH || tag == HTML.Tag.TD) {
                this.table.endCell();
            } else if (tag == HTML.Tag.TR) {
                this.table.endRow();
            } else if (tag == HTML.Tag.CAPTION) {
                this.table.endCaption();
            }
        }

        private static boolean isContentTable(MutableAttributeSet attributes) {
            Object classAttribute = attributes.getAttribute(HTML.Attribute.CLASS);
            if (classAttribute == null) {
                return false;
            }

            String classes = classAttribute.toString().toLowerCase(Locale.ROOT);
            return classes.contains("wikitable") || classes.contains("infobox");
        }
    }

    private static final class TableBuilder {
        private final List<WikiTable.Row> rows = new ArrayList<>();
        private final List<String> currentCells = new ArrayList<>();
        private final StringBuilder text = new StringBuilder();
        private String caption = "";
        private boolean rowHeader;
        private boolean inCell;
        private boolean inCaption;

        private void startRow() {
            this.currentCells.clear();
            this.rowHeader = false;
        }

        private void startCell(boolean header) {
            this.text.setLength(0);
            this.inCell = true;
            this.rowHeader |= header;
        }

        private void appendText(String value) {
            if (!this.inCell && !this.inCaption) {
                return;
            }
            if (!this.text.isEmpty()
                && !Character.isWhitespace(this.text.charAt(this.text.length() - 1))) {
                this.text.append(' ');
            }
            this.text.append(value);
        }

        private void endCell() {
            this.currentCells.add(normalize(this.text.toString()));
            this.text.setLength(0);
            this.inCell = false;
        }

        private void endRow() {
            if (!this.currentCells.isEmpty()
                && this.currentCells.stream().anyMatch(cell -> !cell.isBlank())) {
                this.rows.add(new WikiTable.Row(
                    List.copyOf(this.currentCells),
                    this.rowHeader
                ));
            }
            this.currentCells.clear();
        }

        private void startCaption() {
            this.text.setLength(0);
            this.inCaption = true;
        }

        private void endCaption() {
            this.caption = normalize(this.text.toString());
            this.text.setLength(0);
            this.inCaption = false;
        }

        private WikiTable build() {
            return new WikiTable(this.caption, this.rows);
        }

        private static String normalize(String value) {
            return value.replaceAll("\\s+", " ").strip();
        }
    }
}
