package com.switek.netseed.server.dao;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cn.jpush.api.push.model.PushPayload;

import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.dal.DB;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class JpushHistoryDao extends Dao {

	
	public void addJpushHistory(PushPayload payload, String controllerId,
			String deviceId) {
		// TODO Auto-generated method stub
		JSONObject jsonPayload=JSONObject.fromObject(payload.toString());
		String alert=jsonPayload.getJSONObject("notification").getJSONObject("ios").getString("alert");
		JSONArray registrationId=jsonPayload.getJSONObject("audience").getJSONArray("registration_id");
		String[] alerts=alert.split("\n");
		String str=alerts[0];
		String dateStr=alerts[1];
		DateFormat formate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql="INSERT INTO adpushhistory(Payload,PushDT,RegistrationId,ControllerId,DeviceId) VALUES(?,?,?,?,?)";
		try {
			Date date=formate.parse(dateStr);
			con=DB.getConnection();
			con.setAutoCommit(false);
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, str);
			pstmt.setObject(2, date);
			pstmt.setObject(4, controllerId);
			pstmt.setObject(5, deviceId);
			for(Object o:registrationId){
				pstmt.setObject(3, o);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block, e)
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				ServerForm.showLog(MsgType.Error, "回滚数据库失败！" + sql, e1);
			}
			ServerForm.showLog(MsgType.Error, "#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
				
	}
	
	public List<Map<String,Object>> getJpushHistory(String registrationId,String controllerId,String deviceId,long startDT,long endDT){
		List<Map<String,Object>> list=new ArrayList();
		StringBuffer sql=new StringBuffer("SELECT PushDT,Payload,RegistrationId,ControllerId,DeviceId FROM adpushhistory WHERE RegistrationId=?  ");
		
		if(null!=controllerId &&!"".equals(controllerId)){
			sql.append("  AND ControllerId='"+controllerId+"' ");
		}
		if(null!=deviceId&&!"".equals(deviceId)){
			sql.append("  AND DeviceId='"+deviceId+"' ");
		}
		if(0!=startDT){
			sql.append(" AND UNIX_TIMESTAMP(PushDT)>="+startDT/1000);
		}
		if(0!=endDT){
			sql.append(" AND UNIX_TIMESTAMP(PushDT)<="+endDT/1000);
		}
		
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql.toString());
			pstmt.setString(1, registrationId);
			set=pstmt.executeQuery();
			while(set.next()){
				Map<String,Object> map=new HashMap();
				map.put("PushDT", ((Date)set.getObject("PushDT")).getTime());
				map.put("Payload", set.getObject("Payload"));
				map.put("RegistrationId", set.getObject("RegistrationId"));
				map.put("ControllerId", set.getObject("ControllerId"));
				map.put("DeviceId", set.getObject("DeviceId"));
				list.add(map);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			ServerForm.showLog(MsgType.Error, "#ERROR# :执行SQL语句出错，请检查！\n"+sql.toString(),e);
		}finally{
			DB.close(con, pstmt, set);		
		}
		return list;
		
	}
	
	public void deleteJpushHistory(String registrationId){
		String sql="DELETE FROM adpushhistory WHERE RegistrationId=?";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, registrationId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			ServerForm.showLog(MsgType.Error, "#ERROR# :执行SQL语句出错，请检查！\n"+sql,e);
		}finally{
			DB.close(con, pstmt, set);
		}
	}
	
	public void deleteJpushHistory(String registrationId,long date){
		String sql="DELETE FROM adpushhistory WHERE RegistrationId=? AND UNIX_TIMESTAMP(PushDT)=?";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, registrationId);
			pstmt.setObject(2, date/1000);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			ServerForm.showLog(MsgType.Error, "#ERROR# :执行SQL语句出错，请检查！\n"+sql,e);
		}finally{
			DB.close(con, pstmt, set);
		}
	}
	
	public void deleteJpushHistory(String registrationId,long date[]){
		String sql="DELETE FROM adpushhistory WHERE RegistrationId=? AND UNIX_TIMESTAMP(PushDT) IN ";
		StringBuffer buffer=new StringBuffer(sql);
		buffer.append("(");
		for(int i=0;i<date.length;i++){
			buffer.append(date[i]/1000);
			if(i!=date.length-1){
				buffer.append(",");
			}
		}
		buffer.append(")");
		System.out.println(buffer.toString());
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(buffer.toString());
			pstmt.setString(1, registrationId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			ServerForm.showLog(MsgType.Error, "#ERROR# :执行SQL语句出错，请检查！\n"+buffer.toString(),e);
		}finally{
			DB.close(con, pstmt, set);
		}
	}



}
