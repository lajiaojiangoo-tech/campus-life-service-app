package cn.edu.gdpnc.jsjxy.mmt;

public class PrintHistoryModel {
    public final String fileName;
    public final String config;
    public final String time;
    public final String status;

    public PrintHistoryModel(String fileName, String config, String time, String status) {
        this.fileName = fileName;
        this.config = config;
        this.time = time;
        this.status = status;
    }
}
