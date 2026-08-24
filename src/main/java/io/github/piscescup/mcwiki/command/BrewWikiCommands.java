package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.constant.WikiCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since
 */
public final class BrewWikiCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> BREW_COMMANDS = Commands.literal(WikiCategory.BREWING.id());
}
