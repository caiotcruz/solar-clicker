package com.solarClicker.save;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.solarClicker.domain.GameState;

class SaveManagerTest {

    @Test
    void saveAndLoadRoundTripPreservesState(@TempDir Path tempDir) throws IOException {
        GameState original = GameState.newGame();

        // avança o estado: desbloqueia mercúrio, clica nele, compra upgrade do Sol
        while (original.getSolarCoins().compareTo(original.getPlanet("mercury").getUnlockCost()) < 0) {
            original.clickSun();
        }
        original.unlockPlanet("mercury");
        original.clickPlanet("mercury");
        while (original.getSolarCoins().compareTo(original.getGeneralShop().costForNextSunClickUpgrade()) < 0) {
            original.clickSun();
        }
        original.buyGeneralShopSunClickUpgrade();

        Path saveFile = tempDir.resolve("save.properties");
        SaveManager.saveToFile(original, saveFile);

        GameState restored = GameState.newGame();
        SaveManager.loadFromFile(saveFile, restored);

        assertEquals(original.getSolarCoins(), restored.getSolarCoins());
        assertEquals(original.getGeneralShop().getSunClickUpgradeLevel(), restored.getGeneralShop().getSunClickUpgradeLevel());
        assertEquals(original.getPlanet("mercury").isUnlocked(), restored.getPlanet("mercury").isUnlocked());
        assertEquals(original.getPlanet("mercury").getLocalCurrency(), restored.getPlanet("mercury").getLocalCurrency());
        assertEquals(original.getPlanet("venus").isUnlocked(), restored.getPlanet("venus").isUnlocked());
    }

    @Test
    void loadRejectsUnsupportedSchemaVersion(@TempDir Path tempDir) throws IOException {
        Path saveFile = tempDir.resolve("bad-save.properties");
        Properties props = new Properties();
        props.setProperty("schemaVersion", "999");
        try (var out = Files.newOutputStream(saveFile)) {
            props.store(out, "bad save");
        }

        GameState state = GameState.newGame();
        assertThrows(IllegalStateException.class, () -> SaveManager.loadFromFile(saveFile, state));
    }
}