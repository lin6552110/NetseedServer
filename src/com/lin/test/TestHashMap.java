package com.lin.test;

import java.util.HashMap;


public class TestHashMap {
	//public static Hashtable<String,Name> table=new Hashtable();
	public static HashMap<String,Name> table=new HashMap();
	
	public static Name getName(String str){
		return table.get(str);
	}

}
