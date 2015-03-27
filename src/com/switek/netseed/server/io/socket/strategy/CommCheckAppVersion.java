package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.AppManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.AppVersion;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommCheckAppVersion extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");

		String platform = jsonBody.getString("Platform");
		String curVersion = jsonBody.getString("CurrentVersion");
		int type=0;
		String downPath="";
		
		if(jsonBody.containsKey("Type")){
			type=jsonBody.getInt("Type");
		}
		if(jsonBody.containsKey("Language")){
			String language=jsonBody.getString("Language");
			
		}
		String appName = "NetSeed";

		int resultCode = 0;

		AppVersion appVersion = AppManager.getAppInfo(appName, platform);
		boolean hasNewVersion = false;
		String releaseNotes = "";
		String latestVersion = curVersion;
		if (appVersion == null) {
			resultCode = ErrorCode.ERROR_INVALID_APP_NAME;
		} else {
			try {
				if (Utils.compareVersion(appVersion.getLatestVersion(),
						curVersion) > 0) {
					hasNewVersion = true;
					releaseNotes = appVersion.getReleaseNotes();
					latestVersion = appVersion.getLatestVersion();
				}
			} catch (NumberFormatException e) {
				resultCode = ErrorCode.ERROR_INVALID_VERSION_FORMAT;
			}
		}
		Map<String, Object> replyBody = new HashMap<>();
		if(type!=0){
			if(type!=1){
				hasNewVersion=false;
			}
			if(hasNewVersion){
				downPath="http://www.netseedcn.com/app/netseed/android/netseed.apk";
			}
			replyBody.put("DownPath", downPath);
		}
		replyBody.put("hasNewVersion", hasNewVersion);
		replyBody.put("Version", latestVersion);
		replyBody.put("ReleaseNotes", releaseNotes);
		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), resultCode, "1.0", replyBody);
		
	}

}
