package com.switek.netseed.server.dal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import com.switek.netseed.server.test.Test;

/**
 * 简单的JDBC工具类
 * 
 * @author FengPeng
 */
public class DB {
	private static final Logger log = Logger.getLogger(DB.class);

	static {
		init();
	}

	private static ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<Connection>();
	
	public static void init() {
		
		//初始化数据库连接配置参数
		InputStream in = Test.class.getResourceAsStream("/proxool.xml");
		Reader reader = null;
		try {
			reader = new InputStreamReader(in, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		try {
			JAXPConfigurator.configure(reader, false);
		} catch (ProxoolException e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			try {
				reader.close();
				reader = null;
			} catch (IOException e1) {
				log.error(e1.getLocalizedMessage(), e1);
			}
		}

		try {
			Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
		} catch (ClassNotFoundException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return;

	}

	public static Connection getConnection() throws SQLException {
		
		Connection conn = threadLocalConnection.get();
		if (conn == null || conn.isClosed()){
			conn = DriverManager.getConnection("proxool.DBPool");
			threadLocalConnection.set(conn);
		}
		return conn;
	}

	/**
	 * 在一个数据库连接上执行一个静态SQL语句查询
	 * 
	 * @param conn
	 *            数据库连接
	 * @param staticSql
	 *            静态SQL语句字符串
	 * @return 返回查询结果集ResultSet对象
	 */
	public static ResultSet executeQuery(String staticSql) {
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			//创建执行SQL的对象
			Statement stmt = conn.createStatement();
			//执行SQL，并获取返回结果
			rs = stmt.executeQuery(staticSql);
			//conn.close();
		} catch (SQLException e) {
			log.error("#ERROR# :执行SQL语句出错，请检查！\n" + staticSql, e);
			throw new RuntimeException(e);
		}
		return rs;
	}
	
	public static ResultSet executeQuery(String sql,List<Object> params){
		ResultSet rs=null;
		try {
				Connection conn=DB.getConnection();
				PreparedStatement stmt=conn.prepareStatement(sql);
				for(int i=0;i<params.size();i++){
					stmt.setObject(i+1, params.get(i));
				}
				rs=stmt.executeQuery();
		} catch (SQLException e) {
			// TODO: handle exception
			log.error("#ERROR# :执行SQL语句出错，请检查！\n"+sql,e);
			throw new RuntimeException(e);
		}
		return rs;
		
	}
	public static ResultSet executeQuery(String sql,String params){
		ResultSet rs=null;
		try{
			Connection conn=DB.getConnection();
			PreparedStatement stmt=conn.prepareStatement(sql);
			stmt.setString(0, params);
			System.out.println(stmt.toString());
			rs=stmt.executeQuery();
		}catch(SQLException e){
			// TODO: handle exception
						log.error("#ERROR# :执行SQL语句出错，请检查！\n"+sql,e);
						throw new RuntimeException(e);
		}
		return rs;
	}

	public static boolean executeSQL(String staticSql,
			List<Object> params) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt=null;
		try {
			//创建执行SQL的对象
			conn = getConnection();
			stmt = conn.prepareStatement(staticSql);
			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					stmt.setObject(i + 1, params.get(i));
				}
			}
			//执行SQL，并获取返回结果
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			log.error("#ERROR# :执行SQL语句出错，请检查！\n" + staticSql, e);
			throw e;
		} finally {		
			try{
				if(stmt!=null){
					if(!stmt.isClosed()){
						stmt.close();
					}
				}
			}catch(SQLException e){
				log.error("#ERROR# :关闭数据库连接发生异常，请检查！", e);
				throw e;
			}finally{
				if (conn != null && conn.getAutoCommit()) {
					try {
						closeConnection(conn);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	public static void beginTran() throws SQLException {
		Connection conn;
		conn = getConnection();
		conn.setAutoCommit(false);		
	}

	public static boolean rollback() {		
		Connection conn = threadLocalConnection.get();
		if (conn == null){
			return false;
		}
		
		try {
			threadLocalConnection.set(null);
			conn.rollback();
			conn.close();
			return true;
		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	public static boolean commit() {
		Connection conn = threadLocalConnection.get();
		if (conn == null){
			return false;
		}
		threadLocalConnection.set(null);
		try {			
			conn.commit();
			conn.close();
			return true;
		} catch (SQLException e) {
			log.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	/**
	 * 在一个数据库连接上执行一批静态SQL语句
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sqlList
	 *            静态SQL语句字符串集合
	 * @throws SQLException
	 */
	public static void executeBatch(List<String> sqlList) throws SQLException {
		Connection conn = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			//创建执行SQL的对象
			Statement stmt = conn.createStatement();
			for (String sql : sqlList) {
				stmt.addBatch(sql);
			}
			//执行SQL，并获取返回结果
			stmt.executeBatch();
			conn.commit();
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			log.error("#ERROR# :执行批量SQL语句出错，请检查！", e);

			throw e;
		}
	}

	public static void closeConnection(Connection conn) {
		if (conn == null)
			return;
		try {
			threadLocalConnection.set(null);
			if (!conn.isClosed()) {
				//关闭数据库连接
				conn.close();
			}
		} catch (SQLException e) {
			log.error("#ERROR# :关闭数据库连接发生异常，请检查！", e);
			throw new RuntimeException(e);
		}
	}
	
	public static void close(Connection con,Statement stmt,ResultSet set){
		try {
			if(null!=set&&!set.isClosed()){
				set.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("#ERROR# :关闭数据库连接发生异常，请检查！", e);
			throw new RuntimeException(e);
		}finally{
			try {
				 if(null!=stmt){
					if(!stmt.isClosed()){
						stmt.close();
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.error("#ERROR# :关闭数据库连接发生异常，请检查！", e);
				throw new RuntimeException(e);
			}finally{
				closeConnection(con);
			}
		}
	}
	
	public static void colseConnection(){
		Connection con=threadLocalConnection.get();
		closeConnection(con);
	}
}
