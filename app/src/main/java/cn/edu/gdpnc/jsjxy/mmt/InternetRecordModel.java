package cn.edu.gdpnc.jsjxy.mmt;

public class InternetRecordModel {
    public final String desc;
    public final double amount;
    public final double balance;
    public final String time;

    public InternetRecordModel(String desc, double amount, double balance, String time) {
        this.desc = desc;
        this.amount = amount;
        this.balance = balance;
        this.time = time;
    }
}
