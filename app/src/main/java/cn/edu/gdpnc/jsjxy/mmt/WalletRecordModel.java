package cn.edu.gdpnc.jsjxy.mmt;

public class WalletRecordModel {
    public final String type;
    public final String desc;
    public final double amount;
    public final double balance;
    public final String time;

    public WalletRecordModel(String type, String desc, double amount, double balance, String time) {
        this.type = type;
        this.desc = desc;
        this.amount = amount;
        this.balance = balance;
        this.time = time;
    }
}
