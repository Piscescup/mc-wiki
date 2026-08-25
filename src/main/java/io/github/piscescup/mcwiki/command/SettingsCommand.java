package io.github.piscescup.mcwiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class SettingsCommand {
    public static final LiteralArgumentBuilder<FabricClientCommandSource> SETTINGS_COMMANDS =
        literal("settings")
            .then(literal("lang")
                .then(literal("set")
                    .then(literal("en_us")
                        .executes(context -> WikiCommandExecutor.setLanguage(
                            context.getSource(), "en_us"
                        )))
                    .then(literal("zh_cn")
                        .executes(context -> WikiCommandExecutor.setLanguage(
                            context.getSource(), "zh_cn"
                        )))));

}
