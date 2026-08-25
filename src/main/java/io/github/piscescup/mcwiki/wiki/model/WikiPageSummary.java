package io.github.piscescup.mcwiki.wiki.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the plain-text content of a Minecraft Wiki page.
 *
 * @param pageId the MediaWiki page identifier
 * @param title  the page title
 * @param extract the complete plain-text page content
 * @param pageUrl the URL of the original Wiki page
 * @param html the parsed page HTML used to recover table structure
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public record WikiPageSummary(
    long pageId,
    @NotNull String title,
    @NotNull String extract,
    @NotNull String pageUrl,
    @NotNull String html
) {
}
