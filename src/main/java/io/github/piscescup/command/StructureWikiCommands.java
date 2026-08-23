package io.github.piscescup.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.constant.WikiCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class StructureWikiCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> STRUCTURE_COMMANDS = Commands.literal(WikiCategory.STRUCTURE.id());
}
