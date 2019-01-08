package com.example.huang.myapplication.retrofit;

/**
 * @author huang 3004240957@qq.com
 */
public class VisitorsLeave {

    /**
     * schName : String
     * leaveTime : 2018-07-25 15:15:33
     * deviceId : AK1
     * position : 1
     * visitCardC8 : 19923990
     * visitCardR8 : 19923990
     * visitCardC10 : 1992390890
     * visitCardR10 : 1992399990
     * leaveImg : String
     */

    private String schName;
    private String leaveTime;
    private String deviceId;
    private String position;
    private String visitCardC8;
    private String visitCardR8;
    private String visitCardC10;
    private String visitCardR10;
    private String leaveImg;

    public VisitorsLeave(String schName, String leaveTime, String deviceId, String position, String visitCardC8, String visitCardR8, String visitCardC10, String visitCardR10, String leaveImg) {
        this.schName = schName;
        this.leaveTime = leaveTime;
        this.deviceId = deviceId;
        this.position = position;
        this.visitCardC8 = visitCardC8;
        this.visitCardR8 = visitCardR8;
        this.visitCardC10 = visitCardC10;
        this.visitCardR10 = visitCardR10;
        this.leaveImg = leaveImg;
    }

    public String getSchName() {
        return schName;
    }

    public void setSchName(String schName) {
        this.schName = schName;
    }

    public String getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(String leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getVisitCardC8() {
        return visitCardC8;
    }

    public void setVisitCardC8(String visitCardC8) {
        this.visitCardC8 = visitCardC8;
    }

    public String getVisitCardR8() {
        return visitCardR8;
    }

    public void setVisitCardR8(String visitCardR8) {
        this.visitCardR8 = visitCardR8;
    }

    public String getVisitCardC10() {
        return visitCardC10;
    }

    public void setVisitCardC10(String visitCardC10) {
        this.visitCardC10 = visitCardC10;
    }

    public String getVisitCardR10() {
        return visitCardR10;
    }

    public void setVisitCardR10(String visitCardR10) {
        this.visitCardR10 = visitCardR10;
    }

    public String getLeaveImg() {
        return leaveImg;
    }

    public void setLeaveImg(String leaveImg) {
        this.leaveImg = leaveImg;
    }
}
