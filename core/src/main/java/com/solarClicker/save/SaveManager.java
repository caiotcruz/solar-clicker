package com.solarClicker.save;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import com.solarClicker.domain.GameState;
import com.solarClicker.domain.core.BigNumber;

public final class SaveManager {

    private static final int CURRENT_SCHEMA_VERSION = 1;
    private static final List<String> PLANET_IDS = List.of("mercury", "venus", "earth");

    private SaveManager() {}

    public static Properties toProperties(GameState state) {
        Properties props = new Properties();
        props.setProperty("schemaVersion", String.valueOf(CURRENT_SCHEMA_VERSION));
        props.setProperty("savedAtEpochMillis", String.valueOf(System.currentTimeMillis()));
        props.setProperty("solarCoins", state.getSolarCoins().toSaveString());
        props.setProperty("sunClickUpgradeLevel", String.valueOf(state.getGeneralShop().getSunClickUpgradeLevel()));

        for (String id : PLANET_IDS) {
            var planet = state.getPlanet(id);
            props.setProperty("planet." + id + ".unlocked", String.valueOf(planet.isUnlocked()));
            props.setProperty("planet." + id + ".localCurrency", planet.getLocalCurrency().toSaveString());
        }
        return props;
    }

    public static void applyProperties(Properties props, GameState state) {
        int schemaVersion = Integer.parseInt(props.getProperty("schemaVersion", "1"));
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalStateException(
                "Versão de save não suportada nesta fase: " + schemaVersion
                + " (esperado " + CURRENT_SCHEMA_VERSION + "). Migração será tratada a partir da Fase 2 (doc 6).");
        }

        state.restoreSolarCoins(BigNumber.fromSaveString(props.getProperty("solarCoins", "0:0")));
        state.getGeneralShop().restoreLevel(
            Integer.parseInt(props.getProperty("sunClickUpgradeLevel", "0")), state.getSun());

        for (String id : PLANET_IDS) {
            boolean unlocked = Boolean.parseBoolean(props.getProperty("planet." + id + ".unlocked", "false"));
            BigNumber localCurrency = BigNumber.fromSaveString(props.getProperty("planet." + id + ".localCurrency", "0:0"));
            state.getPlanet(id).restoreState(unlocked, localCurrency);
        }
    }

    public static void saveToFile(GameState state, Path targetFile) throws IOException {
        Properties props = toProperties(state);
        Path tempFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");

        try (OutputStream out = Files.newOutputStream(tempFile)) {
            props.store(out, "Solar Clicker save - schema v" + CURRENT_SCHEMA_VERSION);
        }
        Files.move(tempFile, targetFile,
            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void loadFromFile(Path sourceFile, GameState state) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(sourceFile)) {
            props.load(in);
        }
        applyProperties(props, state);
    }
}