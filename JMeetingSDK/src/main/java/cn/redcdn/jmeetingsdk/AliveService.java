package cn.redcdn.jmeetingsdk;import java.io.IOException;import java.net.DatagramPacket;import java.net.DatagramSocket;import java.net.InetAddress;import cn.redcdn.log.CustomLog;enum AliveServiceStatus {	INIT, DESTROY}public class AliveService {	final static String tag = AliveService.class.getName();	private static AliveService mInstance;	private static final int intervalSec = 3;	private static final String addressStr = "127.0.0.1";	private int serviceListenPort = 25000;	private static final String aliveTag = "redcdn_meeting";	private SendThread sendThread = null;	private DatagramSocket socket;	private DatagramPacket dataPacket;	private byte sendDataByte[];	private String sendStr;	private AliveServiceStatus state = AliveServiceStatus.DESTROY;	class SendThread extends Thread {		public void run() {			while (AliveServiceStatus.INIT == state) {				try {					socket.send(dataPacket);					CustomLog.v(tag, "agent发送心跳包 heart ok");				} catch (IOException e1) {					// TODO Auto-generated catch block					e1.printStackTrace();				}				// CustomLog.d(tag,				// "send heart beating info to phone service. ip: "				// + addressStr + " port: " + portInt + " aliveTag: " +				// aliveTag);				try {					Thread.sleep(intervalSec * 1000);// 不超过 5 s				} catch (InterruptedException e) {					CustomLog.d(tag, e.toString());				}			}			CustomLog.i(tag, "send thread exit");		}	}	public static synchronized AliveService getInstance() {		if (mInstance == null) {			mInstance = new AliveService();		}		return mInstance;	}	public int init(int serviceListenPort) {		CustomLog.i(tag, "AliveService init :" + ",serviceListenPort:"				+ serviceListenPort + ",intervalSec:" + intervalSec);		if (AliveServiceStatus.DESTROY != state) {			CustomLog.e(tag, "AliveService init invalidate state");			return -1;		}		// if (!socket.init("127.0.0.1", aliveLocalPort)) {		// CustomLog.e(tag, "AliveService init udp socket failed");		// return -2;		// }		this.serviceListenPort = serviceListenPort;		try {			// 指定端口号，避免与其他应用程序发生冲突			CustomLog.v(tag, "建立心跳连接,server端口号：" + serviceListenPort);			socket = new DatagramSocket();			sendDataByte = new byte[1024];			sendStr = aliveTag;			sendDataByte = sendStr.getBytes();			dataPacket = new DatagramPacket(sendDataByte, sendDataByte.length,					InetAddress.getByName("localhost"),serviceListenPort);		} catch (IOException ie) {			CustomLog.v(tag, "建立心跳连接异常IOException：" + ie.toString());			ie.printStackTrace();		}		state = AliveServiceStatus.INIT;		sendThread = new SendThread();		sendThread.start();		return 0;	}	public void destroy() {		CustomLog.i(tag, "AliveService destroy");		if (AliveServiceStatus.DESTROY == state) {			CustomLog.e(tag, "AliveService destroy invalidate state");			return;		}		state = AliveServiceStatus.DESTROY;		socket.close();		sendThread.interrupt();		try {			sendThread.join();		} catch (InterruptedException e) {		}		sendThread = null;	}}