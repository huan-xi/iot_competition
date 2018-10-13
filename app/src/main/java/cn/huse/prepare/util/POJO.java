package cn.huse.prepare.util;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "pojo")
public class POJO {
    @Column(name = "id",isId = true)
    private int id;
    @Column(name = "value")
    private double value;
    @Column(name = "time")
    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
