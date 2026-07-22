package com.solarClicker.domain.planets;

import com.solarClicker.domain.core.BigNumber;

public final class Sun {

    private final BigNumber baseClickPower;
    private BigNumber clickPowerBonus;

    public Sun(BigNumber baseClickPower) {
        this.baseClickPower = baseClickPower;
        this.clickPowerBonus = BigNumber.ZERO;
    }

    public BigNumber onClick() {
        return baseClickPower.add(clickPowerBonus);
    }

    public void increaseClickPowerBonus(BigNumber amount) {
        clickPowerBonus = clickPowerBonus.add(amount);
    }

    public BigNumber getClickPowerBonus() {
        return clickPowerBonus;
    }
}