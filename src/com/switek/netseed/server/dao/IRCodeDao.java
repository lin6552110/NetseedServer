package com.switek.netseed.server.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.switek.netseed.server.dal.DB;

public class IRCodeDao extends Dao{
	
	private Logger logger=Logger.getLogger(IRCodeDao.class);
	
	public int getIRCodeCount(int deviceType,String brandCode){
		String sql="select COUNT(*) from adircode where DeviceType=? and BrandCode=?";
		int count=0;
		try {
			con=DB.getConnection();
			con.setAutoCommit(true);
			pstmt=con.prepareStatement(sql);
			pstmt.setInt(1, deviceType);
			pstmt.setString(2, brandCode);
			set=pstmt.executeQuery();
			while(set.next()){
				count=set.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
		}finally{
			DB.close(con, pstmt, set);
		}
		return count;
	}

	

}
