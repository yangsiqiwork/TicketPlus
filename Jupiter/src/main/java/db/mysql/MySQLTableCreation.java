package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//独立的内函数，有main函数。在实际使用过程中，如果测试成功了，数据可以正确添加，这个class不会执行。只是创建和测试的时候才会用。
//方便把数据库恢复到初始状态，用来reset数据库结构，方便添加新数据，用于debug,测试用的方法。先清理再添加！
public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			// This is java.sql.Connection. Not com.mysql.jdbc.Connection.
			Connection conn = null;//java.sql.Connection是JDBC的interface

			// Step 1 Connect to MySQL.
			try {
				System.out.println("Connecting to " + MySQLDBUtil.URL);
//				reflecion:用运行期间出现的值，来创建一些class
//				在运行期给一个driver，程序里可能没有，给了很灵活的使用driver的办法。
//				flexibility：甚至能支持程序开发者都不支持的数据库。只要提供.jar(driver),运行时load进来，就可以用这个类跑
//				jar现在放在程序里面，但也可以运行期调用.jar里面的包
				
				
				//如何获得connection，怎么把driver注册进来的？用forName
				//forName，string作为参数，让java找叫这个名字的在jar里的class。通过这个load class
				//相当于调了一个com.mysql.jdbc.Driver。
				//Step1：注册好driver
//				Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
				Class.forName("com.mysql.cj.jdbc.Driver");
				
				//用注册好的driver获得和数据库的连接。
				//然后通过DriverManager来获得connection。提供的url就是注册MySQL所需要的信息
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			
			if (conn == null) {
				return;
			}
			
			//Step2 Drop tables in case they exist. drop掉已有的数据库（输入一些SQL的语句）
			Statement stmt = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";//if exists语句，部分数据库支持
			stmt.executeUpdate(sql);//返回int有多少被更新了，有条件的更新里面的数据

			sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);
			
			//Step3: Create new tables
			sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL," //类型VARCHAR:变长字符串，不大于255
					+ "name VARCHAR(255),"
					+ "rating FLOAT,"
					+ "address VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"
					//+ "category VARCHAR(255)
					//如果category定义在这个表里 string里需要标记多个，可以倾倒出一个json，或者逗号隔开，取出以后还要自己分析有多少个
					+ "PRIMARY KEY (item_id))";//定义一个
			stmt.executeUpdate(sql);
			
			//item_id和category是多对多的关系，用多对多的方式形成一个表，方便取数
			sql = "CREATE TABLE categories (" 
 	 	 	 	 	+ "item_id VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "category VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "PRIMARY KEY (item_id, category)," //合在一起组成了primary key
 	 	 	 	 	+ "FOREIGN KEY (item_id) REFERENCES items(item_id))"; //引用的是是items里的id	 	 	                 
			stmt.executeUpdate(sql); 
 
 	 	 	sql = "CREATE TABLE users (" 
 	 	 	 	 	+ "user_id VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "password VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "first_name VARCHAR(255)," 
 	 	 	 	 	+ "last_name VARCHAR(255),"  	 	 	 	 	                                                               
 	 	 	 	 	+ "PRIMARY KEY (user_id))"; 
 	 	 	stmt.executeUpdate(sql); 
 	 	 	
 	 	 	//关系表，PRIMARY KEY多个值，要有FOREIGN KEY
 	 	 	sql = "CREATE TABLE history (" 
 	 	 	 	 	+ "user_id VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "item_id VARCHAR(255) NOT NULL," 
 	 	 	 	 	+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," //要求不能是null，所以默认插入当前值
 	 	 	 	 	+ "PRIMARY KEY (user_id, item_id)," 
 	 	 	 	 	+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"  	 	 	 	 	                 
 	 	 	 	 	+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";  	 	 	                  
 	 	 	stmt.executeUpdate(sql); 
			
 	 	 	// Step 4: insert data
            //Create a fake user
			sql = "INSERT INTO users VALUES ("
					+ "'1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
			System.out.println("Executing query: " + sql);
			stmt.executeUpdate(sql);

 	 	 	
			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
