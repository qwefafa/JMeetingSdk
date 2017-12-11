package cn.redcdn.menuview;

public class QosInfoKey {
  public final static String videoType = "VideoPayload";// 视频格式
  public final static String audioType = "AudioPayLoad";// 音频格式
  public final static String pictureType = "PictureFormat";// 图像格式
  public final static String mic1CaptureFPS = "Mic1CaptureFPS";// Mic1采集帧率
  public final static String mic1EncodeFPS = "Mic1EncodeFPS";// Mic1编码帧率
  public final static String mic1RenderFPS = "Mic1RenderFPS";// Mic1渲染帧率
  public final static String mic1DecodeFPS = "Mic1DecodeFPS";// Mic1解码帧率
  public final static String mic2CaptureFPS = "Mic2CaptureFPS";// Mic2采集帧率
  public final static String mic2EncodeFPS = "Mic2EncodeFPS";// Mic2编码帧率
  public final static String mic2RenderFPS = "Mic2RenderFPS";// Mic2渲染帧率
  public final static String mic2DecodeFPS = "Mic2DecodeFPS";// Mic2解码帧率

  public final static String mic1UpNetSpeed = "Mic1UpNetSpeed";// mic1上行速率
  public final static String mic1DownNetSpeed = "Mic1DownNetSpeed";// mic1下行速率
  public final static String mic1UpLossRate = "Mic1UpLossRate";// mic1上行丢包率
  public final static String mic1FecUpLossRate = "Mic1FecUpLossRate";// mic1上行解fec丢包率
  public final static String mic1DownLossRate = "Mic1DownLossRate";// mic1下行丢包率
  public final static String mic1FecDownLossRate = "Mic1FecDownLossRate";// mic1下行解fec丢包率
  public final static String mic2UpNetSpeed = "Mic2UpNetSpeed";// mic2上行速率
  public final static String mic2DownNetSpeed = "Mic2DownNetSpeed";// mic2下行速率
  public final static String mic2UpLossRate = "Mic2UpLossRate";// mic2上行丢包率
  public final static String mic2FecUpLossRate = "Mic2FecUpLossRate";// mic2上行解fec丢包率
  public final static String mic2DownLossRate = "Mic2DownLossRate";// mic2下行丢包率
  public final static String mic2FecDownLossRate = "Mic2FecDownLossRate";// mic2下行解fec丢包率

  public final static String cpuInfo = "CPUInfo";// CPUInfo
  public final static String cpuInfoUsed = "Used";// CPU使用率
  public final static String cpuInfoSystem = "System";// 系统
  public final static String cpuInfoIdle = "Idle";// 空闲

  public final static String memoryInfo = "MemoryInfo";// MemoryInfo
  public final static String memoryInfoTotal = "Total";// 总内存
  public final static String memoryInfoFree = "Free";// 空闲内存
  public final static String memoryInfoUsed = "Used";// 使用内存
  public final static String memoryInfoPercent = "Percent";// 使用百分比

  public final static String baseInfo = "BaseInfo";
  public final static String streamQosInfo = "StreamQosInfo";
 
}
