package cn.redcdn.jmeetingsdk;

interface IJMeetingService{
   int setRootDirectory(String RootDirectory);
   int setVideoParameter(int cameraId,int capWidth,int capHeight,int capFps,int encBitrate);
   int setAppType(String appType);
   int setShowMeetingScreenSharing(boolean isShare);
   int setShowMeetingFloat(boolean isShowMeetingFloat);
   int setSearchContactUrl(String ContactUrl);
   int setInsertContactUrl(String ContactUrl);
   int setContactProvider(String authorities);
   int setWXAppId(String appId);
   int setMeetingAdapter(boolean isMeetingAdapter);
   int  init(String token,String userID,String userName,String masterNps, String slaveNps, String rootDirectory);
   void release();
   int creatMeeting(in List<String> invitersId,String topic,int meetingType,String beginDataTime);
   int joinMeeting(int meetingID,String groupId);
   int resumeMeeting(int meetingID,String groupId);
   int getNowMeetings();
   int incomingCall(String inviterId, String inviterName,int MeetingId, String headUrl);
   int quitMeeting();
   int updateToken(String token);
   int setCurrentUser(String userID,String userName,String token);
   int cancelCreatMeeting();
   int cancelGetNowMeetings();
   int cancelJoinMeeting();
   int setisAllowMobileNet(boolean isAllowMobileNet);
   int setSelectSystemCamera(boolean selectSystemCamera);
   String getActiveMeetingId();
}