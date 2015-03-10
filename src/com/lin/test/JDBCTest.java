package com.lin.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class JDBCTest {
	private String user="cziotappuser";
	private String password="cz.pwd";
	private String url="jdbc:mysql://localhost:13306/cziot";
	
	public void test(){
		Connection con=null;
		PreparedStatement stm=null;
		ResultSet set=null;
		String sql="select * from adapps";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try {
				con=DriverManager.getConnection(url, user, password);
				stm=con.prepareStatement(sql);
				set=stm.executeQuery();
				con.close();
				
				while(set.next()){
					System.out.println(set.getString(1)+"****"+set.getString(2)+"***"+set.getString(3)+"***"+set.getString(4));
				}
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		JDBCTest test=new JDBCTest();
		test.test();
		
	}

}
