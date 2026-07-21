package com.solarClicker.domain.core;

import java.util.Objects;

public final class BigNumber implements Comparable<BigNumber> {

    private static final long PRECISION_CUTOFF_EXPONENT_DIFF = 17;

    public static final BigNumber ZERO = new BigNumber(0.0, 0);

    public static final BigNumber ONE = BigNumber.of(1.0);

    private final double mantissa;
    private final long exponent;

    private BigNumber(double mantissa, long exponent) {
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    private static BigNumber normalize(double mantissa, long exponent) {
        if (Double.isNaN(mantissa) || mantissa == 0.0) {
            return ZERO;
        }
        if (mantissa < 0.0) {
            return ZERO;
        }

        double m = mantissa;
        long e = exponent;

        while (m >= 10.0) {
            m /= 10.0;
            e++;
        }
        while (m < 1.0) {
            m *= 10.0;
            e--;
        }
        return new BigNumber(m, e);
    }

    public static BigNumber of(double value) {
        if (value == 0.0) {
            return ZERO;
        }
        if (value < 0.0) {
            throw new IllegalArgumentException(
                "BigNumber não suporta valores negativos (doc 7, seção 8): " + value);
        }
        long exponent = (long) Math.floor(Math.log10(value));
        double mantissa = value / Math.pow(10, exponent);
        return normalize(mantissa, exponent);
    }

    public static BigNumber of(long value) {
        return of((double) value);
    }

    public BigNumber add(BigNumber other) {
        Objects.requireNonNull(other, "other");
        if (this.isZero()) return other;
        if (other.isZero()) return this;

        BigNumber larger = (this.exponent >= other.exponent) ? this : other;
        BigNumber smaller = (this.exponent >= other.exponent) ? other : this;

        long expDiff = larger.exponent - smaller.exponent;
        if (expDiff > PRECISION_CUTOFF_EXPONENT_DIFF) {
            return larger;
        }

        double adjustedSmallerMantissa = smaller.mantissa / Math.pow(10, expDiff);
        double newMantissa = larger.mantissa + adjustedSmallerMantissa;
        return normalize(newMantissa, larger.exponent);
    }

    public BigNumber subtract(BigNumber other) {
        Objects.requireNonNull(other, "other");
        if (other.isZero()) return this;
        if (this.compareTo(other) <= 0) {
            return ZERO;
        }

        // Neste ponto this > other, logo this.exponent >= other.exponent.
        long expDiff = this.exponent - other.exponent;
        if (expDiff > PRECISION_CUTOFF_EXPONENT_DIFF) {
            return this;
        }

        double adjustedOtherMantissa = other.mantissa / Math.pow(10, expDiff);
        double newMantissa = this.mantissa - adjustedOtherMantissa;
        return normalize(newMantissa, this.exponent);
    }

    public BigNumber multiply(BigNumber other) {
        Objects.requireNonNull(other, "other");
        if (this.isZero() || other.isZero()) return ZERO;
        double newMantissa = this.mantissa * other.mantissa;
        long newExponent = this.exponent + other.exponent;
        return normalize(newMantissa, newExponent);
    }

    public BigNumber multiply(double scalar) {
        if (scalar == 0.0) return ZERO;
        if (scalar < 0.0) {
            throw new IllegalArgumentException(
                "BigNumber não suporta multiplicação por escalar negativo (doc 7, seção 8): " + scalar);
        }
        return normalize(this.mantissa * scalar, this.exponent);
    }

    public BigNumber divide(BigNumber other) {
        Objects.requireNonNull(other, "other");
        if (other.isZero()) {
            throw new ArithmeticException("Divisão por zero em BigNumber");
        }
        if (this.isZero()) return ZERO;
        double newMantissa = this.mantissa / other.mantissa;
        long newExponent = this.exponent - other.exponent;
        return normalize(newMantissa, newExponent);
    }

    public BigNumber pow(double exponentPower) {
        if (this.isZero()) {
            return (exponentPower == 0.0) ? ONE : ZERO;
        }
        double log10Value = Math.log10(this.mantissa) + this.exponent;
        double totalExponent = exponentPower * log10Value;
        long newExponent = (long) Math.floor(totalExponent);
        double newMantissa = Math.pow(10, totalExponent - newExponent);
        return normalize(newMantissa, newExponent);
    }

    public BigNumber pow(long exponentPower) {
        return pow((double) exponentPower);
    }

    public BigNumber sqrt() {
        return pow(0.5);
    }

    public BigNumber floor() {
        if (this.isZero()) return ZERO;
        if (this.exponent >= 15) {
            return this;
        }
        double value = this.mantissa * Math.pow(10, this.exponent);
        double floored = Math.floor(value);
        return of(floored);
    }

    @Override
    public int compareTo(BigNumber other) {
        Objects.requireNonNull(other, "other");
        if (this.isZero() && other.isZero()) return 0;
        if (this.isZero()) return -1;
        if (other.isZero()) return 1;
        if (this.exponent != other.exponent) {
            return Long.compare(this.exponent, other.exponent);
        }
        return Double.compare(this.mantissa, other.mantissa);
    }

    public static BigNumber min(BigNumber a, BigNumber b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static BigNumber max(BigNumber a, BigNumber b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public boolean isZero() {
        return this.mantissa == 0.0;
    }

    public boolean isNegative() {
        return false;
    }

    public double toDouble() {
        if (isZero()) return 0.0;
        if (this.exponent > 308) return Double.POSITIVE_INFINITY;
        return this.mantissa * Math.pow(10, this.exponent);
    }

    public String toPlainString() {
        if (isZero()) return "0";
        return this.mantissa + "e" + this.exponent;
    }

    public String toSaveString() {
        if (isZero()) return "0:0";
        return this.mantissa + ":" + this.exponent;
    }

    public static BigNumber fromSaveString(String saved) {
        if (saved == null || saved.isEmpty() || saved.equals("0:0")) {
            return ZERO;
        }
        String[] parts = saved.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de save inválido para BigNumber: '" + saved + "'");
        }
        double mantissa = Double.parseDouble(parts[0]);
        long exponent = Long.parseLong(parts[1]);
        return normalize(mantissa, exponent);
    }

    public double getMantissa() {
        return mantissa;
    }

    public long getExponent() {
        return exponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BigNumber)) return false;
        BigNumber other = (BigNumber) o;
        if (this.isZero() && other.isZero()) return true;
        return this.exponent == other.exponent
            && Double.compare(this.mantissa, other.mantissa) == 0;
    }

    @Override
    public int hashCode() {
        if (isZero()) return 0;
        return Objects.hash(mantissa, exponent);
    }

    @Override
    public String toString() {
        return toPlainString();
    }
}