package com.mmcauth;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mmcauth implements ModInitializer {
	public static final String MOD_ID = "mmcauth";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Config config;

	@Override
	public void onInitialize() {
		config = Config.load();



		LOGGER.info("MMC Auth initialized.");
	}
}
