package com.solarClicker.domain.planets;

import com.solarClicker.domain.core.BigNumber;

public final class Planet {

    private final PlanetConfig config;
    private boolean unlocked;
    private BigNumber localCurrency;

    public Planet(PlanetConfig config, boolean unlocked) {
        this.config = config;
        this.unlocked = unlocked;
        this.localCurrency = BigNumber.ZERO;
    }

    public String getId() {
        return config.id();
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public BigNumber getUnlockCost() {
        return config.unlockCost();
    }

    public BigNumber getLocalCurrency() {
        return localCurrency;
    }

    public void unlock() {
        if (unlocked) {
            throw new IllegalStateException("Planeta " + config.id() + " já está desbloqueado");
        }
        unlocked = true;
    }

    public BigNumber click() {
        requireUnlocked();
        BigNumber gained = config.clickPower();
        localCurrency = localCurrency.add(gained);
        return gained;
    }

    public BigNumber tick(double deltaSeconds) {
        requireUnlocked();
        BigNumber gained = config.idleProductionPerSecond().multiply(deltaSeconds);
        localCurrency = localCurrency.add(gained);
        return gained;
    }

    public BigNumber exportToSolarCoins(BigNumber amountEarnedLocally) {
        return amountEarnedLocally.multiply(config.exportRate());
    }

    public void restoreState(boolean unlocked, BigNumber localCurrency) {
        this.unlocked = unlocked;
        this.localCurrency = localCurrency;
    }

    private void requireUnlocked() {
        if (!unlocked) {
            throw new IllegalStateException("Planeta " + config.id() + " ainda não foi desbloqueado");
        }
    }
}