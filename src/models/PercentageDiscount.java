package src.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import src.interfaces.Discount;

public class PercentageDiscount implements Discount {
    private BigDecimal percentage;
    private String description;

    public PercentageDiscount(BigDecimal percentage, String description) {
        this.percentage = percentage;
        this.description = description;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percentage)
                     .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() { return description; }

    public BigDecimal getPercentage() { return percentage; }
}
