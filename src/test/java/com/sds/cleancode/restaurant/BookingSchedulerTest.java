package com.sds.cleancode.restaurant;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {
    public static final int CAPACITY_PER_HOUR = 3;
    public static final int UNDER_CAPACITY = 1;
    DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    LocalDateTime NOT_ON_THE_HOUR = LocalDateTime.parse("2021/03/26 09:05", DATE_TIME_FORMAT);
    LocalDateTime ON_THE_HOUR = LocalDateTime.parse("2021/03/26 09:00", DATE_TIME_FORMAT);

    LocalDateTime SUNDAY_DATE = LocalDateTime.parse("2021/03/28 09:00",DATE_TIME_FORMAT);
    LocalDateTime MONDAY_DATE = LocalDateTime.parse("2024/06/03 09:00",DATE_TIME_FORMAT);

    @Mock
    public Customer CUSTOMER;

    @Mock(answer = Answers.RETURNS_MOCKS)
    public Customer CUSTOMER_WITH_MAIL;

    @Spy
    BookingScheduler bookingScheduler = new BookingScheduler(CAPACITY_PER_HOUR);

    @Mock
    private SmsSender smsSender;

    @Mock
    private MailSender mailSender;

    @BeforeEach
    void setUp() {
        bookingScheduler.setMailSender(mailSender);
        bookingScheduler.setSmsSender(smsSender);
    }


    @Test
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {
        Schedule schedule = new Schedule(NOT_ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        assertThrows(RuntimeException.class, ()-> {
            bookingScheduler.addSchedule(schedule);
        });
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);
        assertTrue(bookingScheduler.hasSchedule(schedule));
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대에_Capacity_초과할_경우_예외발생() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        Schedule schedule2 = new Schedule(ON_THE_HOUR, 1, CUSTOMER);
        assertThrows(RuntimeException.class, ()-> {
            bookingScheduler.addSchedule(schedule);
            bookingScheduler.addSchedule(schedule2);
        });
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대가_다르면_Capacity_차있어도_스케쥴_추가_성공() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        Schedule schedule2 = new Schedule(ON_THE_HOUR.plusHours(1), CAPACITY_PER_HOUR , CUSTOMER);

        bookingScheduler.addSchedule(schedule);
        bookingScheduler.addSchedule(schedule2);

        assertTrue(bookingScheduler.hasSchedule(schedule));
        assertTrue(bookingScheduler.hasSchedule(schedule2));

    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        verify(smsSender, times(1)).send(schedule);
        //assertEquals(1, testableSmsSender.getSendMethodCount());
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        verify(mailSender, times(0)).sendMail(schedule);
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER_WITH_MAIL);
        bookingScheduler.addSchedule(schedule);

        verify(mailSender, times(1)).sendMail(schedule);
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {

        when(bookingScheduler.getNow()).thenReturn(SUNDAY_DATE);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingScheduler.addSchedule(new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER))
        );
        assertEquals("Booking system is not available on sunday", exception.getMessage());
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {

        when(bookingScheduler.getNow()).thenReturn(MONDAY_DATE);
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);
        bookingScheduler.addSchedule(schedule);

        assertTrue(bookingScheduler.hasSchedule(schedule));
    }
}
