package cn.edu.gdpnc.jsjxy.mmt;

public class OrderRecordModel {
    public final String id;
    public final String items;
    public final double total;
    public final String status;
    public final String time;

    public OrderRecordModel(String id, String items, double total, String status, String time) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.status = status;
        this.time = time;
    }
}
