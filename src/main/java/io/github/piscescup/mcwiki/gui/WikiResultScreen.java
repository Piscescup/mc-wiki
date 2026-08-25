package io.github.piscescup.mcwiki.gui;

import io.github.piscescup.mcwiki.gui.format.WikiArticleDocument;
import io.github.piscescup.mcwiki.gui.format.WikiTable;
import io.github.piscescup.mcwiki.gui.widget.WikiArticleWidget;
import io.github.piscescup.mcwiki.gui.widget.WikiTableWidget;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import io.github.piscescup.mcwiki.wiki.model.WikiPageSummary;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.List;

/**
 * Displays a loading state followed by a Wiki page introduction.
 */
public final class WikiResultScreen extends Screen {
    private final WikiCategory category;
    private final String query;
    private final String language;

    private Component pageTitle;
    private Component body;
    private WikiArticleDocument articleDocument;
    private String pageUrl = "";
    private String requestedImageUrl = "";
    private List<WikiTable> tables = List.of();
    private WikiImageLoader.LoadedImage articleImage;
    private int tableIndex;
    private boolean showingTables;
    private boolean loaded;
    private boolean imageLoadFailed;
    private boolean disposed;

    public WikiResultScreen(
        WikiCategory category,
        String query,
        String language
    ) {
        super(Component.literal("Minecraft Wiki"));
        this.category = category;
        this.query = query;
        this.language = language;
        this.pageTitle = Component.literal(
            WikiTexts.category(language, category) + " - " + query
        );
        this.body = Component.literal(WikiTexts.text(language, "loading"));
        this.articleDocument = null;
    }

    public void showSummary(
        WikiPageSummary summary,
        WikiArticleDocument article,
        List<WikiTable> tables
    ) {
        this.pageTitle = Component.literal(summary.title())
            .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        this.pageUrl = summary.pageUrl();
        this.tables = List.copyOf(tables);
        this.articleDocument = article;
        this.body = Component.literal(summary.extract());
        this.loaded = true;
        this.showingTables = false;
        this.tableIndex = 0;
        startImageLoad(summary.imageUrl());
        if (this.minecraft != null) {
            rebuildWidgets();
        }
    }

    public void showError(String message) {
        clearArticleImage();
        this.pageTitle = Component.literal(WikiTexts.text(this.language, "error"));
        this.body = Component.literal(message == null ? "Unknown error" : message);
        this.pageUrl = "";
        this.requestedImageUrl = "";
        this.tables = List.of();
        this.articleDocument = null;
        this.loaded = false;
        this.showingTables = false;
        rebuildWidgets();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(620, this.width - 40);
        int left = (this.width - contentWidth) / 2;

        StringWidget heading = new StringWidget(this.pageTitle, this.font);
        heading.setX((this.width - heading.getWidth()) / 2);
        heading.setY(18);
        addRenderableWidget(heading);

        int bodyTop = this.loaded ? 70 : 50;
        if (this.loaded) {
            addViewTabs();
        }

        int bodyHeight = Math.max(80, this.height - bodyTop - 50);
        if (!this.loaded || this.articleDocument == null) {
            addRenderableWidget(new FittingMultiLineTextWidget(
                left, bodyTop, contentWidth, bodyHeight, this.body, this.font
            ));
        } else if (this.showingTables && !this.tables.isEmpty()) {
            addRenderableWidget(new WikiTableWidget(
                left,
                bodyTop,
                contentWidth,
                bodyHeight,
                this.font,
                this.tables.get(this.tableIndex),
                this.tableIndex,
                this.tables.size(),
                this.language
            ));
        } else {
            addRenderableWidget(new WikiArticleWidget(
                left,
                bodyTop,
                contentWidth,
                bodyHeight,
                this.font,
                this.articleDocument,
                this.language,
                this.requestedImageUrl,
                this.articleImage,
                this.imageLoadFailed
            ));
        }

        if (this.showingTables) {
            addTableNavigation();
        } else {
            addArticleActions();
        }
    }

    @Override
    public void removed() {
        this.disposed = true;
        clearArticleImage();
        super.removed();
    }

    private void addViewTabs() {
        Button articleButton = Button.builder(
            Component.literal(WikiTexts.text(this.language, "article")),
            button -> showArticle()
        ).bounds(this.width / 2 - 112, 42, 108, 20).build();
        articleButton.active = this.showingTables;
        addRenderableWidget(articleButton);

        Button tablesButton = Button.builder(
            Component.literal(
                WikiTexts.text(this.language, "tables")
                    + " (" + this.tables.size() + ")"
            ),
            button -> showTable(this.tableIndex)
        ).bounds(this.width / 2 + 4, 42, 108, 20).build();
        tablesButton.active = !this.showingTables && !this.tables.isEmpty();
        addRenderableWidget(tablesButton);
    }

    private void addArticleActions() {
        int closeX = this.width / 2 - 50;
        if (!this.pageUrl.isBlank()) {
            closeX = this.width / 2 + 4;
            addRenderableWidget(Button.builder(
                Component.literal(WikiTexts.text(this.language, "open_source")),
                button -> Util.getPlatform().openUri(this.pageUrl)
            ).bounds(this.width / 2 - 104, this.height - 40, 100, 20).build());
        }

        addRenderableWidget(Button.builder(
            Component.literal(WikiTexts.text(this.language, "close")),
            button -> onClose()
        ).bounds(closeX, this.height - 40, 100, 20).build());
    }

    private void addTableNavigation() {
        Button previous = Button.builder(
            Component.literal(WikiTexts.text(this.language, "previous_table")),
            button -> showTable(this.tableIndex - 1)
        ).bounds(this.width / 2 - 154, this.height - 40, 100, 20).build();
        previous.active = this.tableIndex > 0;
        addRenderableWidget(previous);

        Button next = Button.builder(
            Component.literal(WikiTexts.text(this.language, "next_table")),
            button -> showTable(this.tableIndex + 1)
        ).bounds(this.width / 2 - 50, this.height - 40, 100, 20).build();
        next.active = this.tableIndex + 1 < this.tables.size();
        addRenderableWidget(next);

        addRenderableWidget(Button.builder(
            Component.literal(WikiTexts.text(this.language, "close")),
            button -> onClose()
        ).bounds(this.width / 2 + 54, this.height - 40, 100, 20).build());
    }

    private void showArticle() {
        this.showingTables = false;
        rebuildWidgets();
    }

    private void showTable(int index) {
        if (this.tables.isEmpty()) {
            return;
        }
        this.showingTables = true;
        this.tableIndex = Math.clamp(index, 0, this.tables.size() - 1);
        rebuildWidgets();
    }

    private void startImageLoad(String imageUrl) {
        clearArticleImage();
        this.requestedImageUrl = imageUrl == null ? "" : imageUrl;
        this.imageLoadFailed = false;

        if (this.requestedImageUrl.isBlank() || this.minecraft == null) {
            return;
        }

        String currentRequest = this.requestedImageUrl;
        WikiImageLoader.load(this.minecraft, currentRequest)
            .whenComplete((image, throwable) -> this.minecraft.execute(() -> {
                if (throwable != null) {
                    if (!this.disposed && currentRequest.equals(this.requestedImageUrl)) {
                        this.imageLoadFailed = true;
                        rebuildWidgets();
                    }
                    return;
                }

                if (this.disposed || !currentRequest.equals(this.requestedImageUrl)) {
                    image.release(this.minecraft);
                    return;
                }

                this.articleImage = image;
                rebuildWidgets();
            }));
    }

    private void clearArticleImage() {
        if (this.articleImage != null && this.minecraft != null) {
            this.articleImage.release(this.minecraft);
        }
        this.articleImage = null;
    }
}
