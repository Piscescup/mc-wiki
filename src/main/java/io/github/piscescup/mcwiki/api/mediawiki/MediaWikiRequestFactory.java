package io.github.piscescup.mcwiki.api.mediawiki;

import io.github.piscescup.mcwiki.wiki.model.WikiRequest;
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