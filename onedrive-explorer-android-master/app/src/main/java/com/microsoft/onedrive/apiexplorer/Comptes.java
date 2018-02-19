package com.microsoft.onedrive.apiexplorer;

/**
 * Created by jlma on 28/10/17.
 */

public class Comptes {
    private int day;
    private int month;
    private int year;
    private String label;
    private double jl;
    private double vl;
    private String comment;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getJl() {
        return jl;
    }

    public void setJl(double jl) {
        this.jl = jl;
    }

    public double getVl() {
        return vl;
    }

    public void setVl(double vl) {
        this.vl = vl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
