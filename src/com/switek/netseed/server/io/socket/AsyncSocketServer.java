/**
 * This class is just only for testing
 */
package com.switek.netseed.server.io.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.switek.netseed.server.Settings;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

/**
 * @author F1008570 This class is just only for testing
 */
public class AsyncSocketServer {
	// private static Logger logger = Logger.getLogger(AsyncSocketServer.class);
	ExecutorService mThreadPool = null;
	IoAcceptor acceptor = null;
	private static final int WRITE_TIMEOUT = 1 * 30;
	int port = 0;
	int processorCount = 8;
	int socketIOPool = 512;
	String head = "";
	String sn = "";

	public AsyncSocketServer(int port, int processorCount, int socketIOPool)
			throws IOException {
		this.port = port;
		this.processorCount = processorCount;
		this.socketIOPool = socketIOPool;

		String maxThread = Settings.getSetting("maxthreads");
		ServerForm.debugMsg("MaxThread: " + maxThread);
		mThreadPool = Executors.newFixedThreadPool(Integer.valueOf(maxThread));
		// mThreadPool=Executors.newCachedThreadPool();
	}

	public void stop() {
		if (null != acceptor) {
			acceptor.unbind();
			acceptor.dispose();
		}

		mThreadPool.shutdownNow();
	}

	public void listen() throws IOException {

		ServerForm.showLog(MsgType.Info, "Starting the Netseed server...");

		acceptor = new NioSocketAcceptor(processorCount);

		acceptor.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new NetseedCodecFactory()));

		// acceptor.getFilterChain().addLast("threadPool",
		// new ExecutorFilter(Executors.newCachedThreadPool()));
		acceptor.getFilterChain().addLast("threadPool",
				new ExecutorFilter(Executors.newFixedThreadPool(socketIOPool)));
		acceptor.setHandler(new NetSeedServerHandler());

		acceptor.getSessionConfig().setWriteTimeout(WRITE_TIMEOUT);
		acceptor.getSessionConfig().setReadBufferSize(256);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 20);
		acceptor.bind(new InetSocketAddress(port));

		ServerForm.showLog(MsgType.Info, "Netseed Server started, on "
				+ InetAddress.getLocalHost() + ":" + port);

	}

	class NetSeedServerHandler extends IoHandlerAdapter {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			ServerForm.errorMsg(cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			SocketPacket packet = (SocketPacket) message;
			// System.out.println(packet.getControllerId() + "," +
			// Thread.currentThread().getId() + ": " +
			// System.currentTimeMillis());
			AsyncSocketMsgHandler handler = new AsyncSocketMsgHandler(packet);
			//AsyncSocketMsgHandlerBAK handler = new AsyncSocketMsgHandlerBAK(packet);
			mThreadPool.execute(handler);
			// System.out.println(packet.getControllerId() + "," +
			// Thread.currentThread().getId() + ": " +
			// System.currentTimeMillis());
			// String msg = packet.getControllerId() + ", msg type: "
			// + packet.getPacketType() + ", " + ", raw: "
			// + Utils.bytes2HexString(packet.getRawdata());
			// ServerForm.debugMsg(msg);
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
			ServerForm.showLog("Close the Idle session " + session.toString());
			session.close(false);
		}

		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {
			// TODO Auto-generated method stub
			super.messageSent(session, message);
		}
		
		@Override
		public void sessionClosed(IoSession session) throws Exception {
			// TODO Auto-generated method stub
			System.out.println("session close...");
		}
		
	}
}
