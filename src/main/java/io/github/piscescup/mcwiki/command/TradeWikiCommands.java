package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.constant.WikiCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TradeWikiCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> TRADE_COMMANDS = Commands.literal(WikiCategory.TRADING.id());
}
