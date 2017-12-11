/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\workspace\\013_MobileMeetingSDK\\android\\prototype\\HnydSDKDemotwo\\JMeetingSDK\\src\\main\\aidl\\cn\\redcdn\\imservice\\IIMServe.aidl
 */
package cn.redcdn.imservice;
public interface IIMServe extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.redcdn.imservice.IIMServe
{
private static final java.lang.String DESCRIPTOR = "cn.redcdn.imservice.IIMServe";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.redcdn.imservice.IIMServe interface,
 * generating a proxy if needed.
 */
public static cn.redcdn.imservice.IIMServe asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.redcdn.imservice.IIMServe))) {
return ((cn.redcdn.imservice.IIMServe)iin);
}
return new cn.redcdn.imservice.IIMServe.Stub.Proxy(obj);
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
case TRANSACTION_sendTextMsg:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.sendTextMsg(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_queryHistoryMsg:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
this.queryHistoryMsg(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallBack:
{
data.enforceInterface(DESCRIPTOR);
cn.redcdn.imservice.IIMServeCB _arg0;
_arg0 = cn.redcdn.imservice.IIMServeCB.Stub.asInterface(data.readStrongBinder());
this.registerCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unRegisterCallBack:
{
data.enforceInterface(DESCRIPTOR);
cn.redcdn.imservice.IIMServeCB _arg0;
_arg0 = cn.redcdn.imservice.IIMServeCB.Stub.asInterface(data.readStrongBinder());
this.unRegisterCallBack(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.redcdn.imservice.IIMServe
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
@Override public void sendTextMsg(java.lang.String groupId, java.lang.String msg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(groupId);
_data.writeString(msg);
mRemote.transact(Stub.TRANSACTION_sendTextMsg, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void queryHistoryMsg(long beginTime) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(beginTime);
mRemote.transact(Stub.TRANSACTION_queryHistoryMsg, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void registerCallBack(cn.redcdn.imservice.IIMServeCB callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unRegisterCallBack(cn.redcdn.imservice.IIMServeCB callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unRegisterCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_sendTextMsg = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_queryHistoryMsg = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_registerCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_unRegisterCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void sendTextMsg(java.lang.String groupId, java.lang.String msg) throws android.os.RemoteException;
public void queryHistoryMsg(long beginTime) throws android.os.RemoteException;
public void registerCallBack(cn.redcdn.imservice.IIMServeCB callback) throws android.os.RemoteException;
public void unRegisterCallBack(cn.redcdn.imservice.IIMServeCB callback) throws android.os.RemoteException;
}
