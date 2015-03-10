package com.switek.netseed.server.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.StandardIRCode;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.dal.DB;
import com.switek.netseed.server.dal.IDAL;
import com.switek.netseed.server.dal.MySQLDAL;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.CRC16;
import com.switek.netseed.util.FormatTransfer;
import com.switek.netseed.util.MyBase64;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {

		// System.out.println(Utils.bytes2HexString(generatePacket("201404240011",
		// (byte)0, SocketPacket.COMMAND_ID_CS_HEARTBEAT, null)));

		// System.out.println(Utils.getCurrentTime("yyyy/MM/dd HH:mm"));

		// System.out.println(StringUtils.leftPad("123456", 64, "0"));

		// int currentWeekday = (int) Math.pow(2, 6);
		// int timerWeekdays = 64;
		//
		// System.out.println(timerWeekdays & currentWeekday);

		// Hashtable<String, String> test = new Hashtable<>();
		// test.put("1", "v1");
		// System.out.println(test.get("1"));
		// test.put("1", "v2");
		// System.out.println(test.get("1"));

		// parseJson();

		//long2bit();
		
		exportIRCode();

		// testDate2String();
		// importIRCode();

		// testConvertControllerId();

		// testUDP();

		// System.out.println(PackageVersion.getVersion());

		// DeviceManager.loadDeviceBrands();
		// AsyncSocketMsgHandler asyncSocketMsgHandler=new
		// AsyncSocketMsgHandler(null);
		// asyncSocketMsgHandler.onGetDeviceType(new SocketPacket(null));

		// readFile();
		// testFormatTransfer();
		// testCRC16();
		// new Test().testDBConnection();

		// testProxool();

		// testJson();

		// testConvertControllerId();

		// testBlob();

		// testUUID();
		// testFormatString();

		// testCharset();

		// testOutputParameter();

		// testUpdateDB();
		// testBatchUpdateDB();

		// testBytes2String();

		// testLong2Bytes();

		// testCompareVersion();

		// testAddSubcontroller();
		// testBase64();

		// testAddDevice();
		// System.out.println(System.currentTimeMillis());

		// testFileInputStream();
		// System.out.println(Short.MAX_VALUE);

		// try {
		// testNIOSocketServer();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// testJson2();
		// serverLoadingBenchmark();
		// testMac();
	}

	private static void long2bit() {
		// String str = Integer.toBinaryString(200);
		// System.out.println(str);
		// System.out.println(str.substring(Math.max(0,str.length()-7)));
		// BigInteger i=new BigInteger("11110001001000000", 2);
		// System.out.println(i);
		// i=new BigInteger("111110001001000001", 2);
		// System.out.println(i);

		String binaryString = Long.toBinaryString(1638500L);
		binaryString = StringUtils.leftPad(binaryString, 64, "0");
		System.out.println(String.format(
				"LastPressedKey: %s, binaryString: %s.", 123, binaryString));

		String circuitValue = StringUtils.leftPad(Integer.toBinaryString(50),
				8, "0");
		circuitValue = circuitValue.substring(1);
		System.out.println(circuitValue);

		int circuitNo = 4;
		int startIndex = (9 - circuitNo) * 7 + 1;
		int endIndex = startIndex + 7;

		binaryString = binaryString.substring(0, startIndex) + circuitValue
				+ binaryString.substring(endIndex);

		System.out.println(binaryString);

		BigInteger i = new BigInteger(binaryString, 2);

		System.out.println(i);
	}

	private static void binaryString() {
		long runTime = 6400000 + 3601 * 1000;
		int runTimeDays = (int) (runTime / 86400000);
		runTime = runTime % 86400000;
		int runTimeHours = (int) (runTime / 3600000);
		runTime = runTime % 3600000;

		int runTimeMinutes = (int) (runTime / 60000);
		runTime = runTime % 60000;

		int runTimeSeconds = (int) (runTime / 1000);
		String runTimeString = String.format("%s D %s:%s:%s ", runTimeDays,
				runTimeHours, runTimeMinutes, runTimeSeconds);
		System.out.println(runTimeString);
	}

	static void testUDP() throws Exception {

		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("192.168.22.255");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String sentence = new Date().toString();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, 18899);
		System.out.println("send..." + sendPacket);
		clientSocket.send(sendPacket);
		// DatagramPacket receivePacket = new DatagramPacket(receiveData,
		// receiveData.length);
		// clientSocket.receive(receivePacket);
		// String modifiedSentence = new String(receivePacket.getData());
		// System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();

	}

	private static void testMac() {
		byte[] bytes = new byte[] { (byte) 0x00, (byte) 0x0C, (byte) 0x43,
				(byte) 0x64, (byte) 0x45, (byte) 0x42 };
		System.out.println(Utils.bytes2HexString2(bytes));
	}

	static short sn = 0;

	static void serverLoadingBenchmark() {
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(new Runnable() {
				Socket socket = null;
				OutputStream os = null;
				short curSN = 0;

				@Override
				public void run() {
					try {
						System.out.println("Connect 2 server...");
						if (socket == null || !socket.isConnected()
								|| socket.isClosed()) {
							if (socket != null) {
								os.close();
								socket.close();
							}
							// socket = new Socket("61.145.163.34", 8899);
							socket = new Socket("127.0.0.1", 9123);
							os = socket.getOutputStream();
						}

						final byte[] bytes = new byte[15];
						// start flag
						bytes[0] = (byte) 0xAA;
						bytes[1] = (byte) 0xAA;
						bytes[2] = (byte) 0x0D;
						bytes[3] = (byte) 0x00;
						bytes[4] = (byte) 0xE2;
						bytes[5] = (byte) 0x07;
						bytes[6] = (byte) 0x07;
						bytes[7] = (byte) 0x22;
						sn++;
						curSN = sn;
						byte[] bSN = FormatTransfer.toLH(curSN);
						bytes[8] = bSN[0];
						bytes[9] = bSN[1];
						bytes[10] = 0;
						bytes[11] = 3;
						bytes[12] = 0;
						byte[] crc = CRC16.calcCRC(bytes, 2, 12);
						bytes[13] = crc[0];
						bytes[14] = crc[1];

						Timer timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								System.out.println("write "
										+ Utils.bytes2HexString(bytes) + " to "
										+ curSN);
								try {
									os.write(bytes);
									os.flush();
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}, new Random().nextInt(10000), 30 * 1000);

					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			thread.start();
		}
	}

	static void readFile() {
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream("D:\\IRCode\\5.bin");
			int len = 768;
			byte[] bFile = new byte[len];
			fileInputStream.read(bFile);
			System.out.println(Utils.bytes2HexString(bFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void importIRCode() {

		File file = new File("D:\\IRCode\\New folder");
		importIRCode(file, 1);

	}

	private static void importIRCode(File dir, int type) {

		for (File irFile : dir.listFiles()) {
			if (irFile.isDirectory()) {
				importIRCode(irFile, type);
				continue;
			}

			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(irFile);
				int len = fileInputStream.available();
				byte[] bFile = new byte[len];
				fileInputStream.read(bFile);
				String brandCode = irFile.getParentFile().getName();

				int index = Integer.valueOf(irFile.getName().substring(0,
						irFile.getName().length() - 4)) - 1;

				List<Object> params = new ArrayList<>();
				params.add(type);
				params.add(brandCode);
				params.add(index);
				System.out.println(String.format("delete %s, %s, %s", type,
						brandCode, index));
				DB.executeSQL(
						"delete from adircode where devicetype = ? and brandcode=? and IRCodeIndex=?",
						params);

				params = new ArrayList<>();
				params.add(type);
				params.add(brandCode);
				params.add(index);
				params.add(bFile);
				System.out.println(String.format("insert %s, %s, %s", type,
						brandCode, index));
				DB.executeSQL(
						"insert into adircode VALUES (?, ?, ?, ?, '', CURRENT_TIMESTAMP,'Feng' )",
						params);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	static void testJson2() {
		File file = new File("D:\\as13json.txt");
		BufferedReader reader = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				System.out.println("line " + line + ": " + tempString);
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		// JSONObject jsonObject
		// JSONArray body = jsonObject.getJSONArray("Body");
	}

	public static final int PORT = 722;

	private static void testNIOSocketServer() throws IOException {
		// NIO的通道channel中内容读取到字节缓冲区ByteBuffer时是字节方式存储的，
		// 对于以字符方式读取和处理的数据必须要进行字符集编码和解码
		String encoding = "utf-8";
		// 加载字节编码集
		Charset cs = Charset.forName(encoding);
		// 分配两个字节大小的字节缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(16);
		SocketChannel ch = null;
		// 打开服务端的套接字通道
		ServerSocketChannel ssc = ServerSocketChannel.open();
		// 打开通道选择器
		Selector sel = Selector.open();
		try {
			// 将服务端套接字通道连接方式调整为非阻塞模式
			ssc.configureBlocking(false);
			// 将服务端套接字通道绑定到本机服务端端口
			ssc.socket().bind(new InetSocketAddress(PORT));
			// 将服务端套接字通道OP_ACCEP事件注册到通道选择器上
			SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
			System.out.println("Server on port:" + PORT);
			while (true) {
				System.out.println("A-" + System.currentTimeMillis());
				// 通道选择器开始轮询通道事件
				sel.select();
				Iterator it = sel.selectedKeys().iterator();
				while (it.hasNext()) {
					System.out.println("B-" + System.currentTimeMillis());
					// 获取通道选择器事件键
					SelectionKey skey = (SelectionKey) it.next();
					it.remove();
					// 服务端套接字通道发送客户端连接事件，客户端套接字通道尚未连接
					if (skey.isAcceptable()) {
						// 获取服务端套接字通道上连接的客户端套接字通道
						ch = ssc.accept();
						System.out.println("Accepted connection from:"
								+ ch.socket());
						// 将客户端套接字通过连接模式调整为非阻塞模式
						ch.configureBlocking(false);
						// 将客户端套接字通道OP_READ事件注册到通道选择器上
						ch.register(sel, SelectionKey.OP_READ);
					}
					// 客户端套接字通道已经连接
					else if (skey.isReadable()) {
						// 获取创建此通道选择器事件键的套接字通道
						System.out.println("reading");
						ch = (SocketChannel) skey.channel();
						// 将客户端套接字通道数据读取到字节缓冲区中
						StringBuilder response = new StringBuilder();

						while (ch.read(buffer) > 0) {
							// 使用字符集解码字节缓冲区数据
							CharBuffer cb = cs.decode((ByteBuffer) buffer
									.flip());
							buffer.rewind();
							response.append(cb.toString());
						}
						System.out.println("Echoing:" + response.toString());
						// 重绕字节缓冲区，继续读取客户端套接字通道数据
						// ch.write((ByteBuffer) buffer.rewind());
						// if (response.indexOf("END") != -1)
						// ch.close();
						buffer.clear();
					}
				}
			}
		} finally {
			if (ch != null)
				ch.close();
			ssc.close();
			sel.close();
		}
	}

	static void exportIRCode() {
		ResultSet resultSet = DB
				.executeQuery("select * from AdIRCode order by devicetype, brandcode, ircodeindex");
		try {
			while (resultSet.next()) {
				int deviceType = resultSet.getInt("DeviceType");
				String brandCode = resultSet.getString("BrandCode");
				int IRCodeIndex = resultSet.getInt("IRCodeIndex");
				byte[] IRCodeData = resultSet.getBytes("IRCodeData");
				String fileName = "files/"
						+ (deviceType + "_" + brandCode + "_" + IRCodeIndex)
								.toUpperCase() + ".bin";

				System.out.println("Expoting " + fileName);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(new File(fileName));
					fos.write(IRCodeData);
					fos.flush();
					fos.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fos = null;
					}
				}
			}
		} catch (SQLException e) {
			ServerForm.showLog(e);
		}
	}

	private static void testFileInputStream() {
		File file = new File("d:\\IRCode.png");
		FileInputStream fis;
		byte[] bytesFile;
		try {
			fis = new FileInputStream(file);
			int len = fis.available() + 4;
			bytesFile = new byte[len];
			int index = 4;
			int bufferSize = 512;
			while (fis.available() > 0) {
				if (len - index < 512) {
					bufferSize = len - index;
				}
				int readLen = fis.read(bytesFile, index, bufferSize);
				if (readLen == 0) {
					break;
				}
				index += readLen;
			}
			FileOutputStream fos = new FileOutputStream(new File(
					"d:\\IRCode2.png"));
			fos.write(bytesFile);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void string2date() {
		String time = "2014-04-09 17:55:47.112";
		String timeFormat = "yyyy-MM-dd HH:mm:ss.SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
		Date date;
		try {
			date = sdf.parse(time);
			System.out.println(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void testBase64() {
		// byte[] source = new byte[]{0x01,0x22,0x33,0x44,0x55};
		// String s = MyBase64.encode(source);
		String s = "I0I0I0EkIxQhQjQjQhZEVFRUJEUk7u7u7u4=";
		byte[] bs = MyBase64.decode(s);
		System.out.println(Utils.bytes2HexString(bs));
	}

	private static void testAddSubcontroller() {
		IDAL dal = MySQLDAL.newInstance();

		Controller c = new Controller();
		c.setControllerId("201403260006");
		c.setControllerName("201403260006");
		SubController newSubcontroller = new SubController(c);
		newSubcontroller.setControllerId("1");
		newSubcontroller.setControllerName("1");

		dal.addSubcontroller(newSubcontroller);
	}

	private static void testAddDevice() {
		IDAL dal = MySQLDAL.newInstance();

		Controller c = new Controller();
		c.setControllerId("201403260006");
		c.setControllerName("201403260006");
		SubController newSubcontroller = new SubController(c);
		newSubcontroller.setControllerId("1");
		newSubcontroller.setControllerName("1");

		Device device = new Device(newSubcontroller);
		device.setDeviceId("54564656456");
		device.setDeviceType(1);

		boolean b = dal.addDevice(device);
		device.setDeviceType(2);
		device.setBrandCode("FENGTEST");
		b = dal.updateDevice(device);
		System.out.println(b);
	}

	private static void testCompareVersion() {
		String latestVersion = "1.0.0.1096";
		String curVersion = "1.0.0.1192";
		try {

			System.out.println(Utils.compareVersion(latestVersion, curVersion));
		} catch (NumberFormatException e) {
			System.out.println("number format exception");
		}
	}

	static void testOutputParameter() {
		SocketPacket packet = new SocketPacket(null);
		packet.setCommandId((short) 77);
		System.out.println(packet.getCommandId());
		testOutputParameter(packet);
		System.out.println(packet.getCommandId());
	}

	static void testOutputParameter(SocketPacket packet) {
		packet.setCommandId((short) 88);
		packet = new SocketPacket(null);
		packet.setCommandId((short) 99);
		System.out.println(packet.getCommandId());
	}

	private static void testCharset() {
		String string = "中";
		try {
			System.out.println(Utils.bytes2HexString(string.getBytes("utf-8")));
			System.out.println(Utils.bytes2HexString(string.getBytes()));
			byte[] bytes = new byte[2];
			bytes[0] = 0x4e;
			bytes[1] = 0x2d;
			System.out.println(new String(bytes, "utf-16"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void testFormatString() {

		System.out
				.println(String
						.format("insert into mddevice (controllerid, subcontrollerid, deviceid, devicename, devicetype, brandcode, ircodeindex, islearnedircode, registerdt, registerby, status, lasteditdt, lasteditby) values ('%s','%s','%s','%s','%s','%s',%d,false,CURRENT_TIMESTAMP,'server','00',CURRENT_TIMESTAMP, 'server')",
								"c1", "c2", "c3", "c4", "c5", "c6", 111));

	}

	private static void testUUID() {
		UUID uuid = UUID.randomUUID();
		System.out.println(uuid.toString());
	}

	private static void testBlob() {

		List<String> sqlList = new ArrayList<>();

		// String strInsertController =
		// "insert into adircode (devicetype, brandcode, ircodeindex, ircodedata, description, lasteditby, lasteditdt) values (?, 'HAIER2', ?, ?, 'HAIER TV', ?, ?)";
		// String strInsertController = "insert into testtable values(?,?);";
		String strInsertController = "update mdlearnedircode set ircodedata = ? where deviceid=? and keyindex=?";
		ByteArrayInputStream arrayInputStream = null;

		byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08 };
		arrayInputStream = new ByteArrayInputStream(bytes);

		List<Object> values = new ArrayList<>();
		values.add(arrayInputStream);
		values.add("TVV2");
		values.add(1);
		try {
			DB.executeSQL(strInsertController, values);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			DB.executeBatch(sqlList);

		} catch (SQLException e) {
			ServerForm.showLog(e);
		}

		try {
			arrayInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void testConvertControllerId() {

		ArrayList<Byte> packetBytes = new ArrayList<>();
		packetBytes.add((byte) 0x0);
		packetBytes.add((byte) 0x0);
		packetBytes.add((byte) 0x0);
		packetBytes.add((byte) 0x0);
		packetBytes.add((byte) 0xDE);
		packetBytes.add((byte) 0x07);
		packetBytes.add((byte) 0x0C);
		packetBytes.add((byte) 0x1F);
		packetBytes.add((byte) 0xFE);
		packetBytes.add((byte) 0xFF);
		byte[] bytesControllerId = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytesControllerId[i] = packetBytes.get(4 + i);
		}
		String controllerId = Utils.bytes2HexString2(bytesControllerId);
		System.out.println(controllerId);
		System.out.println(controllerId
				+ ": "
				+ Utils.bytes2HexString(Controller
						.convertControllerId2Bytes(controllerId)));

	}

	private static void testBatchUpdateDB() {

		List<String> sqlList = new ArrayList<>();
		long Id = 5;
		if (true) {
			sqlList.add("delete c from mdlearnedircode as c join mddevice as d on c.deviceid = d.deviceid where d.controllerid = "
					+ Id);
			sqlList.add("delete from mddevice where controllerid = " + Id);
			sqlList.add("delete from mdsubcontroller where controllerid = "
					+ Id);
			sqlList.add("delete from mdcontroller where controllerid = " + Id);
		}

		String strInsertController = "insert into mdcontroller (controllerid, controllername, registerdt, registerby, lasteditby, lasteditdt, status) values ("
				+ Id
				+ ", '"
				+ Id
				+ "', CURRENT_TIMESTAMP, 'Feng','Feng', CURRENT_TIMESTAMP, '01')";
		String strInsertSubcontroller = "insert into MDSubController (controllerid, subcontrollerid, subcontrollername, registerdt, registerby, lasteditby, lasteditdt, status) values ("
				+ Id
				+ ", 0, '中控默认分机', CURRENT_TIMESTAMP, 'Feng','Feng', CURRENT_TIMESTAMP, '01')";
		sqlList.add(strInsertController);
		sqlList.add(strInsertSubcontroller);

		try {
			DB.executeBatch(sqlList);

		} catch (SQLException e) {
			ServerForm.showLog(e);
		}
	}

	private static void testUpdateDB() {

		List<String> sqlList = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String nameSuffix = String.valueOf(i + 25);
			String sql = "insert into mdcontroller (controllerid, controllername, registerdt, registerby, lasteditby, lasteditdt, status) values ("
					+ nameSuffix
					+ ", '丰的中控"
					+ nameSuffix
					+ "', CURRENT_TIMESTAMP, 'Feng','Feng', CURRENT_TIMESTAMP, '00')";
			sqlList.add(sql);
		}

		sqlList.add("delete mdcontroller where controllerid >= 10");
		try {
			DB.executeBatch(sqlList);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Done!");

	}

	private static void testLong2Bytes() {
		long l = 1388629898240935265l;
		System.out.println(l);
		byte[] lhBs = FormatTransfer.toLH(l);
		System.out.println("LH: ");
		System.out.println(Utils.bytes2HexString(lhBs));

		byte[] hhBs = FormatTransfer.toHH(l);
		System.out.println("HH: ");
		System.out.println(Utils.bytes2HexString(hhBs));

		long ll = FormatTransfer.lBytesToLong(lhBs);
		System.out.println("LL: " + ll);

		long HL = FormatTransfer.hBytesToLong(hhBs);
		System.out.println("HL: " + HL);

	}

	private static void testBytes2String() {
		byte[] bytes = new byte[] { (byte) 0xDE, 0x07, 0x03, 0x04, 0x01, 0x00 };

		String string = new String(bytes);
		System.out.println(string);

		// FormatTransfer.toLH(f)
		string = FormatTransfer.bytesToString(bytes);
		System.out.println(string);
	}

	static void testFormatTransfer() {

		System.out.println(Utils.bytes2HexString(FormatTransfer.toLH(25536)));
		System.out.println(Utils.bytes2HexString(FormatTransfer
				.toLH((short) 25536)));
		// System.out.println(Utils.bytes2HexString(FormatTransfer
		// .toLH((short) 13)));
		// System.out.println(Utils.bytes2HexString(Utils
		// .short2TwoBytes((short) 13)));
	}

	static void testCRC16() {
		byte[] data = new byte[] { (byte) 0x13, (byte) 0x00, (byte) 0xDE,
				(byte) 0x07, (byte) 0x04, (byte) 0x1E, (byte) 0x70,
				(byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00,
				(byte) 0x11, (byte) 0x24, (byte) 0x7E, (byte) 0x87,
				(byte) 0x94, (byte) 0x27 };

		byte[] crc = CRC16.calcCRC(data);

		System.out.println(Utils.bytes2HexString(data) + " CRC16 = "
				+ Utils.bytes2HexString(crc));

	}

	static byte[] generatePacket(String controllerId, byte subcontrollerId,
			int commandId, byte[] cmdData) {
		// data
		int cmdDataLen = 0;
		if (cmdData != null) {
			cmdDataLen = cmdData.length;
		}

		// the len of whole packet
		int len = SocketPacket.PACKET_LEN_WITHOUTDATA + cmdDataLen;
		boolean send2MID = commandId >= SocketPacket.COMMAND_ID_QUERY_CTR;
		if (send2MID) {
			len += 2;
		}
		byte[] bytes = new byte[len];

		// start flag
		bytes[0] = (byte) 0xAA;
		bytes[1] = (byte) 0xAA;

		// the len of packet without header.
		int lenWithoutHeader = len - 2;
		byte[] lenWithoutHeaderbytes = FormatTransfer.toLH(lenWithoutHeader);
		int controllerIDStartIndex = 0;
		if (send2MID) {
			bytes[2] = lenWithoutHeaderbytes[0];
			bytes[3] = lenWithoutHeaderbytes[1];
			bytes[4] = lenWithoutHeaderbytes[2];
			bytes[5] = lenWithoutHeaderbytes[3];
			controllerIDStartIndex = 6;
		} else {
			bytes[2] = lenWithoutHeaderbytes[0];
			bytes[3] = lenWithoutHeaderbytes[1];
			controllerIDStartIndex = 4;
		}
		// controller Id
		byte[] controllerIdBytes = Controller
				.convertControllerId2Bytes(controllerId);
		bytes[controllerIDStartIndex + 0] = controllerIdBytes[0];
		bytes[controllerIDStartIndex + 1] = controllerIdBytes[1];
		bytes[controllerIDStartIndex + 2] = controllerIdBytes[2];
		bytes[controllerIDStartIndex + 3] = controllerIdBytes[3];
		bytes[controllerIDStartIndex + 4] = controllerIdBytes[4];
		bytes[controllerIDStartIndex + 5] = controllerIdBytes[5];

		// sub controller Id
		bytes[controllerIDStartIndex + 6] = subcontrollerId;

		// command Id
		byte[] cmdIdBytes = FormatTransfer.toLH(commandId);
		bytes[controllerIDStartIndex + 7] = cmdIdBytes[0];
		bytes[controllerIDStartIndex + 8] = cmdIdBytes[1];

		for (int i = 0; i < cmdDataLen; i++) {
			bytes[controllerIDStartIndex + 9 + i] = cmdData[i];
		}

		byte[] crc = CRC16.calcCRC(bytes, 2, bytes.length - 2 - 1);

		int crcStartIndex = controllerIDStartIndex + 9 + cmdDataLen;
		bytes[crcStartIndex] = crc[0];
		bytes[crcStartIndex + 1] = crc[1];
		return bytes;
	}

	public static void testProxool() {
		String query = "select * from mdcontroller;";
		System.err.println(new Date().toLocaleString());
		ResultSet rs = DB.executeQuery(query);

		try {
			while (rs.next()) {
				System.out.println(rs.getString(3) + "--OK<br />");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.err.println(new Date().toLocaleString());

		query = "select * from mdsubcontroller;";
		rs = DB.executeQuery(query);

		System.err.println(new Date().toLocaleString());
		try {
			while (rs.next()) {
				System.out.println(rs.getString(3) + "--OK<br />");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void parseJson() {
		String data = new String(
				"{\"OptCode\":\"0x01F0\",\"APIVer\":\"1.0\", \"Body\":[{\"ControllerID\":123456, \"Enabled\":true},{\"ControllerID\":2,\"Enabled\":false}]}");

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONArray bodyArray = jsonObject.getJSONArray("Body");
		for (int i = 0; i < bodyArray.size(); i++) {
			JSONObject body = (JSONObject) bodyArray.get(i);
			System.out.println(body.get("ControllerID"));
			System.out.println(body.getBoolean("Enabled"));
		}

	}

	private static void testJson() {

		JSONObject jsonReply = new JSONObject();

		Map<String, Object> replyMap = new HashMap<String, Object>();
		replyMap.put("OptCode", SocketPacket.COMMAND_ID_QUERY_CTR);
		replyMap.put("APIVer", "1.0");
		replyMap.put("ResultCode", "0");

		Map<String, String> replyBody = new HashMap<>();
		replyBody.put("ControllerId", String.valueOf(123456));
		replyBody.put("Status", "00");

		replyMap.put("Body", replyBody);
		jsonReply.putAll(replyMap);

		String replyJson = jsonReply.toString();
		System.out.println(replyJson);

	}

	public static void testProxool2() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {

			// 初始化数据库连接配置参数
			InputStream in = Test.class.getResourceAsStream("/proxool.xml");
			Reader reader = null;
			try {
				reader = new InputStreamReader(in, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			try {
				JAXPConfigurator.configure(reader, false);
			} catch (ProxoolException e) {
				e.printStackTrace();
			}

			try {
				Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			conn = DriverManager.getConnection("proxool.DBPool");
			stmt = conn.createStatement();
			String query = "select * from mdsubcontroller;";
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				System.out.println(rs.getString(3) + "--OK<br />");
			}

			stmt.close();
			conn.close();
		} catch (SQLException sqle) {
			System.out.println("sqle=" + sqle);
			System.out.println("<br />数据库连接失败<br /><br />");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
					System.out.println("<br />数据库关闭成功<br />");
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("数据库关闭失败<br />");
				}
			}
		}
	}

}
