package io.github.piscescup.mcwiki.api.mediawiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.piscescup.mcwiki.exception.WikiRequestException;
import io.github.piscescup.mcwiki.wiki.model.WikiPageSummary;
import io.github.piscescup.mcwiki.wiki.model.WikiSearchResult;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A client for accessing the Minecraft Wiki through the MediaWiki API.
 *
 * <p>All requests are executed asynchronously and do not block the
 * Minecraft client thread.</p>
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public final class MediaWikiClient {

    private static final URI EN_US_API_ENDPOINT =
        URI.create("https://minecraft.wiki/api.php");

    private static final URI ZH_CN_API_ENDPOINT =
        URI.create("https://minecraft.wiki/api.php");

    private static final int DEFAULT_SEARCH_LIMIT = 10;

    @NotNull
    private final URI apiEndpoint;

    @NotNull
    private final HttpClient httpClient;

    /**
     * Creates a client for the English Minecraft Wiki.
     */
    public MediaWikiClient() {
        this(EN_US_API_ENDPOINT);
    }

    /**
     * Creates a client using the specified MediaWiki API endpoint.
     *
     * @param apiEndpoint the MediaWiki API endpoint
     */
    public MediaWikiClient(@NotNull URI apiEndpoint) {
        this.apiEndpoint = apiEndpoint;

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    /**
     * Searches the Minecraft Wiki using the specified query.
     *
     * @param query the search query
     * @return a future containing the matching pages
     */
    @NotNull
    public CompletableFuture<List<WikiSearchResult>> search(
        @NotNull String query
    ) {
        return search(query, DEFAULT_SEARCH_LIMIT);
    }

    /**
     * Searches the Minecraft Wiki using the specified query.
     *
     * @param query the search query
     * @param limit the maximum number of results
     * @return a future containing the matching pages
     */
    @NotNull
    public CompletableFuture<List<WikiSearchResult>> search(
        @NotNull String query,
        int limit
    ) {
        if (query.isBlank()) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("The search query must not be blank.")
            );
        }

        if (limit < 1 || limit > 50) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException(
                    "The search result limit must be between 1 and 50."
                )
            );
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("action", "query");
        parameters.put("list", "search");
        parameters.put("srsearch", normalizeQuery(query));
        parameters.put("srnamespace", "0");
        parameters.put("srlimit", Integer.toString(limit));
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        return send(parameters)
            .thenApply(this::parseSearchResults);
    }

    /**
     * Retrieves the introductory summary of a Wiki page.
     *
     * @param pageId the MediaWiki page identifier
     * @return a future containing the page summary
     */
    @NotNull
    public CompletableFuture<WikiPageSummary> getSummary(long pageId) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("action", "query");
        parameters.put("prop", "extracts|info");
        parameters.put("pageids", Long.toString(pageId));
        parameters.put("exintro", "1");
        parameters.put("explaintext", "1");
        parameters.put("exchars", "1200");
        parameters.put("inprop", "url");
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        return send(parameters)
            .thenApply(this::parsePageSummary);
    }

    @NotNull
    private CompletableFuture<String> send(
        @NotNull Map<String, String> parameters
    ) {
        URI requestUri = createRequestUri(parameters);

        HttpRequest request = HttpRequest.newBuilder(requestUri)
            .header("Accept", "application/json")
            .header("User-Agent", "MC-Wiki/1.0.0")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        return this.httpClient.sendAsync(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        ).thenApply(response -> {
            if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {
                throw new WikiRequestException(
                    "Minecraft Wiki returned HTTP status: "
                        + response.statusCode()
                );
            }

            return response.body();
        });
    }

    @NotNull
    private List<WikiSearchResult> parseSearchResults(
        @NotNull String responseBody
    ) {
        JsonObject root = parseResponse(responseBody);
        JsonObject query = root.getAsJsonObject("query");
        JsonArray searchResults = query.getAsJsonArray("search");

        List<WikiSearchResult> results =
            new ArrayList<>(searchResults.size());

        for (JsonElement element : searchResults) {
            JsonObject result = element.getAsJsonObject();

            results.add(new WikiSearchResult(
                result.get("pageid").getAsLong(),
                result.get("title").getAsString()
            ));
        }

        return List.copyOf(results);
    }

    @NotNull
    private WikiPageSummary parsePageSummary(
        @NotNull String responseBody
    ) {
        JsonObject root = parseResponse(responseBody);
        JsonObject query = root.getAsJsonObject("query");
        JsonArray pages = query.getAsJsonArray("pages");

        if (pages == null || pages.isEmpty()) {
            throw new WikiRequestException("The Wiki page was not found.");
        }

        JsonObject page = pages.get(0).getAsJsonObject();

        if (page.has("missing")) {
            throw new WikiRequestException("The Wiki page was not found.");
        }

        return new WikiPageSummary(
            page.get("pageid").getAsLong(),
            page.get("title").getAsString(),
            getOptionalString(page, "extract"),
            getOptionalString(page, "fullurl")
        );
    }

    @NotNull
    private JsonObject parseResponse(@NotNull String responseBody) {
        try {
            JsonObject root =
                JsonParser.parseString(responseBody).getAsJsonObject();

            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");

                throw new WikiRequestException(
                    getOptionalString(error, "info")
                );
            }

            return root;
        } catch (WikiRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new WikiRequestException(
                "Failed to parse the Minecraft Wiki response.",
                exception
            );
        }
    }

    @NotNull
    private URI createRequestUri(
        @NotNull Map<String, String> parameters
    ) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!queryBuilder.isEmpty()) {
                queryBuilder.append('&');
            }

            queryBuilder
                .append(encode(entry.getKey()))
                .append('=')
                .append(encode(entry.getValue()));
        }

        return URI.create(
            this.apiEndpoint + "?" + queryBuilder
        );
    }

    @NotNull
    private static String normalizeQuery(@NotNull String query) {
        return query.trim().replace('_', ' ');
    }

    @NotNull
    private static String encode(@NotNull String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @NotNull
    private static String getOptionalString(
        @NotNull JsonObject object,
        @NotNull String property
    ) {
        JsonElement element = object.get(property);

        if (element == null || element.isJsonNull()) {
            return "";
        }

        return element.getAsString();
    }
}