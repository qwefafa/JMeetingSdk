/*===================================================================
 * 南京青牛通讯技术有限公司
 * 日期：2014-10-9 下午10:44:43
 * 作者：Kevin
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2014-10-9     Kevin      创建
 */
package cn.redcdn.jmeetingsdk.config;

public class NpsParamConfig {
  // 公共参数
  public static final String COMMON = "X1MeetingCommon";
  public static final String COMMON_RC_URL = "RC_URL"; // STP 地址
  public static final String COMMON_MASTER_MS_URL = "MS_URL"; //主会议接口地址
  public static final String COMMON_SLAVE_MS_URL = "SLAVE_MS_URL"; // 从会议接口地址
  
  public static final String COMMON_EUC_URL = "EUC_URL"; // 企业用户中心地址
  public static final String COMMON_PUC_URL = "PUC_URL"; // 个人用户中心地址
  public static final String COMMON_Persion_Contact_URL = "Persion_Contact_URL";  // 个人通讯录地址
  public static final String COMMON_Persion_Head_Upload_URL = "Head_Upload_URL";  // 头像上传服务器地址
  

  // 应用升级
  public static final String APP_UPDATE = "X1MeetingAppUpdate";
  public static final String APP_UPDATE_MASTER_ServerUrl = "ServerUrl";
  public static final String APP_UPDATE_SLAVE_ServerUrl = "SLAVE_ServerUrl";
  public static final String APP_UPDATE_ProjectName = "ProjectName";
  public static final String APP_UPDATE_CheckInterval = "CheckInterval";
  public static final String APP_UPDATE_DeviceType = "DeviceType";

  // 流媒体
  public static final String MediaPlay = "X1MeetingMediaPlay";
  public static final String MediaPlay_Jfec_in = "Jfec_in";
  public static final String MediaPlay_Jfec_out = "Jfec_out";

  // 帮助
  public static final String HELP = "X1MeetingHelp";
  public static final String HELP_URL = "Mobile_Help_Url";
  
  // 推荐中使用的应用下载地址
  public static final String HELP_DOWNLOAD_LINK = "Mobile_Download_Link";
  
  
  // 日志上传
  public static final String  LogUpload = "JustMeetingLogUpload";
  public static final String  LogUpload_serverIp = "ServerIP";
  public static final String  LogUpload_serverPort = "ServerPort";
  
  // 极会诊官网
  public static final String HELP_WEBSITE = "JMeeting_Website";
}
