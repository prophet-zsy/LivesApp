package com.example.livesapp.model.beans;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;


@Table("StepData")
public class StepData {
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;
    @Column("date")
    private String date;
    @Column("step")
    private String step;

    public StepData() {
    }

    public StepData(int id, String date, String step) {
        this.id = id;
        this.date = date;
        this.step = step;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", step='" + step + '\'' +
                '}';
    }
}
