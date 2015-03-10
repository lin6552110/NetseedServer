package com.switek.netseed.server.dal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.LearnIRCode;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.bean.TimerStep;
import com.switek.netseed.server.dao.ControllerDao;
import com.switek.netseed.server.dao.TimerDao;

public class MySQLDAL implements IDAL {

	private ControllerDao controllerDao=new ControllerDao();
	Logger logger = Logger.getLogger(MySQLDAL.class);
	static IDAL dal = null;

	public static synchronized IDAL newInstance() {
		if (dal == null) {
			dal = new MySQLDAL();
		}
		return dal;
	}

	static final boolean autoCreateSensor = true;

	@Override
	public boolean createTimer(ControllerTimer timer){		
		String Id = Utils.generateUUID();
		
		String sql = "insert into adtimer (TimerId,TimerName,Weekdays,TriggerTime) values (?,?,?,?)";
		ArrayList<Object> params = new ArrayList<>();
		params.add(Id);
		params.add(timer.getName());
		params.add(timer.getWeekDays());
		params.add(timer.getTime());
		
		try {
			DB.beginTran();
			boolean result = DB.executeSQL(sql, params);

			// Add timer;
			if (!result) {
				DB.rollback();
				return false;
			}
			
			sql = "insert into adtimersteps (timerid, seqno, stepname, controllerid, subcontrollerid, deviceid, keyindex, delay, value) values(?,?,?,?,?,?,?,?,?)";
			for (int i = 0; i < timer.getSteps().size(); i++) {
				TimerStep step = timer.getSteps().get(i);
				params = new ArrayList<>();
				params.add(Id);
				params.add(step.getSeqNo());
				params.add(step.getName());
				params.add(step.getControllerId());
				params.add(step.getSubcontrollerId());
				params.add(step.getDeviceId());
				params.add(step.getKeyIndex());
				params.add(step.getDelay());
				params.add(step.getValue());
				
				result = DB.executeSQL(sql, params);

				// Add timer steps;
				if (!result) {
					DB.rollback();
					return false;
				}
			}
			DB.commit();
			timer.setTimerId(Id);
			return result;
		} catch (Exception e) {
			DB.rollback();
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}finally{
			DB.colseConnection();
		}
	}
	
	@Override
	public boolean removeTimer(ControllerTimer timer){
	
		String sql = "update adtimer set isdeleted = 1, deletedt=CURRENT_TIMESTAMP() where timerid = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(timer.getTimerId());
		
		try {
			boolean result = DB.executeSQL(sql, params);
			return result;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
	@Override
	public boolean addSubcontroller(SubController subcontroller) {
		String sql = "insert into mdsubcontroller (controllerid, subcontrollerid, subcontrollername, registerdt, registerby, status, lasteditdt, lasteditby) values(?, ?, ?, CURRENT_TIMESTAMP, 'controller', '00', current_timestamp, 'controller')";
		ArrayList<Object> params = new ArrayList<>();
		params.add(subcontroller.getParent().getControllerId());
		params.add(subcontroller.getControllerId());
		params.add(subcontroller.getControllerName());
		try {
			DB.beginTran();
			boolean result = DB.executeSQL(sql, params);

			// Add device;
			if (!result) {
				DB.rollback();
				return false;
			}
			Device device = null;
			if (autoCreateSensor) {
				device = new Device(subcontroller);
				device.setDeviceType(0); // 0: sensor
				device.setDeviceName("Sensor");
				boolean addDeviceResult = addDevice(device);
				if (!addDeviceResult) {
					DB.rollback();
					return false;
				}
			}

			DB.commit();
			if (autoCreateSensor) {
				subcontroller.addDevice(device.getDeviceId(), device);
			}
			return result;
		} catch (Exception e) {
			DB.rollback();
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean addDevice(Device device) {
		String deviceId = device.getDeviceId();
		if (Device.isNewDevice(deviceId) || deviceId.equals("")){
			deviceId = Utils.generateUUID();
		}
		String sql = "insert into mddevice (controllerid"
				+ ", subcontrollerid, deviceid, devicename"
				+ ", devicetype, deviceIndex, brandcode"
				+ ", ircodeindex, islearnedircode, registerdt"
				+ ", registerby, status, lasteditdt"
				+ ", lasteditby, circuitcount, jsondata"
				+ ") values (?,?,?,?,?,?,?,?,?,?,'server',?,?, 'server',?,?)";
		ArrayList<Object> params = new ArrayList<>();
		params.add(device.getControllerId());
		params.add(device.getSubcontrollerId());
		params.add(deviceId);
		params.add(device.getDeviceName());
		params.add(device.getDeviceType());
		params.add(device.getDeviceIndex());
		params.add(device.getBrandCode());
		params.add(device.getIRCodeIndex());
		params.add(device.isLearnedIRCode());

		Date lastEditDT = new Date();
		params.add(lastEditDT);
		String status=device.getStatus();
		if(null==status && "".equals(status)){
			status="00";
		}
		params.add(status);
		params.add(lastEditDT);
		params.add(device.getCircuitCount());
		params.add(device.getJsonData());
		try {
			boolean result = DB.executeSQL(sql, params);
			if (result) {
				device.setLastEditDT(lastEditDT.getTime());
				device.setDeviceId(deviceId);
				//新增了设备，修改设备的CodeUpdateDT
				controllerDao.codeUpdateDT(device.getControllerId(), lastEditDT);
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateDeviceLastPressKey(Device device, long lastPressedKey) {
		//String sql = "update mddevice set lastpressedkey=? where controllerid = ? and subcontrollerid = ? and deviceid = ?";
		String sql = "update mddevice set lastpressedkey=? where deviceid = ?";
		ArrayList<Object> params = new ArrayList<>();

		params.add(lastPressedKey);

		//params.add(device.getControllerId());
		//params.add(device.getSubcontrollerId());
		params.add(device.getDeviceId());
		try {
			boolean result = DB.executeSQL(sql, params);
			if (result) {
				device.setLastPressedKey(lastPressedKey);
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateDevice(Device device) {
		String sql = "update mddevice set devicename=?, ircodeindex = ?, islearnedircode=?, devicetype=?,brandcode=?, lasteditdt = ?, lastpressedkey=?,jsondata=? where controllerid = ? and subcontrollerid = ? and deviceid = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(device.getDeviceName());
		params.add(device.getIRCodeIndex());
		params.add(device.isLearnedIRCode());
		params.add(device.getDeviceType());
		params.add(device.getBrandCode());

		Date lastEditDT = new Date();
		params.add(lastEditDT);

		params.add(device.getLastPressedKey());
		params.add(device.getJsonData());
		params.add(device.getControllerId());
		params.add(device.getSubcontrollerId());
		params.add(device.getDeviceId());
		try {
			boolean result = DB.executeSQL(sql, params);
			if (result) {
				device.setLastEditDT(lastEditDT.getTime());
				controllerDao.codeUpdateDT(device.getControllerId(), lastEditDT);
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean deleteDevice(Device device) {
		String sql = "delete d,l from mddevice as d left join mdlearnedircode as l on d.controllerid = l.controllerid and d.subcontrollerid = l.subcontrollerid and d.deviceid = l.deviceid where d.controllerid = ? and d.subcontrollerid = ? and d.deviceid= ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(device.getControllerId());
		params.add(device.getSubcontrollerId());
		params.add(device.getDeviceId());
		try {
			
			boolean result=DB.executeSQL(sql, params);
			if(result){
				controllerDao.codeUpdateDT(device.getControllerId(), new Date());
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean deleteSubController(SubController subcontroller) {
		String sql = "delete s,d,l from mdsubcontroller as s left join mddevice as d on s.controllerid = d.controllerid and s.subcontrollerid = d.subcontrollerid left join mdlearnedircode as l on d.controllerid = l.controllerid and d.subcontrollerid = l.subcontrollerid and d.deviceid = l.deviceid where s.controllerid = ? and s.subcontrollerid = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(subcontroller.getParent().getControllerId());
		params.add(subcontroller.getControllerId());
		try {
			return DB.executeSQL(sql, params);
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean registerController(String Id, String name,
			String macAddress, boolean isReregister) {

		List<String> sqlList = new ArrayList<>();
		String IdValue = Id.replace("'", "'");
		macAddress = macAddress.replace("'", "''");
		if (isReregister) {
			// sqlList.add("delete c from mdlearnedircode as c join mddevice as d on c.deviceid = d.deviceid where d.controllerid = "
			// + IdValue);
			// sqlList.add("delete from mddevice where controllerid = " +
			// IdValue);
			// sqlList.add("delete from mdsubcontroller where controllerid = "
			// + IdValue);
			// sqlList.add("delete from mdcontroller where controllerid = "
			// + IdValue);
			sqlList.add("update mdcontroller set MacAddress='"
					+ macAddress
					+ "', RegisterDT = CURRENT_TIMESTAMP, LastEditDT=CURRENT_TIMESTAMP, LastEditBy='reregister' where controllerid = '"
					+ IdValue + "'");
		} else {
			String strInsertController = "insert into mdcontroller (controllerid, controllername, MacAddress, registerdt, registerby, lasteditby, lasteditdt, status) values ('"
					+ IdValue
					+ "', '"
					+ name.replace("'", "''")
					+ "', '"
					+ macAddress
					+ "', CURRENT_TIMESTAMP, 'auto','auto', CURRENT_TIMESTAMP, '01')";
			String strInsertSubcontroller = "insert into MDSubController (controllerid, subcontrollerid, subcontrollername, registerdt, registerby, lasteditby, lasteditdt, status) values ('"
					+ Id
					+ "', 0, '"
					+ Id
					+ "', CURRENT_TIMESTAMP, 'Auto','Auto', CURRENT_TIMESTAMP, '01')";
			sqlList.add(strInsertController);
			sqlList.add(strInsertSubcontroller);
		}

		try {
			System.out.println(sqlList.get(0));
			DB.executeBatch(sqlList);
			return true;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean addLearnedIRCode(Device device, LearnIRCode learnIRCode) {
		String sql = "insert into mdlearnedircode (controllerid, subcontrollerid, deviceid, keyindex, codetype, ircodedata, createdt, createby, lasteditdt, lasteditby) values(?, ?, ?, ?, ?, ?, ?, 'server', ?, 'server')";
		ArrayList<Object> params = new ArrayList<>();
		params.add(device.getControllerId());
		params.add(device.getSubcontrollerId());
		params.add(device.getDeviceId());
		params.add(learnIRCode.getKeyIndex());
		params.add(learnIRCode.getCodeType());
		params.add(learnIRCode.getIRCodeData());

		Date lastEditDT = new Date();
		params.add(lastEditDT);
		params.add(lastEditDT);

		try {
			boolean result = DB.executeSQL(sql, params);
			if (result) {
				learnIRCode.setLastEditDT(lastEditDT.getTime());
				controllerDao.codeUpdateDT(device.getControllerId(), lastEditDT);
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateLearnedIRCode(Device device, LearnIRCode learnIRCode) {
		String sql = "update mdlearnedircode set codetype=?, ircodedata = ?, lasteditdt= ? where controllerid = ? and subcontrollerid = ? and DeviceId = ? and keyindex = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(learnIRCode.getCodeType());
		params.add(learnIRCode.getIRCodeData());

		Date lastEditDT = new Date();
		params.add(lastEditDT);

		params.add(device.getControllerId());
		params.add(device.getSubcontrollerId());
		params.add(device.getDeviceId());
		params.add(learnIRCode.getKeyIndex());

		try {
			boolean result = DB.executeSQL(sql, params);
			if (result) {
				learnIRCode.setLastEditDT(lastEditDT.getTime());
				controllerDao.codeUpdateDT(device.getControllerId(), lastEditDT);
			}
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}

	/*@Override
	public boolean logTimerHistory(ControllerTimer timer, TimerStep step,   ActionResult result) {
	
		String sql = "insert into tdtimerhistory (timerid, stepseqno, resultcode, logmessage, remark)values(?,?,?,?,?)";
		ArrayList<Object> params = new ArrayList<>();
		params.add(timer.getTimerId());
		params.add(step.getSeqNo());
		params.add(result.getResultCode());
		params.add(result.getMessage());
		params.add(""); //remark
		try {			
			return DB.executeSQL(sql, params);
		} catch (Exception e) {		
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}*/

	@Override
	public boolean logTimerHistory(ControllerTimer timer, TimerStep step,   ActionResult result) {
	
		String sql = "insert into tdtimerhistory (timerid, stepseqno, resultcode, logmessage, remark)values(?,?,?,?,?)";
		ArrayList<Object> params = new ArrayList<>();
		params.add(timer.getTimerId());
		params.add(step.getSeqNo());
		params.add(result.getResultCode());
		params.add(result.getMessage());
		params.add(""); //remark
		TimerDao dao=new TimerDao();
		try {			
			return dao.logTimerHistory(params);
			//return DB.executeSQL(sql, params);
		} catch (Exception e) {		
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
	@Override
	public boolean updateTimer(ControllerTimer timer) {
		String Id = timer.getTimerId();
		
		String sql = "update adtimer set timername = ?, weekdays=?,triggertime=?, lasteditdt = CURRENT_TIMESTAMP where timerid = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(timer.getName());
		params.add(timer.getWeekDays());
		params.add(timer.getTime());
		params.add(Id);
		
		try {
			DB.beginTran();
			boolean result = DB.executeSQL(sql, params);

			// update timer;
			if (!result) {
				DB.rollback();
				return false;
			}
			
			sql = "delete from adtimersteps where timerid = ?";
			params = new ArrayList<>();
			params.add(Id);
			// delete steps of the timer;
			result = DB.executeSQL(sql, params);
			if (!result) {
				DB.rollback();
				return false;
			}
			
			sql = "insert into adtimersteps (timerid, seqno, stepname, controllerid, subcontrollerid, deviceid, keyindex, delay, value) values(?,?,?,?,?,?,?,?,?)";
			for (int i = 0; i < timer.getSteps().size(); i++) {
				TimerStep step = timer.getSteps().get(i);
				params = new ArrayList<>();
				params.add(Id);
				params.add(step.getSeqNo());
				params.add(step.getName());
				params.add(step.getControllerId());
				params.add(step.getSubcontrollerId());
				params.add(step.getDeviceId());
				params.add(step.getKeyIndex());
				params.add(step.getDelay());
				params.add(step.getValue());
				
				result = DB.executeSQL(sql, params);

				// Add timer steps;
				if (!result) {
					DB.rollback();
					return false;
				}
			}
			DB.commit();			
			return result;
		} catch (Exception e) {
			DB.rollback();
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
	@Override
	public boolean disableTimer(ControllerTimer timer, boolean enabled) {
		String sql = "update adtimer set isenabled = ?, lasteditdt = CURRENT_TIMESTAMP where timerid = ?";
		ArrayList<Object> params = new ArrayList<>();
		params.add(enabled);
		params.add(timer.getTimerId());

		try {
			boolean result = DB.executeSQL(sql, params);		
			return result;
		} catch (SQLException e) {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		}
	}
	
//	@Override
//	public List<String> getTimerId(List<String> controllerId) {
//		// TODO Auto-generated method stub
//		String sql="select TimerId from adtimersteps where  ControllerId in ? GROUP BY TimerId;";
//		StringBuffer paramsBuff=new StringBuffer();
//		List<String> timerId=new ArrayList<>();	
//		if(controllerId.size()<=0){
//			return timerId;
//		}else{
//			paramsBuff.append("(");
//			for(String id : controllerId){
//				paramsBuff.append("'"+id+"',");
//			}
//			paramsBuff.replace( paramsBuff.length()-1, paramsBuff.length(), "");
//			paramsBuff.append(")");
//			try {
//				sql=sql.replace("?", paramsBuff);
//				ResultSet result=DB.executeQuery(sql);
//				while(result.next()){
//					timerId.add(result.getString("TimerId"));
//				}
//				return timerId;
//			} catch (SQLException e) {
//				// TODO: handle exception
//				logger.error(e.getLocalizedMessage(), e);
//				return timerId;
//			}
//		}
//		
//	}
	
	

}
