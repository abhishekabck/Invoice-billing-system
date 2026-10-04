package src.models;

import java.math.BigDecimal;
import src.interfaces.Discount;

public class FlatDiscount implements Discount {
    private BigDecimal discountAmount;
    private String description;

    // constructor
    public FlatDiscount(BigDecimal discountAmount, String description) {
        this.discountAmount = discountAmount;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public BigDecimal
     calculateDiscount(BigDecimal amount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount.min(amount);
    }
}