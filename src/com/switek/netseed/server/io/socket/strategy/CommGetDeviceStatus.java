package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

/**
 * @author Lin 2015年2月6日
 * 获取同一中控下多个设备的lastPressedKey
 * 
 */

public class CommGetDeviceStatus extends CommStrategy{
	private SendPacket send=new SendPacket();
	private ActionResult result=new ActionResult();
	private String controllerId="";
	private String subControllerId="";
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		controllerId=packet.getControllerId();
		subControllerId=packet.getExtensionIdString();
		getDeviceStatus(packet);
		send.sendPacket(packet
				, controllerId
				, packet.getExtensionId()
				, packet.getCommandId()
				, result.getResultCode()
				, "1.0"
				, (List)result.getOutputObject());
	}
	
	private void getDeviceStatus(SocketPacket packet){
		SubController subController=DeviceManager.getSubcontroller(controllerId, subControllerId);
		int resultCode=0;
		if(null==subController){
			resultCode=ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
		}else{
			SocketPacket con=connect2Controller(controllerId);
			if(null!=con){
				JSONObject json=JSONObject.fromObject(packet.getCommandDataString());
				JSONArray jsonBody=json.getJSONArray("Body");
				List<Map<String,Object>> resultList=new ArrayList();
				for(int i=0;i<jsonBody.size();i++){
					JSONObject index=jsonBody.getJSONObject(i);
					int deviceType=index.getInt("DeviceType");
					int deviceIndex=0;
					if(index.containsKey("DeviceIndex")){
						deviceIndex=index.getInt("DeviceIndex");
					}
					long lastPressedKey=getLastPressedKey(subController,deviceType,deviceIndex);
					Map map=new HashMap<String,Object>();
					map.put("DeviceType", deviceType);
					map.put("DeviceIndex", deviceIndex);
					map.put("LastPressedKeyIndex", lastPressedKey);
					resultList.add(map);
				}
				result.setOutputObject(resultList);
			}else{
				resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
				// status = "01";
				ServerForm.showLog(MsgType.Warn,
						"Couldn't connect to controller: " + controllerId);
			}
		}
		result.setResultCode(resultCode);
	}

	private long getLastPressedKey(SubController subCon,int deviceType,int deviceIndex){
		Device device=subCon.getDeviceByType(deviceType, deviceIndex);
		return device.getLastPressedKey();
	}
}
