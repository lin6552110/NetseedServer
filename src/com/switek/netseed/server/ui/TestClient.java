package com.switek.netseed.server.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.NetseedCodecFactory;
import com.switek.netseed.util.CRC16;
import com.switek.netseed.util.FormatTransfer;

public class TestClient {

	protected Shell shell;
	private Text tbxIP;
	private Text tbxPort;
	private Text tbxPacket;
	private Text tbxLog;
	
	Logger logger = Logger.getLogger(TestClient.class);

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			TestClient window = new TestClient();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(534, 391);
		shell.setText("SWT Application");

		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		lblNewLabel.setBounds(0, 10, 55, 15);
		lblNewLabel.setText("IP:");

		tbxIP = new Text(shell, SWT.BORDER);
		tbxIP.setText("192.168.33.126");
		tbxIP.setBounds(62, 7, 73, 21);

		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setAlignment(SWT.RIGHT);
		lblNewLabel_1.setBounds(141, 10, 55, 15);
		lblNewLabel_1.setText("Port:");

		tbxPort = new Text(shell, SWT.BORDER);
		tbxPort.setText("38899");
		tbxPort.setBounds(202, 7, 73, 21);

		Label lblPacket = new Label(shell, SWT.NONE);
		lblPacket.setAlignment(SWT.RIGHT);
		lblPacket.setBounds(0, 31, 55, 15);
		lblPacket.setText("Packet:");

		tbxPacket = new Text(shell, SWT.BORDER);
		tbxPacket.setText("123!@#ABC");
		tbxPacket.setBounds(62, 31, 326, 100);

		Button btnSend = new Button(shell, SWT.NONE);
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startHeartbeat();
			}
		});
		btnSend.setBounds(394, 31, 114, 25);
		btnSend.setText("StartHeartbeat");

		Label lblLog = new Label(shell, SWT.NONE);
		lblLog.setText("Log:");
		lblLog.setAlignment(SWT.RIGHT);
		lblLog.setBounds(0, 137, 55, 15);

		tbxLog = new Text(shell, SWT.BORDER | SWT.MULTI);
		tbxLog.setEditable(false);
		tbxLog.setBounds(62, 137, 326, 134);

		btnConnect = new Button(shell, SWT.NONE);
		btnConnect.setText("Connect2Controller");
		btnConnect.setBounds(394, 62, 114, 25);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if(null!=server){
					System.out.println(server.isClosed());
					System.out.println(server.isConnected());
					if(!server.isClosed()){
						if(server.isConnected()){
							try {
								OutputStream out=server.getOutputStream();
								String str=tbxPacket.getText();
								byte[] commId={0x12,0x0F};
								out.write(createPacke(commId, str.getBytes()));
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
					}
				}
					
			}
		});
		btnControl = new Button(shell, SWT.NONE);
		btnControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				send(new Date().toString());
			}
		});
		btnControl.setText("Control");
		btnControl.setBounds(394, 158, 114, 25);

		button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("TID:" + Thread.currentThread().getId());
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							System.out.println("TID:"
									+ Thread.currentThread().getId());
							testMINAServer();							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				};
				Thread thread = new Thread(runnable);

				thread.start();
				
				button.setEnabled(false);
			}
		});

		button.setText("Start");
		button.setBounds(394, 124, 114, 25);

	}

	Socket server = null;
	private Button btnConnect;
	private Button btnControl;

	private void startHeartbeat() {
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//			int i=0;
//			@Override
//			public void run() {
//				System.out.println("---------***********——————————"+i++);
//				sendPacket();
//				testLin();
//			}
//		}, 5000,5000);
		Display display=Display.getDefault();
		if(!display.isDisposed()){
			Runnable runnable=new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
						sendPacket();
				}
			};
			
			display.syncExec(runnable);
			//display.timerExec(5000, runnable);

		}

	}

	void send(String msg) {
		if (ioSession == null || !ioSession.isConnected()) {
			System.out.println("ioSession==null||!ioSession.isConnected())");
			return;
		}

		ioSession.write(msg);
	}

	void send2(String msg) {
		if (ch == null || !ch.isConnected()) {
			System.out.println("ch==null||!ch.isConnected())");
			return;
		}
		ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

		try {
			ch.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	void sendUrgentData() {
//		send(new Date().toString());
//	}

	void testMINAServer() throws IOException {
		IoAcceptor acceptor = new NioSocketAcceptor();
		BlacklistFilter blacklistFilter = new BlacklistFilter();
//		InetAddress[] address = new InetAddress[1];
//		address[0] = InetAddress.getByName("169.254.11.186");
//		blacklistFilter.setBlacklist(address);
//		acceptor.getFilterChain().addFirst("black", blacklistFilter);
//		acceptor.getFilterChain().addLast("logger", new LoggingFilter());

		// acceptor.getFilterChain().addLast("codec", new
		// ProtocolCodecFilter(new
		// TextLineCodecFactory(Charset.forName("GBK"))));
		acceptor.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new NetseedCodecFactory()));
		acceptor.setHandler(new NetSeedServerHandler());

		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		acceptor.bind(new InetSocketAddress(PORT));

		System.out.println("MINA Time Server started,bind port:" + PORT);
		
	}

	IoSession ioSession = null;
	
	HashMap<SocketAddress, IoSession> sessionsHashMap = new HashMap<>();
	class NetSeedServerHandler extends IoHandlerAdapter {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {

			cause.printStackTrace();
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {

			
			SocketPacket packet = (SocketPacket) message;
			
			long sendTimestamp = FormatTransfer.lBytesToLong(packet.getCommandData());
			long spentTime = System.currentTimeMillis()-sendTimestamp;
			String msg = packet.getControllerId() + ", msg type: "
					+ packet.getPacketType() + ", spent: " + spentTime + ", raw: "
					+ Utils.bytes2HexString(packet.getRawdata());
			if (spentTime > 1000){
				logger.error(msg);
				System.err.println(msg);
			}else{				
				logger.info(msg);
			}
			
			//System.out.println("save session." + session);
			
			ioSession = session;
			sessionsHashMap.put(session.getRemoteAddress(), session);
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
			//System.out.println("IDLE " + session.getIdleCount(status));
		}

		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {
			// TODO Auto-generated method stub
			super.messageSent(session, message);
		}
	}

	public final int PORT = 722;
	SocketChannel ch = null;
	private Button button;

	private void testNIOSocketServer() throws IOException {
		// NIO的通道channel中内容读取到字节缓冲区ByteBuffer时是字节方式存储的，
		// 对于以字符方式读取和处理的数据必须要进行字符集编码和解码
		String encoding = "utf-8";
		// 加载字节编码集
		Charset cs = Charset.forName(encoding);
		// 分配两个字节大小的字节缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(64);

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
						// StringBuilder response = new StringBuilder();
						ArrayList<Byte> bytes = new ArrayList<>();

						while (ch.read(buffer) > 0) {
							// 使用字符集解码字节缓冲区数据

							for (int i = 0; i < buffer.position(); i++) {
								bytes.add(buffer.get(i));
							}
							buffer.clear();
							// CharBuffer cb = cs.decode((ByteBuffer)
							// buffer.flip());
							// buffer.rewind();
							// response.append( cb.toString());
						}

						System.out.println(Utils.bytes2HexString(bytes));
						byte[] bytes2 = new byte[bytes.size()];
						for (int i = 0; i < bytes.size(); i++) {
							bytes2[i] = bytes.get(i);
						}
						String string = new String(bytes2, "utf-8");
						System.out.println(string);
						// byte[] bbb;
						// bytes.toArray(bbb);
						//
						// System.out.println("Echoing:" + response.toString());
						// 重绕字节缓冲区，继续读取客户端套接字通道数据
						// ch.write((ByteBuffer) buffer.rewind());
						// if (response.indexOf("END") != -1)
						// ch.close();
						// buffer.clear();
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
	
	public void testLin() {
		System.out.println("lin：      sendPacket");
	}

	public void sendPacket() {
		try {

			/* if(!SocketServer.isConnected(server)){
			 server = new Socket(tbxIP.getText(), Integer.parseInt(tbxPort
			 .getText()));
			 }*/
			if(null==server){

				//server = new Socket("www.netseedcn.com",38899);
				server = new Socket("localhost",38899);
				if(server.isClosed()){
					//server.connect(endpoint);
				}
			}
			if(server.isClosed()){
			}

//			BufferedReader in = new BufferedReader(new InputStreamReader(
//					server.getInputStream()));

			// PrintWriter out = new PrintWriter(server.getOutputStream());

			OutputStream out = server.getOutputStream();

			// byte[] packetBytes =
			// Utils.long2Bytes(System.currentTimeMillis());
			
			byte[] commId=new byte[2];
			commId[0]=0x15;
			commId[1]=0x00;
			String str=tbxPacket.getText();
			byte[] data=new byte[8];
			data[0]=(byte)0x00;
			data[1]=(byte)0x00;
			data[2]=(byte)0x00;
			data[3]=(byte)0x00;
			data[4]=(byte)0x08;
			data[5]=(byte)0xAD;
			data[6]=(byte)0xE9;
			data[7]=(byte)0xAC;
			
//			data[2]=(byte)0xDE;
//			data[3]=(byte)0x07;
//			data[4]=(byte)0x05;
//			data[5]=(byte)0x07;
//			data[6]=(byte)0x30;
////			DE07050730
//			out.write(createPacke(commId,str.getBytes()));
			out.write(createPacke(commId,data));
			//out.flush();
			//out.close();
			//System.out.println("心跳时收到：" + in.readLine());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public byte[] createPacke(byte[] commId,byte[] packetBytes){

		// 数据包格式：数据存储：小端模式。
		// 1. 两个字节的启动码（0xAAAA）+
		// 2. 两个字节的数据包长度（不包含两个字节的启动码，包含校验和两个字节）+
		// 3. 主控制器ID(六个字节) +
		// 4. 主从控制器编号（一个字节） +
		// 5. 操作码（两个字节）+
		// 6. 若干字节的数据（不定长） +
		// 7. 校验和(两个字节)。
		int len = 13 + packetBytes.length + 2;
		byte[] data = new byte[len];

		// 1. 两个字节的启动码（0xAAAA
		data[0] = (byte) 0xAA;
		data[1] = (byte) 0xAA;

		// 2. 两个字节的数据包长度（不包含两个字节的启动码，包含校验和两个字节）+
		byte[] lenBytes = Utils.int2Bytes(len - 2);
		data[2] = lenBytes[3];
		data[3] = lenBytes[2];

		// 3. 主控制器ID(六个字节)
		//DE0707190032
//		data[4] = (byte)0xDE;
//		data[5] = (byte)0x07;
//		data[6] = (byte)0x07;
//		data[7] = (byte)0x19;
//		data[8] = (byte)0x00;
//		data[9] = (byte)0x32;
		
		data[4] = (byte)0xDE;
		data[5] = (byte)0x07;
		data[6] = (byte)0x05;
		data[7] = (byte)0x07;
		data[8] = (byte)0xE0;
		data[9] = (byte)0x09;

		// 4. 主从控制器编号（一个字节）
		data[10] = 0;

		// 5. 操作码（两个字节）
//		data[11] = 0;
//		data[12] = 0x65; // 心跳？
		data[11] = commId[0];
		data[12] = commId[1]; // 心跳？

		// 6. 若干字节的数据（不定长）
		System.arraycopy(packetBytes, 0, data, 13, packetBytes.length);

		// 7. 校验和(两个字节)
		byte[] crc=CRC16.calcCRC(data, 2, len-2-1);
		data[12 + packetBytes.length + 1] = crc[0];
		data[12 + packetBytes.length + 2] = crc[1];

		System.out.println("Send: " + Utils.bytes2HexString(data));

		//System.out.println("心跳时收到：" + in.readLine());
		return  data;
		
	}
}
