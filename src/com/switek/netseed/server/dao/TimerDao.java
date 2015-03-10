package com.switek.netseed.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.switek.netseed.server.dal.DB;

public class TimerDao extends Dao{

	private Logger logger=Logger.getLogger(TimerDao.class);
	
	public List<String> getTimerId(List<String> controllerIdList){
		List timerIdlist=new ArrayList<String>();
		if(null!=controllerIdList&&controllerIdList.size()>0){
			String sql="select TimerId from adtimersteps where  ControllerId in ? GROUP BY TimerId";
			StringBuffer buff=new StringBuffer();
			buff.append("(");
			for(String str:controllerIdList){
				buff.append("'"+str+"',");
			}
			buff=buff.replace(buff.length()-1,buff.length(),"");
			buff.append(")");
			sql=sql.replace("?", buff);
			System.out.println(sql);
			try {
				con=DB.getConnection();
				stmt=con.createStatement();
				set=stmt.executeQuery(sql);
				while(set.next()){
					timerIdlist.add(set.getString("TimerId"));
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				DB.close(con,stmt, set);
			}
		}	
		return timerIdlist;
	}

	public boolean logTimerHistory(ArrayList<Object> params) throws SQLException {
		// TODO Auto-generated method stub
		String sql = "insert into tdtimerhistory (timerid, stepseqno, resultcode, logmessage, remark)values(?,?,?,?,?)";
		boolean result=false;
		try {
			con=DB.getConnection();
			con.setAutoCommit(true);
			pstmt=con.prepareStatement(sql);
			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					 pstmt.setObject(i + 1, params.get(i));
				}
			}
			//执行SQL，并获取返回结果
			result=(pstmt.executeUpdate()>=1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + sql, e);
			throw e;
		}finally{
			DB.close(con, pstmt, set);
		}
		return result;
	}
	

}
