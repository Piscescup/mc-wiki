package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.mcwiki.wiki.WikiCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since
 */
public final class BiomeWikiCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> BIOME_COMMANDS = Commands.literal(WikiCategory.BIOME.id());
}
