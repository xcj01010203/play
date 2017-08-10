package com.xiaotu.play.constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.xiaotu.play.dto.ScripteleDto;
import com.xiaotu.play.dto.SeparatorDto;

/**
 * 常量类
 * @author xuchangjian 2017-6-15上午11:52:52
 */
/**
 * 常量类
 * @author xuchangjian 2017年8月2日下午4:47:11
 */
public class PlayAnalysisConstants {

	/**
	 * 剧本格式列表（排序决定了遍历的先后）
	 */
	public static final List<String> SCRIPT_RULE_LIST = new LinkedList<String>();

	/**
	 * 剧本标题中的元素
	 */
	public static final List<ScripteleDto> SCRIPTELE_LIST = new ArrayList<ScripteleDto>();

	/**
	 * 剧本标题中元素之间分隔符信息
	 */
	public static final List<SeparatorDto> SEPARATOR_LIST = new ArrayList<SeparatorDto>();

	/**
	 * 包含特殊拍摄手法的正则表达式
	 */
	public static final String REGEX_SPECIAL = "闪回|回忆|梦境|空镜";

	/**
	 * 包含内外景关键字的正则表达式
	 */
	public static final String REGEX_SITE = "内|外";

	/**
	 * 包含气氛关键字的正则表达式
	 */
	public static final String REGEX_ATMOSPHERE = "⽇|日|白天|夜|晚|晨|午|夕|昏|晓|明|阴|雨|雪|风|月|同上";

	/**
	 * 包含跟人物相关的关键字的正则表达式
	 */
	public static final String REGEX_FIGURE = "人：|人物：|角色：|主要人物：|主要角色：|出场：|人 |人物 |出场 |角色 ";

	/**
	 * 包含需要进一步检查是否代表场景的关键字的正则表达式
	 */
	public static final String NEED_CHECK_REGEX_SPACE = "S|场 |时 |景 |地 |地点|场景 ";

	/**
	 * 包含不需要进一步检查就可以判定为其后的内容代表场景的关键字的正则表达式
	 */
	public static final String NOT_NEED_CHECK_REGEX_SPACE = "S：|场：|时：|景：|地：|地点：|\\|场景：";
	
	/**
	 * 包含需要进一步检查是否代表气氛的关键字的正则表达式
	 */
	public static final String NEED_CHECK_REGEX_ATMOSPHERE = "时间";
	
	/**
	 * 包含不需要进一步检查是否代表气氛的关键字的正则表达式
	 */
	public static final String NOT_NEED_CHECK_REGEX_ATMOSPHERE = "时间：";
	
	/**
	 * 通配符
	 */
	public static final String COMMON_SEPARATOR="(，|、|-|—|/|；|：| |\\.|\t| |．|。|－)+";
	
	/**
	 * 包含量词关键字的正则表达式
	 */
	public static final String REGEX_MEASUREWORD = "号|层|栋|分|集|场|回|万|岁";
	
	/**
	 * 包含季节关键字的正则表达式
	 */
	public static final String REGEX_SEASON = "春|夏|秋|冬";
	
	/**
	 * 数字开头中间带空格的日期正则表达式
	 * 示例：2016  年 4月 5日、 5  月19  日、2016  -  3  -4  
	 */
	public static final String REGEX_DATE ="^\\d{1,4}\\s+[年|月|-|—|.|/|∕]{1}\\s+";
	
	/**
	 * 集次的正则表达式
	 */
	public static final String REGEX_SERIES = ".*第([一|二|三|四|五|六|七|八|九|十|百|0-9| | ]+)(集|回).*";
	
	/**
	 *剧本中元素ID
	 */
	public static final String SCRIPTELE_SERIESNO = "e1";		//集
	public static final String SCRIPTELE_VIEWNO = "e2";			//场
	public static final String SCRIPTELE_VIEWLOCATION = "e3";	//场景
	public static final String SCRIPTELE_ATMOSPHERE = "e4";		//气氛
	public static final String SCRIPTELE_SITE = "e5";			//内外景
	public static final String SCRIPTELE_SEASON = "e6";			//季节
	public static final String SCRIPTELE_FIGURE = "e7";			//人物
	
	/**
	 * 剧本中符号ID
	 */
	public static final String SEPARATOR_LINE = "s1";	//换行符
	
	public static final String lineSeprator = "\r\n";
	
	static {
		//装载预定义的剧本格式
		/*
		 * 1-5a 上海马路上 日/外
		 * 角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e4s0e5s1s2e7");
		
		/*
		 * 1-5a 上海马路上 外/日
		 * 角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e5s0e4s1s2e7");
		
		/*
		 * 1-5a 上海马路上 日/外 
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e4s0e5s1e7");
		
		/*
		 * 1-5a 上海马路上 外/日 
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e5s0e4s1e7");
		
		/*
		 * 1-5a 上海马路上 日/外   角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e4s0e5s2e7");
		
		/*
		 * 1-5a 上海马路上 外/日   角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e5s0e4s2e7");
		
		/*
		 * 1-5a 日/外
		 * 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e4s0e5s1e3s1e7");
		
		/*
		 * 1-5a 日
		 * 上海马路上 外
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e4s1e3s0e5s1e7");
		
		/*
		 * 1-5a 日 上海马路上 
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e4s0e3s1e7");
		
		/*
		 * 1-5a 上海马路上 日/外
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e4s0e5");
		
		/*
		 * 1-5a 上海马路上 外/日
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e5s0e4");
		
		/*
		 * 1-5a 外 上海马路上 日
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e5s0e3s0e4");
		
		/*
		 * 1-5a
		 * 日/外
		 * 上海马路上
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s1e4s0e5s1e3");
		
		/*
		 * 5a 上海马路上 日/外
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e4s0e5s1e7");
		
		/*
		 * 5a 上海马路上 外/日
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e5s0e4s1e7");
		
		/*
		 * 5a 日/外 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e4s0e5s0e3s1e7");
		
		/*
		 * 5a 外/日 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e5s0e4s0e3s1e7");
		
		/*
		 * 5a 日/外 上海马路上 角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e2s0e4s0e5s0e3s2e7");
		
		/*
		 * 5a 外/日 上海马路上 角色：徐桂林、马赫、美国兵甲
		 */
//		SCRIPT_RULE_LIST.add("e2s0e5s0e4s0e3s2e7");
		
		/*
		 * 5a 上海马路上 外
		 * 日
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e5s1e4s1e7");
		
		/*
		 * 5a 外
		 * 上海马路上
		 * 日
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e5s1e3s1e4s1e7");
		
		/*
		 * 5a
		 * 上海马路上 日/外
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s1e3s0e4s0e5s1e7");
		
		/*
		 * 5a
		 * 日
		 * 外
		 * 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s1e4s1e5s1e3s1e7");
		
		/*
		 * 5a 
		 * 上海马路上 日/外
		 */
		SCRIPT_RULE_LIST.add("e2s0s1e3s0e4s0e5");
		
		/*
		 * 1-5a 上海马路上 日
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e3s0e4");
		
		/*
		 * 1 5a 日 上海马路上
		 */
		SCRIPT_RULE_LIST.add("e1s0e2s0e4s0e3");
		
		/*
		 * 5a 上海马路上 日/外
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e4s0e5");
		
		/*
		 * 5a 日 上海马路上 外
		 */
		SCRIPT_RULE_LIST.add("e2s0e4s0e3s0e5");
		
		/*
		 * 5a 日 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e4s0e3s1e7");
		
		/*
		 * 5a 日/外 上海马路上
		 */
		SCRIPT_RULE_LIST.add("e2s0e4s0e5s0e3");
		
		/*
		 * 5a 日
		 * 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s0e4s1e3s1e7");
		
		/*
		 * 5a 外 上海马路上 日
		 */
		SCRIPT_RULE_LIST.add("e2s0e5s0e3s0e4");
		
		/*
		 * 5a
		 * 上海马路上
		 * 日
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s1e3s1e4s1e7");
		
		/*
		 * 5a
		 * 日
		 * 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s1e4s1e3s1e7");
		
		/*
		 * 5a 上海马路上 日
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e4");
		
		/*
		 * 5a 上海马路上 外
		 */
		SCRIPT_RULE_LIST.add("e2s0e3s0e5");
		
		/*
		 * 5a
		 * 上海马路上
		 * 徐桂林、马赫、美国兵甲
		 */
		SCRIPT_RULE_LIST.add("e2s1e3s1e7");
		
		/*
		 * 5a 上海马路上
		 */
		SCRIPT_RULE_LIST.add("e2s0e3");
		
		
		//剧本元素信息
		ScripteleDto e1Scriptele = new ScripteleDto();
		e1Scriptele.setId("e1");
		e1Scriptele.setName("集");
		e1Scriptele.setExample("1");
		e1Scriptele.setRegex("\\d+");
		
		ScripteleDto e2Scriptele = new ScripteleDto();
		e2Scriptele.setId("e2");
		e2Scriptele.setName("场");
		e2Scriptele.setExample("5a");
		e2Scriptele.setRegex("[A-Za-z]{0,1}\\d+[A-Za-z]{0,1}");
		
		ScripteleDto e3Scriptele = new ScripteleDto();
		e3Scriptele.setId("e3");
		e3Scriptele.setName("场景");
		e3Scriptele.setExample("上海马路上");
		e3Scriptele.setRegex(".*");
		
		ScripteleDto e4Scriptele = new ScripteleDto();
		e4Scriptele.setId("e4");
		e4Scriptele.setName("气氛");
		e4Scriptele.setExample("日");
		e4Scriptele.setRegex(".*");
		
		ScripteleDto e5Scriptele = new ScripteleDto();
		e5Scriptele.setId("e5");
		e5Scriptele.setName("内外景");
		e5Scriptele.setExample("外");
		e5Scriptele.setRegex(".*");
		
		ScripteleDto e6Scriptele = new ScripteleDto();
		e6Scriptele.setId("e6");
		e6Scriptele.setName("季节");
		e6Scriptele.setExample("冬");
		e6Scriptele.setRegex(".*");
		
		ScripteleDto e7Scriptele = new ScripteleDto();
		e7Scriptele.setId("e7");
		e7Scriptele.setName("人物");
		e7Scriptele.setExample("徐桂林、马赫、美国兵甲");
		e7Scriptele.setRegex(".*");
		
		SCRIPTELE_LIST.add(e1Scriptele);
		SCRIPTELE_LIST.add(e2Scriptele);
		SCRIPTELE_LIST.add(e3Scriptele);
		SCRIPTELE_LIST.add(e4Scriptele);
		SCRIPTELE_LIST.add(e5Scriptele);
		SCRIPTELE_LIST.add(e6Scriptele);
		SCRIPTELE_LIST.add(e7Scriptele);
		
		
		//分隔符信息
		SeparatorDto separator1 = new SeparatorDto();
		separator1.setId("s0");
		separator1.setName(" ");
		separator1.setDescription("通配（逗号，顿号，中划线，右斜杠，分号，空格，冒号，点）");
		separator1.setRegex("(，|、|-|—|/|；|：| |\\.|	| |．|。|－)+");
		
		SeparatorDto separator2 = new SeparatorDto();
		separator2.setId("s1");
		separator2.setName("\r\n");
		separator2.setDescription("回车换行");
		separator2.setRegex("(\r\n)");
		
		SeparatorDto separator3 = new SeparatorDto();
		separator3.setId("s2");
		separator3.setName("角色：");
		separator3.setDescription("角色");
		separator3.setRegex("(角色：)");
		
		SEPARATOR_LIST.add(separator1);
		SEPARATOR_LIST.add(separator2);
		SEPARATOR_LIST.add(separator3);
	}
}
