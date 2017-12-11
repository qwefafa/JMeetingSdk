package cn.redcdn.dep;

public class MeetingHostAgentJNI {

  public static native int Start(String accounted, String localip,
      int localport, String localCmdIp, int localCmdPort, String UICmdIp,
      int UICmdPort, String rcAddr);

  /**
   * 
   * @param accounted：  视讯号
   * @param localip： 本机ip地址
   * @param localport： agent与服务器通信的端口地址
   * @param localCmdIp： agent接收UI命令的地址： 127.0.0.1
   * @param localCmdPort： agent接收UI命令的端口，该端口实际上由agent返回，用于UI向这个端口发送UDP命令
   * @param UICmdIp：  UI接收agent命令的地址：127.0.0.1
   * @param UICmdPort： UI接收agent命令的端口，UI保持对这个端口的监听，用于agent向这个端口发送UDP命令
   * @param rcAddr： rc地址
   * @param logConfPath： 配置文件地址
   * @param logOutPath： 日志输出地址
   * @param token 
   * @return
   */
  public static native int Start2(String accounted, String localip,
      int localport, String localCmdIp, int localCmdPort, String UICmdIp,
      int UICmdPort, String rcAddr, String logConfPath, String logOutPath,
      String token);
  
  public final static native void LoadBreakpad();
  public final static native void LoadBreakpad2(String dir);

  public static native int Stop();

  static {
	System.loadLibrary("breakpad");
    System.loadLibrary("litezip");
    System.loadLibrary("logwriter");
    System.loadLibrary("mhostclient");
  }
}
