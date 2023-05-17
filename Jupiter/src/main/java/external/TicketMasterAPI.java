package external;//external package:从外部Ticketmaster提取数据的部分

import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.net.HttpURLConnection; 
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder; 


public class TicketMasterAPI {
	
	
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";  	
	private static final String DEFAULT_KEYWORD = ""; // no restriction  	
    private static final String API_KEY = "AGgWnaB69j6AEATQw5PREjbOeWZ7i2HW";//Ticketmaster使用情况统计用
    

//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [//array
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
    
    //三个helper function，提取比较深的数据
    //最后需要一个整体的string，拼一起，用StringBuilder
    //可能有很多地址，可能多地同时举行。
    //获得一个就可以了，如果需要多个，可以改成return List，build的String都放到arrayList里
    
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				
				//成功获取一个以后就return了, 为空，就继续遍历，直到找到。
				for (int i = 0; i < venues.length(); i++ ) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						
						//加回车可能接受处理会有问题
						if (!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							sb.append(" ");
							sb.append(city.getString("name"));
						}
					}
					if (!sb.toString().equals("")) {
						return sb.toString();
					}
				}
			}
		}
		return "";
	}		
		
		
		
//		if (!event.isNull("_embedded")) {
//			JSONObject embedded = event.getJSONObject("_embedded");
//			
//			if (!embedded.isNull("venues")) {
//				JSONArray venues = embedded.getJSONArray("venues");
//				
//				for (int i = 0; i < venues.length(); ++i) {
//					JSONObject venue = venues.getJSONObject(i);
//					
//					StringBuilder sb = new StringBuilder();
//					
//					if (!venue.isNull("address")) {
//						JSONObject address = venue.getJSONObject("address");
//						
//						if (!address.isNull("line1")) {
//							sb.append(address.getString("line1"));
//						}
//						if (!address.isNull("line2")) {
//							sb.append(" ");
//							sb.append(address.getString("line2"));
//						}
//						if (!address.isNull("line3")) {
//							sb.append(" ");
//							sb.append(address.getString("line3"));
//						}
//					}
//					
//					if (!venue.isNull("city")) {
//	 	 	 	 	 	 	 	sb.append(address.getString("line3")); 
//	 	 	 	 	 	 	} 
//	 	 	 	 	 	} 
// 	 	 	 	 	 
//	 	 	 	 	 	if (!venue.isNull("city")) { 
//
//
//
//                                                                                                JSONObject city = venue.getJSONObject("city");
//						
//						if (!city.isNull("name")) {
//							sb.append(" ");
//							sb.append(city.getString("name"));
//						}
//					}
//					
//					if (!sb.toString().equals("")) {
//						return sb.toString();
//					}
//				}
//			}
//		}
//
//		return "";



	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
//			if (images.length() > 0) {
//				JSONObject image = images.getJSONObject(0);
//				
//				if (!image.isNull("url")) {
//					return image.getString("url");
//				}
//			}
			
			//只关心第一个url
			for (int i = 0; i < images.length(); ++i) {
				JSONObject image = images.getJSONObject(i);//先取出image
				
				
				if (!image.isNull("url")) {
					return image.getString("url");//再取出url
				}
			}
		}

		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	//把所有categories都取出来，都塞到hashSetlimian 
	//支持 多个keyword，搜索： tagging system
	
	//写完要回顾！！！！！！！
	private Set<String> getCategories(JSONObject event) throws JSONException {
		//exception specification JSONException extends  Exception
		//itemList 接throw过来的Exception
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						String name = segment.getString("name");
						categories.add(name);
						//categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}

	// 第一步：Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();

		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();//同时引用了import entity.Item;和import entity.Item.ItemBuilder; 
			
			//前五项可以直接获得，因为在event里的第一层
			if (!event.isNull("name")) {//判断是否为空，negate！
				builder.setName(event.getString("name"));//-->events.getJSONObject(i).getString("name")
				
			}
			
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			//目前网站里没有rating了
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			//利用helper function
			builder.setCategories(getCategories(event));//classification-segment-name
			builder.setAddress(getAddress(event));//embedded-venues-adress-...
			builder.setImageUrl(getImageUrl(event));//images-url

			//创建一个itemList放到返回的结果里。
			itemList.add(builder.build());
		}

		return itemList;
	}
    
	//用于debug，显示在console里面，检测search获取数据对不对
    private void queryAPI(double lat, double lon) { 
    	
    	List<Item> events = search(lat, lon, null); 
 	 	try { 
// 	 	    for (int i = 0; i < events.size(); i++) { 
// 	 	        Item event = events.get(i); 
// 	 	        System.out.println(event); 
 	 		//光获得event打印object的信息不是非常有用。用Item类里的toJSONObject变成
// 	 	    } 
 	 	  for (Item event : events) {
				System.out.println(event.toJSONObject());
			}

 	 	} catch (Exception e) { 
 	 	 	e.printStackTrace(); 
 	 	} 
 	} 
    
    
    public List<Item> search(double lat, double lon, String keyword) { 
    	//返回值由JSONArray改成List<Item>用java自己的数据结构保存它
  
    	//方案2：一开始增加返回值。JSONArray ret = new JSONArray();后面只return ret，否则抛异常。
    	
    	//step1:保证参数都可用，keyword可以省略
    	if (keyword == null){
    		keyword = DEFAULT_KEYWORD;
    	}
    	//step2:对keyword encode一下,转化成可以传输的格式
    	//,非英文会超过1个字符大小,可能和用来控制（比如&）的字节冲突
    	try {
    		keyword = java.net.URLEncoder.encode(keyword, "UTF-8");//每个字节8个bit或更少更长？，出现频率越高，字节越小。byte String
    		//编码方式：如果有特殊字符，会有问题。转化成url里支持的格式。因为要用http向Ticketmaster request.
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	//step3:用Geohash拿到geohash
    	String geoHash = Geohash.encodeGeohash(lat, lon, 8);
    	
    	//step3:根据例子的格式拼接 query String（endpoint）
    	//https://app.ticketmaster.com/discovery/v2/events.json
    	//        ?apikey=12345&geoPoint=abcd&keyword=music&radius=50
    	String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s",
    			API_KEY, geoHash, keyword, 50);
    	
    	//step4:创建整个String: new URL常量（最上面定义了）+"?" + query, 
    	//并获得connection, 不同的类型生成不同的URLConnection（有可能是文件什么的），用来连接Ticketmaster和我们程序
    	try {
    		HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
    		//将URLConnection转成http的URLConnection,根据类型自动识别创建new什么类型的URL（句柄handle）
    		//能不能直接创建，就看有没有public的constructor。URL定义在java.net.URL里。
    		
    		
    		connection.setRequestMethod("GET");
    		int responseCode = connection.getResponseCode();//返回值200 OK or
    		System.out.println("\nSending 'Get' request to URL:" + URL + "?" + query);//debug用
    		System.out.println("Response Code:" + responseCode);//debug用
    		if (responseCode != 200){
    			//place holder
    		}//需要处理不OK的情况，这里省略
    		
    		//step5:发送请求，读Ticketmaster response提供的stream。
    		//获取的是handle in（相当于指针，指向bufferedReader）需要用readLine函数读出东西来。
        	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); 
        	//InputStreamReader相当于一个设备（类似System.in），开了闸。一行一行读
        	//为了获得一个绑定到控制台的字符流，你可以把 connection.getInputStream() 包装在一个 BufferedReader 对象中来创建一个字符流。
        	
        	//转成string，因为string是java中传输数据最通用的格式
        	String inputLine;//每次要读的一行
        	StringBuilder response = new StringBuilder();//只需要分配一次内存，可以高效拼接。java 的String是immutable
        	while((inputLine = in.readLine()) != null) {//最后一行返回空
        		response.append(inputLine);
        	}
        	in.close();//也可以用try{}包起来？？？try resource
        	//connection.disconnect();可以加
        	
        	JSONObject obj = new JSONObject(response.toString());//StringBuider转String 
        	if(obj.isNull("_embedded")) {//判断embedded值是不是存在
        		//return new JSONArray();//返回空
        		//return new JSONArray();修改为：
        		return new ArrayList<>();
        		
        		//方案2：throw new Exception(); 代替return new JSONArray();
        	}
        	
        	//拆解获得的JSONObject
        	JSONObject embedded = obj.getJSONObject("_embedded");//new JSONObject  hold embedded结果
        	JSONArray events = embedded.getJSONArray("events");
        	
        	//return events;//不管try里发生什么，java都会执行return
        	//return events;修改为：
        	return getItemList(events); //因为有helper function getItemList，就可以转化格式
        	//getItemList的Exception会被所在的这个try catch抓到，这里会处理所有的Exception
        	
        	//方案2：ret = events; 代替 return events;
    	} catch (Exception e) {
    		e.printStackTrace();//对debug有好处
    		//不robust，如果一部分数据出了问题，要保存这部分数据，然后返回
    	}
    	//return new JSONArray(); //如果查找请求失败，会抛异常，就没有events，没有return events，才会走到这一行
    	//return new JSONArray();修改为：
    	return new ArrayList<Item>();
    	
    	//方案2：return ret; 代替 return new JSONArray();
    	
    } 

    /** 
 	 * Main entry for sample TicketMaster API requests. 
 	 */ 
 	public static void main(String[] args) { 
 	 	TicketMasterAPI tmApi = new TicketMasterAPI(); 
 	 	// Mountain View, CA 
 	 	// tmApi.queryAPI(37.38, -122.08); 
 	 	// London, UK 
 	 	// tmApi.queryAPI(51.503364, -0.12); 
 	 	// Houston, TX 
 	 	tmApi.queryAPI(29.682684, -95.295410); 
 	 	//
 	} 

}
