package com.solarClicker.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.solarClicker.domain.core.BigNumber;

class GameStateTest {

    @Test
    void clickingSunIncreasesSolarCoins() {
        GameState state = GameState.newGame();
        BigNumber gained = state.clickSun();
        assertEquals(gained, state.getSolarCoins());
        assertTrue(gained.compareTo(BigNumber.ZERO) > 0);
    }

    @Test
    void clickingLockedPlanetThrows() {
        GameState state = GameState.newGame();
        assertThrows(IllegalStateException.class, () -> state.clickPlanet("mercury"));
    }

    @Test
    void clickingUnknownPlanetThrows() {
        GameState state = GameState.newGame();
        assertThrows(IllegalArgumentException.class, () -> state.clickPlanet("saturn"));
    }

    @Test
    void unlockingPlanetFailsWithoutEnoughSolarCoins() {
        GameState state = GameState.newGame();
        assertFalse(state.unlockPlanet("mercury"));
        assertFalse(state.getPlanet("mercury").isUnlocked());
    }

    @Test
    void unlockingPlanetSucceedsAndDeductsCost() {
        GameState state = GameState.newGame();
        BigNumber unlockCost = state.getPlanet("mercury").getUnlockCost();
        while (state.getSolarCoins().compareTo(unlockCost) < 0) {
            state.clickSun();
        }
        BigNumber balanceBeforeUnlock = state.getSolarCoins();

        assertTrue(state.unlockPlanet("mercury"));
        assertTrue(state.getPlanet("mercury").isUnlocked());
        assertEquals(balanceBeforeUnlock.subtract(unlockCost), state.getSolarCoins());
    }

    @Test
    void unlockingAlreadyUnlockedPlanetReturnsFalse() {
        GameState state = GameState.newGame();
        BigNumber unlockCost = state.getPlanet("mercury").getUnlockCost();
        while (state.getSolarCoins().compareTo(unlockCost) < 0) {
            state.clickSun();
        }
        assertTrue(state.unlockPlanet("mercury"));
        assertFalse(state.unlockPlanet("mercury")); // segunda tentativa
    }

    @Test
    void clickingUnlockedPlanetIncreasesLocalCurrencyAndExportsToSolarCoins() {
        GameState state = GameState.newGame();
        BigNumber unlockCost = state.getPlanet("mercury").getUnlockCost();
        while (state.getSolarCoins().compareTo(unlockCost) < 0) {
            state.clickSun();
        }
        state.unlockPlanet("mercury");
        BigNumber solarCoinsBefore = state.getSolarCoins();

        BigNumber gained = state.clickPlanet("mercury");

        assertEquals(gained, state.getPlanet("mercury").getLocalCurrency());
        assertTrue(state.getSolarCoins().compareTo(solarCoinsBefore) > 0); // exportação aumentou o saldo geral
    }

    @Test
    void tickOnlyAffectsUnlockedPlanets() {
        GameState state = GameState.newGame();
        state.tick(10.0); // nenhum planeta desbloqueado ainda
        assertTrue(state.getPlanet("mercury").getLocalCurrency().isZero());
        assertTrue(state.getSolarCoins().isZero());
    }

    @Test
    void tickAddsIdleProductionForUnlockedPlanet() {
        GameState state = GameState.newGame();
        BigNumber unlockCost = state.getPlanet("mercury").getUnlockCost();
        while (state.getSolarCoins().compareTo(unlockCost) < 0) {
            state.clickSun();
        }
        state.unlockPlanet("mercury");

        state.tick(10.0);

        assertFalse(state.getPlanet("mercury").getLocalCurrency().isZero());
    }

    @Test
    void buyingGeneralShopUpgradeFailsWithoutEnoughSolarCoins() {
        GameState state = GameState.newGame();
        assertFalse(state.buyGeneralShopSunClickUpgrade());
        assertEquals(0, state.getGeneralShop().getSunClickUpgradeLevel());
    }

    @Test
    void buyingGeneralShopUpgradeIncreasesSunClickPowerAndLevel() {
        GameState state = GameState.newGame();
        BigNumber cost = state.getGeneralShop().costForNextSunClickUpgrade();
        while (state.getSolarCoins().compareTo(cost) < 0) {
            state.clickSun();
        }
        BigNumber clickPowerBefore = state.clickSun(); // referência do poder de clique atual (antes do upgrade)

        assertTrue(state.buyGeneralShopSunClickUpgrade());
        assertEquals(1, state.getGeneralShop().getSunClickUpgradeLevel());

        BigNumber clickPowerAfter = state.clickSun();
        assertTrue(clickPowerAfter.compareTo(clickPowerBefore) > 0);
    }
}