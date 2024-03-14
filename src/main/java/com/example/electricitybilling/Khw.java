package com.example.electricitybilling;

public class Khw {
    private double dayKhw;
    private double nightKhw;

    public double getDayKhw() {
        return dayKhw;
    }

    public double getNightKhw() {
        return nightKhw;
    }

    public Khw(double dayKhw, double nightKhw) {
        this.dayKhw = dayKhw;
        this.nightKhw = nightKhw;
    }

}
