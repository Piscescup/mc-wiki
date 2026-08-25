package io.github.piscescup.mcwiki.api.mediawiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.piscescup.mcwiki.exception.WikiRequestException;
import io.github.piscescup.mcwiki.wiki.model.WikiRequest;
import io.github.piscescup.mcwiki.wiki.model.WikiPageSummary;
import io.github.piscescup.mcwiki.wiki.model.WikiSearchResult;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A client for accessing complete Minecraft Wiki page text through the
 * MediaWiki API.
 *
 * <p>All requests are executed asynchronously and do not block the
 * Minecraft client thread.</p>
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public final class MediaWikiClient {

    private static final String USER_AGENT =
        "MC-Wiki/1.0.0";

    private final HttpClient httpClient;

    private final MediaWikiRequestFactory requestFactory;

    public MediaWikiClient(@NotNull URI apiEndpoint) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        this.requestFactory =
            new MediaWikiRequestFactory(
                apiEndpoint,
                USER_AGENT
            );
    }

    @NotNull
    public CompletableFuture<List<WikiSearchResult>> search(
        @NotNull WikiRequest request
    ) {
        HttpRequest httpRequest =
            this.requestFactory.createSearchRequest(request);

        return this.httpClient.sendAsync(
                httpRequest,
                HttpResponse.BodyHandlers.ofString(
                    StandardCharsets.UTF_8
                )
            )
            .thenApply(this::requireSuccessfulResponse)
            .thenApply(this::parseSearchResults);
    }

    @NotNull
    public CompletableFuture<WikiPageSummary> fetchPageSummary(
        long pageId,
        @NotNull Duration timeout
    ) {
        HttpRequest summaryRequest =
            this.requestFactory.createPageSummaryRequest(pageId, timeout);
        HttpRequest htmlRequest =
            this.requestFactory.createPageHtmlRequest(pageId, timeout);

        CompletableFuture<WikiPageSummary> summaryFuture = this.httpClient.sendAsync(
                summaryRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            )
            .thenApply(this::requireSuccessfulResponse)
            .thenApply(this::parsePageSummary);

        CompletableFuture<String> htmlFuture = this.httpClient.sendAsync(
                htmlRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            )
            .thenApply(this::requireSuccessfulResponse)
            .thenApply(this::parsePageHtml);

        return summaryFuture.thenCombine(
            htmlFuture,
            (summary, html) -> new WikiPageSummary(
                summary.pageId(),
                summary.title(),
                summary.extract(),
                summary.pageUrl(),
                html
            )
        );
    }

    @NotNull
    private String requireSuccessfulResponse(
        @NotNull HttpResponse<String> response
    ) {
        if (response.statusCode() < 200 ||
            response.statusCode() >= 300) {
            throw new WikiRequestException(
                "Minecraft Wiki returned HTTP status: "
                    + response.statusCode()
            );
        }

        return response.body();
    }

    @NotNull
    private List<WikiSearchResult> parseSearchResults(
        @NotNull String responseBody
    ) {
        try {
            JsonObject root = JsonParser
                .parseString(responseBody)
                .getAsJsonObject();

            if (root.has("error")) {
                JsonObject error =
                    root.getAsJsonObject("error");

                String errorMessage = error.has("info")
                    ? error.get("info").getAsString()
                    : "Unknown MediaWiki API error.";

                throw new WikiRequestException(
                    errorMessage
                );
            }

            JsonObject query = root.getAsJsonObject("query");

            if (query == null) {
                return List.of();
            }

            JsonArray search =
                query.getAsJsonArray("search");

            if (search == null || search.isEmpty()) {
                return List.of();
            }

            List<WikiSearchResult> results =
                new ArrayList<>(search.size());

            for (JsonElement element : search) {
                JsonObject page =
                    element.getAsJsonObject();

                if (!page.has("pageid") ||
                    !page.has("title")) {
                    continue;
                }

                results.add(
                    new WikiSearchResult(
                        page.get("pageid").getAsLong(),
                        page.get("title").getAsString()
                    )
                );
            }

            return List.copyOf(results);
        } catch (WikiRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new WikiRequestException(
                "Failed to parse the MediaWiki search response.",
                exception
            );
        }
    }

    @NotNull
    private WikiPageSummary parsePageSummary(@NotNull String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            if (root.has("error")) {
                throw new WikiRequestException(
                    root.getAsJsonObject("error").get("info").getAsString()
                );
            }

            JsonArray pages = root.getAsJsonObject("query").getAsJsonArray("pages");
            if (pages == null || pages.isEmpty()) {
                throw new WikiRequestException("The requested Wiki page was not found.");
            }

            JsonObject page = pages.get(0).getAsJsonObject();
            return new WikiPageSummary(
                page.get("pageid").getAsLong(),
                page.get("title").getAsString(),
                page.has("extract") ? page.get("extract").getAsString() : "",
                page.has("fullurl") ? page.get("fullurl").getAsString() : "",
                ""
            );
        } catch (WikiRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new WikiRequestException(
                "Failed to parse the MediaWiki page response.",
                exception
            );
        }
    }

    @NotNull
    private String parsePageHtml(@NotNull String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            if (root.has("error")) {
                throw new WikiRequestException(
                    root.getAsJsonObject("error").get("info").getAsString()
                );
            }

            JsonObject parse = root.getAsJsonObject("parse");
            return parse != null && parse.has("text")
                ? parse.get("text").getAsString()
                : "";
        } catch (WikiRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new WikiRequestException(
                "Failed to parse the MediaWiki HTML response.",
                exception
            );
        }
    }
}
