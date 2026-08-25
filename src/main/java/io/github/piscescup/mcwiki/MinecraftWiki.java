package io.github.piscescup.mcwiki;

import net.fabricmc.api.ModInitializer;


import static io.github.piscescup.mcwiki.References.MOD_LOGGER;


public class MinecraftWiki implements ModInitializer {

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		MOD_LOGGER.info("Minecraft Wiki initialized.");
	}

}
