package cn.edu.gdpnc.jsjxy.mmt;

public class HotWaterRecordModel {
    public final String type;
    public final String detail;
    public final double amount;
    public final double balance;
    public final String time;

    public HotWaterRecordModel(String type, String detail, double amount, double balance, String time) {
        this.type = type;
        this.detail = detail;
        this.amount = amount;
        this.balance = balance;
        this.time = time;
    }
}
