package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class SettingsCommand {
    public static final LiteralArgumentBuilder<CommandSourceStack> SETTINGS_COMMANDS =
        Commands.literal("settings");

}
