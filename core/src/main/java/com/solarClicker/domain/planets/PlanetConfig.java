package com.solarClicker.domain.planets;

import com.solarClicker.domain.core.BigNumber;

public record PlanetConfig(
    String id,
    BigNumber unlockCost,
    BigNumber clickPower,
    BigNumber idleProductionPerSecond,
    double exportRate
) {}