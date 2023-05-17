package db;

import db.mysql.MySQLConnection;

//如何创建DBConnection
public class DBConnectionFactory {//简版工厂模式，正常还会有暂时没有出现的类型
	private static final String DEFAUT_DB = "mysql";

	//获得具体用哪个数据库来实现接口
	//有大量的地方需要new connection，如果改数据库不用一一修改。改一下调用参数就行了
	public static DBConnection getConnection(String db) {
		switch(db) {
		case "mysql":
			return new MySQLConnection();
			//return null;//实现以后开启上面一行具体实现。
		case"mongodb":
			//return new MongoDBConnection();
			return null;
		default:
			throw new IllegalArgumentException("Invalid db:" + db);
		}
	}
	//没有参数的默认情况
	public static DBConnection getConnection() {
		return getConnection(DEFAUT_DB);
	}
}
