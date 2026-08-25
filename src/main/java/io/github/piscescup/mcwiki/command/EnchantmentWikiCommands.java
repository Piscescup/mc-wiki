package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 *
 * @author REN YuanTong
 * @since
 */
public final class EnchantmentWikiCommands {
    public static final LiteralArgumentBuilder<FabricClientCommandSource> ENCHANTING_COMMANDS =
        WikiCategoryCommand.create(WikiCategory.ENCHANTING);
}
