package com.mmcauth;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class AuthLogic {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static void handleJoinServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getCurrentServerEntry() == null) {
            Mmcauth.LOGGER.warn("Player or Server Info is null, cannot authenticate.");
            return;
        }

        long startTime = System.currentTimeMillis();
        boolean authenticated = false;
        int attempt = 1;

        sendMessageToPlayer("§eStarting authentication process...");

        while (System.currentTimeMillis() - startTime < 7000) {
            sendMessageToPlayer(String.format("§eAuthentication attempt %d...", attempt));
            
            authenticated = sendVerificationRequest().join();

            if (authenticated) {
                sendMessageToPlayer("§aSuccessfully authenticated!");
                break;
            }

            if (System.currentTimeMillis() - startTime < 6000) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Mmcauth.LOGGER.error("Auth retry thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            attempt++;
        }

        if (!authenticated) {
            sendMessageToPlayer("§cAuthentication failed after multiple attempts.");
        }
    }

    private static CompletableFuture<Boolean> sendVerificationRequest() {
        long timestamp = System.currentTimeMillis();
        String playerName = MinecraftClient.getInstance().getSession().getUsername();
        String serverHash = mmcHash(timestamp);

        String payload = String.format("{\"messageType\":\"LAUNCHER_AUTH\",\"secret\":\"%s\",\"identifier\":\"%s\",\"timestamp\":%d,\"serverHash\":\"%s\",\"version\":\"%s\",\"playerName\":\"%s\"}",
                Mmcauth.config.getLauncherSecret(),
                Mmcauth.config.getLauncherIdentifier(),
                timestamp,
                serverHash,
                Mmcauth.config.getLauncherVersion(),
                playerName
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://57.128.198.223:25581/launcher/verify"))
                .header("Content-Type", "application/json")
                .header("User-Agent", "MesterMC-Auth-Mod/1.0.0")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Mmcauth.LOGGER.info("Verification successful.");
                        return true;
                    } else {
                        Mmcauth.LOGGER.error("Verification failed. Status: {}, Body: {}", response.statusCode(), response.body());
                        return false;
                    }
                })
                .exceptionally(e -> {
                    Mmcauth.LOGGER.error("Error during verification request", e);
                    return false;
                });
    }

    private static void sendMessageToPlayer(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a[MMC Auth] " + message), false);
        }
    }

    private static String mmcHash(long timestamp) {
        Config config = Mmcauth.config;
        String str = String.format("%s|%s|%d|%s|mestermc_verification_salt_2024",
                config.getLauncherSecret(),
                config.getLauncherIdentifier(),
                timestamp,
                config.getLauncherVersion()
        );

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            Mmcauth.LOGGER.error("Could not generate SHA-256 hash", e);
            throw new RuntimeException(e);
        }
    }
}
