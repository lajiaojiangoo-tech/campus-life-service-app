package cn.edu.gdpnc.jsjxy.mmt;

public class ExpressModel {
    public final String id;
    public final String trackingNo;
    public final String company;
    public final String status;
    public final String pickupCode;
    public final String arrivalTime;

    public ExpressModel(String id, String trackingNo, String company, String status, String pickupCode, String arrivalTime) {
        this.id = id;
        this.trackingNo = trackingNo;
        this.company = company;
        this.status = status;
        this.pickupCode = pickupCode;
        this.arrivalTime = arrivalTime;
    }
}
