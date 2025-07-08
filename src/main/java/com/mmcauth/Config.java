package com.mmcauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "mmcauth_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    private String launcherSecret = "MesterMC_Secret_2024_v2.0_OnlyOfficialLauncher_HibridSupport";
    private String launcherIdentifier = "MesterMC-Official-Launcher-v2.0-Hibrid";
    private String launcherVersion = "2.0-hibrid";


    public String getLauncherSecret() { return launcherSecret; }
    public String getLauncherIdentifier() { return launcherIdentifier; }
    public String getLauncherVersion() { return launcherVersion; }





    public static Config load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                Mmcauth.LOGGER.error("Could not read MMC Auth config", e);
            }
        }
        return new Config();
    }
}
