package com.mmcauth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;


public class MmcauthClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Event for handling authentication on server join
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			new Thread(() -> {
				try {
					Thread.sleep(1000);
					AuthLogic.handleJoinServer();
				} catch (InterruptedException e) {
					Mmcauth.LOGGER.error("Auth thread interrupted", e);
				}
			}).start();
		});


	}
}
