package io.github.piscescup.mcwiki.api.mediawiki;

import io.github.piscescup.mcwiki.wiki.model.WikiRequest;
import io.github.piscescup.mcwiki.wiki.model.WikiSearchModeConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates HTTP requests for the MediaWiki Action API.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class MediaWikiRequestFactory {

    @NotNull
    private final URI apiEndpoint;

    @NotNull
    private final String userAgent;

    /**
     * Creates a MediaWiki request factory.
     *
     * @param apiEndpoint the MediaWiki Action API endpoint
     * @param userAgent   the HTTP user agent
     */
    public MediaWikiRequestFactory(
        @NotNull URI apiEndpoint,
        @NotNull String userAgent
    ) {
        this.apiEndpoint = apiEndpoint;
        this.userAgent = userAgent;
    }

    /**
     * Creates an HTTP request for the specified Wiki search.
     *
     * @param request the Wiki request
     * @return the HTTP request
     */
    @NotNull
    public HttpRequest createSearchRequest(
        @NotNull WikiRequest request
    ) {
        Map<String, String> parameters =
            new LinkedHashMap<>();

        parameters.put("action", "query");
        parameters.put("list", "search");
        parameters.put("srsearch", request.target());
        parameters.put("srnamespace", "0");
        parameters.put(
            "srlimit",
            Integer.toString(request.resultLimit())
        );
        parameters.put(
            "sroffset",
            Integer.toString(request.offset())
        );
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        addSearchMode(
            parameters,
            request.searchMode()
        );

        URI requestUri = createRequestUri(parameters);

        return HttpRequest.newBuilder(requestUri)
            .header("Accept", "application/json")
            .header("User-Agent", this.userAgent)
            .timeout(request.timeout())
            .GET()
            .build();
    }

    /**
     * Creates an HTTP request for a page's complete plain-text content.
     *
     * @param pageId  the MediaWiki page identifier
     * @param timeout the request timeout
     * @return the HTTP request
     */
    @NotNull
    public HttpRequest createPageSummaryRequest(
        long pageId,
        @NotNull java.time.Duration timeout
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("action", "query");
        parameters.put("prop", "extracts|info|pageimages");
        parameters.put("pageids", Long.toString(pageId));
        parameters.put("explaintext", "1");
        parameters.put("inprop", "url");
        parameters.put("piprop", "thumbnail|original");
        parameters.put("pithumbsize", "640");
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        return HttpRequest.newBuilder(createRequestUri(parameters))
            .header("Accept", "application/json")
            .header("User-Agent", this.userAgent)
            .timeout(timeout)
            .GET()
            .build();
    }

    /**
     * Creates an HTTP request for parsed HTML used by GUI table formatting.
     *
     * @param pageId  the MediaWiki page identifier
     * @param timeout the request timeout
     * @return the HTTP request
     */
    @NotNull
    public HttpRequest createPageHtmlRequest(
        long pageId,
        @NotNull java.time.Duration timeout
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("action", "parse");
        parameters.put("pageid", Long.toString(pageId));
        parameters.put("prop", "text");
        parameters.put("format", "json");
        parameters.put("formatversion", "2");

        return HttpRequest.newBuilder(createRequestUri(parameters))
            .header("Accept", "application/json")
            .header("User-Agent", this.userAgent)
            .timeout(timeout)
            .GET()
            .build();
    }

    private static void addSearchMode(
        @NotNull Map<String, String> parameters,
        @NotNull WikiSearchModeConfig searchMode
    ) {
        switch (searchMode) {
            case AUTO -> {
                // Let the MediaWiki search engine choose the strategy.
            }

            case TITLE ->
                parameters.put("srwhat", "title");

            case FULL_TEXT ->
                parameters.put("srwhat", "text");
        }
    }

    @NotNull
    private URI createRequestUri(
        @NotNull Map<String, String> parameters
    ) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> parameter
            : parameters.entrySet()) {
            if (!query.isEmpty()) {
                query.append('&');
            }

            query.append(encode(parameter.getKey()))
                .append('=')
                .append(encode(parameter.getValue()));
        }

        return URI.create(
            this.apiEndpoint + "?" + query
        );
    }

    @NotNull
    private static String encode(@NotNull String value) {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        );
    }
}
