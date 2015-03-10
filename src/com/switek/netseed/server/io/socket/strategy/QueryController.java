package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class QueryController extends CommStrategy{
	private SendPacket send=new SendPacket();
	/**
	 * Query the status for controller by Id
	 * 
	 * @param packet
	 */
	@Override
	public  void analysisPacket(SocketPacket packet) {

		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONArray bodyArray = jsonObject.getJSONArray("Body");

		List<Map<String, Object>> replyBody = new ArrayList<>();
		for (int i = 0; i < bodyArray.size(); i++) {
			JSONObject body = (JSONObject) bodyArray.get(i);
			String controllerId = body.get("ControllerID").toString();
			System.out.println(controllerId);

			Controller controller = DeviceManager.getController(controllerId);

			Map<String, Object> replyBodyMap = new HashMap<>();
			String status = "00";
			String macAddress = "";
			long codeUpdateDt=0;
			if (controller == null) {
				status = "02";
			} else {
				status = controller.isOnline() ? "00" : "01";
				macAddress = controller.getMacAddress();
				codeUpdateDt=controller.getCodeUpdateDT();
			}
			replyBodyMap.put("IRCodeUpdateDT", codeUpdateDt);
			replyBodyMap.put("ControllerId", controllerId);
			replyBodyMap.put("MacAddress", macAddress);

			// if (controllerId.equals("DE070506FFFF")) {
			// status = "00"; // TODO: remove this hard code.
			// }

			replyBodyMap.put("Status", status);
			replyBody.add(replyBodyMap);
		}

		send.sendPacket(packet, "", (byte) 0, SocketPacket.COMMAND_ID_QUERY_CTR, 0,
				"1.0", replyBody);
	}

}
