package entity;//entity package:存数据的类

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Item {//Item helper：用于保存获得的数据data store，获得的过程不在这里
	//清理数据 clean，方便servlet读取

	//不是所有的值都有，有的可能是null。java不知道默认值，就有很多组合，用constructor很麻烦。
	//compute and generate fields 
	private String itemId;  	
	private String name;  	
    private double rating;  	
    private String address;  	
    private Set<String> categories;  	
    private String imageUrl;  	
    private String url; 
    private double distance; 
    
    /** 
	 	 * This is a builder pattern in Java.  
	 	 */ 
	 	//Item constructor
    	//是private，不用innerClass，没法访问item constructor
    private Item(ItemBuilder builder) {  	 	
	 		this.itemId = builder.itemId;  	//this指Item instance
	 		this.name = builder.name;  	 	
	 		this.rating = builder.rating;  	 	
	 		this.address = builder.address;  	 	
	 		this.categories = builder.categories;  	 	
	 		this.imageUrl = builder.imageUrl;  	 	
	 		this.url = builder.url; 
	 	 	this.distance = builder.distance; 
	 	} 
   //在java里，如果是public constructor加多个参数，需要把多个参数都写进去。因为不支持default argument。
    
 //只加getters
    public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}

	//要把普通java的field转化成json object，因为最后要作为server的serblet传给前端程序	
	public JSONObject toJSONObject() { 
 	 	JSONObject obj = new JSONObject(); 
 	 	try { 
 	 	 	obj.put("item_id", itemId);  	 	  	  	 	 	
 	 	 	obj.put("name", name);  	 	 	
	 	 	obj.put("rating", rating);  	 	 	
	 	 	obj.put("address", address);  	 	 	
	 	 	obj.put("categories", new JSONArray(categories)); 
 	 	 	obj.put("image_url", imageUrl);  	 	 	              
 	 	 	obj.put("url", url);  	 	  	 	 	
 	 	 	obj.put("distance", distance); 
 	 	} catch (JSONException e) { 
 	 	 	e.printStackTrace(); 
 	 	} 
 	 	return obj; 
	}
 
//Add static class ItemBuilder in Item class. 
//需要ItemBuilder来帮助设置默认值，需要修改的时候加入值。
 	public static class ItemBuilder {  
 		//static是必须的，需要能直接访问Itembuilder，否则需要建一个item类，再new innerClass。而Item需要ItemBuilder，会死循环。
 	//因为是static，Itembuilder是跟着Item class走的内部类，我觉得像个constructor工具
 		private String itemId;  	 	
 		private String name;  	 	
 		private double rating;  	 	
 		private String address;  	 	
 		private Set<String> categories;  	 	
 		private String imageUrl; 
 		private String url;
 		private double distance;
 		
 		//需要set哪个就set哪个，不用都赋。
 		//Generate Setters for all data fields in ItemBuilder
 		public ItemBuilder setItemId(String itemId) {
			this.itemId = itemId;
			return this;
		}
		public ItemBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
 		
		//create a ItemBuilder object from Item object.
 		public Item build() {
 			return new Item(this);//this指ItemBuilder instance
 		}

 	}

	public static void main() {
		Item item = new Item.ItemBuilder().setItemId("123").setName("abc").build();
		//要现有一个Itembuilder的object，才能set			
		//ItemBuilder()是static的所以直接用类Item就能直接调用
	}
	
}
