package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Builds the shared query branch used by every Wiki category command.
 */
public final class WikiCategoryCommand {
    private WikiCategoryCommand() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> create(
        WikiCategory category
    ) {
        return literal(category.id())
            .then(argument("query", StringArgumentType.greedyString())
                .executes(context -> WikiCommandExecutor.search(
                    context.getSource(),
                    category,
                    StringArgumentType.getString(context, "query")
                )));
    }
}
