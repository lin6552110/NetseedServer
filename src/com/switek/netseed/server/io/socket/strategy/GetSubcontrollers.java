package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class GetSubcontrollers extends CommStrategy{
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		Controller controller = DeviceManager.getController(controllerId);
		List<Map<String, Object>> replyBody = new ArrayList<>();
		int resultCode = 0;
		if (controller != null) {
			Hashtable<String, SubController> subcontrollers = controller
					.getSubControllers();
			Enumeration<SubController> en = subcontrollers.elements();
			while (en.hasMoreElements()) {
				SubController subcontroller = en.nextElement();
				Map<String, Object> replyBodyMap = new HashMap<>();
				replyBodyMap.put("ExtId", subcontroller.getControllerId());
				replyBodyMap.put("Name", subcontroller.getControllerName());
				replyBody.add(replyBodyMap);
			}
		} else {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
		}

		send.sendPacket(packet, controllerId, (byte) 0,
				SocketPacket.COMMAND_ID_GET_SUBCONTROLLERS, resultCode, "1.0",
				replyBody);
	}

}
