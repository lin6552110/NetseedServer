package com.switek.netseed.server.io.socket.strategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.json.JSONObject;

import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.util.FormatTransfer;

public class CommGetFileContent extends CommStrategy{
	
	private SendPacket send =new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");

		int resultCode;
		String fileName = jsonBody.getString("FileName");
		fileName = "files\\" + fileName;
		File file = new File(fileName);
		byte[] bytesFile = null;
		if (!file.exists()) {
			resultCode = ErrorCode.ERROR_COULDNOT_FIND_FILE;
			bytesFile = FormatTransfer.toLH(resultCode);
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);

				// 4 bytes for result code.
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
			} catch (IOException e) {
				resultCode = ErrorCode.ERROR_READ_FILE;
				bytesFile = FormatTransfer.toLH(resultCode);
				ServerForm.showLog(e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fis = null;
				}
			}
		}

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_FILE_CONTENT, bytesFile);
	}

}
