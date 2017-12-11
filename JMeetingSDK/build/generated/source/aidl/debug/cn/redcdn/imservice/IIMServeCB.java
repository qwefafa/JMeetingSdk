/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\workspace\\013_MobileMeetingSDK\\android\\prototype\\HnydSDKDemotwo\\JMeetingSDK\\src\\main\\aidl\\cn\\redcdn\\imservice\\IIMServeCB.aidl
 */
package cn.redcdn.imservice;
public interface IIMServeCB extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.redcdn.imservice.IIMServeCB
{
private static final java.lang.String DESCRIPTOR = "cn.redcdn.imservice.IIMServeCB";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.redcdn.imservice.IIMServeCB interface,
 * generating a proxy if needed.
 */
public static cn.redcdn.imservice.IIMServeCB asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.redcdn.imservice.IIMServeCB))) {
return ((cn.redcdn.imservice.IIMServeCB)iin);
}
return new cn.redcdn.imservice.IIMServeCB.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onMsgUpdate:
{
data.enforceInterface(DESCRIPTOR);
cn.redcdn.imservice.IMMessageBean _arg0;
if ((0!=data.readInt())) {
_arg0 = cn.redcdn.imservice.IMMessageBean.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onMsgUpdate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onQueryHistoryMsg:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<cn.redcdn.imservice.IMMessageBean> _arg0;
_arg0 = data.createTypedArrayList(cn.redcdn.imservice.IMMessageBean.CREATOR);
this.onQueryHistoryMsg(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.redcdn.imservice.IIMServeCB
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onMsgUpdate(cn.redcdn.imservice.IMMessageBean item) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((item!=null)) {
_data.writeInt(1);
item.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onMsgUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onQueryHistoryMsg(java.util.List<cn.redcdn.imservice.IMMessageBean> historyMsg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(historyMsg);
mRemote.transact(Stub.TRANSACTION_onQueryHistoryMsg, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onMsgUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onQueryHistoryMsg = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void onMsgUpdate(cn.redcdn.imservice.IMMessageBean item) throws android.os.RemoteException;
public void onQueryHistoryMsg(java.util.List<cn.redcdn.imservice.IMMessageBean> historyMsg) throws android.os.RemoteException;
}
