package db.mysql;

import java.util.Map;

//
public class MySQLDBUtil {//两个public值，其他的不重要
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "8889"; // change it to your mysql port number
	public static final String DB_NAME = "jupiter";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
	// 注册MySQL所需要的信息（url）：jdbc:mysql://localhost:8889/laiproject?user=root&password=root&&autoReconnect=true&serverTimezone=UTC
	
	//static initialization
	//用于比较麻烦的初始化变量
	//还可以执行任意语句，在load class的时候，就会自动执行
//	private static final Map<String, String> map;
//	
//	static {
//		map = new HashMap<>();
//		map.put("a", "a");
//	}

}
