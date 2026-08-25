package io.github.piscescup.mcwiki;

import io.github.piscescup.mcwiki.command.WikiCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/**
 * Registers the client-side Wiki command and GUI.
 */
public final class MinecraftWikiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(
            WikiCommands::registerMCWikiCommands
        );
    }
}
