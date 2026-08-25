package io.github.piscescup.mcwiki.gui.widget;

import io.github.piscescup.mcwiki.gui.WikiImageLoader;
import io.github.piscescup.mcwiki.gui.WikiTexts;
import io.github.piscescup.mcwiki.gui.format.WikiArticleDocument;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Scrollable article view with optional lead image support.
 */
public final class WikiArticleWidget extends AbstractTextAreaWidget {
    private static final int IMAGE_MAX_WIDTH = 340;
    private static final int IMAGE_MIN_HEIGHT = 96;
    private static final int IMAGE_MAX_HEIGHT = 220;
    private static final int IMAGE_PADDING = 12;

    private final Font font;
    private final WikiArticleDocument document;
    private final String language;
    private final boolean hasImageRequest;
    private final boolean imageLoadFailed;
    private final WikiImageLoader.LoadedImage image;

    public WikiArticleWidget(
        int x,
        int y,
        int width,
        int height,
        Font font,
        WikiArticleDocument document,
        String language,
        String imageUrl,
        WikiImageLoader.LoadedImage image,
        boolean imageLoadFailed
    ) {
        super(
            x,
            y,
            width,
            height,
            document.narration(),
            AbstractScrollArea.defaultSettings(9)
        );
        this.font = font;
        this.document = document;
        this.language = language;
        this.hasImageRequest = !imageUrl.isBlank();
        this.image = image;
        this.imageLoadFailed = imageLoadFailed;
    }

    @Override
    protected int getInnerHeight() {
        return layoutArticle(null);
    }

    @Override
    protected void extractContents(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float deltaTicks
    ) {
        layoutArticle(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.document.narration());
    }

    private int layoutArticle(GuiGraphicsExtractor graphics) {
        int contentWidth = Math.max(96, getWidth() - totalInnerPadding());
        int y = getInnerTop();

        if (shouldShowImageArea()) {
            y = layoutImage(graphics, getInnerLeft(), y, contentWidth);
            y += IMAGE_PADDING;
        }

        for (WikiArticleDocument.Block block : this.document.blocks()) {
            int blockX = getInnerLeft() + Math.max(0, block.indent());
            int blockWidth = Math.max(72, contentWidth - Math.max(0, block.indent()));
            y += block.topMargin();

            if (graphics != null) {
                graphics.textWithWordWrap(
                    this.font,
                    block.text(),
                    blockX,
                    y,
                    blockWidth,
                    0xFFF4F8FC
                );
            }

            y += Math.max(
                this.font.lineHeight,
                this.font.wordWrapHeight(block.text(), blockWidth)
            );
            y += block.bottomMargin();
        }

        return y - getInnerTop();
    }

    private int layoutImage(
        GuiGraphicsExtractor graphics,
        int left,
        int top,
        int contentWidth
    ) {
        int drawWidth = Math.min(contentWidth, IMAGE_MAX_WIDTH);
        int drawHeight = imageHeight(drawWidth);
        int drawX = left + (contentWidth - drawWidth) / 2;
        int frameLeft = drawX - 4;
        int frameTop = top - 4;
        int frameRight = drawX + drawWidth + 4;
        int frameBottom = top + drawHeight + 4;

        if (graphics != null) {
            graphics.fill(frameLeft, frameTop, frameRight, frameBottom, 0xC4172230);
            graphics.outline(frameLeft, frameTop, frameRight - frameLeft, frameBottom - frameTop, 0xFF35506A);

            if (this.image != null) {
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.image.textureId(),
                    drawX,
                    top,
                    0.0F,
                    0.0F,
                    drawWidth,
                    drawHeight,
                    this.image.width(),
                    this.image.height()
                );
            } else {
                graphics.fill(drawX, top, drawX + drawWidth, top + drawHeight, 0x7F203040);
                graphics.centeredText(
                    this.font,
                    this.imageLoadFailed
                        ? WikiTexts.text(this.language, "image_unavailable")
                        : WikiTexts.text(this.language, "image_loading"),
                    drawX + drawWidth / 2,
                    top + drawHeight / 2 - this.font.lineHeight / 2,
                    0xFFD9E2EC
                );
            }
        }

        return top + drawHeight;
    }

    private boolean shouldShowImageArea() {
        return this.image != null || (this.hasImageRequest && !this.imageLoadFailed);
    }

    private int imageHeight(int drawWidth) {
        if (this.image == null || this.image.width() <= 0 || this.image.height() <= 0) {
            return Math.clamp(drawWidth * 9 / 16, IMAGE_MIN_HEIGHT, IMAGE_MAX_HEIGHT);
        }

        int scaled = Math.max(
            1,
            Math.round(drawWidth * (float) this.image.height() / this.image.width())
        );
        return Math.clamp(scaled, IMAGE_MIN_HEIGHT, IMAGE_MAX_HEIGHT);
    }
}
