package com.amalitech.dovewing;

import java.math.BigDecimal;

public final class Premium implements Category {

    @Override
    public BigDecimal cost(BigDecimal rate, int days) {
        return rate.multiply(BigDecimal.valueOf(days));
    }

    @Override
    public BigDecimal deposit(BigDecimal baseDeposit, RenterType renterType) {
        if (renterType == RenterType.MEMBER) return BigDecimal.ZERO;
        return baseDeposit;
    }

    @Override
    public String name() {
        return "PREMIUM";
    }
}