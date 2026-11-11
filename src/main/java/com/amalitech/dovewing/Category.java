package com.amalitech.dovewing;

import java.math.BigDecimal;

public interface Category {
    BigDecimal cost(BigDecimal rate, int days);
    BigDecimal deposit(BigDecimal baseDeposit, RenterType renterType);
    String name();
}