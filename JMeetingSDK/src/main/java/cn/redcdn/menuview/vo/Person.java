package cn.redcdn.menuview.vo;

import cn.redcdn.log.CustomLog;

public class Person {

    private String accountName;

    private String accountId;

    private String photo;

    private boolean isInTXL;

    private boolean isInvited;

    private boolean isSelected;

    private String appType;// mobile,n8

    private int userType = 0;// 用户类型 0：全能用户 1：普通用户

    private int isInvitedFrom = 999;// 0 视讯号 1 参会列表

    private int statusType;

    private String name;

    private boolean isWaitingResult;

    private int loudSpeakerStatus; // 1。开启状态  2.关闭状态

    //TODO 请删除
    // public String uid;
    // public String headThumUrl;
    // public String headPreviewUrl;
    // public String nickName;
    // public String nubeNumber;
    // public String qrCodeUrl;
    // public String account;

    //TODO 请删除
    // values.put(DBConf.ACCOUNT_TYPE, 0);
    // values.put(DBConf.WORKUNIT_TYPE, "123");
    // values.put(DBConf.WORK_UNIT, "123");
    // values.put(DBConf.DEPARTMENT, "123");
    // values.put(DBConf.PROFESSIONAL,"123");
    // values.put(DBConf.OFFICETEL, "12311111");
    // values.put(DBConf.SAVE_TO_CONTACTS_TIME, "");
    // values.put(DBConf.EMAIL,"1194171358@qq.com");

    //红云医疗添加字段
    public String accountType;
    public String workUnitType;
    public String workUnit;
    public String department;
    public String professional;
    public String officTel;



    public String getAccountType() {
        return accountType;
    }


    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public String getWorkUnitType() {
        return workUnitType;
    }


    public void setWorkUnitType(String workUnitType) {
        this.workUnitType = workUnitType;
    }


    public String getWorkUnit() {
        return workUnit;
    }


    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit;
    }


    public String getDepartment() {
        return department;
    }


    public void setDepartment(String department) {
        this.department = department;
    }


    public String getProfessional() {
        return professional;
    }


    public void setProfessional(String professional) {
        this.professional = professional;
    }


    public String getOfficTel() {
        return officTel;
    }


    public void setOfficTel(String officTel) {
        this.officTel = officTel;
    }


    public String getAccountName() {

        return accountName;

    }


    public void setAccountName(String accountName) {

        this.accountName = accountName;

    }


    public String getAccountId() {

        return accountId;

    }


    public void setLoudSpeakerStatus(int loudSpeakerStatus) {
        this.loudSpeakerStatus = loudSpeakerStatus;
        CustomLog.d(Person.this.getClass().getName(), accountName + " Person setLoudSpeakerStatus:" + String.valueOf(loudSpeakerStatus));
    }


    public int getLoudSpeakerStatus() {
        return loudSpeakerStatus;
    }


    public void setAccountId(String accountId) {

        this.accountId = accountId;

    }


    public String getName() {

        return name;

    }


    public void setName(String name) {

        this.name = name;

    }


    public String getAppType() {

        return appType;

    }


    public void setAppType(String appType) {

        this.appType = appType;

    }


    public int getUserType() {

        return userType;

    }


    public void setUserType(int userType) {

        this.userType = userType;

    }


    public String getPhoto() {

        return photo;

    }


    public void setPhoto(String photo) {

        this.photo = photo;

    }


    public boolean isInTXL() {

        return isInTXL;

    }


    public void setInTXL(boolean isInTXL) {

        this.isInTXL = isInTXL;

    }


    public boolean isInvited() {

        return isInvited;

    }


    public void setInvited(boolean isInvited) {

        this.isInvited = isInvited;

    }


    public boolean isSelected() {

        return isSelected;

    }


    public void setSelected(boolean isSelected) {

        this.isSelected = isSelected;

    }


    public int isInvitedFrom() {

        return isInvitedFrom;

    }


    public void setInvitedFrom(int isInvitedFrom) {

        this.isInvitedFrom = isInvitedFrom;

    }


    public int getStatusType() {

        return statusType;

    }


    public void setStatusType(int statusType) {

        this.statusType = statusType;

    }


    public boolean isWaitingResult() {

        return isWaitingResult;

    }


    public void setWaitingResult(boolean isWaitingResult) {

        this.isWaitingResult = isWaitingResult;

    }


    @Override

    public boolean equals(Object o) {

        Person p = (Person) o;

        if (this.accountId.equals(p.getAccountId())) {

            return true;

        } else {

            return false;

        }

    }

}

