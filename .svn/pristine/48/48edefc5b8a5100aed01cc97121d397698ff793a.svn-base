package com.switek.netseed.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.switek.netseed.server.bean.AppVersion;
import com.switek.netseed.server.dal.DB;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class AppManager {

	static long lastRefreshTime = 0;
	static List<AppVersion> appList = new ArrayList<>();

	public static AppVersion getAppInfo(String appName, String platform) {

		if (System.currentTimeMillis() - lastRefreshTime >= 1 * 60 * 60 * 1000) {
			refreshAppList();
		}

		for (int i = 0; i < appList.size(); i++) {
			AppVersion app = appList.get(i);
			if (app.getAppName().equalsIgnoreCase(appName)
					&& app.getPlatform().equalsIgnoreCase(platform)) {
				return app;
			}
		}
		return null;
	}

	public static void refreshAppList() {
		ServerForm.showLog(MsgType.Info,"Loading app list from database...");
		ResultSet appSet = DB.executeQuery("select * from adApps");

		List<AppVersion> tmpList = new ArrayList<>();
		try {
			while (appSet.next()) {
				String appName = appSet.getString("AppName");
				String platform = appSet.getString("Platform");
				String latestVersion = appSet.getString("LatestVersion");
				String releaseNotes = appSet.getString("ReleaseNotes");
				AppVersion appVersion = new AppVersion();
				appVersion.setAppName(appName);
				appVersion.setPlatform(platform);
				appVersion.setLatestVersion(latestVersion);
				appVersion.setReleaseNotes(releaseNotes);
				tmpList.add(appVersion);
			}
			appList = tmpList;
			lastRefreshTime = System.currentTimeMillis();
		} catch (SQLException e) {
			ServerForm.showLog(e);
		} finally {
			try {
				appSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
