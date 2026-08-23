package io.github.piscescup;

import io.github.piscescup.command.WikiCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;


import static io.github.piscescup.References.MOD_LOGGER;


public class MinecraftWiki implements ModInitializer {

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register(WikiCommands::registerMCWikiCommands);


		MOD_LOGGER.info("Hello Fabric world!");
	}

}
