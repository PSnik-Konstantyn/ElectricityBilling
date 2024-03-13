package com.example.electricitybilling;

public class Khw {
    private double dayKhw;
    private double nightKhw;

    public double getDayKhw() {
        return dayKhw;
    }

    public void setDayKhw(double dayKhw) {
        this.dayKhw = dayKhw;
    }

    public double getNightKhw() {
        return nightKhw;
    }

    public void setNightKhw(double nightKhw) {
        this.nightKhw = nightKhw;
    }

    public Khw(double dayKhw, double nightKhw) {
        this.dayKhw = dayKhw;
        this.nightKhw = nightKhw;
    }

}
