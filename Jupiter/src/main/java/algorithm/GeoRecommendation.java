package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;


//Recommendation based on geo distance and similar categories.
public class GeoRecommendation {
	  public List<Item> recommendItems(String userId, double lat, double lon) {//通过为止推荐信息
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getConnection();
		
		// Step 1 Get all favorite items
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);

		// Step 2 Get all categories of favorite items, sort by count（看出最喜欢的额category）
		Map<String, Integer> allCategories = new HashMap<>();//用java数据结构处理更简单
		for (String itemId : favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);//用favoriteItemIds取all categories 
			for (String category : categories) {//存categories
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
//				if (allCategories.containsKey(category)) {
//					allCategories.put(category, allCategories.get(category) + 1);//更新
//				} else {
//					allCategories.put( category, 1);
//				}
			}
			
		}
		
		List<Entry<String, Integer>> categoryList =
				new ArrayList<Entry<String, Integer>>(allCategories.entrySet());
				//Entry<String, Integer>是每个map里的key value pair，用一个List来存，方便排序
				//entrySet返回一个set，放到ArrayList里。
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			@Override//匿名class Comparator
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
				//会越界 o2.getValue() - o1.getvalue();
			}
		});

		// Step 3, do search based on category, filter out favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();
		//这次搜索添加过的值，在添加item的过程中，不同的category也可能添加相同的event
		
		for (Entry<String, Integer> category : categoryList) {//遍历Entry
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<>();//存没有被favorite过，也没被添加过的item
			for (Item item : items) {
				//如果favoriteItemIds不包含之前查找的项，并且访问过的项里也没有这个item
				if (!favoriteItemIds.contains(item.getItemId())
						&& !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			
			//sort by distance
			Collections.sort(filteredItems, new Comparator<Item>() {//比较Item
				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}
		
		return recommendedItems;
	  }

}
  