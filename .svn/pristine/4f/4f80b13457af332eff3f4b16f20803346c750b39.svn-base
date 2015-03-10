package com.switek.netseed.server.io.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.Inflater;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.mysql.jdbc.Util;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.FormatTransfer;

public class NetseedDecoder implements ProtocolDecoder {

	Hashtable<Long, ArrayList<Byte>> readingPackets = new Hashtable<>();
	public void decode(IoSession session, IoBuffer buffer,
			ProtocolDecoderOutput out) throws Exception {
		//System.out.println(Thread.currentThread().getId() + ": " + System.currentTimeMillis());
//		System.out.println("MyDecoder, Thread: "
//				+ Thread.currentThread().getId() + session);

		long sessionId = session.getId();
		
		ArrayList<Byte> packetBytes = readingPackets.remove(sessionId);
		if (packetBytes==null){
			packetBytes = new ArrayList<>();
		}else{
			ServerForm.debugMsg("Resume read from: " + Utils.bytes2HexString(packetBytes));
		}
		
		while (buffer.hasRemaining()) {
			packetBytes.add(buffer.get());
		}

		if (packetBytes.size() < 15) {
			ServerForm.showLog(MsgType.Info, "Incomplete packet will wait for next clip. "
					+ packetBytes.size() + ". " + Utils.bytes2HexString(packetBytes));
			readingPackets.put(sessionId, packetBytes);
			return;
		}
		if (!packetBytes.get(0).equals((byte) 0xAA)
				|| !packetBytes.get(1).equals((byte) 0xAA)) {
			ServerForm
					.showLog(
							MsgType.Warn,
							"Invalid packet head."
									+ Utils.bytes2HexString(packetBytes));
			packetBytes.clear(); // 丢弃
			return;
		}
		
		byte[] bLen = new byte[2];
		bLen[0] = packetBytes.get(2);
		bLen[1] = packetBytes.get(3);

		int packetLen = FormatTransfer.lBytesToShort(bLen) + 2;
		if (packetBytes.size() < packetLen) {
			ServerForm.showLog(MsgType.Info, "Incomplete packet will wait for next clip. "
					+ packetBytes.size() + " / " + packetLen + ". " + Utils.bytes2HexString(packetBytes));
			readingPackets.put(sessionId, packetBytes);
			return;
		}
		if (packetBytes.size() != packetLen) {
			ServerForm.showLog(MsgType.Warn,
					"Invalid packet len. " + packetLen + "/"
							+ packetBytes.size() + ". raw data: "
							+ Utils.bytes2HexString(packetBytes));
			
			packetBytes.clear(); // 丢弃
			return;
		} 
		
		boolean isValidCRC = AsyncSocketMsgHandler.isValidCRC(packetBytes);
		if (!isValidCRC) {
			ServerForm.showLog(MsgType.Warn, "Invalid CRC!");
			return;
		}
		
		SocketPacket packet = new SocketPacket(session);
		packet.setReceiveTime(System.currentTimeMillis());

//		byte[] bytesControllerIdYear = new byte[2];
//		bytesControllerIdYear[0] = packetBytes.get(4);
//		bytesControllerIdYear[1] = packetBytes.get(5);
//		String year = Utils
//				.formatDecimal(FormatTransfer
//						.lBytesToShort(bytesControllerIdYear),
//						"0000");
//
//		String month = Utils.formatDecimal(packetBytes.get(6)
//				.intValue(), "00");
//		String day = Utils.formatDecimal(packetBytes.get(7)
//				.intValue(), "00");
//
//		byte[] bytesControllerIdNo = new byte[2];
//		bytesControllerIdNo[0] = packetBytes.get(8);
//		bytesControllerIdNo[1] = packetBytes.get(9);
//
//		String No = Utils.formatDecimal(
//				FormatTransfer.lBytesToShort(bytesControllerIdNo),
//				"0000");

//		String controllerId = year + month + day + No;
		byte[] bytesControllerId = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytesControllerId[i] = packetBytes.get(4+i);
		}	
		String controllerId = Utils.bytes2HexString2(bytesControllerId);
		packet.setControllerId(controllerId);
		packet.setRawdata(new ArrayList<>(packetBytes));
		packet.setExtensionId(packetBytes.get(10));

		byte[] bytesCmdId = new byte[2];
		bytesCmdId[0] = packetBytes.get(11);
		bytesCmdId[1] = packetBytes.get(12);
		packet.setCommandId(FormatTransfer
				.lBytesToShort(bytesCmdId));
		
		out.write(packet);
		
		//System.out.println(packet.getControllerId() + "," + Thread.currentThread().getId() + ": " + System.currentTimeMillis());
	}

	public void dispose(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
			throws Exception {

	}

	public static String byteToHexString(byte[] bs) {
		StringBuffer _hexValue = new StringBuffer();
		for (int i = 0; i < bs.length; i++) {
			int _val = ((int) bs[i]) & 0xff;
			if (_val < 16) {
				_hexValue.append("0");
			}
			_hexValue.append(Integer.toHexString(_val) + " ");
			if (i > 0 && (i + 1) % 8 == 0) {
				_hexValue.append("\n");
			}
		}
		System.out.println(_hexValue.toString());
		return _hexValue.toString();
	}

	public static byte[] decompress(byte[] data) {
		byte[] output = new byte[0];

		Inflater decompresser = new Inflater(false);
		decompresser.reset();
		decompresser.setInput(data);

		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				o.write(buf, 0, i);
			}
			output = o.toByteArray();
		} catch (Exception e) {
			output = data;
			e.printStackTrace();
		} finally {
			try {
				o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		decompresser.end();
		return output;
	}

}
