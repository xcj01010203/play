package com.xiaotu.play;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaotu.play.constants.PlayAnalysisConstants;
import com.xiaotu.play.util.MapUtils;
import com.xiaotu.play.util.RegexUtils;

/**
 * 标题合理性校验工具 
 * 主要校验标题中的每个元素是否符合系统设定的合理性规则
 * @author xuchangjian 2017-6-15下午3:53:58
 */
public class TitleReasonableCheckUtils {
	
	private static Logger logger = LoggerFactory.getLogger(TitleReasonableCheckUtils.class);
	
	//场景关键字
	private static String viewLocationWords = "";

	/**
	 * 长度合理性过滤
	 * 用于过滤掉含有不符合系统长度规则的元素的标题
	 * 
	 * 元素长度规则：
	 * 1、所有场景字数加起来不超过30
	 * 2、气氛的字数不超过10
	 * 3、内外景的字数不超过5
	 * 4、季节的字数不超过5
	 * 5、所有任务字数加起来不超过50
	 * 6、标题中总字数不超过100
	 * @param elementList	标题列表（含有标题中的元素信息）
	 * @return	符合规则的标题列表（含有标题中的元素信息）
	 * @throws ParseException
	 */
	public static List<Map<String, Object>> lengthFilter(List<Map<String, Object>> elementList) throws ParseException {
		List<Map<String, Object>> filterElementList = new ArrayList<Map<String, Object>>();
		String viewLocation = "";
		String atmosphere = "";
		String site = "";
		String season = "";
		String figure = "";
		String title = "";

		for (Map<String, Object> elementMap : elementList) {
			boolean flag = true;
			if (elementMap.get("viewLocation") != null) {
				viewLocation = (String) elementMap.get("viewLocation");
			}
			if (elementMap.get("atmosphere") != null) {
				atmosphere = (String) elementMap.get("atmosphere");
			}
			if (elementMap.get("site") != null) {
				site = (String) elementMap.get("site");
			}
			if (elementMap.get("season") != null) {
				season = (String) elementMap.get("season");
			}
			if (elementMap.get("figure") != null) {
				figure = (String) elementMap.get("figure");
			}
			if (elementMap.get("title") != null) {
				title = (String) elementMap.get("title");
			}

			if (viewLocation.length() > 30) {
				flag = false;
				continue;
			}

			if (atmosphere.length() > 10) {
				flag = false;
				continue;
			}

			if (site.length() > 5) {
				flag = false;
				continue;
			}

			if (season.length() > 5) {
				flag = false;
				continue;
			}

			if (figure.length() > 50) {
				flag = false;
				continue;
			}

			if (title.length() > 100) {
				flag = false;
				continue;
			}

			if (flag) {
				filterElementList.add(elementMap);
			}
		}

		return filterElementList;
	}

	/**
	 * 集次合理性过滤 
	 * 过滤掉含有不符合规则的集次的标题
	 * 
	 * 过滤逻辑： 
	 * 如果先将集次列表按照extralSeriesNo（从文件名或者剧本内容中“第xxx集”获取的集次）分解成子列表，
	 * 在子列表中获取集次重复次数最大的集次maxRepeatSeriesNo，
	 * 如果maxRepeatSeriesNo在子列表中占有的场次比例超过80%，则在子列表中剔除集次不等于maxRepeatSeriesNo的场次
	 * 否则，则在子列表中剔除集次不等于extralSeriesNo的场次
	 * 
	 * TODO：
	 * 由于extralSeriesNo获取逻辑，会出现如下bug
	 * bug条件：如果一个文件中有1-1, 1-2, ..., 2-1, 2-2, ..., 3-1, 3-2场次的剧本内容，但是每次集次变化时没有“第xxx集”标识
	 * 则通过该逻辑过滤后，只会留存第一集的场次标题信息
	 * 
	 * @param elementList 标题列表（含有标题中的元素信息）
	 * @return 符合规则的标题列表（含有标题中的元素信息）
	 * @throws ParseException
	 */
	public static List<Map<String, Object>> seriesViewFilter(List<Map<String, Object>> elementList) throws ParseException {

		List<Map<String, Object>> filterElementList = new ArrayList<Map<String, Object>>();

		List<String> seriesNoList = new ArrayList<String>();
		List<String> extralSeriesNoList = new ArrayList<String>();
		for (Map<String, Object> elementMap : elementList) {
			seriesNoList.add((String) elementMap.get("seriesNo"));
			extralSeriesNoList.add((String) elementMap.get("extralSeriesNo"));
		}

		//以extralSeriesNo作为key分解子列表
		Map<String, List<String>> splitLists = getSplitList(extralSeriesNoList, extralSeriesNoList);

		int index = 0;
		Set<String> extralSeriesNoSet = splitLists.keySet();
		for (String extralSeriesNo : extralSeriesNoSet) {
			List<String> splitSeriesNoList = splitLists.get(extralSeriesNo);

			// 确定子列表重复次数最大的集次
			Map<String, Integer> tempMap = new HashMap<String, Integer>();
			for (String seriesNo : splitSeriesNoList) {
				if (tempMap.containsKey(seriesNo)) {
					tempMap.put(seriesNo, tempMap.get(seriesNo) + 1);
				} else {
					tempMap.put(seriesNo, 1);
				}
			}
			List<Map.Entry<String, Integer>> tempList = MapUtils.sortMap(tempMap, "valueDesc");
			String maxRepeatSeriesNo = tempList.get(0).getKey();
			int repeatNum = tempList.get(0).getValue();
			
			// 剔除不合理的集次
			String keySeriesNo = extralSeriesNo;
			if (100 * repeatNum / splitSeriesNoList.size() >= 80) {
				keySeriesNo = maxRepeatSeriesNo;
			}

			for (int i = 0; i < splitSeriesNoList.size(); i++) {
				if (keySeriesNo.equals(splitSeriesNoList.get(i))) {
					filterElementList.add(elementList.get(index));
				}
				index++;
			}
		}

		return filterElementList;
	}
	
	/**
	 * 把所有场次信息按照extralSeriesNo分组
	 * 
	 * @param extralSeriesNoList	所有场次中的extralSeriesNo列表（有重复）
	 * @param seriesNoList	所有场次中的集次列表（有重复）
	 * @return key为extralSeriesNo, value为extralSeriesNo对应的实际标题中的列表
	 */
	private static Map<String, List<String>> getSplitList(List<String> extralSeriesNoList, List<String> seriesNoList) {
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		
		for (int i = 0; i < extralSeriesNoList.size(); i++) {
			String extralSeriesNo = extralSeriesNoList.get(i);
			String seriesNo = seriesNoList.get(i);
			
			if (!map.containsKey(extralSeriesNo)) {
				List<String> mySeriesNoList = new ArrayList<String>();
				mySeriesNoList.add(seriesNo);
				map.put(extralSeriesNo, mySeriesNoList);
			} else {
				map.get(extralSeriesNo).add(seriesNo);
			}
		}

		return map;
	}
	
	/**
	 * 检查标题元素数量的匹配率，
	 * 只有当标题中解析的元素个数与剧本格式中配置的元素个数相同时，才判定该标题和格式匹配
	 * 标题的匹配率根据tryNum变化
	 * 当tryNum == 1 或 tryNum == 3，匹配率为80%
	 * 当tryNum == 2 或 tryNum == 4，匹配率为50%
	 * 
	 * @param elementList 标题列表（含有标题中的元素信息）
	 * @param tryNum	尝试的次数
	 * @param scenarioFormatList	剧本格式中单个元素列表
	 * @return	是否检查通过
	 * @throws ParseException
	 */
	public static boolean elementNumCheck(List<Map<String, Object>> elementList, int tryNum, List<String> scenarioFormatList) throws ParseException {
		boolean checkFlag = false;
		int matchNum = 0;
		float matchRage = 0.75f;// 匹配率阈值
		if (tryNum == 1 || tryNum == 3) {
			matchRage = 0.8f;
		} else if (tryNum == 2 || tryNum == 4) {
			matchRage = 0.5f;
		}
		for (Map<String, Object> elementContentMap : elementList) {
			boolean flag = true;
			if (scenarioFormatList.contains("e1")) {
				if (StringUtils.isBlank((String) elementContentMap.get("seriesNo"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e2")) {
				if (StringUtils.isBlank((String) elementContentMap.get("viewNo"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e3")) {
				if (StringUtils.isBlank((String) elementContentMap.get("viewLocation"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e4")) {
				if (StringUtils.isBlank((String) elementContentMap.get("atmosphere"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e5")) {
				if (StringUtils.isBlank((String) elementContentMap.get("site"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e6")) {
				if (StringUtils.isBlank((String) elementContentMap.get("season"))) {
					flag = false;
					continue;
				}
			}
			if (scenarioFormatList.contains("e7")) {
				if (StringUtils.isBlank((String) elementContentMap.get("figure"))) {
					flag = false;
					continue;
				}
			}

			if (flag) {
				matchNum++;
			}
		}

		logger.info("标题元素数量整体匹配率" + 100 * matchNum / elementList.size() + "%,匹配率阈值为" + 100 * matchRage + "%,");
		if (100 * matchNum / elementList.size() >= 100 * matchRage) {
			checkFlag = true;
		}

		return checkFlag;
	}
	
	/**
	 * 场次检查，比较场次重复率，重复率越高，通过几率越小（比较的是“集-场”的重复率）
	 * 不重复率根据tryNum变化
	 * 当tryNum == 1 或 tryNum == 3，匹配率为80%
	 * 当tryNum == 2 或 tryNum == 4，匹配率为50%
	 * 
	 * seriesNoList和viewNoList的长度都为解析出的场次的数量
	 * @param seriesNoList 所有场的集次信息列表（有重复）
	 * @param viewNoList 所有场的场次信息列表（有重复）
	 * @param tryNum	尝试次数
	 * @return	Map
	 * key为checkFlag时,value表示是否检查通过
	 * key为titleWarnMsgList时，value表示警告信息列表
	 * key为titleInfoMsgList时，value表示提示信息列表
	 * @throws ParseException
	 */
	public static Map<String, Object> seriesViewCheck(List<String> seriesNoList, List<String> viewNoList, int tryNum) throws ParseException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean checkFlag = false;
		List<String> titleWarnMsgList = new ArrayList<String>();
		List<String> titleInfoMsgList = new ArrayList<String>();
		
		
		float matchRage = 0.5f;// 重复率阈值
		if (tryNum == 1 || tryNum == 3) {
			matchRage = 0.8f;
		} else if (tryNum == 2 || tryNum == 4) {
			matchRage = 0.5f;
		}
		String titleInfoMsg = "";
		String titleWarnMsg = "";
		
		//所有的“集-场”列表
		List<String> seriesViewList = new ArrayList<String>();
		for (int i = 0; i < seriesNoList.size(); i++) {
			seriesViewList.add(seriesNoList.get(i) + "-" + viewNoList.get(i));
		}

		//不重复的“集-场”列表
		List<String> distinctSeriesViewList = new ArrayList<String>();
		for (String seriesView : seriesViewList) {
			if (!distinctSeriesViewList.contains(seriesView)) {
				distinctSeriesViewList.add(seriesView);
			}
		}

		if (100 * distinctSeriesViewList.size() / seriesViewList.size() >= 100 * matchRage) {
			checkFlag = true;

			// 组装入库情况提示信息
			Map<String, Integer> tempMap = new HashMap<String, Integer>();
			for (String seriesNo : seriesNoList) {
				if (tempMap.containsKey(seriesNo)) {
					tempMap.put(seriesNo, tempMap.get(seriesNo) + 1);
				} else {
					tempMap.put(seriesNo, 1);
				}
			}

			if (!tempMap.isEmpty()) {
				List<Map.Entry<String, Integer>> tempList = MapUtils.sortMap(tempMap, "keyAsc");
				if (tempList.size() > 1) {
					// 警告入库有多个集次
					for (Map.Entry<String, Integer> entry : tempList) {
						titleWarnMsg += "第" + entry.getKey() + "集共" + entry.getValue() + "场,";
					}
					titleWarnMsg = titleWarnMsg.substring(0, titleWarnMsg.length() - 1);
					
					titleWarnMsgList.add("入库多个集次，其中:" + titleWarnMsg);
				} else {
					// 提示入库集次和对应场数
					for (Map.Entry<String, Integer> entry : tempList) {
						titleInfoMsg += "第" + entry.getKey() + "集共" + entry.getValue() + "场";
					}
					
					titleInfoMsgList.add("入库情况:" + titleInfoMsg);
				}

				// 警告同一集中有重复场次
				titleWarnMsg = "";
				for (String seriesView : seriesViewList) {
					if (!distinctSeriesViewList.contains(seriesView)) {
						distinctSeriesViewList.add(seriesView);
					}
				}

				tempMap.clear();
				for (String seriesView : seriesViewList) {
					if (tempMap.containsKey(seriesView)) {
						tempMap.put(seriesView, tempMap.get(seriesView) + 1);
					} else {
						tempMap.put(seriesView, 1);
					}
				}

				for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
					String seriesView = entry.getKey();
					int repeatNum = entry.getValue();
					if (repeatNum > 1) {
						// 重复则组装警告信息
						String[] seriesViews = seriesView.split("-");
						titleWarnMsg += "第" + seriesViews[0] + "集第" + seriesViews[1] + "场出现" + repeatNum + "次,";
					}
				}
				if (!StringUtils.isBlank(titleWarnMsg)) {
					titleWarnMsg = titleWarnMsg.substring(0, titleWarnMsg.length() - 1);
					titleWarnMsgList.add("入库同一集中有重复场次:" + titleWarnMsg);
				}
			}

			// 警告同一集有不连续的场次
			titleWarnMsg = "";
			for (int i = 0; i < seriesNoList.size() - 1; i++) {
				if (seriesNoList.get(i).equals(seriesNoList.get(i + 1))) {
					int currentViewNo = Integer.parseInt(viewNoList.get(i).replaceAll("[a-zA-Z]", ""));
					int nextViewNo = Integer.parseInt(viewNoList.get(i + 1).replaceAll("[a-zA-Z]", ""));	//TODO 此处未考虑场次中带有汉字的情况
					if (nextViewNo < currentViewNo || nextViewNo > currentViewNo + 1) {
						titleWarnMsg += "第" + seriesNoList.get(i) + "集第[" + viewNoList.get(i) + "," + viewNoList.get(i + 1) + "]场,";
					}
				}
			}
			if (!StringUtils.isBlank(titleWarnMsg)) {
				titleWarnMsg = titleWarnMsg.substring(0, titleWarnMsg.length() - 1);
				titleWarnMsgList.add("入库同一集有不连续的场次:" + titleWarnMsg);
			}
		}
		logger.info("场次重复率检查，不重复率为" + 100 * distinctSeriesViewList.size() / seriesViewList.size() + "%,匹配率阈值为" + 100 * matchRage + "%,");
		
		resultMap.put("checkFlag", checkFlag);
		resultMap.put("titleInfoMsgList", titleInfoMsgList);
		resultMap.put("titleWarnMsgList", titleWarnMsgList);
		return resultMap;
	}
	
	/**
	 * 关键词匹配率检查
	 * 
	 * 匹配率根据tryNum变化
	 * 当tryNum == 1 或 tryNum == 3，匹配率为80%
	 * 当tryNum == 2 或 tryNum == 4，匹配率为50%
	 * @param elementList 标题列表（含有标题中的元素信息）
	 * @param elementType	元素对应的字段名
	 * @param tryNum	尝试的次数
	 * @param containsFlag	标识剧本格式中是否包含该字段
	 * @return
	 * @throws ParseException
	 */
	public static boolean keywordCheck(List<Map<String, Object>> elementList, String elementType, int tryNum, boolean containsFlag) throws ParseException {
		String regexSite = PlayAnalysisConstants.REGEX_SITE;
		String regexAtmosphere = PlayAnalysisConstants.REGEX_ATMOSPHERE;
		String regexSeason = PlayAnalysisConstants.REGEX_SEASON;
		
		boolean checkFlag = false;
		int matchNum = 0;
		int allNum = 0;
		float matchRage = 0.8f;// 匹配率阈值
		if (tryNum == 1 || tryNum == 3) {
			matchRage = 0.8f;
		} else if (tryNum == 2 || tryNum == 4) {
			matchRage = 0.5f;
		}
		String regex = "";
		String element = "";
		String elementTypeName = "";

		if ("site".equals(elementType)) {// 内外景
			elementTypeName = "内外景";
			regex = regexSite;
		} else if ("atmosphere".equals(elementType)) {// 氛围
			elementTypeName = "氛围";
			regex = regexAtmosphere;
		} else if ("season".equals(elementType)) {// 季节
			elementTypeName = "季节";
			regex = regexSeason;
		} else {
			throw new IllegalArgumentException("不支持的关键字校验");
		}

		for (Map<String, Object> elementMap : elementList) {
			element = (String) elementMap.get(elementType);
			if (!StringUtils.isBlank(element)) {
				allNum++;
				boolean flag = RegexUtils.regexFind(regex, element);
				if (flag) {
					matchNum++;
				}
			}
		}

		if (allNum == 0) {// 如果一个元素也没有，该项免检
			if (containsFlag) {// 匹配规则中包含该元素
				logger.info("关键词匹配率检查，关键字为："+ elementTypeName +"，匹配规则中包含元素,但是标题中该元素均不合法，该项检查不通过");
				checkFlag = false;
			} else {
				checkFlag = true;
			}
		} else {
			logger.info("关键词匹配率检查，关键字为："+ elementTypeName +"，元素整理匹配率" + 100 * matchNum / allNum + "%,匹配率阈值为" + 100 * matchRage + "%");
			if (100 * matchNum / allNum >= 100 * matchRage) {
				checkFlag = true;
			}
		}

		return checkFlag;
	}
	
	/**
	 * 重要场景检查
	 * 先计算每一个拍摄场景中的字和系统中收录的场景关键字匹配率，如果达到匹配率则判定该场景检验通过，目前匹配阈值为50%
	 * 再由此计算每一场中所有场景的通过率，如果达到匹配阈值，则判定该场有效，目前匹配阈值为50%
	 * 进而计算所有场景的通过率，如果通过率满足系统要求，才判定本次校验通过，目前匹配阈值根据tryNum动态变化
	 * 当tryNum == 1 或 tryNum == 3，匹配率为80%
	 * 当tryNum == 2 或 tryNum == 4，匹配率为50%
	 * 
	 * @param viewLocationGroupList	 所有场的拍摄场景信息列表（每个场中的拍摄场景多个用逗号隔开）
	 * @param tryNum	尝试的次数
	 * @param viewLocationKeywordFilePath 场景关键字库文件路径
	 * @return	检查是否通过
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean viewLocationCheck(List<String> viewLocationGroupList, int tryNum,  boolean containsFlag, String viewLocationKeywordFilePath) throws ParseException, FileNotFoundException, IOException {
		boolean checkFlag = false;
		int singleViewLocationPassNum = 0;
		int groupViewLocationPassNum = 0;
		float singleViewLocationPassRage = 0.5f;// 单个场景全部字匹配率阈值
		float groupViewLocationPassRage = 0.5f;// 一个标题中的场景组合通过率阈值
		float wholeViewLocationPassRage = 0.8f;// 全部场景组合整体匹配率阈值
		if (tryNum == 1 || tryNum == 3) {
			wholeViewLocationPassRage = 0.8f;
		} else if (tryNum == 2 || tryNum == 4) {
			wholeViewLocationPassRage = 0.5f;
		}

		if (viewLocationGroupList == null || viewLocationGroupList.size() == 0) {// 重要场景列表为空，该项免检
			if(containsFlag){
				logger.info("重要场景检查，匹配规则中包含场景元素，标题中场景元素均为空值，该项检查不通过"); 					
				return false;
			}else{
				logger.info("重要场景检查，匹配规则中不包含场景元素，该项免检"); 					
				return true;	
			}
		}

		// 查询出数据库中非本剧本的历史场景信息(空值已剔除,目前接近30W条记录)
		if (StringUtils.isBlank(viewLocationWords) && !StringUtils.isBlank(viewLocationKeywordFilePath)) {
			File file = new File(viewLocationKeywordFilePath);
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader reader = new BufferedReader(read);
			String text = "";
			while (reader.ready()) {
				text += reader.readLine() + "\r\n";
			}
			viewLocationWords = text.replaceAll(",|\r\n", "");

		}
		
		if (StringUtils.isBlank(viewLocationWords)) {
			return true;
		}

		// 遍历viewLocationList
		for (String viewLocationGroup : viewLocationGroupList) {
			if (viewLocationGroup == null) {
				continue;
			}
			String[] singleViewLocations = viewLocationGroup.split(",");
			for (String singleViewLocation : singleViewLocations) {
				if (singleViewLocation.length() <= 1) {// 场景元素只有1个字，直接判定为不通过
					continue;
				}
				// 拆分成一个个单字
				char[] words = singleViewLocation.toCharArray();
				// 循环和数据库中历史场景信息做比对
				int wordPassNum = 0;
				for (char word : words) {
					if (viewLocationWords.indexOf(word) > -1) {
						wordPassNum++;
					}

					// 单个场景拆分出来的全部字匹配通过的比例超过预设比例，则判定该场景匹配通过
					if (100 * wordPassNum / words.length >= 100 * singleViewLocationPassRage) {
						singleViewLocationPassNum++;
						break;
					}
				}

				if (100 * singleViewLocationPassNum / singleViewLocations.length >= 100 * groupViewLocationPassRage) {
					groupViewLocationPassNum++;
					break;
				}
			}
		}

		
		if (100 * groupViewLocationPassNum / viewLocationGroupList.size() >= 100 * wholeViewLocationPassRage) {
			checkFlag = true;
		}
		logger.info("组合场景元素整理匹配率" + 100 * groupViewLocationPassNum / viewLocationGroupList.size() + "%,匹配率阈值为" + 100 * wholeViewLocationPassRage + "%,");

		return checkFlag;
	}
	
	/**
	 * 主要角色检查
	 * @param figureGroupList	 所有场的主要角色信息列表（每个场中的拍摄场景多个用逗号隔开）
	 * @param tryNum	尝试的次数
	 * @param containsFlag	标识剧本格式中是否包含人物元素
	 * @param figureMinRepeatNum	人物最少重复次数
	 * @param keepFigurant	是否保留群众演员
	 * @param tempFigureList	临时人物列表（从剧本内容中解析出的人物）
	 * @return	校验是否通过
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean figureCheck(List<String> figureGroupList, int tryNum, boolean containsFlag, int figureMinRepeatNum, boolean keepFigurant, List<String> tempFigureList) throws ParseException, FileNotFoundException, IOException {
		boolean checkFlag = false;
		int singleFigurePassNum = 0;
		int groupFigurePassNum = 0;
		float groupFigurePassRage = 0.5f;// 一个标题中的人物组合通过率阈值
		float wholeFigurePassRage = 0.8f;// 全部人物组合整体匹配率阈值
		if (tryNum == 1 || tryNum == 3) {
			wholeFigurePassRage = 0.8f;
		} else if (tryNum == 2 || tryNum == 4) {
			wholeFigurePassRage = 0.5f;
		}

		if (tryNum == 3 || tryNum == 4 || !containsFlag) {
			logger.info("主要角色检查，第" + tryNum + "次检测人物元素免检");
			return true;
		}

		int allNum = 0;
		for (String figureGroup : figureGroupList) {
			if (figureGroup != null && figureGroup.replaceAll(",", "").length() > 0) {
				allNum++;
			}
		}
		if (allNum == 0) {
			logger.info("主要角色检查，匹配规则中包含人物元素，标题中人物元素全为空值，该项检查不通过");
			return false;
		}

		// 如果约定人物元素至少重复出现次数大于或等于1，则预先将重复出现次数满足要求的人物元素列表准备好
		List<String> repeatFigureList = new ArrayList<String>();
		if (figureMinRepeatNum >= 1) {
			repeatFigureList = getRepeatFigure(figureGroupList, figureMinRepeatNum);
		}

		// 常用姓氏
		String[] surnames = new String[] { "赵", "钱", "孙", "李", "周", "吴", "郑",
				"王", "冯", "陈", "楮", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤",
				"许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶",
				"姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘",
				"葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花",
				"方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐", "费", "廉", "岑",
				"薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安",
				"常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元",
				"卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹", "姚", "邵", "湛",
				"汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧", "计", "伏", "成",
				"戴", "谈", "宋", "茅", "庞", "熊", "纪", "舒", "屈", "项", "祝", "董",
				"梁", "杜", "阮", "蓝", "闽", "席", "季", "麻", "强", "贾", "路", "娄",
				"危", "江", "童", "颜", "郭", "梅", "盛", "林", "刁", "锺", "徐", "丘",
				"骆", "高", "夏", "蔡", "田", "樊", "胡", "凌", "霍", "虞", "万", "支",
				"柯", "昝", "管", "卢", "莫", "经", "房", "裘", "缪", "干", "解", "应",
				"宗", "丁", "宣", "贲", "邓", "郁", "单", "杭", "洪", "包", "诸", "左",
				"石", "崔", "吉", "钮", "龚", "程", "嵇", "邢", "滑", "裴", "陆", "荣",
				"翁", "荀", "羊", "於", "惠", "甄", "麹", "家", "封", "芮", "羿", "储",
				"靳", "汲", "邴", "糜", "松", "井", "段", "富", "巫", "乌", "焦", "巴",
				"弓", "牧", "隗", "山", "谷", "车", "侯", "宓", "蓬", "全", "郗", "班",
				"仰", "秋", "仲", "伊", "宫", "宁", "仇", "栾", "暴", "甘", "斜", "厉",
				"戎", "祖", "武", "符", "刘", "景", "詹", "束", "龙", "叶", "幸", "司",
				"韶", "郜", "黎", "蓟", "薄", "印", "宿", "白", "怀", "蒲", "邰", "从",
				"鄂", "索", "咸", "籍", "赖", "卓", "蔺", "屠", "蒙", "池", "乔", "阴",
				"郁", "胥", "能", "苍", "双", "闻", "莘", "党", "翟", "谭", "贡", "劳",
				"逄", "姬", "申", "扶", "堵", "冉", "宰", "郦", "雍", "郤", "璩", "桑",
				"桂", "濮", "牛", "寿", "通", "边", "扈", "燕", "冀", "郏", "浦", "尚",
				"农", "温", "别", "庄", "晏", "柴", "瞿", "阎", "充", "慕", "连", "茹",
				"习", "宦", "艾", "鱼", "容", "向", "古", "易", "慎", "戈", "廖", "庾",
				"终", "暨", "居", "衡", "步", "都", "耿", "满", "弘", "匡", "国", "文",
				"寇", "广", "禄", "阙", "东", "欧", "殳", "沃", "利", "蔚", "越", "夔",
				"隆", "师", "巩", "厍", "聂", "晁", "勾", "敖", "融", "冷", "訾", "辛",
				"阚", "那", "简", "饶", "空", "曾", "毋", "沙", "乜", "养", "鞠", "须",
				"丰", "巢", "关", "蒯", "相", "查", "后", "荆", "红", "游", "竺", "权",
				"逑", "盖", "益", "桓", "公", "仉", "督", "晋", "楚", "阎", "法", "汝",
				"鄢", "涂", "钦", "岳", "帅", "缑", "亢", "况", "后", "有", "琴", "归",
				"海", "墨", "哈", "谯", "笪", "年", "爱", "阳", "佟", "商", "牟", "佘",
				"佴", "伯", "赏",
				"朴",
				"邱",
				"来",
				"肖",
				"洛",
				// 常用绰号首字（例如：小飞侠，大壮，肥龙，瘦猴,阿龙，老王,众人）
				"小", "大", "肥", "瘦", "阿", "老", "飞", "恶", "众", "酒", "万俟", "司马",
				"上官", "欧阳", "夏侯", "诸葛", "闻人", "东方", "赫连", "皇甫", "尉迟", "公羊",
				"澹台", "公冶", "宗政", "濮阳", "淳于", "单于", "太叔", "申屠", "公孙", "仲孙",
				"轩辕", "令狐", "锺离", "宇文", "长孙", "慕容", "鲜于", "闾丘", "司徒", "司空",
				"丌官", "司寇", "子车", "微生", "颛孙", "端木", "巫马", "公西", "漆雕", "乐正",
				"壤驷", "公良", "拓拔", "夹谷", "宰父", "谷梁", "段干", "百里", "东郭", "南门",
				"呼延", "羊舌", "梁丘", "左丘", "东门", "西门", "南宫" };

		// 以如下关键字结尾即通过
		String regex_endWords = "(哥|姐|爸|父|父亲|妈|母|母亲|爷|叔|婶|妻|儿|子|女|教官|老师|警察|保安|城管|路人|客户|经理)+$";
		// 含有如下关键字即通过
		String regex_containWords = "众|个|名|位|若干";

		String regex_outWords = "“|”|：|。|\\.|!";

		List<String> checkedFigureGroupList = new ArrayList<String>();
		String checkedFigureGroup = "";
		for (String figureGroup : figureGroupList) {
			checkedFigureGroup = "";
			singleFigurePassNum = 0;
			String[] singleFigures = figureGroup.split(",");
			for (String singleFigure : singleFigures) {
				// 直否规则1：单个人物元素重复出现次数不达标
				if (figureMinRepeatNum >= 1) {
					if (!repeatFigureList.contains(singleFigure)) {
						continue;
					}
				}

				// 直通规则1：在临时人物列表中
				if (tempFigureList.contains(singleFigure)) {
					checkedFigureGroup += singleFigure + ",";
					singleFigurePassNum++;
					continue;
				}
				// 直通规则2：全部由字母组成
				if (RegexUtils.regexFind("^[a-zA-Z]+$", singleFigure)) {
					checkedFigureGroup += singleFigure + ",";
					singleFigurePassNum++;
					continue;
				}

				// 直通规则3：包含·或*字符
				if (RegexUtils.regexFind("[·|\\*]+", singleFigure)) {
					checkedFigureGroup += singleFigure + ",";
					singleFigurePassNum++;
					continue;
				}

				// 直通规则4：以指定关键字结尾
				if (RegexUtils.regexFind(regex_endWords, singleFigure)) {
					checkedFigureGroup += singleFigure + ",";
					singleFigurePassNum++;
					continue;
				}

				// 直通规则5：包含群演关键字
				if (keepFigurant) {
					if (RegexUtils.regexFind(regex_containWords, singleFigure)) {
						checkedFigureGroup += singleFigure + ",";
						singleFigurePassNum++;
						continue;
					}
				}

				// 直否规则1：含有标点符号
				if (RegexUtils.regexFind(regex_outWords, singleFigure)) {
					continue;
				}

				// 直否规则2：6个字以上
				if (singleFigure.length() > 6) {// 6个字以上，直接不通过
					continue;
				}

				// 常规检测规则：姓氏检查
				for (String surname : surnames) {
					if (RegexUtils.regexFind("^" + surname, singleFigure)) {
						checkedFigureGroup += singleFigure + ",";
						singleFigurePassNum++;
						break;
					}
				}
			}

			if (100 * singleFigurePassNum / singleFigures.length >= 100 * groupFigurePassRage) {
				checkedFigureGroup = checkedFigureGroup.substring(0,
						checkedFigureGroup.length() - 1);
				checkedFigureGroupList.add(checkedFigureGroup);
				groupFigurePassNum++;
			} else {
				checkedFigureGroupList.add("");
			}
		}
		
		if (100 * groupFigurePassNum / figureGroupList.size() >= 100 * wholeFigurePassRage) {
			checkFlag = true;
		}
		logger.info("人物元素整体检测通过率" + 100 * groupFigurePassNum / figureGroupList.size() + "%,通过率阈值为" + 100 * wholeFigurePassRage + "%,");

		figureGroupList.clear();
		figureGroupList.addAll(checkedFigureGroupList);
		return checkFlag;
	}
	
	/**
	 * 计算符合重复次数条件的主要角色列表
	 * 如果约定人物元素至少重复出现次数大于或等于1，则预先将重复出现次数满足要求的人物元素列表准备好
	 * @param figureGroupList	所有场
	 * @param figureMinRepeatNum
	 * @return
	 */
	private static List<String> getRepeatFigure(List<String> figureGroupList,int figureMinRepeatNum){
		List<String> repeatFigureList = new ArrayList<String>();
		
		//构造元素值-重复次数的map
		Map<String,Integer> map = new  HashMap<String,Integer>();
		for(String figureGroup : figureGroupList){
			String[] singleFigures = figureGroup.split(",");
            for(String singleFigure : singleFigures){
            	if(map.containsKey(singleFigure)){					
            		map.put(singleFigure, map.get(singleFigure)+1);
				}else{
					map.put(singleFigure, 1);
				}
            }													
		}	
		
		//根据map中的value（重复次数）降序排列
		if(!map.isEmpty()){	        
			for(Map.Entry<String,Integer> entry : map.entrySet() ){   				 
				 if(entry.getValue()>figureMinRepeatNum){
					 repeatFigureList.add(entry.getKey());  
				 }		       
			}
		}
      
		return repeatFigureList;
	}
}
