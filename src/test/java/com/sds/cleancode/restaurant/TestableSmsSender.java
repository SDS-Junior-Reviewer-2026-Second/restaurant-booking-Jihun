package com.sds.cleancode.restaurant;

public class TestableSmsSender extends SmsSender {
    private int sendMethodCount = 0;
    @Override
    public void send(Schedule schedule) {
        System.out.println("테스트용 sms sender 실행됨");
        sendMethodCount++;
    }

    public int getSendMethodCount() {
        return sendMethodCount;
    }
}
