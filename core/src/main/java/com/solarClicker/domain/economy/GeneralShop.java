package com.solarClicker.domain.economy;

import java.util.Optional;

import com.solarClicker.domain.core.BigNumber;
import com.solarClicker.domain.planets.Sun;

public final class GeneralShop {

    private static final BigNumber SUN_CLICK_UPGRADE_BASE_COST = BigNumber.of(10.0);
    private static final double SUN_CLICK_UPGRADE_GROWTH_RATE = 1.12;
    private static final BigNumber SUN_CLICK_UPGRADE_INCREMENT = BigNumber.of(1.0);

    private int sunClickUpgradeLevel = 0;

    public int getSunClickUpgradeLevel() {
        return sunClickUpgradeLevel;
    }

    public BigNumber costForNextSunClickUpgrade() {
        return CostFormula.exponentialCost(SUN_CLICK_UPGRADE_BASE_COST, SUN_CLICK_UPGRADE_GROWTH_RATE, sunClickUpgradeLevel);
    }

    public Optional<BigNumber> buySunClickUpgrade(BigNumber currentSolarCoins, Sun sun) {
        BigNumber cost = costForNextSunClickUpgrade();
        if (currentSolarCoins.compareTo(cost) < 0) {
            return Optional.empty();
        }
        BigNumber remaining = currentSolarCoins.subtract(cost);
        sun.increaseClickPowerBonus(SUN_CLICK_UPGRADE_INCREMENT);
        sunClickUpgradeLevel++;
        return Optional.of(remaining);
    }

    public void restoreLevel(int level, Sun sun) {
        for (int i = 0; i < level; i++) {
            sun.increaseClickPowerBonus(SUN_CLICK_UPGRADE_INCREMENT);
        }
        this.sunClickUpgradeLevel = level;
    }
}