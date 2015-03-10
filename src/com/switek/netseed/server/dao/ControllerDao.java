package com.switek.netseed.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.dal.DB;

public class ControllerDao extends Dao{
	
	private Logger logger=Logger.getLogger(ControllerDao.class);

	public boolean codeUpdateDT(String controllerId,Date date){
		String sql="update mdcontroller set CodeUpdateDT=? where ControllerId=?";
		boolean result=false;
		try {
			con=DB.getConnection();
			con.setAutoCommit(true);
			pstmt=con.prepareStatement(sql);
			pstmt.setObject(1, date);
			pstmt.setObject(2, controllerId);
			result=(pstmt.executeUpdate()>=1);
			if(result){
				Controller controller=DeviceManager.getController(controllerId);
				controller.setCodeUpdateDT(date.getTime());	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return result;
	}
	

}
