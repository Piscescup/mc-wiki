package io.github.piscescup.mcwiki.wiki.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a page returned by a Minecraft Wiki search.
 *
 * @param pageId the MediaWiki page identifier
 * @param title  the page title
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public record WikiSearchResult(
    long pageId,
    @NotNull String title
) {
}