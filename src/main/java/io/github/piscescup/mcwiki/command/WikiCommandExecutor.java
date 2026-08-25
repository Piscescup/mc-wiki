package io.github.piscescup.mcwiki.command;

import io.github.piscescup.mcwiki.api.mediawiki.MediaWikiClient;
import io.github.piscescup.mcwiki.WikiResultScreen;
import io.github.piscescup.mcwiki.format.WikiHtmlTableParser;
import io.github.piscescup.mcwiki.format.WikiArticleHtmlFormatter;
import io.github.piscescup.mcwiki.format.WikiTable;
import io.github.piscescup.mcwiki.WikiTexts;
import io.github.piscescup.mcwiki.config.MCWikiSettings;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import io.github.piscescup.mcwiki.wiki.model.WikiRequest;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * Executes Wiki searches without blocking Minecraft's render thread.
 */
public final class WikiCommandExecutor {
    private static final MCWikiSettings SETTINGS = MCWikiSettings.load();

    private WikiCommandExecutor() {
    }

    public static MCWikiSettings settings() {
        return SETTINGS;
    }

    public static int search(
        FabricClientCommandSource source,
        WikiCategory category,
        String query
    ) {
        WikiResultScreen screen =
            new WikiResultScreen(category, query, SETTINGS.language());

        // ChatScreen closes itself after command execution, so open our screen
        // from the following client task instead of letting that close overwrite it.
        CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS)
            .execute(() -> source.getClient().execute(
                () -> source.getClient().setScreenAndShow(screen)
            ));

        WikiRequest request = WikiRequest.fromCommand(category, query, SETTINGS);
        MediaWikiClient client = new MediaWikiClient(apiEndpoint(SETTINGS.language()));

        client.search(request)
            .thenCompose(results -> {
                if (results.isEmpty()) {
                    return java.util.concurrent.CompletableFuture.failedFuture(
                        new IllegalStateException(WikiTexts.text(
                            SETTINGS.language(), "no_results"
                        ))
                    );
                }
                return client.fetchPageSummary(results.getFirst().pageId(), request.timeout());
            })
            .whenComplete((summary, throwable) -> {
                List<WikiTable> tables = throwable == null
                    ? WikiHtmlTableParser.parse(summary.html())
                    : List.of();
                Component article = throwable == null
                    ? WikiArticleHtmlFormatter.format(
                        summary.extract(),
                        summary.html(),
                        summary.pageUrl(),
                        SETTINGS.language()
                    )
                    : Component.empty();
                source.getClient().execute(() -> {
                    if (throwable == null) {
                        screen.showSummary(summary, article, tables);
                    } else {
                        Throwable cause = throwable instanceof CompletionException
                            && throwable.getCause() != null
                            ? throwable.getCause()
                            : throwable;
                        screen.showError(cause.getMessage());
                    }
                });
            });

        return 1;
    }

    public static int setLanguage(
        FabricClientCommandSource source,
        String language
    ) {
        try {
            SETTINGS.setLanguage(language);
            SETTINGS.save();
            source.sendFeedback(Component.literal(
                WikiTexts.text(language, "language_set")
            ));
            return 1;
        } catch (Exception exception) {
            source.sendError(Component.literal(exception.getMessage()));
            return 0;
        }
    }

    private static URI apiEndpoint(String language) {
        return URI.create(language.equals("zh_cn")
            ? "https://zh.minecraft.wiki/api.php"
            : "https://minecraft.wiki/api.php");
    }
}
