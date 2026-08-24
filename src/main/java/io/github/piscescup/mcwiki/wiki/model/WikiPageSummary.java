package io.github.piscescup.mcwiki.wiki.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the summary of a Minecraft Wiki page.
 *
 * @param pageId the MediaWiki page identifier
 * @param title  the page title
 * @param extract the plain-text page summary
 * @param pageUrl the URL of the original Wiki page
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public record WikiPageSummary(
    long pageId,
    @NotNull String title,
    @NotNull String extract,
    @NotNull String pageUrl
) {
}