package src.interfaces;

import java.math.BigDecimal;

public interface Billable {

    String getName();

    BigDecimal getPrice();

    String getDescription();
}