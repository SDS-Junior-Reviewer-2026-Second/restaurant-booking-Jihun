package com.sds.cleancode.restaurant;

public class TestableMailSender extends MailSender{
    private int sendMethodCount = 0;
    @Override
    public void sendMail(Schedule schedule) {
        System.out.println("테스트용 mail sender 실행됨");
        sendMethodCount++;
    }
    public int getSendMethodCount() {
        return sendMethodCount;
    }
}
