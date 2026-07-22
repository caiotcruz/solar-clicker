package com.solarClicker.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.solarClicker.domain.core.BigNumber;
import com.solarClicker.domain.economy.GeneralShop;
import com.solarClicker.domain.planets.Planet;
import com.solarClicker.domain.planets.PlanetConfig;
import com.solarClicker.domain.planets.Sun;

public final class GameState {

    private BigNumber solarCoins;
    private final Sun sun;
    private final Map<String, Planet> planets;
    private final GeneralShop generalShop;

    public GameState(Sun sun, Map<String, Planet> planets, GeneralShop generalShop, BigNumber initialSolarCoins) {
        this.sun = sun;
        this.planets = planets;
        this.generalShop = generalShop;
        this.solarCoins = initialSolarCoins;
    }

    public static GameState newGame() {
        Sun sun = new Sun(BigNumber.of(1.0));

        Map<String, Planet> planets = new LinkedHashMap<>();

        planets.put("mercury", new Planet(new PlanetConfig(
            "mercury",
            BigNumber.of(50.0),
            BigNumber.of(2.0),
            BigNumber.of(0.5),
            0.5
        ), false));

        planets.put("venus", new Planet(new PlanetConfig(
            "venus",
            BigNumber.of(300.0),
            BigNumber.of(8.0),
            BigNumber.of(0.2),
            0.4
        ), false));

        planets.put("earth", new Planet(new PlanetConfig(
            "earth",
            BigNumber.of(1500.0),
            BigNumber.of(1.0),
            BigNumber.of(3.0),
            0.3
        ), false));

        return new GameState(sun, planets, new GeneralShop(), BigNumber.ZERO);
    }

    public BigNumber getSolarCoins() {
        return solarCoins;
    }

    public Sun getSun() {
        return sun;
    }

    public GeneralShop getGeneralShop() {
        return generalShop;
    }

    public Planet getPlanet(String id) {
        return requirePlanet(id);
    }

    public BigNumber clickSun() {
        BigNumber gained = sun.onClick();
        solarCoins = solarCoins.add(gained);
        return gained;
    }

    public BigNumber clickPlanet(String planetId) {
        Planet planet = requirePlanet(planetId);
        BigNumber gained = planet.click();
        exportToSolarCoins(planet, gained);
        return gained;
    }

    public void tick(double deltaSeconds) {
        for (Planet planet : planets.values()) {
            if (!planet.isUnlocked()) continue;
            BigNumber gained = planet.tick(deltaSeconds);
            exportToSolarCoins(planet, gained);
        }
    }

    public boolean unlockPlanet(String planetId) {
        Planet planet = requirePlanet(planetId);
        if (planet.isUnlocked()) {
            return false;
        }
        BigNumber cost = planet.getUnlockCost();
        if (solarCoins.compareTo(cost) < 0) {
            return false;
        }
        solarCoins = solarCoins.subtract(cost);
        planet.unlock();
        return true;
    }

    public boolean buyGeneralShopSunClickUpgrade() {
        Optional<BigNumber> result = generalShop.buySunClickUpgrade(solarCoins, sun);
        if (result.isEmpty()) {
            return false;
        }
        solarCoins = result.get();
        return true;
    }

    public void restoreSolarCoins(BigNumber solarCoins) {
        this.solarCoins = solarCoins;
    }

    private void exportToSolarCoins(Planet planet, BigNumber amountEarnedLocally) {
        BigNumber exported = planet.exportToSolarCoins(amountEarnedLocally);
        solarCoins = solarCoins.add(exported);
    }

    private Planet requirePlanet(String id) {
        Planet planet = planets.get(id);
        if (planet == null) {
            throw new IllegalArgumentException("Planeta desconhecido: " + id);
        }
        return planet;
    }
}