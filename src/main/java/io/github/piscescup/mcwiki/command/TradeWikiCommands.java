package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TradeWikiCommands {
    public static final LiteralArgumentBuilder<FabricClientCommandSource> TRADE_COMMANDS =
        WikiCategoryCommand.create(WikiCategory.TRADING);
}
