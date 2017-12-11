/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\workspace\\013_MobileMeetingSDK\\android\\prototype\\HnydSDKDemotwo\\JMeetingSDK\\src\\main\\aidl\\cn\\redcdn\\jmeetingsdk\\IJMeetingService.aidl
 */
package cn.redcdn.jmeetingsdk;
public interface IJMeetingService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.redcdn.jmeetingsdk.IJMeetingService
{
private static final java.lang.String DESCRIPTOR = "cn.redcdn.jmeetingsdk.IJMeetingService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.redcdn.jmeetingsdk.IJMeetingService interface,
 * generating a proxy if needed.
 */
public static cn.redcdn.jmeetingsdk.IJMeetingService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.redcdn.jmeetingsdk.IJMeetingService))) {
return ((cn.redcdn.jmeetingsdk.IJMeetingService)iin);
}
return new cn.redcdn.jmeetingsdk.IJMeetingService.Stub.Proxy(obj);
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
case TRANSACTION_setRootDirectory:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setRootDirectory(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setVideoParameter:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _result = this.setVideoParameter(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setAppType:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setAppType(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setShowMeetingScreenSharing:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setShowMeetingScreenSharing(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setShowMeetingFloat:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setShowMeetingFloat(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSearchContactUrl:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setSearchContactUrl(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setInsertContactUrl:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setInsertContactUrl(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setContactProvider:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setContactProvider(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setWXAppId:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.setWXAppId(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setMeetingAdapter:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setMeetingAdapter(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
int _result = this.init(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_release:
{
data.enforceInterface(DESCRIPTOR);
this.release();
reply.writeNoException();
return true;
}
case TRANSACTION_creatMeeting:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<java.lang.String> _arg0;
_arg0 = data.createStringArrayList();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
java.lang.String _arg3;
_arg3 = data.readString();
int _result = this.creatMeeting(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_joinMeeting:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.joinMeeting(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_resumeMeeting:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.resumeMeeting(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getNowMeetings:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNowMeetings();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_incomingCall:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
java.lang.String _arg3;
_arg3 = data.readString();
int _result = this.incomingCall(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_quitMeeting:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.quitMeeting();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_updateToken:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.updateToken(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setCurrentUser:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _result = this.setCurrentUser(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelCreatMeeting:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.cancelCreatMeeting();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelGetNowMeetings:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.cancelGetNowMeetings();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelJoinMeeting:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.cancelJoinMeeting();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setisAllowMobileNet:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setisAllowMobileNet(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSelectSystemCamera:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
int _result = this.setSelectSystemCamera(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getActiveMeetingId:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getActiveMeetingId();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.redcdn.jmeetingsdk.IJMeetingService
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
@Override public int setRootDirectory(java.lang.String RootDirectory) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(RootDirectory);
mRemote.transact(Stub.TRANSACTION_setRootDirectory, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setVideoParameter(int cameraId, int capWidth, int capHeight, int capFps, int encBitrate) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(cameraId);
_data.writeInt(capWidth);
_data.writeInt(capHeight);
_data.writeInt(capFps);
_data.writeInt(encBitrate);
mRemote.transact(Stub.TRANSACTION_setVideoParameter, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setAppType(java.lang.String appType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appType);
mRemote.transact(Stub.TRANSACTION_setAppType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setShowMeetingScreenSharing(boolean isShare) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isShare)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setShowMeetingScreenSharing, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setShowMeetingFloat(boolean isShowMeetingFloat) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isShowMeetingFloat)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setShowMeetingFloat, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setSearchContactUrl(java.lang.String ContactUrl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(ContactUrl);
mRemote.transact(Stub.TRANSACTION_setSearchContactUrl, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setInsertContactUrl(java.lang.String ContactUrl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(ContactUrl);
mRemote.transact(Stub.TRANSACTION_setInsertContactUrl, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setContactProvider(java.lang.String authorities) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(authorities);
mRemote.transact(Stub.TRANSACTION_setContactProvider, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setWXAppId(java.lang.String appId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appId);
mRemote.transact(Stub.TRANSACTION_setWXAppId, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setMeetingAdapter(boolean isMeetingAdapter) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isMeetingAdapter)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setMeetingAdapter, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int init(java.lang.String token, java.lang.String userID, java.lang.String userName, java.lang.String masterNps, java.lang.String slaveNps, java.lang.String rootDirectory) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(token);
_data.writeString(userID);
_data.writeString(userName);
_data.writeString(masterNps);
_data.writeString(slaveNps);
_data.writeString(rootDirectory);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void release() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_release, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int creatMeeting(java.util.List<java.lang.String> invitersId, java.lang.String topic, int meetingType, java.lang.String beginDataTime) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStringList(invitersId);
_data.writeString(topic);
_data.writeInt(meetingType);
_data.writeString(beginDataTime);
mRemote.transact(Stub.TRANSACTION_creatMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int joinMeeting(int meetingID, java.lang.String groupId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(meetingID);
_data.writeString(groupId);
mRemote.transact(Stub.TRANSACTION_joinMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int resumeMeeting(int meetingID, java.lang.String groupId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(meetingID);
_data.writeString(groupId);
mRemote.transact(Stub.TRANSACTION_resumeMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getNowMeetings() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNowMeetings, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int incomingCall(java.lang.String inviterId, java.lang.String inviterName, int MeetingId, java.lang.String headUrl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(inviterId);
_data.writeString(inviterName);
_data.writeInt(MeetingId);
_data.writeString(headUrl);
mRemote.transact(Stub.TRANSACTION_incomingCall, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int quitMeeting() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_quitMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int updateToken(java.lang.String token) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(token);
mRemote.transact(Stub.TRANSACTION_updateToken, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setCurrentUser(java.lang.String userID, java.lang.String userName, java.lang.String token) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(userID);
_data.writeString(userName);
_data.writeString(token);
mRemote.transact(Stub.TRANSACTION_setCurrentUser, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int cancelCreatMeeting() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelCreatMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int cancelGetNowMeetings() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelGetNowMeetings, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int cancelJoinMeeting() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelJoinMeeting, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setisAllowMobileNet(boolean isAllowMobileNet) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isAllowMobileNet)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setisAllowMobileNet, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int setSelectSystemCamera(boolean selectSystemCamera) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((selectSystemCamera)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setSelectSystemCamera, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getActiveMeetingId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActiveMeetingId, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setRootDirectory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setVideoParameter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setAppType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setShowMeetingScreenSharing = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setShowMeetingFloat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setSearchContactUrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_setInsertContactUrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setContactProvider = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_setWXAppId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_setMeetingAdapter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_release = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_creatMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_joinMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_resumeMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getNowMeetings = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_incomingCall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_quitMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_updateToken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_setCurrentUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_cancelCreatMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_cancelGetNowMeetings = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_cancelJoinMeeting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_setisAllowMobileNet = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_setSelectSystemCamera = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_getActiveMeetingId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
}
public int setRootDirectory(java.lang.String RootDirectory) throws android.os.RemoteException;
public int setVideoParameter(int cameraId, int capWidth, int capHeight, int capFps, int encBitrate) throws android.os.RemoteException;
public int setAppType(java.lang.String appType) throws android.os.RemoteException;
public int setShowMeetingScreenSharing(boolean isShare) throws android.os.RemoteException;
public int setShowMeetingFloat(boolean isShowMeetingFloat) throws android.os.RemoteException;
public int setSearchContactUrl(java.lang.String ContactUrl) throws android.os.RemoteException;
public int setInsertContactUrl(java.lang.String ContactUrl) throws android.os.RemoteException;
public int setContactProvider(java.lang.String authorities) throws android.os.RemoteException;
public int setWXAppId(java.lang.String appId) throws android.os.RemoteException;
public int setMeetingAdapter(boolean isMeetingAdapter) throws android.os.RemoteException;
public int init(java.lang.String token, java.lang.String userID, java.lang.String userName, java.lang.String masterNps, java.lang.String slaveNps, java.lang.String rootDirectory) throws android.os.RemoteException;
public void release() throws android.os.RemoteException;
public int creatMeeting(java.util.List<java.lang.String> invitersId, java.lang.String topic, int meetingType, java.lang.String beginDataTime) throws android.os.RemoteException;
public int joinMeeting(int meetingID, java.lang.String groupId) throws android.os.RemoteException;
public int resumeMeeting(int meetingID, java.lang.String groupId) throws android.os.RemoteException;
public int getNowMeetings() throws android.os.RemoteException;
public int incomingCall(java.lang.String inviterId, java.lang.String inviterName, int MeetingId, java.lang.String headUrl) throws android.os.RemoteException;
public int quitMeeting() throws android.os.RemoteException;
public int updateToken(java.lang.String token) throws android.os.RemoteException;
public int setCurrentUser(java.lang.String userID, java.lang.String userName, java.lang.String token) throws android.os.RemoteException;
public int cancelCreatMeeting() throws android.os.RemoteException;
public int cancelGetNowMeetings() throws android.os.RemoteException;
public int cancelJoinMeeting() throws android.os.RemoteException;
public int setisAllowMobileNet(boolean isAllowMobileNet) throws android.os.RemoteException;
public int setSelectSystemCamera(boolean selectSystemCamera) throws android.os.RemoteException;
public java.lang.String getActiveMeetingId() throws android.os.RemoteException;
}
