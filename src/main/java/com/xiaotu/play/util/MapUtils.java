package com.xiaotu.play.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 处理Map的工具类
 * @author xuchangjian 2017-6-15下午4:09:34
 */
public class MapUtils {

	/**
	 * 对Map进行排序
	 * @param map	待排序的Map
	 * @param sortModel	排序规则：
	 * 			valueDesc--按照value值降序排列
	 * 			keyAsc--按照key值升序排列
	 * 			valueAscKeyAsc--先按照value值升序再按照key值升序排列
	 * @return
	 */
	public static List<Map.Entry<String,Integer>> sortMap(Map<String, Integer> map,String sortModel){
		if(map.isEmpty()){
			return null;
		}
		
		List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
				
		if("valueDesc".equals(sortModel)){//按照value值降序排列
			Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
			    public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
			        return -o1.getValue().compareTo(o2.getValue());
			    }		    
			});
		}else if("keyAsc".equals(sortModel)){//按照key值升序排列
			Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
			    public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
			    	return o1.getKey().compareTo(o2.getKey());
			    }		    
			});
		}else if("valueAscKeyAsc".equals(sortModel)){//先按照value值升序再按照key值升序排列
			Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
				public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
	            	if(o1.getValue()!=o2.getValue()){
	            		return o1.getValue().compareTo(o2.getValue());
	            	}else{
	            		return o1.getKey().compareTo(o2.getKey());
	            	}
	            }           		    
			});
		}
		return list;
	}
}
