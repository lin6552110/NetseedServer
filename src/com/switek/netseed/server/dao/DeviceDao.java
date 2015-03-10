package com.switek.netseed.server.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.dal.DB;

/**
 * @author Lin 2015年3月2日
 */
public class DeviceDao extends Dao{
	private Logger logger=Logger.getLogger(ControllerDao.class);
	
	//获取安防设备状态
	public String getSAPDeviceStatus(String deviceId){
		
		String sql="select status  from mddevice where deviceId=?";
		String status="";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, deviceId);
			set=pstmt.executeQuery();
			while(set.next()){
				 status=set.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return status;
	}
	
	//单个设备撤防或布防
	public boolean updateSAPStatus(String deviceId,String status){
		boolean result=false;
		String sql="UPDATE mddevice SET Status=? WHERE DeviceId=?";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setString(2, deviceId);
			int count=pstmt.executeUpdate();
			if(1==count){
				result=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return result;
	}
	//批量设备撤防布防
	public boolean updateSAPStatus(List<Map<String,String>> list){
		String sql="UPDATE mddevice SET Status=? WHERE DeviceId=? AND ControllerId=?";
		boolean result=false;
		try {
			con=DB.getConnection();
			con.setAutoCommit(false);
			pstmt=con.prepareStatement(sql);
			for(Map<String,String> map: list){
				pstmt.setString(1, map.get("Status"));
				pstmt.setString(2, map.get("DeviceId"));
				pstmt.setString(3, map.get("ControllerId"));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			con.commit();
			result=true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :正在回滚数据库,执行SQL语句出错，请检查！\n" + sql, e);
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				logger.error("#ERROR# :批量执行SQL语句出错，回滚数据库失败！\n" + sql, e);
			}
		}finally{
			DB.close(con, pstmt, set);
		}
		return result;
	}

	public List getDeviceIndexByType(String controllerId,
			String subcontrollerId, int deviceType) {
		// TODO Auto-generated method stub
		List deviceIndexList=new ArrayList();
		String sql="select DeviceIndex from mddevice where ControllerId=? and SubControllerId=? and DeviceType=? ORDER BY DeviceIndex ";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, controllerId);
			pstmt.setString(2, subcontrollerId);
			pstmt.setInt(3, deviceType);
			set=pstmt.executeQuery();
			while(set.next()){
				deviceIndexList.add(set.getInt("DeviceIndex"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);

		}finally{
			DB.close(con, pstmt, set);
		}
		return deviceIndexList;
	}

}
