package com.switek.netseed.server.io.socket;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;


public class NetseedCodecFactory implements ProtocolCodecFactory{
	static final NetseedDecoder dec = new NetseedDecoder();
	static final NetseedEncoder enc = new NetseedEncoder();
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return dec;
	}

	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return enc;
	}

 

}
