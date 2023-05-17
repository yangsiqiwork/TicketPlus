package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;//每次Factory创建的MySQL的时候，conn就 包含在里面
	
	public MySQLConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(MySQLDBUtil.URL);//用创建出的connection，对数据库进行操作
			//作为资源管理，理论上一般会在constructor里判断一下是否常见成功，不成功报错
			//但是作为API要保证长时间运行下，connection一直是好的，所以在每个方法调用开始都先判断connection
			System.out.println("connected to database success!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {//自动连接，手动关闭
		if (conn != null) {
			try {
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			//两个constrain， 两个参数都找到了，才删除。
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			return new HashSet<>();//conn没有成功，也不能让serveice崩溃
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);//给一个userId，找到对应itemIds，遍历items信息，存储
		
		//通过itemIds获得具体的数据，需要操作数据库，有异常处理
		try {
			//创建sql语句
				String sql = "SELECT * FROM items WHERE item_id = ?";//所有内容yong *,?是PreparedStatement，防止sql injection
				//= "SELECT item_id rating FROM items WHERE item_id = ? AND rating > 4.0";
				PreparedStatement stmt = conn.prepareStatement(sql);//prepare sql语句
				for (String itemId : itemIds) {
					stmt.setString(1, itemId);//把每次的语句设置成
					
					ResultSet rs = stmt.executeQuery();//关注按返回结果，获得返回类型ResultSet，想象成可以用iterator访问的数据结构
					//从数据库里取出的值直接存入item数据结构里
					//先创建ItemBuilder
					ItemBuilder builder = new ItemBuilder(); 
					//Item.ItemBuilder builder = new Item.ItemBuilder();
					while (rs.next()){//用iterator检查resultset是否有值在里面，每次指向一个record。最开始next指向-1
						builder.setItemId(rs.getString("item_Id"));//"item_Id"是String
						builder.setName(rs.getString("name"));
						builder.setAddress(rs.getString("address"));
						builder.setImageUrl(rs.getString("image_url"));
						builder.setUrl(rs.getString("url"));
						builder.setCategories(getCategories(itemId));//不在同一个表里，单独写方法
						builder.setDistance(rs.getDouble("distance"));
						builder.setRating(rs.getDouble("rating"));
						
						//数据取完，放到favoriteItems里面
						favoriteItems.add(builder.build());
					}
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> categories = new HashSet<>();
		
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			return;//silent error。在工程上应该返回给前端提示。
		}
		
		try {//数据库操作会抛异常
			//使用PreparedStatement，
			//1.防止SQL injection！！！
			//2.如果要插入多次，只需要PreparedStatement一次，创出一个template模板，要快一些。
				String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";//类似先把参数设好，在调用
					//ignore：如果插入重复的值，就不执行了，如果需要覆盖，就用UPDATE语句。不加关键字直接插入，如果有重复，会显示失败。
					PreparedStatement stmt = conn.prepareStatement(sql);
					stmt.setString(1, item.getItemId());//要符合类型的要求，避免语句的发生
					stmt.setString(2, item.getName());
					stmt.setDouble(3, item.getRating());
					stmt.setString(4, item.getAddress());
					stmt.setString(5, item.getImageUrl());
					stmt.setString(6, item.getUrl());
					stmt.setDouble(7, item.getDistance());
					stmt.execute();//语句执行，只插入一条，返回boolean就行了
					
					sql = "INSERT IGNORE INTO categories VALUES(?,?)";
					stmt = conn.prepareStatement(sql);
					for (String category : item.getCategories()) {
						stmt.setString(1, item.getItemId());
						stmt.setString(2, category);
						stmt.execute();
					}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//user_id是unique key，逻辑上只可能有一个返回结果，所以用if (rs.next())
	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			//根据user——Id获得全名
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) { //如果还有值，就把名和姓连起来
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;

	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
		 	statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) { //用id和password查询，判断是否有返回结果，有，表明至少能匹配一个人
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

}
