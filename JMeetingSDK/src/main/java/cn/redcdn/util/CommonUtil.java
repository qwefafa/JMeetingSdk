package cn.redcdn.util;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.log.CustomLog;


@SuppressLint("SimpleDateFormat")

public class CommonUtil {



  public static final int REQUEST_CODE_NETWORKSETTINGACTIVITY = 102;

  public static final int REQUEST_CODE_DETAILACTIVITY = 101;



  private static final String LOGIN_PAGE = "com.channelsoft.android.BOOTWIZARD_WELCOME_LOGIN_ACTIVITY";

  private static final String NEW_MESSAGE_ACTION = "channelsoft.intent.action.NEW_MESSAGE";

  private static final int VIDEO_NUMBER_NOT_LOGIN = 14;



  public static final int EC_MODULELOAD = 39400; // LOAD FAILED

  public static final int EC_MEDIABUSYING = 39401; // mediaplayctrl busying

  public static final int EC_MODULEERR = 39402; // 影射 dll 失败

  public static final int EC_NETSETTING = 39403; // 正在设置网络

  public static final int EC_FAIL = 39404; // 失败

  public static final int EC_NOCONN = 39405; // 连接未建立

  public static final int EC_ALERADYCONN = 39406; // 已经连接状态

  public static final int EC_CONNING = 39407; // 正在连接

  public static final int EC_READMEETCFGERR = 39408; // 会议配置错误

  public static final int EC_UPDATING = 39409; // 正在检查更新

  public static final int EC_CMDIDERR = 39410; // 命令ID错

  public static final int EC_MSGERROR = 39411; // 消息错误

  public static final int EC_PARAMERR = 39412; // 参数错误

  public static final int EC_TIMEOUT = 39413; // 超时

  public static final int EC_N8NOMEM = 39414; // n8侧内存不足

  public static final int EC_BOXNOMEM = 39415; // box 侧内存不足

  public static final int EC_GETWLANFAIL = 39419; // 获取无线错误.

  public static final int EC_WINDOWSFAIL = 39420; // 窗口错误

  public static final int EC_MDPLAYCTRLFAIL = 39421; // mediaplay error

  public static final int EC_MEPLAYNOTINIT = 39422; // mediaplay not init

  public static final int EC_NOMEDIAIP = 39423; // mediaplay ip is not

  public static final int EC_SPKUPLIMITED = 39424; // speaker more than one

  public static final int EC_CONNERR = 39425; // conn guid error

  public static final int EC_NOTSTART = 39426; // 会议主机未启动。

  public static final int EC_SETMODEBUSY = 39427; // 忙，不能设置模式

  public static final int EC_QUETIMEOUT = 39428; // time out in que

  public static final int EC_SETDEVLOSTFUNNULL = 39429; // 设置设备接口不存在



  public static final int MS_SUCCESS = 0; // 成功

  public static final int MS_TOKEN_ERROR = -900; // Token不存在或过期

  public static final int MS_BOXID_ERROR = -901; // 会议主机Id不存在

  public static final int MS_PD_ERROR = -902; // 密码错误

  public static final int MS_OLD_PD_ERROR = -903; // 旧密码错误

  public static final int MS_NEW_PD_ERROR = -904; // 新密码不合法

  public static final int MS_MEETINGTIME_ERROR = -905; // 会议时间重合

  public static final int MS_MEETINGID_ERROR = -906; // 无效的会议Id

  public static final int MS_MEETING_ALREDY_BEGIN = -907; // 会议已是“开始”状态

  public static final int MS_MEETING_ALREDY_END = -908; // 会议已是“结束”状态

  public static final int MS_PHONEID_ERROR = -909; // 无效的终端账号

  public static final int MS_COMPANY_ERROR = -910; // 无法找到企业账号

  public static final int MS_MEETING_TIME_ERROR = -911; // 会议时间重合

  public static final int MS_TOKEN_PHONEID_ERROR = -912; // 无法找到Token对应终端账号

  public static final int MS_PHONEID_PD_ERROR = -913; // 登录账号不存在或密码错误

  public static final int MS_PHONEID_VALID_ERROR = -914; // 视频账号无参会权

  public static final int MS_PHONEID_TIME_ERROR = -915; // 终端账号未到启用时间

  public static final int MS_PHONEID_TIMEOUT_ERROR = -916; // 终端账号已过期

  // public static final int MS_MEETING_TIME_ERROR = -917; // 设置相同的会议状态

  public static final int MS_PHONEID_NO_GET_POWER_ERROR = -918; // 帐号无权限获取该会议

  public static final int MS_PHONEID_NO_ATTEND_POWER_ERROR = -919; // 视频账号无参会权

  public static final int MS_PHONEID_NO_CREATE_POWER_ERROR = -920; // 无权限创建会议



  public static final int MS_CANCEL = -1; // 请求已取消

  public static final int MS_NO_REQUEST_DATA = -2; // 请求服务器无相应

  public static final int MS_JSON_ERROR = -3; // 错误的数据格式

  public static final int MS_PARAMS_INIT_ERROR = -4; // 初始化参数错误

  public static final int MS_OTHER_REASON = -100; // 其他错误



  public static final int MS_NET_ERROR = 3; // 网络请求失败



  /*********************************************** public ***********************************************/

  public static void logOut(Context context) {

    accountLogout(context);

  }



  public static void goToLoginPage(Context context) {

    Intent intent = new Intent(LOGIN_PAGE);

    context.startActivity(intent);

  }



  public static void goToVideoCallPage(Context context) {

    String action = "com.android.settings.NubeVisualTelephoneSettings";

    Intent intent = new Intent();

    intent.setAction(action);

    context.startActivity(intent);

  }



  /**

   * 得到自定义的progressDialog

   * 

   * @param context

   * @param msg

   * @return

   */

  public static Dialog createLoadingDialog(Context context, String msg) {

    LayoutInflater inflater = LayoutInflater.from(context);

    View v = inflater.inflate(MResource.getIdByName(context, MResource.LAYOUT, "jmeetingsdk_loading_dialog"), null);

    ((RelativeLayout) v).setGravity(Gravity.CENTER);
         
    RelativeLayout layout = (RelativeLayout) v.findViewById(MResource.getIdByName(context, MResource.ID, "dialog_view"));

    layout.setGravity(Gravity.CENTER);  
    
    ImageView spaceshipImage = (ImageView) v.findViewById(MResource.getIdByName(context, MResource.ID, "img"));

    TextView tipTextView = (TextView) v.findViewById(MResource.getIdByName(context, MResource.ID, "tipTextView"));



    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,

        MResource.getIdByName(context, MResource.ANIM,"jmeetingsdk_loading_animation"));



    spaceshipImage.startAnimation(hyperspaceJumpAnimation);



    if (msg == null || msg.equals("")) {

      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(

          LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

      params.addRule(RelativeLayout.CENTER_IN_PARENT);

      spaceshipImage.setLayoutParams(params);

    }



    tipTextView.setText(msg);



    Dialog loadingDialog = new Dialog(context, MResource.getIdByName(context, MResource.STYLE,"jmetingsdk_loading_dialog"));

    loadingDialog.setCanceledOnTouchOutside(false);

    loadingDialog.setCancelable(false);

    loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(

        LinearLayout.LayoutParams.MATCH_PARENT,

        LinearLayout.LayoutParams.MATCH_PARENT));

    return loadingDialog;



  }



  public static Dialog createLoadingDialog(Context context, String msg,

      DialogInterface.OnCancelListener listener) {

    LayoutInflater inflater = LayoutInflater.from(context);

    View v = inflater.inflate(MResource.getIdByName(context, MResource.LAYOUT, "jmeetingsdk_loading_dialog"), null);

    ((RelativeLayout) v).setGravity(Gravity.CENTER);
         
    RelativeLayout layout = (RelativeLayout) v.findViewById(MResource.getIdByName(context, MResource.ID, "dialog_view"));

    layout.setGravity(Gravity.CENTER);  
    
    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(

    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    params1.addRule(RelativeLayout.CENTER_IN_PARENT);

    layout.setLayoutParams(params1);
   
    ImageView spaceshipImage = (ImageView) v.findViewById(MResource.getIdByName(context, MResource.ID, "img"));

    TextView tipTextView = (TextView) v.findViewById(MResource.getIdByName(context, MResource.ID, "tipTextView"));



    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,

        MResource.getIdByName(context, MResource.ANIM,"jmeetingsdk_loading_animation"));



    if (msg == null || msg.equals("")) {

      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(

          LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

      params.addRule(RelativeLayout.CENTER_IN_PARENT);

      spaceshipImage.setLayoutParams(params);

    }



    spaceshipImage.startAnimation(hyperspaceJumpAnimation);

    tipTextView.setText(msg);



    Dialog loadingDialog = new Dialog(context, MResource.getIdByName(context, MResource.STYLE,"jmetingsdk_loading_dialog"));



    loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(

        LinearLayout.LayoutParams.MATCH_PARENT,

        LinearLayout.LayoutParams.MATCH_PARENT));

     loadingDialog.setCanceledOnTouchOutside(false);



    if (listener != null) {

      loadingDialog.setOnCancelListener(listener);

    }



    return loadingDialog;



  }



  /*********************************************** private ***********************************************/

  private static void accountLogout(final Context context) {

    AccountManager am = AccountManager.get(context);

    Account[] accounts = am.getAccountsByType("com.channelsoft");

    if (accounts != null && accounts.length != 0) {

      am.removeAccount(accounts[0], new AccountManagerCallback<Boolean>() {

        @Override

        public void run(AccountManagerFuture<Boolean> arg0) {

          Intent intent = new Intent();

          intent.setAction("channelsoft.intent.action.USER_CHANGE");

          context.sendBroadcast(intent);

          sendNoAccountBroadcast(context);

        }

      }, null);

    }

  }



  private static void sendNoAccountBroadcast(Context context) {

    Intent intent = new Intent();

    intent.setAction(NEW_MESSAGE_ACTION);

    intent.putExtra("TYPE", String.valueOf(VIDEO_NUMBER_NOT_LOGIN));

    intent.putExtra("TIME", formatSystemTime());

    intent.putExtra("TITLE", "VIDEO_NUMBER_NOT_LOGIN");

    intent.putExtra("CONTENT", "");

    intent.putExtra("ACTION",

        "com.channelsoft.android.BOOTWIZARD_WELCOME_LOGIN_ACTIVITY");

    context.sendBroadcast(intent);

  }



  @SuppressLint("SimpleDateFormat")

  private static String formatSystemTime() {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date curDate = new Date(System.currentTimeMillis());

    String str = formatter.format(curDate);

    return str;

  }



  /* 为wifi信号时，网络接口返回的类型基本为wlan0

  * 为移动网络时，网络接口返回类型主要集中在：rmnet0、rmnet0_usb0、ccmni0

  * 不排除其他手机在使用移动网络时返回的网络接口类型为其他类型

  * 所以"wlan0"之外的类型，暂时用"unwlan"标识

  * 取IP时， 如果根据wlan0、rmnet0、rmnet0_usb0、ccmni0能获取到，则返回，如果不能，再用之前的方法遍历出IP	

  */

//  public static String getLocalIpAddress(Context context) {
//
//    CustomLog.i("CommonUtil", "CommonUtil::getLocalIpAddress())");
//
//    String localIp = getIpAddress(context);
//
//    if(null == localIp || localIp.equals("")){
//
//    try {
//
//      for (Enumeration<NetworkInterface> en = NetworkInterface
//
//          .getNetworkInterfaces(); en.hasMoreElements();) {
//
//        NetworkInterface intf = en.nextElement();
//
//        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
//
//            .hasMoreElements();) {
//
//          InetAddress inetAddress = enumIpAddr.nextElement();
//
//
//
//          String ip = inetAddress.getHostAddress().toString();
//
//          
//
//          CustomLog.i("CommonUtil", "CommonUtil::getLocalIpAddress()  ip:"
//
//              + ip
//
//              + " is: " + inetAddress.isSiteLocalAddress());
//
//
//
//          if (!inetAddress.isLoopbackAddress()
//
//              && !inetAddress.isLinkLocalAddress()
//
//              && inetAddress.isSiteLocalAddress()
//
//              && !("10.0.2.15").equals(ip)) {
//
//             return inetAddress.getHostAddress().toString();
//
//          }
//
//        }
//
//      }
//
//    } catch (java.net.SocketException ex) {
//
//      CustomLog.d("getLocalIpAddress", ex.toString());
//
//    }
//
//    return "";
//
//    }else{
//
//    	return localIp;
//
//    }
//
//  }
  
  
  public static String getLocalIpAddress(Context context) {
	  return NetConnectHelper.getLocalIp(context);
  }
  

 // 获取手机网络类型接口

  public static int getNetType(Context context){

	  int type = -1;

	  int netType ;

	  ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

	  NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

	  if (null != networkInfo) {

		  netType = networkInfo.getType();

		  if (ConnectivityManager.TYPE_WIFI == netType) {

			  CustomLog.i("CommonUtil","TYPE_WIFI == netType");

			  type = 1;

		  }else if(ConnectivityManager.TYPE_MOBILE == netType){

			  CustomLog.i("CommonUtil","TYPE_MOBILE == netType");

			  type = 0;

		  }

	  }else{

		  CustomLog.e("CommonUtil","null == networkInfo");

		  type = -1;

	  }

	  return type;

  }

  

/*

 * 新增获取IP接口

 */



  public static String getIpAddress(Context context) {

	    Enumeration<NetworkInterface> netInterfaces = null;

	    String mType = ""; // 网络类型



	    int type = getNetType(context);

	    if(type == 1){

	    	 mType = "wlan0";  //  标识是WIFI网络

	    }else if(type == 0){

	    	 mType = "unwlan"; //  标识是手机网络

	    }else{

	    	return "";

	    }

	    try {

	        netInterfaces = NetworkInterface.getNetworkInterfaces();

//	        while (netInterfaces.hasMoreElements()) {

//	          NetworkInterface intf = netInterfaces.nextElement();

//	          if (intf.getName().toLowerCase().equals(mType)) {

//	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr

//	                .hasMoreElements();) {

//	              InetAddress inetAddress = enumIpAddr.nextElement();

//	              if (!inetAddress.isLoopbackAddress()) {

//	                String ipaddress = inetAddress.getHostAddress().toString();

//	                if (!ipaddress.contains("::")&&  // ipV6的地址

//	                	!("10.0.2.15").equals(ipaddress)) {

//	                	CustomLog.i("CommonUtil","IpAddress = "+ipaddress);

//	                	return ipaddress;

//	                }

//	              }

//	            }

//	          }else if (intf.getName().toLowerCase().equals("rmnet0")||

//	        		  intf.getName().toLowerCase().equals("ccmni0")){

//	        	  for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr

//	  	                .hasMoreElements();) {

//	  	              InetAddress inetAddress = enumIpAddr.nextElement();

//	  	              if (!inetAddress.isLoopbackAddress()) {

//	  	                String ipaddress = inetAddress.getHostAddress().toString();

//	  	                if (!ipaddress.contains("::")&&  // ipV6的地址

//	  	                	!("10.0.2.15").equals(ipaddress)) {

//	  	                	CustomLog.i("CommonUtil","IpAddress = "+ipaddress);

//	  	                  return ipaddress;

//	  	                }

//	  	              }

//	  	            }

//	          }

//	        }

	        if(mType.equals("wlan0")){

		        while (netInterfaces.hasMoreElements()) {

			          NetworkInterface intf = netInterfaces.nextElement();

			          if (intf.getName().toLowerCase().equals("waln0")) {

			            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr

			                .hasMoreElements();) {

			              InetAddress inetAddress = enumIpAddr.nextElement();

			              if (!inetAddress.isLoopbackAddress()) {

			                String ipaddress = inetAddress.getHostAddress().toString();

			                if (!ipaddress.contains("::")&&  // ipV6的地址

			                	!("10.0.2.15").equals(ipaddress)) {

			                	CustomLog.i("CommonUtil","IpAddress = "+ipaddress);

			                	return ipaddress;

			                }

			              }

			            }

			          }

			        }

	        }else{

	            while (netInterfaces.hasMoreElements()) {

			          NetworkInterface intf = netInterfaces.nextElement();

			          if (intf.getName().toLowerCase().equals("rmnet0")||

			        		  intf.getName().toLowerCase().equals("ccmni0")||

			        		  intf.getName().toLowerCase().equals("rmnet0_usb0")) {

			            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr

			                .hasMoreElements();) {

			              InetAddress inetAddress = enumIpAddr.nextElement();

			              if (!inetAddress.isLoopbackAddress()) {

			                String ipaddress = inetAddress.getHostAddress().toString();

			                if (!ipaddress.contains("::")&&  // ipV6的地址

			                	!("10.0.2.15").equals(ipaddress)) {

			                	CustomLog.i("CommonUtil","IpAddress = "+ipaddress);

			                	return ipaddress;

			                }

			              }

			            }

			          }

			        }

	        }

	      } catch (Exception e) {

	        e.printStackTrace();

	      }

	      return "";

 }

  

  public static boolean isValid(String string) {

    if (string == null || "".equals(string)) {

      return false;

    }

    return true;

  }



  public static String GetBoxAgentErrorDesp(int errorCode) {

    switch (errorCode) {

    case EC_MODULELOAD:

      return "LOAD FAILED";

    case EC_MODULEERR:

      return "影射 dll 失败";

    case EC_MEDIABUSYING:

      return "meidaplaycontrol is busying";

    case EC_NETSETTING:

      return "正在设置网络";

    case EC_FAIL:

      return "失败";

    case EC_NOCONN:

      return "连接未建立";

    case EC_ALERADYCONN:

      return "已经连接状态";

    case EC_CONNING:

      return "正在连接";

    case EC_READMEETCFGERR:

      return "会议配置错误";

    case EC_UPDATING:

      return "正在检查更新";

    case EC_CMDIDERR:

      return "命令ID错";

    case EC_MSGERROR:

      return "消息错误";

    case EC_PARAMERR:

      return "参数错误";

    case EC_TIMEOUT:

      return "超时";

    case EC_N8NOMEM:

      return "n8侧内存不足";

    case EC_BOXNOMEM:

      return "box 侧内存不足";

    case EC_GETWLANFAIL:

      return "无线网卡获取失败";

    case EC_WINDOWSFAIL:

      return "获取无线错误";

    case EC_MDPLAYCTRLFAIL:

      return "mediaplay error";

    case EC_MEPLAYNOTINIT:

      return "mediaplay not init";

    case EC_NOMEDIAIP:

      return "mediaplay ip is not";

    case EC_SPKUPLIMITED:

      return "speaker more than one";

    case EC_CONNERR:

      return "conn guid error";

    case EC_NOTSTART:

      return "会议主机未启动";

    case EC_SETMODEBUSY:

      return "忙，不能设置模式";

    case EC_QUETIMEOUT:

      return "time out in que";

    case EC_SETDEVLOSTFUNNULL:

      return "设置设备接口不存在";



    default:

      return "未知错误!";

    }

  }



  public static String getTime(String time) {

    if (time == null || time.equals("")) {

      return "";

    }



    time = time + "000";

    Date date = new Date(Long.valueOf(time));

    SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    String result = format.format(date);

    return result;

  }



  public static String getMSErrorMessage(int errorCode) {



    switch (errorCode) {

    case MS_SUCCESS: // 成功

      return "成功";

    case MS_TOKEN_ERROR: // Token不存在或过期

      return "Token不存在或过期";

    case MS_BOXID_ERROR: // 会议主机Id不存在

      return "会议主机Id不存在";

    case MS_PD_ERROR: // 密码错误

      return "密码不正确!";

    case MS_OLD_PD_ERROR: // 旧密码错误

      return "旧密码错误!";

    case MS_NEW_PD_ERROR: // 新密码不合法

      return "新密码不合法!";

    case MS_MEETINGTIME_ERROR: // 会议时间重合

      return "会议时间重合";

    case MS_MEETINGID_ERROR: // 无效的会议Id

      return "会议号不存在";

    case MS_MEETING_ALREDY_BEGIN: // 会议已是“开始”状态

      return "会议已是开始状态";

    case MS_MEETING_ALREDY_END: // 会议已是“结束”状态

      return "会议已是结束状态";

    case MS_PHONEID_ERROR: // 无效的终端账号

      return "无效的终端账号";

    case MS_COMPANY_ERROR: // 无法找到企业账号

      return "无法找到企业账号";

    case MS_MEETING_TIME_ERROR: // 会议时间重合

      return "会议时间重合";

    case MS_TOKEN_PHONEID_ERROR: // 无法找到Token对应终端账号

      return "无法找到Token对应终端账号";

    case MS_PHONEID_PD_ERROR: // 登录账号不存在或密码错误

      return "登录账号不存在或密码错误";

    case MS_PHONEID_VALID_ERROR: // 视频账号无参会权

      return "视频账号无参会权限";



    case MS_PHONEID_TIME_ERROR: // 终端账号未到启用时间

      return "终端账号未到启用时间";

    case MS_PHONEID_TIMEOUT_ERROR: // 终端账号已过期

      return "终端账号已过期";

    case MS_PHONEID_NO_GET_POWER_ERROR: // 帐号无权限获取该会议

      return "帐号无权限获取该会议";

    case MS_PHONEID_NO_CREATE_POWER_ERROR: // 无权限创建会议

      return "无权限创建会议";

    case MS_PHONEID_NO_ATTEND_POWER_ERROR: // 视频账号无参会权

      return "视频账号无参会权";



    case MS_CANCEL:

      return "请求已取消";

    case MS_NO_REQUEST_DATA:

      return "无法连接外网,请检查网络!";

    case MS_JSON_ERROR:

      return "错误的数据格式";

    case MS_PARAMS_INIT_ERROR:

      return "初始化参数错误";

    case MS_OTHER_REASON:

      return "其他错误";



    case MS_NET_ERROR:

      return "网络请求失败";



    default:

      return "未知错误!";

    }



  }



  public static String formatDate(Date date, String formatString) {

    SimpleDateFormat sdf = new SimpleDateFormat(formatString);

    return sdf.format(date);

  }



  public static Date formatDate(String dateString, String formatString) {

    SimpleDateFormat sdf = new SimpleDateFormat(formatString);

    try {

      return sdf.parse(dateString);

    } catch (ParseException e) {

      e.printStackTrace();

    }

    return null;

  }



  public static String formatMeetingTime(Date beginTime) {

    Calendar begin = Calendar.getInstance();

    begin.setTime(beginTime);



    int beginDay = begin.get(Calendar.DAY_OF_MONTH);



    int beginMonth = begin.get(Calendar.MONTH) + 1;

    int beginHour = begin.get(Calendar.HOUR_OF_DAY);

    int beginMinute_ = begin.get(Calendar.MINUTE);

    String beginMinute = beginMinute_ < 10 ? ("0" + beginMinute_)

        : ("" + beginMinute_);



    String resultStr = "";



    resultStr = beginMonth + "月" + beginDay + "日" + beginHour + ":"

        + beginMinute;



    return resultStr;

  }



  public static String formatMeetingTimeWithBlank(Date beginTime) {

    Calendar begin = Calendar.getInstance();

    begin.setTime(beginTime);



    int beginDay = begin.get(Calendar.DAY_OF_MONTH);



    int beginMonth = begin.get(Calendar.MONTH) + 1;

    int beginHour = begin.get(Calendar.HOUR_OF_DAY);

    int beginMinute_ = begin.get(Calendar.MINUTE);

    String beginMinute = beginMinute_ < 10 ? ("0" + beginMinute_)

        : ("" + beginMinute_);



    String resultStr = "";



    resultStr = beginMonth + "月" + beginDay + "日  " + beginHour + ":"

        + beginMinute;



    return resultStr;

  }



  public static String formatMeetingTime(Date beginTime, Date endTime) {



    if (beginTime == null || endTime == null) {

      return "";

    }



    Calendar begin = Calendar.getInstance();

    begin.setTime(beginTime);

    Calendar end = Calendar.getInstance();

    end.setTime(endTime);



    int beginYear = begin.get(Calendar.YEAR);

    int endYear = end.get(Calendar.YEAR);

    int beginDay = begin.get(Calendar.DAY_OF_MONTH);

    int endDay = end.get(Calendar.DAY_OF_MONTH);



    int beginMonth = begin.get(Calendar.MONTH) + 1;

    int beginHour = begin.get(Calendar.HOUR_OF_DAY);

    int beginMinute_ = begin.get(Calendar.MINUTE);

    String beginMinute = beginMinute_ < 10 ? ("0" + beginMinute_)

        : ("" + beginMinute_);



    int endMonth = end.get(Calendar.MONTH) + 1;

    int endHour = end.get(Calendar.HOUR_OF_DAY);

    int endMinute_ = end.get(Calendar.MINUTE);

    String endMinute = endMinute_ < 10 ? ("0" + endMinute_) : ("" + endMinute_);



    String resultStr = "";

    if (beginYear != endYear) {

      resultStr = beginMonth + "/" + beginDay + " " + beginHour + ":"

          + beginMinute + "-" + endYear + "/" + endMonth + "/" + endDay + " "

          + endHour + ":" + endMinute;

    } else if (beginMonth != endMonth || beginDay != endDay) {

      resultStr = beginMonth + "月" + beginDay + "日" + beginHour + ":"

          + beginMinute + "至" + endMonth + "月" + endDay + "日" + endHour + ":"

          + endMinute;

    } else {

      resultStr = beginMonth + "月" + beginDay + "日" + beginHour + ":"

          + beginMinute + "至" + endHour + ":" + endMinute;

    }



    return resultStr;

  }



  public static String addWeekForDay(Date date) {

    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);



    Calendar today = Calendar.getInstance();

    today.setTime(new Date());



    int year = calendar.get(Calendar.YEAR);

    int month = calendar.get(Calendar.MONTH) + 1;

    int day = calendar.get(Calendar.DAY_OF_MONTH);

    int year_ = today.get(Calendar.YEAR);

    int month_ = today.get(Calendar.MONTH) + 1;

    int day_ = today.get(Calendar.DAY_OF_MONTH);



    if (year == year_ && month == month_ && day == day_) {

      return "今天";

    }



    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);



    String day_week = null;

    switch (dayOfWeek) {

    case Calendar.MONDAY:

      day_week = "星期一";

      break;

    case Calendar.TUESDAY:

      day_week = "星期二";

      break;

    case Calendar.WEDNESDAY:

      day_week = "星期三";

      break;

    case Calendar.THURSDAY:

      day_week = "星期四";

      break;

    case Calendar.FRIDAY:

      day_week = "星期五";

      break;

    case Calendar.SATURDAY:

      day_week = "星期六";

      break;

    case Calendar.SUNDAY:

      day_week = "星期日";

      break;

    }



    return year + "年" + month + "月" + day + "日" + " , " + day_week;

  }



  public static String removeWeekForDay(String dateString) {



    if ("今天".equals(dateString)) {



      Calendar today = Calendar.getInstance();

      today.setTime(new Date());

      int year_ = today.get(Calendar.YEAR);

      int month_ = today.get(Calendar.MONTH) + 1;

      int day_ = today.get(Calendar.DAY_OF_MONTH);



      return year_ + "年" + month_ + "月" + day_ + "日";

    }



    int index = dateString.indexOf(",");

    dateString = dateString.substring(0, index);

    return dateString;

  }



  public static String convertDateFromSeconds(String secondString,

      String dateFormatter) {



    if (CommonUtil.isValid(secondString)) {

      Calendar date = Calendar.getInstance();

      date.setTimeInMillis(Integer.valueOf(secondString).longValue() * 1000);

      return CommonUtil.formatDate(date.getTime(), dateFormatter);

    }

    return "";

  }



  public static String formatMeetingId(String meetingId) {

    if (meetingId.length() >= 8) {

      return meetingId.substring(0, 3) + "-" + meetingId.substring(3, 6) + "-"

          + meetingId.substring(6, meetingId.length());

    } else {

      return meetingId;

    }

  }



  /***

   * MD5加码 生成32位md5码

   */

  public static String string2MD5(String inStr) {

    MessageDigest md5 = null;

    try {

      md5 = MessageDigest.getInstance("MD5");

    } catch (Exception e) {

      System.out.println(e.toString());

      e.printStackTrace();

      return "";

    }

    char[] charArray = inStr.toCharArray();

    byte[] byteArray = new byte[charArray.length];



    for (int i = 0; i < charArray.length; i++)

      byteArray[i] = (byte) charArray[i];

    byte[] md5Bytes = md5.digest(byteArray);

    StringBuffer hexValue = new StringBuffer();

    for (int i = 0; i < md5Bytes.length; i++) {

      int val = ((int) md5Bytes[i]) & 0xff;

      if (val < 16)

        hexValue.append("0");

      hexValue.append(Integer.toHexString(val));

    }

    return hexValue.toString();



  }



  public static String getUUID() {

    String s = UUID.randomUUID().toString();

    // 去掉“-”符号

    return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)

        + s.substring(19, 23) + s.substring(24);

  }



  /**

   * @Title: simpleFormatMoPhone

   * @Description: 简单格式化手机号码

   * @param phone

   * @return

   */

  public static String simpleFormatMoPhone(String phone) {

    if (TextUtils.isEmpty(phone)) {

      return "";

    }

    String oldPhone = phone;

    phone = phone.replace("-", "").replace(" ", "");

    if (phone.startsWith("+86") && phone.length() == 14) {

      phone = phone.substring(3);

    }

    // CustomLog.d("CommonUtil", "简单格式化手机号码:" + oldPhone + "---->" + phone);

    return phone;

  }



  /**

   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)

   */

  public static int dip2px(Context context, float dpValue) {

    final float scale = context.getResources().getDisplayMetrics().density;

    return (int) (dpValue * scale + 0.5f);

  }



  /**

   * 根据手机的分辨率从 px(像素) 的单位 转成为 dp

   */

  public static int px2dip(Context context, float pxValue) {

    final float scale = context.getResources().getDisplayMetrics().density;

    return (int) (pxValue / scale + 0.5f);

  }



  /** 没有网络 */

  public static final int NETWORKTYPE_INVALID = -1;

  /** wifi网络 */

  public static final int NETWORKTYPE_WIFI = 1;

  /** 2G网络 */

  public static final int NETWORKTYPE_2G = 2;

  /** 3G和3G以上网络，或统称为快速网络 */

  public static final int NETWORKTYPE_3G = 3;

  /** wap网络 */

  public static final int NETWORKTYPE_WAP = 4;



  /**

   * 获取网络状态: wifi,wap,2g,3g.

   *

   * @param appcontext

   *          上下文

   * @return int 网络状态

   * 

   *         -1: 没有网络；

   * 

   *         1：wifi网络；

   * 

   *         2: 2G网络；

   * 

   *         3: 3G及以上网络；

   * 

   *         4：wap网络

   */



  public static int getNetWorkType(Context appcontext) {



    ConnectivityManager manager = (ConnectivityManager) appcontext

        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo networkInfo = manager.getActiveNetworkInfo();



    int mNetWorkType = NETWORKTYPE_INVALID;



    if (networkInfo != null && networkInfo.isConnected()) {

      String type = networkInfo.getTypeName();



      if (type.equalsIgnoreCase("WIFI")) {

        mNetWorkType = NETWORKTYPE_WIFI;

      } else if (type.equalsIgnoreCase("MOBILE")) {

        String proxyHost = android.net.Proxy.getDefaultHost();



        mNetWorkType = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(appcontext) ? NETWORKTYPE_3G

            : NETWORKTYPE_2G)

            : NETWORKTYPE_WAP;

      }

    }



    return mNetWorkType;

  }



  private static boolean isFastMobileNetwork(Context context) {

    TelephonyManager telephonyManager = (TelephonyManager) context

        .getSystemService(Context.TELEPHONY_SERVICE);

    switch (telephonyManager.getNetworkType()) {

    case TelephonyManager.NETWORK_TYPE_1xRTT:

      return false; // ~ 50-100 kbps

    case TelephonyManager.NETWORK_TYPE_CDMA:

      return false; // ~ 14-64 kbps

    case TelephonyManager.NETWORK_TYPE_EDGE:

      return false; // ~ 50-100 kbps

    case TelephonyManager.NETWORK_TYPE_EVDO_0:

      return true; // ~ 400-1000 kbps

    case TelephonyManager.NETWORK_TYPE_EVDO_A:

      return true; // ~ 600-1400 kbps

    case TelephonyManager.NETWORK_TYPE_GPRS:

      return false; // ~ 100 kbps

    case TelephonyManager.NETWORK_TYPE_HSDPA:

      return true; // ~ 2-14 Mbps

    case TelephonyManager.NETWORK_TYPE_HSPA:

      return true; // ~ 700-1700 kbps

    case TelephonyManager.NETWORK_TYPE_HSUPA:

      return true; // ~ 1-23 Mbps

    case TelephonyManager.NETWORK_TYPE_UMTS:

      return true; // ~ 400-7000 kbps

    case TelephonyManager.NETWORK_TYPE_EHRPD:

      return true; // ~ 1-2 Mbps

    case TelephonyManager.NETWORK_TYPE_EVDO_B:

      return true; // ~ 5 Mbps

    case TelephonyManager.NETWORK_TYPE_HSPAP:

      return true; // ~ 10-20 Mbps

    case TelephonyManager.NETWORK_TYPE_IDEN:

      return false; // ~25 kbps

    case TelephonyManager.NETWORK_TYPE_LTE:

      return true; // ~ 10+ Mbps

    case TelephonyManager.NETWORK_TYPE_UNKNOWN:

      return false;

    default:

      return false;

    }

  }

  

  public static String getLimitSubstring(String inputStr, int length) {

	    if (inputStr == null) {

	      return "";

	    }

	    char[] ch = inputStr.toCharArray();

	    int varlength = 0;

	    for (int i = 0; i < ch.length; i++) {

	      if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)

	          || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {

	        varlength = varlength + 2;

	      } else {

	        varlength++;

	      }

	      if (varlength > length) {

	        return  inputStr = inputStr.substring(0, i) + "...";

	      }

	    }

	    return inputStr;

	  }

  public static String getLimitSubstringWithoutDos(String inputStr, int length) {

	    if (inputStr == null) {

	      return "";

	    }

	    char[] ch = inputStr.toCharArray();

	    int varlength = 0;

	    for (int i = 0; i < ch.length; i++) {

	      if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)

	          || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {

	        varlength = varlength + 2;

	      } else {

	        varlength++;

	      }

	      if (varlength > length) {

	        return  inputStr = inputStr.substring(0, i);

	      }

	    }

	    return inputStr;

	  }

}

