package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.DeviceTypeDef;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommGetDeviceType extends CommStrategy{
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		Hashtable<Integer, DeviceTypeDef> list = DeviceManager
				.getDeviceTypeList();
		Enumeration<DeviceTypeDef> en = list.elements();

		int resultCode = 0;

		List<Map<String, Object>> replyBody = new ArrayList<>();
		while (en.hasMoreElements()) {
			DeviceTypeDef def = en.nextElement();
			HashMap<String, Object> typeJsonMap = new HashMap<>();
			typeJsonMap.put("Type", def.getDeviceType());
			typeJsonMap.put("Name", def.getName());
			typeJsonMap.put("LOGO", def.getLogoUrl());
			replyBody.add(typeJsonMap);
		}

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_DEVICETYPE, resultCode, "1.0",
				replyBody);
	}

}
