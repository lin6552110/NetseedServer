package com.switek.netseed.server.dal;

import java.util.ArrayList;
import java.util.List;

public class SQL {
	String sql = "";
	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}
	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	/**
	 * @return the params
	 */
	public List<Object> getParams() {
		return params;
	}
	/**
	 * @param params the params to set
	 */
	public void setParams(List<Object> params) {
		this.params = params;
	}
	List<Object> params= new ArrayList<>();
}
