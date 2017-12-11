package cn.redcdn.imservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Desc
 * Created by wangkai on 2017/8/24.
 */

public class IMMessageBean implements Parcelable {

    private String msgId;
    private String nickName;
    private String nubeNumber;
    private String headUrl;
    private long time;
    private String msgContent;
    private int msgStatus; //0成功 1失败

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getNubeNumber() {
        return nubeNumber;
    }

    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msgId);
        dest.writeString(this.nickName);
        dest.writeString(this.nubeNumber);
        dest.writeString(this.headUrl);
        dest.writeLong(this.time);
        dest.writeString(this.msgContent);
        dest.writeInt(this.msgStatus);
    }

    public IMMessageBean() {
    }

    protected IMMessageBean(Parcel in) {
        this.msgId = in.readString();
        this.nickName = in.readString();
        this.nubeNumber = in.readString();
        this.headUrl = in.readString();
        this.time = in.readLong();
        this.msgContent = in.readString();
        this.msgStatus = in.readInt();
    }

    public static final Creator<IMMessageBean> CREATOR = new Creator<IMMessageBean>() {
        @Override
        public IMMessageBean createFromParcel(Parcel source) {
            return new IMMessageBean(source);
        }

        @Override
        public IMMessageBean[] newArray(int size) {
            return new IMMessageBean[size];
        }
    };
}
