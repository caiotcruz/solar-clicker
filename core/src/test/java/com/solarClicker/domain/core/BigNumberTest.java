package com.solarClicker.domain.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BigNumberTest {

    private static final double EPSILON = 1e-9;

    @Test
    void addSimpleSameMagnitude() {
        BigNumber a = BigNumber.of(500.0);
        BigNumber b = BigNumber.of(300.0);
        assertEquals(800.0, a.add(b).toDouble(), EPSILON);
    }

    @Test
    void addDifferentMagnitudesSmallTermStillCounts() {
        BigNumber a = BigNumber.of(1_000_000.0);
        BigNumber b = BigNumber.of(1.0);
        BigNumber result = a.add(b);
        assertEquals(1_000_001.0, result.toDouble(), 1.0);
    }

    @Test
    void addNegligibleTermIsIgnoredSafelyWithoutCrashing() {
        BigNumber huge = BigNumber.of(10.0).pow(20L);
        BigNumber tiny = BigNumber.of(1.0);
        BigNumber result = huge.add(tiny);
        assertEquals(huge.getExponent(), result.getExponent());
    }

    @Test
    void addWithZeroReturnsOtherOperand() {
        BigNumber a = BigNumber.of(42.0);
        assertEquals(a, BigNumber.ZERO.add(a));
        assertEquals(a, a.add(BigNumber.ZERO));
    }

    @Test
    void subtractTruncatesAtZeroInsteadOfGoingNegative() {
        BigNumber a = BigNumber.of(5.0);
        BigNumber b = BigNumber.of(10.0);
        BigNumber result = a.subtract(b);
        assertTrue(result.isZero());
        assertEquals(BigNumber.ZERO, result);
    }

    @Test
    void subtractNormalCase() {
        BigNumber a = BigNumber.of(10.0);
        BigNumber b = BigNumber.of(3.0);
        assertEquals(7.0, a.subtract(b).toDouble(), EPSILON);
    }

    @Test
    void subtractEqualValuesYieldsZero() {
        BigNumber a = BigNumber.of(123.0);
        assertTrue(a.subtract(a).isZero());
    }

    @Test
    void multiplyTwoBigNumbers() {
        BigNumber a = BigNumber.of(2.0);
        BigNumber b = BigNumber.of(3.0);
        assertEquals(6.0, a.multiply(b).toDouble(), EPSILON);
    }

    @Test
    void multiplyByScalar() {
        BigNumber a = BigNumber.of(100.0);
        assertEquals(150.0, a.multiply(1.5).toDouble(), EPSILON);
    }

    @Test
    void multiplyByZeroScalarYieldsZero() {
        BigNumber a = BigNumber.of(100.0);
        assertTrue(a.multiply(0.0).isZero());
    }

    @Test
    void multiplyByNegativeScalarThrows() {
        BigNumber a = BigNumber.of(100.0);
        assertThrows(IllegalArgumentException.class, () -> a.multiply(-1.0));
    }

    @Test
    void divideNormalCase() {
        BigNumber a = BigNumber.of(10.0);
        BigNumber b = BigNumber.of(4.0);
        assertEquals(2.5, a.divide(b).toDouble(), EPSILON);
    }

    @Test
    void divideByZeroThrowsArithmeticException() {
        BigNumber a = BigNumber.of(10.0);
        assertThrows(ArithmeticException.class, () -> a.divide(BigNumber.ZERO));
    }

    @Test
    void divideZeroByNonZeroYieldsZero() {
        assertTrue(BigNumber.ZERO.divide(BigNumber.of(5.0)).isZero());
    }

    @Test
    void powIntegerExponent() {
        BigNumber base = BigNumber.of(2.0);
        assertEquals(1024.0, base.pow(10L).toDouble(), 1e-6);
    }

    @Test
    void powFractionalSqrt() {
        BigNumber base = BigNumber.of(16.0);
        assertEquals(4.0, base.sqrt().toDouble(), 1e-9);
    }

    @Test
    void powZeroExponentYieldsOne() {
        BigNumber base = BigNumber.of(123.0);
        assertEquals(1.0, base.pow(0L).toDouble(), EPSILON);
    }

    @Test
    void powOfZeroWithPositiveExponentYieldsZero() {
        assertTrue(BigNumber.ZERO.pow(5.0).isZero());
    }

    @Test
    void powOfZeroWithZeroExponentYieldsOne() {
        assertEquals(BigNumber.ONE, BigNumber.ZERO.pow(0.0));
    }

    @Test
    void sublinearAscensionExponentDoesNotCrash() {
        BigNumber nivel = BigNumber.of(10.0);
        BigNumber bonus = nivel.pow(0.8);
        assertTrue(bonus.compareTo(BigNumber.ONE) > 0);
        assertTrue(bonus.compareTo(nivel) < 0);
    }

    @Test
    void floorTruncatesFractionalPart() {
        BigNumber value = BigNumber.of(7.8);
        assertEquals(7.0, value.floor().toDouble(), EPSILON);
    }

    @Test
    void floorOnAlreadyIntegerValue() {
        BigNumber value = BigNumber.of(42.0);
        assertEquals(42.0, value.floor().toDouble(), EPSILON);
    }

    @Test
    void floorOnZero() {
        assertTrue(BigNumber.ZERO.floor().isZero());
    }

    @Test
    void compareToOrdering() {
        BigNumber small = BigNumber.of(100.0);
        BigNumber big = BigNumber.of(100_000.0);
        assertTrue(small.compareTo(big) < 0);
        assertTrue(big.compareTo(small) > 0);
        assertEquals(0, small.compareTo(BigNumber.of(100.0)));
    }

    @Test
    void compareToWithZero() {
        BigNumber positive = BigNumber.of(1.0);
        assertTrue(BigNumber.ZERO.compareTo(positive) < 0);
        assertTrue(positive.compareTo(BigNumber.ZERO) > 0);
        assertEquals(0, BigNumber.ZERO.compareTo(BigNumber.ZERO));
    }

    @Test
    void minAndMax() {
        BigNumber a = BigNumber.of(50.0);
        BigNumber b = BigNumber.of(500.0);
        assertEquals(a, BigNumber.min(a, b));
        assertEquals(b, BigNumber.max(a, b));
    }

    @Test
    void saveStringRoundTrip() {
        BigNumber original = BigNumber.of(1234.5);
        String saved = original.toSaveString();
        BigNumber restored = BigNumber.fromSaveString(saved);
        assertEquals(original, restored);
    }

    @Test
    void saveStringRoundTripForZero() {
        assertEquals("0:0", BigNumber.ZERO.toSaveString());
        assertEquals(BigNumber.ZERO, BigNumber.fromSaveString("0:0"));
    }

    @Test
    void fromSaveStringRejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> BigNumber.fromSaveString("nao-e-um-formato-valido"));
    }

    @Test
    void ofNegativeValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> BigNumber.of(-5.0));
    }

    @Test
    void isZeroAndIsNegative() {
        assertTrue(BigNumber.ZERO.isZero());
        assertFalse(BigNumber.of(1.0).isZero());
        assertFalse(BigNumber.of(1.0).isNegative());
    }

    @Test
    void equalsAndHashCodeConsistency() {
        BigNumber a = BigNumber.of(999.0);
        BigNumber b = BigNumber.of(999.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void handlesVeryLargeExponentsWithoutCrashing() {
        BigNumber huge = BigNumber.of(5.0).pow(1000L);
        assertFalse(huge.isZero());
        assertTrue(huge.compareTo(BigNumber.of(1.0)) > 0);
        assertEquals(Double.POSITIVE_INFINITY, huge.toDouble());
    }

    @Test
    void handlesExponentsNearLongBoundaryWithoutCrashing() {
        BigNumber nearMax = BigNumber.of(10.0).pow(1_000_000_000.0);
        assertFalse(nearMax.isZero());
        assertTrue(nearMax.getExponent() > 0);
    }
}