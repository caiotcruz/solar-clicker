package com.solarClicker.domain.economy;

import com.solarClicker.domain.core.BigNumber;

public final class CostFormula {

    private CostFormula() {}

    public static BigNumber exponentialCost(BigNumber baseCost, double growthRate, int level) {
        if (level < 0) {
            throw new IllegalArgumentException("level não pode ser negativo: " + level);
        }
        BigNumber growthFactor = BigNumber.of(growthRate).pow((long) level);
        return baseCost.multiply(growthFactor);
    }
}