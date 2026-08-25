package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class WikiCommands {
    private WikiCommands() {}

    public static final LiteralArgumentBuilder<FabricClientCommandSource> ROOT_COMMAND =
        literal("mc-wiki");

    public static final LiteralArgumentBuilder<FabricClientCommandSource> COMMANDS =
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
            .then(TradeWikiCommands.TRADE_COMMANDS)
            .then(SettingsCommand.SETTINGS_COMMANDS);

    public static void registerMCWikiCommands(
        CommandDispatcher<FabricClientCommandSource> dispatcher,
        CommandBuildContext context
    ) {
        dispatcher.register(COMMANDS);
    }
}
