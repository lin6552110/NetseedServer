package com.lin.test;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.switek.netseed.server.io.socket.NetseedCodecFactory;

public class TestMinaClient {
	public static void main(String[] args) {
		NioSocketConnector connector=new NioSocketConnector();
		connector.getFilterChain().addLast("threadPool",
				new ExecutorFilter(Executors.newFixedThreadPool(512)));
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetseedCodecFactory()));
		connector.setConnectTimeout(30);
		connector.setHandler(new TimeServerHandler());
		ConnectFuture future=connector.connect(new InetSocketAddress("127.0.0.1", 28898));
		future.awaitUninterruptibly();
		future.getSession().write("hello");
		future.getSession().write("quit");
		future.getSession().getCloseFuture().awaitUninterruptibly();
		connector.dispose();
	}

}
