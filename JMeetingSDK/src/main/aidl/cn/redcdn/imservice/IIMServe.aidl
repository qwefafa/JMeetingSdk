// IIMServe.aidl
package cn.redcdn.imservice;
import cn.redcdn.imservice.IIMServeCB;

interface IIMServe {
    void sendTextMsg(String groupId,String msg);
    void queryHistoryMsg(long beginTime);
    void registerCallBack(IIMServeCB callback);
    void unRegisterCallBack(IIMServeCB callback);
}
