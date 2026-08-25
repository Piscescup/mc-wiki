package io.github.piscescup.mcwiki.wiki.model;

import io.github.piscescup.mcwiki.config.MCWikiSettings;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Represents a Wiki search requested by a player.
 *
 * <p>This class contains the information required to construct a
 * MediaWiki search request. It does not perform network operations
 * or depend on a specific MediaWiki API endpoint.</p>
 *
 * @param category    the Wiki category selected by the player
 * @param target      the target to search for
 * @param searchMode  the search mode
 * @param resultLimit the maximum number of search results
 * @param offset      the search result offset
 * @param timeout     the HTTP request timeout
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public record WikiRequest(
    @NotNull WikiCategory category,
    @NotNull String target,
    @NotNull WikiSearchModeConfig searchMode,
    int resultLimit,
    int offset,
    @NotNull Duration timeout
) {

    /**
     * Validates and normalizes the request.
     */
    public WikiRequest {
        target = target.trim();

        if (target.isEmpty()) {
            throw new IllegalArgumentException(
                "The Wiki search target must not be empty."
            );
        }

        if (resultLimit < 1 || resultLimit > 20) {
            throw new IllegalArgumentException(
                "The search result limit must be between 1 and 20."
            );
        }

        if (offset < 0) {
            throw new IllegalArgumentException(
                "The search result offset must not be negative."
            );
        }

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException(
                "The request timeout must be positive."
            );
        }
    }

    /**
     * Creates a Wiki request using the current settings.
     *
     * @param category the selected Wiki category
     * @param target   the search target
     * @param settings the current MC Wiki settings
     * @return the new Wiki request
     */
    @NotNull
    public static WikiRequest fromCommand(
        @NotNull WikiCategory category,
        @NotNull String target,
        @NotNull MCWikiSettings settings
    ) {
        return new WikiRequest(
            category,
            target,
            settings.searchMode(),
            settings.searchResultLimit(),
            0,
            Duration.ofSeconds(
                settings.requestTimeoutSeconds()
            )
        );
    }

    /**
     * Returns a request for the next page of search results.
     *
     * @return the next-page request
     */
    @NotNull
    public WikiRequest nextPage() {
        return new WikiRequest(
            this.category,
            this.target,
            this.searchMode,
            this.resultLimit,
            Math.addExact(this.offset, this.resultLimit),
            this.timeout
        );
    }

    /**
     * Returns a request for the previous page of search results.
     *
     * @return the previous-page request
     */
    @NotNull
    public WikiRequest previousPage() {
        return new WikiRequest(
            this.category,
            this.target,
            this.searchMode,
            this.resultLimit,
            Math.max(0, this.offset - this.resultLimit),
            this.timeout
        );
    }
}
