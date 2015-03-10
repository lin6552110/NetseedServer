package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Brand;
import com.switek.netseed.server.bean.DeviceTypeDef;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommGetBrandCode extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		int type = packet.getJsonBody().getInt("Type");

		DeviceTypeDef def = DeviceManager.getDeviceTypeDef(type);
		List<Map<String, Object>> replyBody = new ArrayList<>();
		int resultCode = 0;
		if (def == null) {
			resultCode = ErrorCode.ERROR_INVALID_DEVICETYPE;
		} else {
			for (Brand brand : def.getBrands()) {
				HashMap<String, Object> typeJsonMap = new HashMap<>();
				typeJsonMap.put("BrandCode", brand.getBrandCode());
				typeJsonMap.put("Name", brand.getBrandName());
				typeJsonMap.put("LOGO", brand.getLogoUrl());
				replyBody.add(typeJsonMap);
			}
		}

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_BRANDCODE, resultCode, "1.0",
				replyBody);
	}

}
