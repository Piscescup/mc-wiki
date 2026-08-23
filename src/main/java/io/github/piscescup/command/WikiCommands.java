package io.github.piscescup.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class WikiCommands {
    private WikiCommands() {}

    public static final LiteralArgumentBuilder<CommandSourceStack> ROOT_COMMAND = Commands.literal("mc-wiki");

    public static final LiteralArgumentBuilder<CommandSourceStack> COMMANDS =
        ROOT_COMMAND
            .then(BiomeWikiCommands.BIOME_COMMANDS)
            .then(BlockWikiCommands.BLOCK_COMMANDS)
            .then(BrewWikiCommands.BREW_COMMANDS)
            .then(EffectWikiCommands.EFFECT_COMMANDS)
            .then(EnchantmentWikiCommands.ENCHANTING_COMMANDS)
            .then(ItemWikiCommands.ITEM_COMMANDS)
            .then(MobWikiCommands.MOB_COMMANDS)
            .then(SmithWikiCommands.SMITH_COMMANDS)
            .then(StructureWikiCommands.STRUCTURE_COMMANDS)
            .then(TradeWikiCommands.TRADE_COMMANDS);

    public static void registerMCWikiCommands(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext context,
        Commands.CommandSelection selection
    ) {
        dispatcher.register(COMMANDS);
    }
}
