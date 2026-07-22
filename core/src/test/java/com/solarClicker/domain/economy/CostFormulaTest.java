package com.solarClicker.domain.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.solarClicker.domain.core.BigNumber;

class CostFormulaTest {

    @Test
    void levelZeroCostEqualsBaseCost() {
        BigNumber base = BigNumber.of(10.0);
        assertEquals(base, CostFormula.exponentialCost(base, 1.15, 0));
    }

    @Test
    void costGrowsWithLevel() {
        BigNumber base = BigNumber.of(10.0);
        BigNumber costLevel1 = CostFormula.exponentialCost(base, 1.15, 1);
        BigNumber costLevel5 = CostFormula.exponentialCost(base, 1.15, 5);
        assertTrue(costLevel1.compareTo(base) > 0);
        assertTrue(costLevel5.compareTo(costLevel1) > 0);
    }

    @Test
    void negativeLevelThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> CostFormula.exponentialCost(BigNumber.of(10.0), 1.15, -1));
    }
}