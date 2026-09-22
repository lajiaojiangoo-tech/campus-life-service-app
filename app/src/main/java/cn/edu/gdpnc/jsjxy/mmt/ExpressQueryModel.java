package cn.edu.gdpnc.jsjxy.mmt;

public class ExpressQueryModel {
    public final String trackingNo;
    public final String company;
    public final String status;
    public final String pickupCode;

    public ExpressQueryModel(String trackingNo, String company, String status, String pickupCode) {
        this.trackingNo = trackingNo;
        this.company = company;
        this.status = status;
        this.pickupCode = pickupCode;
    }
}
