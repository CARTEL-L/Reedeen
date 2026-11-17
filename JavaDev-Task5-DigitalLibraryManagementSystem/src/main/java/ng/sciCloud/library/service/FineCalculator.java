package ng.sciCloud.library.service;

import ng.sciCloud.library.config.LibraryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component("fineCalculator")
public class FineCalculator {

    private final BigDecimal ratePerDay;

    @Autowired
    public FineCalculator(LibraryProperties properties) {
        this(properties.getFinePerDay());
    }

    public FineCalculator(BigDecimal ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    public BigDecimal calculate(LocalDate dueDate, LocalDate returnedOn) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnedOn);
        if (daysLate <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return ratePerDay.multiply(BigDecimal.valueOf(daysLate)).setScale(2, RoundingMode.HALF_UP);
    }
}
