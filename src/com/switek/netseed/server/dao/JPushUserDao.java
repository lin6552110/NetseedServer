package com.switek.netseed.server.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.switek.netseed.server.bean.JPushUser;
import com.switek.netseed.server.dal.DB;

public class JPushUserDao extends Dao{
	
	private Logger logger=Logger.getLogger(JPushUserDao.class);
	
	public boolean exist(JPushUser jpushUser){
		String sql="select count(*) from aduser where RegistrationId=? and ControllerId=?";
		boolean exist=false;
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, jpushUser.getRegistrationId());
			pstmt.setString(2, jpushUser.getControllerId());
			set=pstmt.executeQuery();
			while(set.next()){
				if(1==set.getInt(1)){
					exist=true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return exist;
	} 
	
	public boolean insert(JPushUser jpushUser){
		String sql="INSERT INTO aduser(RegistrationId,ControllerId,Platform,Tag,Status) VALUES(?,?,?,?,?)";
		boolean result=false;
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, jpushUser.getRegistrationId());
			pstmt.setString(2, jpushUser.getControllerId());
			pstmt.setString(3, jpushUser.getPlatform());
			pstmt.setString(4, jpushUser.getTag());
			pstmt.setInt(5, jpushUser.getStatus());
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
	
	public boolean update(JPushUser jpushUser){
		boolean result=false;
		String sql="UPDATE aduser set Tag=?,Status=? where RegistrationId=? AND ControllerId=?";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, jpushUser.getTag());
			pstmt.setInt(2, jpushUser.getStatus());
			pstmt.setString(3, jpushUser.getRegistrationId());
			pstmt.setString(4, jpushUser.getControllerId());
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
	
	//Audience 根据controllerId查询所有关联的registrationId
	public List<JPushUser> jpushUserList(String controllerId){
		String sql="SELECT RegistrationId,Platform,ControllerId,Tag FROM aduser WHERE ControllerId=? AND Status=1";
		List<JPushUser> list=new ArrayList();
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, controllerId);
			set=pstmt.executeQuery();
			while(set.next()){
				JPushUser jpushUser=new JPushUser();
				String registrationId=set.getString("RegistrationId");
				String platform=set.getString("Platform");
				String tag=set.getString("Tag");
				controllerId=set.getString("ControllerId");
				jpushUser.setRegistrationId(registrationId);
				jpushUser.setPlatform(platform);
				jpushUser.setControllerId(controllerId);
				jpushUser.setTag(tag);
				list.add(jpushUser);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return list;
	}
	
	public List<String> registrationIdList(String controllerId){
		String sql="SELECT RegistrationId FROM aduser WHERE ControllerId=? AND Status=1";
		List<String> list=new ArrayList();
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, controllerId);
			set=pstmt.executeQuery();
			while(set.next()){
				String registrationId=set.getString("RegistrationId");
				list.add(registrationId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return list;
	}
	
	public void delete(String registrationId){
		String sql="DELETE FROM aduser WHERE RegistrationId=?";
		try {
			con=DB.getConnection();
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, registrationId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		
	}
}
