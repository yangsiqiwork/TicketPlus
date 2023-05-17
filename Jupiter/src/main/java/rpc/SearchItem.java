package rpc;

import java.io.IOException;
//import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
//import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
//import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//response.setContentType("application/json");  	 	
        //PrintWriter out = response.getWriter(); 
		
		//拼接JSONArray 用于返回
		JSONArray array = new JSONArray();
        try {
        	//后面五行放到try catch里，防止没有加参数，浏览器直接挂掉
        	//先从url获取数据get parameter lat and lon
        	String userid = request.getParameter("user_id");//之前只有lat和lon，现在加上user_id
    		double lat = Double.parseDouble(request.getParameter("lat"));//传过来的都是String类型，要转换成为search所需要的double
    		double lon = Double.parseDouble(request.getParameter("lon"));
    		String keyword = request.getParameter("term");       
//            String username = "";
//            if (request.getParameter("username") != null) {
//            	username = request.getParameter( "username");
//            }
            
    		//获取结果
//    		TicketMasterAPI tmAPI = new TicketMasterAPI();
//    		//如果想变成static method， 还需要把访问到的field都变成static的，不方便
//    		List<Item> items = tmAPI.search(lat, lon, keyword);
    		//更新成：把MySQLConnction实现，放在DBConnection的interface下面
    		//比之前多做的是，saveItem把找到的数据存到数据库里
    		DBConnection connection = DBConnectionFactory.getConnection();
    		List<Item> items = connection.searchItems(lat,lon, keyword);
    		connection.close();
    		
    		
//        	array.put(new JSONObject().put("username", username));
//        	array.put(new JSONObject().put("username", "Charls"));
        	
    		Set<String> favorite = connection.getFavoriteItemIds(userid);
    		
    		for (Item item : items) {
    			// Add a thin version of item object
				JSONObject obj = item.toJSONObject();
 				// Check if this is a favorite one.
				// This field is required by frontend to correctly display favorite items.
				//判断获得的id是不是在favotite set里。返回true or false
				obj.put("favorite", favorite.contains(item.getItemId()));
				array.put(obj);

        	}
//        } catch (JSONException e) {
        	//没有new JSONObject()，就不会throw JSONExcption了，改为标准的Exception
        	//IOException, however, is a checked exception - only method calls which are declared to throw it can do so. 
        	//或者直接删掉try catch。
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
//        out.print(array);
//		out.close();
        RpcHelper.writeJsonArray(response , array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
