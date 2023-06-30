package ru.klokov.model;

import java.util.Objects;

public class ExchangeRate {
    private Long id;
    private Long baseCurrencyId;
    private Long targetCurrencyId;
    private double rate;

    public ExchangeRate(Long id, Long baseCurrencyId, Long targetCurrencyId, double rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public ExchangeRate(Long baseCurrencyId, Long targetCurrencyId, double rate) {
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return id + " " + baseCurrencyId + " " + targetCurrencyId + " " + rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Objects.equals(id, that.id) && Objects.equals(baseCurrencyId, that.baseCurrencyId) && Objects.equals(targetCurrencyId, that.targetCurrencyId) && Double.compare(that.rate, rate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, baseCurrencyId, targetCurrencyId, rate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(Long baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public Long getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public void setTargetCurrencyId(Long targetCurrencyId) {
        this.targetCurrencyId = targetCurrencyId;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
