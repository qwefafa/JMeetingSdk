/*===================================================================
 * 南京青牛通讯技术有限公司
 * 日期：2014-9-11 下午1:36:44
 * 作者：Kevin
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2014-9-11     Kevin      创建
 */
package cn.redcdn.jmeetingsdk.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.datacenter.meetingmanage.AcquireParameter;
import cn.redcdn.datacenter.meetingmanage.data.DeviceResolutionType;
import cn.redcdn.jmeetingsdk.JMeetingService;
import cn.redcdn.jmeetingsdk.MeetingManage;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;

public class SettingData {
  public final String TAG = getClass().getName();
  private Context context;
  
  public static final String VIRTUAL_DEVICE_NUM = "GDDXX1144500045"; //虚拟串号，用于nps获取
  
//  public static final String NpsDeviceType = "Mobile"; //设备类型，用于hvs红云会诊版本获取nps地址
  
  public String JMEETING_WEBSITE = "http://www.jmeeting.cn/"; // 极会诊官网地址
  
  public String RC_URL = "";// 公网

  public String MASTER_MS_URL = ""; // 主后台接口服务器地址
  public String SLAVE_MS_URL = ""; // 从后台接口服务器地址
  
  //双服务器第二个nps地址
  public String SECOND_NPS_URL = null;
  
//public String SECOND_NPS_URL = "http://103.25.23.83:8088/nps_x1/";

  public String NPS_URL = "http://xmeeting.butel.com/nps_x1/";// "http://222.73.29.12/nps_x1";//"http://xmeeting.butel.com/nps_x1/";
                                                              // /"http://114.112.74.14/nps_x1/";//"http://xmeeting.butel.com/nps_x1/";
  // // nps服务器默认地址

  public String ENTERPRISE_CENTER_URL = ""; // 企业用户中心地址
  public String PERSONAL_CENTER_URL = ""; // 个人用户中心地址
  public String PERSION_CONTACT_URL = "";// 个人通讯录地址
  public String UPLOADIMAGEURL = ""; // 头像上传地址
  private String UPLOAD_IMAGE_URL_SUFFIX = "/dfs_upload/NubePhotoUpload"; // 头像上传后缀

  public String DOWNLAOD_LINK = "http://jihuiyi.cn"; // 推荐中的下载地址

  public String HELP_URL;

  public String UPLOADIMGNAME = "file";
  public int tokenUnExist = -902;
  public int tokenInvalid = -903;

  /**
   * 
   * 0: qvga, 320*240
   *
   * 1: vga,  640*480
   *
   * 2: hdpi, 720p
   *
   * 3: xhdpi 1080p
   */
  public int DEVICE_RESOLUTION_TYPE = 1; // 分辨率参数

  public static enum DeviceCategory {
    X1, N8, Mobile
  }

//   public final String DeviceType = "Mobile";
  
  public final String DeviceType = "MobileHvs4";
  
  public int NewRCUrlPort = 9044; // 新路由端口
  public String rootPath = "";
  public String CfgPath = Environment.getExternalStorageDirectory().getPath()
      + "/jmeetingsdk/config"; // 配置文件路径
  public String LogFileOutPath = ""; // 日志输出的路径
  public String CachePath; // 缓存目录路径
  public String logUploadPath = ""; // 上传数据压缩包临时放置目录

  public AppUpdateData AppUpdateConfig; // 应用升级配置
  public MeetingControlData MeetingCtrlConfig; // 会议控制配置
  public DetectData DetectConfig; // 短链配置
  public MediaPlayData MediaPlayConfig; // 流媒体配置
  public QosAgentData QosAgentConfig; // Qos Agent 配置
  public HostAgentData HostAgentConfig; // Host Agent 配置
  public ScreenSharingData ScreenSharingConfig; // 屏幕分享配置
  public AliseServiceData AliseServiceConfig; // 存活服务配置
  public final String customerServicephone = "400-668-2396";// 客服电话
  public final String serviceDeadLine = "2015-06-08";// 服务截至日期
  public NetDetectionData NetDetectionConfig; // 网络检测配置
  public LogUploadData LogUploadConfig; // 日志上传配置

  private static SettingData mInstance;
  public final String AppRestorePath; // app下载保存路径

  public static synchronized SettingData getInstance() {
    if (mInstance == null) {
      mInstance = new SettingData();
    }

    return mInstance;
  }
  
  public void init(Context context,String rootDirectory) {
    this.context = context;
    rootPath = context
            .getDir("jmeetingsdk", Context.MODE_PRIVATE).getAbsolutePath();
        CfgPath = Environment.getExternalStorageDirectory().getPath()
            + "/" + rootDirectory+ "/jmeetingsdk/config"; // 配置文件路径
        LogFileOutPath = Environment.getExternalStorageDirectory().getPath()
            + "/" + rootDirectory + "/jmeetingsdk/log/logwriter";
        CachePath = rootPath + "/cache"; // 缓存目录
       
  }
  
  public void release() {
    
  }

  public String readNpsUrlFromLocal() {
    File file = new File(Environment.getExternalStorageDirectory().getPath()
        + "/jmeetingsdk/config/mobile_nps_address.txt");
    if (!file.exists()) {
      CustomLog.i(TAG, "存放nps地址的本地文件[[[" + file.getAbsolutePath()
          + "]]]不存在!使用默认的nps地址--->" + NPS_URL);
      return NPS_URL;
    }
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String line = br.readLine();
      if (!TextUtils.isEmpty(line)) {
        line = line.trim();
        if (TextUtils.isEmpty(line)) {
          CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
              + "]]]内容为空!使用默认的nps地址--->" + NPS_URL);
        } else {
          NPS_URL = line;
        }
      } else {
        CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
            + "]]]内容为空!使用默认的nps地址--->" + NPS_URL);
      }
    } catch (Exception e) {
      CustomLog.e(TAG, "文件 [[[" + file.getAbsolutePath()
          + "]]] 读取失败!使用默认的nps地址--->" + NPS_URL);
    } finally {
      if (br != null) {
        try {
          br.close();
          br = null;
        } catch (IOException e) {
          CustomLog.e(TAG, "关闭文件流失败!!!");
        }
      }
    }

    return NPS_URL;
  }
  
  public String readSecondNpsUrlFromLocal() {
	    File file = new File(Environment.getExternalStorageDirectory().getPath()
	            + "/jmeetingsdk/config/mobile_second_nps_address.txt");
	    if (!file.exists()) {
	      CustomLog.i(TAG, "存放second nps地址的本地文件[[[" + file.getAbsolutePath()
	          + "]]]不存在!使用默认的second nps地址--->" + SECOND_NPS_URL);
	      return SECOND_NPS_URL;
	    }
	    BufferedReader br = null;
	    try {
	      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	      String line = br.readLine();
	      if (!TextUtils.isEmpty(line)) {
	        line = line.trim();
	        if (TextUtils.isEmpty(line)) {
	          CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
	              + "]]]内容为空!使用默认的second nps地址--->" + SECOND_NPS_URL);
	        } else {
	        	SECOND_NPS_URL = line;
	        }
	      } else {
	        CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
	            + "]]]内容为空!使用默认的second nps地址--->" + SECOND_NPS_URL);
	      }
	    } catch (Exception e) {
	      CustomLog.e(TAG, "文件 [[[" + file.getAbsolutePath()
	          + "]]] 读取失败!使用默认的sdecond nps地址--->" + SECOND_NPS_URL);
	    } finally {
	      if (br != null) {
	        try {
	          br.close();
	          br = null;
	        } catch (IOException e) {
	          CustomLog.e(TAG, "关闭文件流失败!!!");
	        }
	      }
	    }

	    return SECOND_NPS_URL;
	  }

  public void setDeviceResolutionType(DeviceResolutionType type) {
    switch (type) {
    case qvga:
      DEVICE_RESOLUTION_TYPE = 0;
      break;
    case vga:
      DEVICE_RESOLUTION_TYPE = 1;
      break;
    case hdpi:
      DEVICE_RESOLUTION_TYPE = 2;
      break;
    case xhdpi:
      DEVICE_RESOLUTION_TYPE = 3;
      break;
    default:
      DEVICE_RESOLUTION_TYPE = 1;
      break;
    }
  }

  private SettingData() {
    AppUpdateConfig = new AppUpdateData();
    MeetingCtrlConfig = new MeetingControlData();
    DetectConfig = new DetectData();
    MediaPlayConfig = new MediaPlayData();
    QosAgentConfig = new QosAgentData();
    HostAgentConfig = new HostAgentData();
    ScreenSharingConfig = new ScreenSharingData();
    AliseServiceConfig = new AliseServiceData();
    NetDetectionConfig = new NetDetectionData();
    LogUploadConfig = new LogUploadData();

    AppRestorePath = Environment.getExternalStorageDirectory().getPath()
            + "/meeting/appdownload";
        logUploadPath = Environment.getExternalStorageDirectory().getPath()
            + "/meeting";// 上传数据压缩包临时放置目录
  }

  public void setUploadImageUrl(String url) {
    UPLOADIMAGEURL = url + UPLOAD_IMAGE_URL_SUFFIX;
  }

  // 网络检测
  public class NetDetectionData {
    public int srcPort = 6000;
    public String destIp;
    public int destPort = 6001;
  }

  // 获取设备分类
  public DeviceCategory getDeivceCategory() {
    DeviceCategory category = DeviceCategory.N8;
    if (DeviceType.endsWith("JM1") || DeviceType.endsWith("M1")) {
      category = DeviceCategory.X1;
    } else if (DeviceType.endsWith("Mobile")) {
      category = DeviceCategory.Mobile;
    } else if(DeviceType.endsWith("MobileHvs4")) {
      category = DeviceCategory.Mobile;
    }

    return category;
  }

  // 应用升级相关
  public class AppUpdateData {
    public String Master_ServerUrl = ""; // 主服务器地址
    public String Slave_ServerUrl = ""; // 从服务器地址
    public String ProjectName = ""; // 项目名称
    public String DeviceType = SettingData.this.DeviceType; // 设备名称
    public String CheckInterval = "7200"; // 检查版本间隔，单位秒
  }

  // 会议控制相关
  public class MeetingControlData {
    public String AgentClientIp = "127.0.0.1"; // 会议控制Agent客户端Ip
    public int AgentClientPort = 7001; // 会议控制Agent客户端端口
    public String AgentServerIp = "127.0.0.1"; // 会议控制Agent服务器Ip
    public int AgentServerPort = 7000; // 会议控制Agent服务器端端口
  }

  // 短链相关
  public class DetectData {
    public int LocalPort = 9055; // 短链本地端口
    public int LocalLogClentPort = 9066;// 短链日志端口
  }

  // 流媒体相关
  public class MediaPlayData {
    public int VideoPort1 = 8000; // 视频接收端口1
    public int VideoPort2 = 8010; // 视频接收端口2
    public int AudioPort1 = 8020; // 音频接收端口1
    public int AudioPort2 = 8030; // 音频接收端口2
    public int MsOrtp = 8040; // 媒体服务器agent接收ortp端口
    public int MsServer = 8050; // 接收媒体服务器数据流端口
    public int Jfec_in = 5; // fec输入包个数
    public int Jfec_out = 1; // fec输出包个数
    @SuppressLint("SdCardPath")
    public String LibRk264Path = "/data/data/cn.redcdn.meeting/lib";
  }

  // Qos相关
  public class QosAgentData {
    public String QosAgentIp = "127.0.0.1";
    public int QosAgentPort = 40080;
    public String QosServerIp = "127.0.0.1";
    public int QosServerPort = 40080;
    public String MsgDisplayClientIp = "127.0.0.1";
    public int MsgDisplayClientPort = 40091;
  }

  // Host Agent相关
  public class HostAgentData {
    public int LocalPort = 10000;
    public String LocalCmdIp = "127.0.0.1";
    public int LocalCmdPort = 10001;
    public String UICmdIp = "127.0.0.1";
    public int UICmdPort = 10002;
  }

  // 屏幕分享相关
  public class ScreenSharingData {
    public int CmdPort = 5000;
    public int DataPort = 5001;
  }

  // 存活服务相关 TODO 添加到nps服务器
  public class AliseServiceData {
    public int ClientPort = 25001;
    public int ServerPort = 25000;
    public int Interval = 2;
  }

  // 日志上传
  public class LogUploadData {
    public String ServerIP = "192.168.1.1";
    public int ServerPort = 7661;

  }

  // // 解析nps信息
  public void aquireMeetingParameter() {
    String serialNum = "GDDXX1144500045";
    CustomLog.i(TAG, "BootManager::aquireMeetingParameter() 获取NPS参数");

    ConstConfig.npsWebDomain = SettingData.getInstance().readNpsUrlFromLocal();
    CustomLog.i(TAG, "BootManager::aquireMeetingParameter() NPS_URL: "
        + ConstConfig.npsWebDomain + " | serialNum: " + serialNum + " | type: "
        + SettingData.getInstance().DeviceType);
    AcquireParameter ap = new AcquireParameter() {

      @Override
      public void onSuccess(JSONObject bodyObject) {
        if (bodyObject == null) {
          CustomLog.e(TAG,
              "SettingData::aquireMeetingParameter() 获取NPS参数失败，返回为空");
          KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
              + "_fail_"
              + "Mobile_reAquireNps_return==null");
          return;
        }

        try {
          JSONObject paramList = (JSONObject) bodyObject.get("paramList");
          if (null != paramList) {
            // 获取公共参数
            JSONObject commonObj = new JSONObject(paramList.get(
                NpsParamConfig.COMMON).toString());
            if (null != commonObj) {
              SettingData.getInstance().MASTER_MS_URL = commonObj
                  .getString(NpsParamConfig.COMMON_MASTER_MS_URL);
              String slave_ms_url = commonObj
                  .optString(NpsParamConfig.COMMON_SLAVE_MS_URL);
              if (!slave_ms_url.equals("")) {
                SettingData.getInstance().SLAVE_MS_URL = commonObj
                    .getString(NpsParamConfig.COMMON_SLAVE_MS_URL);
              }
              SettingData.getInstance().RC_URL = commonObj
                  .getString(NpsParamConfig.COMMON_RC_URL);
              SettingData.getInstance().ENTERPRISE_CENTER_URL = commonObj
                  .getString(NpsParamConfig.COMMON_EUC_URL);
              SettingData.getInstance().PERSONAL_CENTER_URL = commonObj
                  .getString(NpsParamConfig.COMMON_PUC_URL);
              SettingData.getInstance().PERSION_CONTACT_URL = commonObj
                  .getString(NpsParamConfig.COMMON_Persion_Contact_URL);
              SettingData
                  .getInstance()
                  .setUploadImageUrl(
                      commonObj
                          .getString(NpsParamConfig.COMMON_Persion_Head_Upload_URL));
            }

            // 获取APP升级参数
            JSONObject appUpdateObj = new JSONObject(paramList.get(
                NpsParamConfig.APP_UPDATE).toString());
            if (null != appUpdateObj) {
              // TODO 升级地址nps配置
              SettingData.getInstance().AppUpdateConfig.Master_ServerUrl = appUpdateObj
                  .getString(NpsParamConfig.APP_UPDATE_MASTER_ServerUrl);
              String slave_serverUrl = appUpdateObj
                  .optString(NpsParamConfig.APP_UPDATE_SLAVE_ServerUrl);
              
              if (!slave_serverUrl.equals("")) {
                SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl = slave_serverUrl;
              }
              
              SettingData.getInstance().AppUpdateConfig.ProjectName = appUpdateObj
                  .getString(NpsParamConfig.APP_UPDATE_ProjectName);
//              SettingData.getInstance().AppUpdateConfig.DeviceType = "MOBILE";
              SettingData.getInstance().AppUpdateConfig.DeviceType = SettingData.this.DeviceType;
              SettingData.getInstance().AppUpdateConfig.CheckInterval = appUpdateObj
                  .getString(NpsParamConfig.APP_UPDATE_CheckInterval);
            }

            // 获取流媒体参数
            JSONObject mediaPlayObj = new JSONObject(paramList.get(
                NpsParamConfig.MediaPlay).toString());
            if (null != mediaPlayObj) {
              SettingData.getInstance().MediaPlayConfig.Jfec_in = mediaPlayObj
                  .getInt(NpsParamConfig.MediaPlay_Jfec_in);
              SettingData.getInstance().MediaPlayConfig.Jfec_out = mediaPlayObj
                  .getInt(NpsParamConfig.MediaPlay_Jfec_out);
            }

            // 获取帮助参数
            JSONObject helpObj = new JSONObject(paramList.get(
                NpsParamConfig.HELP).toString());
            if (null != helpObj) {
              SettingData.getInstance().HELP_URL = helpObj
                  .getString(NpsParamConfig.HELP_URL);

              SettingData.getInstance().DOWNLAOD_LINK = helpObj
                  .getString(NpsParamConfig.HELP_DOWNLOAD_LINK);
            }

            // 获取日志上传参数
            JSONObject logUploadObj = new JSONObject(paramList.get(
                NpsParamConfig.LogUpload).toString());
            if (null != logUploadObj) {
              SettingData.getInstance().LogUploadConfig.ServerIP = logUploadObj
                  .getString(NpsParamConfig.LogUpload_serverIp);
              SettingData.getInstance().LogUploadConfig.ServerPort = logUploadObj
                  .getInt(NpsParamConfig.LogUpload_serverPort);
            }

            SettingData.getInstance().LogConfig();
            initHttpRequestConfig();
            KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
                + "_ok_"
                + "Mobile_reAquireNps");
          }
        } catch (JSONException e) {
          CustomLog.e(TAG, e.getMessage());
          KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
              + "_fail_"
              + "Mobile_reAquireNps_jsonError");
        } catch (ClassCastException e) {
          CustomLog.e(TAG, e.getMessage());
          KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
              + "_fail_"
              + "Mobile_reAquireNps_jsonError");
        }
      }

      @Override
      public void onFail(int statusCode, String statusInfo) {
        CustomLog.e(TAG,
            "SettingData::aquireMeetingParameter() 获取NPS参数失败 statusCode: "
                + statusCode + " | statusInfo: " + statusInfo);
        KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
            + "_fail_"
            + "Mobile_reAquireNps_" + statusCode + "_" + statusInfo);
      }

    };

    ArrayList<String> requestList = new ArrayList<String>();

    requestList.add(NpsParamConfig.COMMON);
    requestList.add(NpsParamConfig.APP_UPDATE);
    requestList.add(NpsParamConfig.MediaPlay);
    requestList.add(NpsParamConfig.HELP);
    requestList.add(NpsParamConfig.LogUpload);

    ap.acquire(requestList, SettingData.getInstance().DeviceType, serialNum);
  }

  // 初始化 DataCenter 数据
  private void initHttpRequestConfig() {
    CustomLog.i(TAG, "SettingData::initHttpRequestConfig() 初始化 DataCenter 中配置");

    ConstConfig.masterBmsWebDomain = SettingData.getInstance().MASTER_MS_URL;
    ConstConfig.slaveBmsWebDomain = SettingData.getInstance().SLAVE_MS_URL;
    ConstConfig.enterPriseUserCenterWebDomain = SettingData.getInstance().ENTERPRISE_CENTER_URL;
    ConstConfig.personalUserCenterWebDomain = SettingData.getInstance().PERSION_CONTACT_URL;
    ConstConfig.personalContactWebDomain = SettingData.getInstance().PERSION_CONTACT_URL;
    ConstConfig.masterAppUpdateServerWebDomain = SettingData.getInstance().AppUpdateConfig.Master_ServerUrl;
    ConstConfig.slaveAppUpdateServerWebDomain = SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl;
  }

  // 打印配置信息
  public void LogConfig() {
    CustomLog.i(TAG, "SettingData 配置参数");
    CustomLog.i(TAG, "设备类型： " + DeviceType + " | 类别：" + getDeivceCategory());
    CustomLog.i(TAG, "NPS 参数服务器地址：" + NPS_URL);
    CustomLog.i(TAG, "主后台服务器地址：" + MASTER_MS_URL);
    CustomLog.i(TAG, "从后台服务器地址：" + SLAVE_MS_URL);
    CustomLog.i(TAG, "企业用户中心地址：" + ENTERPRISE_CENTER_URL);
    CustomLog.i(TAG, "个人用户中心地址：" + PERSONAL_CENTER_URL);
    CustomLog.i(TAG, "个人通讯录服务器地址：" + PERSION_CONTACT_URL);
    CustomLog.i(TAG, "头像上传服务器地址：" + UPLOADIMAGEURL);
    CustomLog.i(TAG, "RC 地址：" + RC_URL);
    CustomLog.i(TAG, "设备类型：" + DeviceType);
    CustomLog.i(TAG, "RC 端口：" + NewRCUrlPort);
    CustomLog.i(TAG, "CfgPath：" + CfgPath);
    CustomLog.i(TAG, "LogFileOutPath：" + LogFileOutPath);
    CustomLog.i(TAG, "CachePath：" + CachePath);
    CustomLog.i(TAG, "AppRestorePath: " + AppRestorePath);
    CustomLog.i(TAG, "帮助页面：" + HELP_URL);
    CustomLog.i(TAG, "推荐中应用下载地址：" + DOWNLAOD_LINK);

    CustomLog.i(TAG, "App升级配置参数");
    CustomLog.i(TAG, "主App升级 服务器地址： " + AppUpdateConfig.Master_ServerUrl);
    CustomLog.i(TAG, "从App升级 服务器地址： " + AppUpdateConfig.Slave_ServerUrl);
    CustomLog.i(TAG, "App升级 项目名称： " + AppUpdateConfig.ProjectName);
    CustomLog.i(TAG, "App升级 设备类型： " + AppUpdateConfig.DeviceType);
    CustomLog.i(TAG, "App升级检测时间间隔： " + AppUpdateConfig.CheckInterval);

    CustomLog.i(TAG, "会议控制配置参数");
    CustomLog.i(TAG, "会议控制 ServerIp： " + MeetingCtrlConfig.AgentServerIp);
    CustomLog.i(TAG, "会议控制 ServerPort： " + MeetingCtrlConfig.AgentServerPort);
    CustomLog.i(TAG, "会议控制 ClientIp： " + MeetingCtrlConfig.AgentClientIp);
    CustomLog.i(TAG, "会议控制 ClientPort： " + MeetingCtrlConfig.AgentClientPort);

    CustomLog.i(TAG, "流媒体参数");
    CustomLog.i(TAG, "流媒体 视频接收端口1： " + MediaPlayConfig.VideoPort1);
    CustomLog.i(TAG, "流媒体 视频接收端口2： " + MediaPlayConfig.VideoPort2);
    CustomLog.i(TAG, "流媒体 音频接收端口1： " + MediaPlayConfig.AudioPort1);
    CustomLog.i(TAG, "流媒体 音频接收端口2： " + MediaPlayConfig.AudioPort1);
    CustomLog.i(TAG, "流媒体 接收ortp端口： " + MediaPlayConfig.MsOrtp);
    CustomLog.i(TAG, "流媒体 服务器数据流端口： " + MediaPlayConfig.MsServer);
    CustomLog.i(TAG, "流媒体 fec输入包个数： " + MediaPlayConfig.Jfec_in);
    CustomLog.i(TAG, "流媒体 fec输出包个数： " + MediaPlayConfig.Jfec_out);
    CustomLog.i(TAG, "流媒体 LibRk264Path： " + MediaPlayConfig.LibRk264Path);

    CustomLog.i(TAG, "Host Agent配置参数");
    CustomLog.i(TAG, "Host Agent LocalPort： " + HostAgentConfig.LocalPort);
    CustomLog.i(TAG, "Host Agent LocalCmdIp： " + HostAgentConfig.LocalCmdIp);
    CustomLog
        .i(TAG, "Host Agent LocalCmdPort： " + HostAgentConfig.LocalCmdPort);
    CustomLog.i(TAG, "Host Agent UICmdIp： " + HostAgentConfig.UICmdIp);
    CustomLog.i(TAG, "Host Agent UICmdPort： " + HostAgentConfig.UICmdPort);
  }
}
