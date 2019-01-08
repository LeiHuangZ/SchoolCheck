package com.example.huang.myapplication.retrofit;

/**
 * @author huang 3004240957@qq.com
 */
public class StudentLeave {


    /**
     * schName : String
     * leaveTime : 2018-07-25 15:18:33
     * deviceId : AK1
     * position : 2
     * studentCardC8 : 19923990
     * studentCardR8 : 19923990
     * studentCardC10 : 1992390890
     * studentCardR10 : 1992399990
     * leaveImg : String
     */

    private String schName;
    private String leaveTime;
    private String deviceId;
    private String position;
    private String studentCardC8;
    private String studentCardR8;
    private String studentCardC10;
    private String studentCardR10;
    private String leaveImg;

    public StudentLeave(String schName, String leaveTime, String deviceId, String position, String studentCardC8, String studentCardR8, String studentCardC10, String studentCardR10, String leaveImg) {
        this.schName = schName;
        this.leaveTime = leaveTime;
        this.deviceId = deviceId;
        this.position = position;
        this.studentCardC8 = studentCardC8;
        this.studentCardR8 = studentCardR8;
        this.studentCardC10 = studentCardC10;
        this.studentCardR10 = studentCardR10;
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

    public String getStudentCardC8() {
        return studentCardC8;
    }

    public void setStudentCardC8(String studentCardC8) {
        this.studentCardC8 = studentCardC8;
    }

    public String getStudentCardR8() {
        return studentCardR8;
    }

    public void setStudentCardR8(String studentCardR8) {
        this.studentCardR8 = studentCardR8;
    }

    public String getStudentCardC10() {
        return studentCardC10;
    }

    public void setStudentCardC10(String studentCardC10) {
        this.studentCardC10 = studentCardC10;
    }

    public String getStudentCardR10() {
        return studentCardR10;
    }

    public void setStudentCardR10(String studentCardR10) {
        this.studentCardR10 = studentCardR10;
    }

    public String getLeaveImg() {
        return leaveImg;
    }

    public void setLeaveImg(String leaveImg) {
        this.leaveImg = leaveImg;
    }
}
