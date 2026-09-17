package com.sds.cleancode.restaurant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestableBookingScheduler extends BookingScheduler{


    LocalDateTime date;
    public TestableBookingScheduler(int capacityPerHour, LocalDateTime date) {
        super(capacityPerHour);
        this.date = date;
    }

    @Override
    public LocalDateTime getNow() {
        return date;
    }

}

