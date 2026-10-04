package src.interfaces;

import java.math.BigDecimal;

public interface Discount {
    BigDecimal calculateDiscount(BigDecimal amount);
    String getDescription();
}
