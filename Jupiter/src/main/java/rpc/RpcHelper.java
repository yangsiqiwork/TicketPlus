package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj){
	 	try { 
 	 	 	response.setContentType("application/json"); 
 	 	 	response.addHeader("Access-Control-Allow-Origin", "*"); 
 	 	 	PrintWriter out = response.getWriter(); 
 	 	 	out.print(obj);  	 	 	
 	 	 	out.close(); 
 
	 	} catch (Exception e) { 
	 		e.printStackTrace(); 
	 	} 
	}
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) {
		try { 
 	 	 	response.setContentType("application/json"); 
 	 	 	response.addHeader("Access-Control-Allow-Origin", "*"); 
 	 	 	PrintWriter out = response.getWriter();  	 	 	
 	 	 	out.print(array); 
 	 	 	out.close(); 
 	 	} catch (Exception e) { 
 	 	 	e.printStackTrace(); 
 	 	} 
	}
	
	// Parses a JSONObject from http request.
	//request得到的是string，helper把字符串转换成了JSONObject，好进行操作
	public static JSONObject readJsonObject(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = request.getReader();//从request里得到reader
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());//只要符合JSON数据结构的定义，就会转成JSON
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;//如果失败，返回空指针。
	}	
}
