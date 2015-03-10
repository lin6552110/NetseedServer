package com.switek.netseed.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.switek.netseed.server.dal.DB;

public class Dao {
	protected Connection con=null;
	protected Statement stmt=null;
	protected PreparedStatement pstmt=null;
	protected ResultSet set=null;
}
