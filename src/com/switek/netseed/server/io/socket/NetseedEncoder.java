package com.switek.netseed.server.io.socket;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.switek.netseed.server.Utils;


public class NetseedEncoder implements ProtocolEncoder {

	public void dispose(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public void encode(IoSession session, Object obj, ProtocolEncoderOutput output)
			throws Exception {
		byte[] bytes = (byte[])obj;
		System.out.println("encode," + session+","+ Utils.bytes2HexString(bytes));		
		IoBuffer buffer = IoBuffer.allocate(0).setAutoExpand(true);
		buffer.put((byte[])obj);
		buffer.flip();
		output.write(buffer);
		output.flush();
	}
}
