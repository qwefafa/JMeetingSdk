// IIMServeCB.aidl
package cn.redcdn.imservice;
import cn.redcdn.imservice.IMMessageBean;

interface IIMServeCB {
    void onMsgUpdate(in IMMessageBean item);
    void onQueryHistoryMsg(in List<IMMessageBean> historyMsg);
}
