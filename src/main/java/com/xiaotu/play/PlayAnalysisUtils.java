package com.xiaotu.play;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaotu.play.constants.PlayAnalysisConstants;
import com.xiaotu.play.dto.PlayTitleMsgDto;
import com.xiaotu.play.dto.ScripteleDto;
import com.xiaotu.play.dto.SeparatorDto;
import com.xiaotu.play.dto.ViewInfoDto;
import com.xiaotu.play.util.MapUtils;
import com.xiaotu.play.util.MyStringUtils;
import com.xiaotu.play.util.OfficeUtils;
import com.xiaotu.play.util.PDFUtils;
import com.xiaotu.play.util.RegexUtils;
import com.xiaotu.play.util.ZipUtils;

/**
 * 剧本解析工具类
 * TODO 有待解决的问题
 * 1、最低匹配度的支持
 * 2、场次不支持“一二三四”大写的形式（电影剧本常有）
 * 3、场次不支持带汉字
 * 4、不支持一个文件中有多个集的数据
 * 5、
 * @author xuchangjian 2017-6-14下午5:01:49
 */
public class PlayAnalysisUtils {
	
	private static Logger logger = LoggerFactory.getLogger(PlayAnalysisUtils.class);

	/**
	 * 标题错误信息
	 */
	private static List<String> titleWarnMsg = new ArrayList<String>();
	private static List<String> titleInfoMsg = new ArrayList<String>();
	
	/**
	  *  临时角色列表（从剧本全文中获得）
	  */
	private static List<String>  tempFigureList = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		String fileName = "极速捕手 中文剧本20160815.docx";
		String filepath = "C:\\Users\\Administrator\\Desktop\\广告功能讲义.docx";
		String openInstallPath = "C:\\Program Files (x86)\\OpenOffice 4";
		String winrarInstallPath = "C:\\Program Files\\WinRAR/WinRAR.exe";
		String viewLocationKeywordFilePath = "";
		analysePlay(fileName, filepath, openInstallPath, winrarInstallPath, viewLocationKeywordFilePath);
		
		
//		String format = "e2s0e3s0e5s1e4s1e7";
//		List<String> formatList = genScenarioFormatList(format);
//		String str = "";
//		for (String f : formatList) {
//			for (SeparatorDto st : PlayAnalysisConstants.SEPARATOR_LIST) {
//				if (f.equals(st.getId())) {
//					str += st.getName();
//				}
//			}
//			for (ScripteleDto sp : PlayAnalysisConstants.SCRIPTELE_LIST) {
//				if (f.equals(sp.getId())) {
//					str += sp.getExample();
//				}
//			}
//		}
//		
//		System.out.println(str);
	}
	
	/**
	 * 解析剧本<br>
	 * 如果文件是压缩包，则会在文件所在目录新建一个“文件名_unzip”的文件夹，并把压缩包中文件解压到此处<br>
	 * 对于word文件，会在word文件所在目录下新建convert文件夹，存储由word转换的txt文件<br>
	 * @param fileName	文件名称
	 * @param filepath	剧本文件存储路径
	 * @param openOfficeInstallPath	openoffice安装地址
	 * @param winrarInstallPath	winrar安装地址
	 * @param figureMinRepeatNum	人物最小重复次数
	 * @param keepFigurant	是否保留群众演员
	 * @param viewLocationKeywordFilePath 存储拍摄场景关键字的文件路径
	 * @return 
	 * key为viewInfoList时，value表示场次列表<br>
	 * key为titleMsgList时，value表示标题信息<br>
	 * 具体返回结构见接口文档
	 * @throws Exception 
	 */
	public static Map<String, Object> analysePlay(String fileName, String filepath, String openOfficeInstallPath, 
			String winrarInstallPath, int figureMinRepeatNum, 
			boolean keepFigurant, String viewLocationKeywordFilePath) throws Exception {
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("文件名称不可为空");
		}
		if (StringUtils.isBlank(filepath)) {
			throw new IllegalArgumentException("文件存储路径不可为空");
		}
		if (StringUtils.isBlank(openOfficeInstallPath)) {
			throw new IllegalArgumentException("OpenOffice安装路径不可为空");
		}
		if (StringUtils.isBlank(winrarInstallPath)) {
			throw new IllegalArgumentException("winRar安装路径不可为空");
		}
		
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<String> filepathList = new ArrayList<String>();
		// 如果是压缩文档，则解压文件
		String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
		if (".zip".equals(suffix) || ".rar".equals(suffix)) {
			String unzipDir = filepath.substring(0, filepath.lastIndexOf(File.separator) + 1) + fileName.substring(0, fileName.lastIndexOf('.')) + "_unzip";
			filepathList = ZipUtils.unRarByCmd(new File(filepath), unzipDir, winrarInstallPath);
			if (filepathList.size() == 0) {
				throw new IllegalArgumentException("压缩包[" + fileName + "]不能正常解压缩,请尝试使用其他压缩工具进行压缩");
			}
			// 按照文件名排序
			filepathList = orderFileList(filepathList);
		} else {
			filepathList.add(filepath);
		}

		
		List<ViewInfoDto> viewInfoList = new ArrayList<ViewInfoDto>();	//最终的场景信息
		List<PlayTitleMsgDto> titleMsgList = new ArrayList<PlayTitleMsgDto>();
		for (String myFilepath : filepathList) {
			String fileNameWithSuffix = "";
			Map<String, Object> viewDataMap = new HashMap<String, Object>();
			try {
				fileNameWithSuffix = myFilepath.substring(myFilepath.lastIndexOf(File.separator) + 1);
				String myFileName = fileNameWithSuffix.substring(0, fileNameWithSuffix.lastIndexOf("."));
				
				String fileContent = "";

				String mySuffix = myFilepath.substring(myFilepath.lastIndexOf("."));
				if (".doc".equals(mySuffix) || ".docx".equals(mySuffix)) {
					fileContent = OfficeUtils.readWordFile(myFilepath, openOfficeInstallPath);
					if (StringUtils.isBlank(fileContent)) {
						throw new IllegalArgumentException("无法读取文件，请尝试用Microsoft office工具保存文档再重新上传");
					}
				} else if (".pdf".equals(mySuffix)) {
					fileContent = PDFUtils.readPDFFile(myFilepath);
				} else {
					throw new IllegalArgumentException("不支持的文件格式");
				}
				List<String> contentList = getSceContent(fileContent);
				if (contentList == null || contentList.size() == 0) {
					throw new IllegalArgumentException("内容为空，请检查之后重新上传");
				}
				
				viewDataMap = matchAndCheck(myFileName, contentList, viewLocationKeywordFilePath, figureMinRepeatNum, keepFigurant);
			} catch (IllegalArgumentException ie) {
				List<String> titleErrorMsgList = new ArrayList<String>();
				titleErrorMsgList.add(ie.getMessage());
				viewDataMap.put("titleErrorMsg", titleErrorMsgList);
				logger.error(ie.getMessage(), ie);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new IllegalArgumentException(e);
			}
			
			//合并解析出来的提示、警告、错误信息
			List<String> titleInfoMsgList = (List<String>) viewDataMap.get("titleInfoMsg");
			List<String> titleWarnMsgList = (List<String>) viewDataMap.get("titleWarnMsg");
			List<String> titleErrorMsgList = (List<String>) viewDataMap.get("titleErrorMsg");
			
			String titleInfoMsgs = "";
			if (titleInfoMsgList != null) {
				for (String titleInfoMsg : titleInfoMsgList) {
					titleInfoMsgs += titleInfoMsg + "\r\n";
				}
			}
			
			String titleWarnMsgs = "";
			if (titleWarnMsgList != null) {
				for (String titleWarnMsg : titleWarnMsgList) {
					titleWarnMsgs += titleWarnMsg + "\r\n";
				}
			}
			
			String titleErrorMsgs = "";
			if (titleErrorMsgList != null) {
				for (String titleErrorMsg : titleErrorMsgList) {
					titleErrorMsgs += titleErrorMsg + "\r\n";
				}
			}
			
			PlayTitleMsgDto playTitleMsgDto = new PlayTitleMsgDto();
			playTitleMsgDto.setPlayName(fileNameWithSuffix);
			playTitleMsgDto.setTitleInfoMsgs(titleInfoMsgs);
			playTitleMsgDto.setTitleWarnMsgs(titleWarnMsgs);
			playTitleMsgDto.setTitleErrorMsgs(titleErrorMsgs);
			playTitleMsgDto.setScriptRule((String)viewDataMap.get("scriptRule"));
			titleMsgList.add(playTitleMsgDto);
			
			//合并解析出来的场景信息
			List<ViewInfoDto> viewInfoDtoList = (List<ViewInfoDto>) viewDataMap.get("viewInfoDtoList");
			if (viewInfoDtoList != null) {
				for (ViewInfoDto playInfoDto : viewInfoDtoList) {
					if (!viewInfoList.contains(playInfoDto)) {
						viewInfoList.add(playInfoDto);
					}
				}
			}
		}
		
		/*
		 * 对playInfoList进行排序
		 * 规则：
		 * 先按照集次升序排列（1，2，3，4，5），然后按照场次升序排列（1, 1, 4a, 4b, 5, 5a, 5c, 7）
		 */
		Collections.sort(viewInfoList, new Comparator<ViewInfoDto>() {
			public int compare(ViewInfoDto o1, ViewInfoDto o2) {
				int o1SeriesNo = o1.getSeriesNo();
				int o2SeriesNo = o2.getSeriesNo();
				
				String o1ViewNo = o1.getViewNo();
				String o2ViewNo = o2.getViewNo();
				
				int result = 0;
				result = o1SeriesNo - o2SeriesNo;
				if (result == 0) {
					
					// 前面3个IF主要是判空的
	                if (o1ViewNo == o2ViewNo) {
	                	result = 0;
	                }
	                if (o1ViewNo == null) {
	                	result = 1;
	                }
	                if (o2ViewNo == null) {
	                	result = -1;
	                }
	                // 这里没有做太多的判断, index 代表第几个开始是数字
	                int index = 0;
	                for (;index <= o1ViewNo.length() -1 && 
	                		index >= 0 
	                		&& (o1ViewNo.charAt(index) >= '0' && o1ViewNo.charAt(index) <= '9'); index++) {
	                	
	                }
	                String str1 = o1ViewNo.substring(index);
	                int num1 = 0;
	                if (index != 0) {
	                	num1 = Integer.parseInt(o1ViewNo.substring(0, index));
	                }
	                
	                index = 0;
	                for (;index <= o2ViewNo.length() -1 && index >= 0 && (o2ViewNo.charAt(index) >= '0' && o2ViewNo.charAt(index) <= '9'); index++) {
	                	
	                }
	                String str2 = o2ViewNo.substring(index);
	                int num2 = 0;
	                if (index != 0) {
	                	num2 = Integer.parseInt(o2ViewNo.substring(0, index));
	                }
	                if (num1 == num2) {
	                	result = str1.compareTo(str2);
	                }
				}
				
        		return result;
			}
		});
		
		resultMap.put("viewInfoList", viewInfoList);
		resultMap.put("titleMsgList", titleMsgList);
		return resultMap;
	}

	/**
	 * 解析剧本<br>
	 * 默认人物最小重复次数为0，不保留群众演员<br>
	 * 如果文件是压缩包，则会在文件所在目录新建一个“文件名_unzip”的文件夹，并把压缩包中文件解压到此处<br>
	 * 对于word文件，会在word文件所在目录下新建convert文件夹，存储由word转换的txt文件
	 * @param fileName	文件名称
	 * @param filepath	剧本文件存储路径
	 * @param openOfficeInstallPath	openoffice安装地址
	 * @param winrarInstallPath	winrar安装地址
	 * @param viewLocationKeywordFilePath 存储场景关键字的文件所在目录
	 * @return
	 * key为viewInfoList时，value表示场次列表<br>
	 * key为titleMsgList时，value表示标题信息<br>
	 * 具体返回结构见接口文档
	 * @throws Exception 
	 */
	public static Map<String, Object> analysePlay(String fileName, String filepath, String openOfficeInstallPath, String winrarInstallPath, String viewLocationKeywordFilePath) throws Exception {
		return analysePlay(fileName, filepath, openOfficeInstallPath, winrarInstallPath, 0, false, viewLocationKeywordFilePath);
	}
	
	/**
	 * 把剧本内容格式化为List<String> 格式
	 * @param fileContent 剧本内容
	 * @throws IOException 
	 * @return
	 */
	private static List<String> getSceContent(String fileContent) throws IOException {
		//剧本文本读取到缓冲区 
		ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
		
		//缓冲区中的内容读取到集合list中
		List<String> list = new ArrayList<String>();
		String line = null;
		while((line = reader.readLine()) != null) {
			list.add(line);
		}
		return list;
	}
	
	/**
	 * 根据文件名中的集次把文件列表排序
	 * @param fileList
	 * @return
	 */
	public static List<String> orderFileList(List<String> fileList) {
		List<String> resultList = new ArrayList<String>();
		/*
		 * 只有一个文件，返回原列表 获取第*集和第*回字样，大写数字转小写，定为本文件集次
		 * 如果找不到第*集和第*回字样，获取文件名中小于2位的连续数字作为集次，过滤掉数字后接“季|年|月|日”字样
		 * 提取到集次的文件按照集次升序排列，未提取到集次的文件按照文件名升序排列
		 */
		int seriesNo = 0;
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		for (String file : fileList) {
			seriesNo = getSeriesNoFromStr(file);
			map.put(file, seriesNo);

		}

		List<Map.Entry<String, Integer>> list = MapUtils.sortMap(map, "valueAscKeyAsc");

		for (Map.Entry<String, Integer> entry : list) {
			resultList.add(entry.getKey());
		}
		return resultList;
	}

	/**
	 * 从字符串中获取集次信息
	 * @param str
	 * @return
	 */
	public static int getSeriesNoFromStr(String str) {
		int seriesNo = 0;
		
		/*
		 * 获取“第*集”和“第*回”字样，大写数字转小写，定为本文件集次
		 * 如果找不到“第*集”和“第*回”字样，过滤掉数字后接“季|年|月|日”字样，获取字符串中小于2位的连续数字作为集次
		 */

		Matcher m = Pattern.compile(PlayAnalysisConstants.REGEX_SERIES).matcher(str);
		if (m.find()) {
			String num = TitlePreDealUtils.genCNViewNo(m.group(1)).trim();
			if (!StringUtils.isBlank(num)) {
				seriesNo = Integer.parseInt(num);
			}
		} else {
			m = Pattern.compile("\\d+[季|年|月|日]+").matcher(str);
			if (m.find()) {
				str = str.replace(m.group(), "");
			}

			m = Pattern.compile("\\d+").matcher(str);
			if (m.find()) {
				if (Integer.parseInt(m.group()) < 100) {
					seriesNo = Integer.parseInt(m.group());
				}
			}
		}
		return seriesNo;
	}
	
	/**
	 * 遍历预存的规则去匹配剧本内容
	 * @param fileName	文件名信息
	 * @param contentList	剧本内容信息
	 * @param viewLocationKeywordFilePath	存储拍摄场景关键字的文件路径
	 * @param figureMinRepeatNum 人物最小重复次数
	 * @param keepFigurant	是否保留群众演员
	 * @return Map
	 * key为checkSceTitle时，value表示是否匹配到合适的格式
	 * key为playInfoDtoList时，value表示解析出的场景信息列表
	 * key为titleInfoMsg时，value表示标题提示信息
	 * key为titleWarnMsg时，value表示标题警告信息
	 * key为titleErrorMsg时，value表示标题错误消息
	 * @throws Exception
	 */
	public static Map<String, Object> matchAndCheck(String fileName, List<String> contentList,
			String viewLocationKeywordFilePath, int figureMinRepeatNum, boolean keepFigurant) throws Exception {
		Map<String, Object> viewDataMap = new HashMap<String, Object>();
		
		// 剧本符号信息
		Map<String, Object> titleMap = new HashMap<String, Object>();
		List<String> scenarioFormatList = null;
		Map<String, Object> formatInfo = null;

		boolean checkSceTitle = false;
		contentList = filterContent(contentList);

		// 经过四次循环检测，每次检测的标准逐次降低
		for (int tryNum = 1; tryNum <= 4; tryNum++) {
			if (checkSceTitle) {
				break;
			}
			logger.info("第" + tryNum + "次检测开始");
			int i = 0;
			for (String example : PlayAnalysisConstants.SCRIPT_RULE_LIST) {
				i++;
				scenarioFormatList = genScenarioFormatList(example);
				formatInfo = genFormatInfo( PlayAnalysisConstants.SCRIPTELE_LIST, PlayAnalysisConstants.SEPARATOR_LIST, scenarioFormatList);
				if (RegexUtils.regexFind(PlayAnalysisConstants.REGEX_SERIES, fileName)) {
					Pattern p = Pattern.compile(PlayAnalysisConstants.REGEX_SERIES);
					Matcher m = p.matcher(fileName);
					if (m.find()) {
						String num = TitlePreDealUtils.genCNViewNo(m.group(1)).trim();
						if (!StringUtils.isBlank(num)) {
							titleMap.put("extralSeriesNo", num);
							titleMap.put("extralSeriesNoFrom", "filename");
						}
					}
				} else if (RegexUtils.regexFind("[E|e|\u4e00-\u9fa5][0-9]+", fileName)) {
					Pattern p = Pattern.compile("[E|e|\u4e00-\u9fa5][0-9]+");
					Matcher m = p.matcher(fileName);
					if (m.find()) {
						String temp1String = m.group();
						p = Pattern.compile("[0-9]+");
						m = p.matcher(temp1String);
						if (m.find()) {
							int num = Integer.parseInt(m.group());
							if (num < 100) {
								titleMap.put("extralSeriesNo", num);
								titleMap.put("extralSeriesNoFrom", "filename");
							}
						}
					}
				}

				if (!"filename".equals(titleMap.get("extralSeriesNoFrom"))) {
					titleMap.put("extralSeriesNo", "1");
					titleMap.put("extralSeriesNoFrom", "default");
				}

				titleMap.put("scenarioFormatList", scenarioFormatList);
				titleMap.put("scenarioFormat", example);
				titleMap.put("formatInfo", formatInfo);
				titleMap.put("tryNum", tryNum);
				
				// 根据给定的剧本格式解析剧本
				viewDataMap = analysisScenario(contentList, titleMap, viewLocationKeywordFilePath, figureMinRepeatNum, keepFigurant);
				checkSceTitle = (Boolean) viewDataMap.get("checkSceTitle");
				if (checkSceTitle) {// 匹配出标题且整体检测通过
					logger.info("第" + tryNum + "次检测，第" + i + "条规则匹配成功，匹配的剧本格式为:" + example);
					break;
				} else {
					logger.info("第" + tryNum + "次检测，第" + i + "条规则匹配失败");
				}
			}
			logger.info("第" + tryNum + "次检测结束");
		}
		if (!checkSceTitle) {// 所有规则都不能同时通过匹配和检测
			List<String> titleErrorMsg = new ArrayList<String>();
			titleErrorMsg.add("所有规则都不能同时通过匹配和检测");
			viewDataMap.put("titleErrorMsg", titleErrorMsg);
		}

		return viewDataMap;
	}
	
	/**
	 * 简单处理一下剧本内容信息
	 * 顺便提取出临时人物列表
	 * @param contentList
	 * @return
	 */
	public static List<String> filterContent(List<String> contentList) {
		List<String> tempList = new ArrayList<String>();
		tempFigureList.clear();
		Map<String, Integer> map = new HashMap<String, Integer>();
		String figure = "";
		int repeatNum = 0;
		for (String myLineContent : contentList) {
			myLineContent = myLineContent.trim();

			// 跳过空行
			if (StringUtils.isBlank(myLineContent)) {
				continue;
			}

			if (!RegexUtils.regexFind("[\u4e00-\u9fa5_a-zA-Z0-9]+", myLineContent)) {
				continue;
			}

			// 跳过不含数字、字母和汉字的行
			Pattern p = Pattern.compile("[\u4e00-\u9fa5_a-zA-Z0-9].*");
			Matcher m = p.matcher(myLineContent);
			if (m.find()) {
				int index = myLineContent.indexOf(m.group());
				if (index > 0) {
					String before = myLineContent.substring(index - 1, index);
					if (RegexUtils.regexFind("\\[|【|\\(|（|《|<|\\{", before)) {// 保留有效的标点符号
						myLineContent = myLineContent.substring(index - 1);
					} else {
						myLineContent = myLineContent.substring(index);
					}
				}
			}
			
			//处理特殊字符
			myLineContent = dealSpecialChar(myLineContent);
			tempList.add(myLineContent);

			// 提取"莫某说："样式的人物，组成临时人物列表
			myLineContent = MyStringUtils.EnToCHSeparator(myLineContent);
			if (RegexUtils.regexFind("^[\u4e00-\u9fa5]{1,6}(（.*）)*说{0,1}[：|:]", myLineContent)) {
				Matcher figureMather = Pattern.compile("^[\u4e00-\u9fa5]{1,4}(（.*）)*说{0,1}[：|:]").matcher(myLineContent);
				while (figureMather.find()) {
					if (!RegexUtils.regexFind(PlayAnalysisConstants.REGEX_FIGURE, figureMather.group())) {// figure不能为人物关键词
						figure = figureMather.group().replaceAll("说{0,1}[：|:]", "").replaceAll("(（.*）)*", "");
						if (map.containsKey(figure)) {
							repeatNum = map.get(figure);
							map.put(figure, repeatNum + 1);
						} else {
							map.put(figure, 1);
						}
					}
				}
			}
		}

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() > 2) {
				tempFigureList.add(entry.getKey());
			}
		}

		return tempList;
	}
	
	/**
	 * 处理字符串中的特殊字符
	 * @param str
	 * @return
	 */
	private static String dealSpecialChar(String str) {
		str = str.trim();
		
		//此处""不是空格，而是一个特殊字符，该字符会在自动编号的剧本中出现
		if (str.substring(0, 1).equals("﻿")) {
			str = str.substring(1, str.length());
		}
		
		//兼容自动编号中的符号自动替换问题
		String dealedGroupLineContent = str.replaceAll("-", ".");
		dealedGroupLineContent = dealedGroupLineContent.replaceAll("—", ".");
		dealedGroupLineContent = dealedGroupLineContent.replaceAll(" +", " ");
		
		//全角转半角
		dealedGroupLineContent = MyStringUtils.ToDBC(dealedGroupLineContent);
		//英文替换为中文
		dealedGroupLineContent = MyStringUtils.EnToCHSeparator(dealedGroupLineContent);
		
		return dealedGroupLineContent;
	}
	
	/**
	 * 根据给定的剧本格式解析剧本
	 * @param playId	
	 * @param contentList	剧本内容（按行拆分后的列表）
	 * @param scenarioFormatMap		从剧本格式中分析出来的信息
	 * @param viewLocationKeywordFilePath	存储拍摄场景关键字的文件路径
	 * @param figureMinRepeatNum	人物最少重复次数
	 * @param keepFigurant	是否保留群众演员
	 * @return	如果格式和剧本匹配，返回成功标识及解析出来的场景信息列表
	 * 如果格式和剧本不匹配，则返回匹配失败标识
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Map<String, Object> analysisScenario(List<String> contentList, Map<String, Object> scenarioFormatMap,
			String viewLocationKeywordFilePath, int figureMinRepeatNum, boolean keepFigurant) throws IOException, ParseException {
		
		titleWarnMsg.clear();
		titleInfoMsg.clear();

		Map<String, Object> viewDataMap = new HashMap<String, Object>();

		// 根据当前规则是否解析出标题项，以及标题项是有错误和警告信息
		Map<String, Object> findTitleMap = findTitle(contentList, scenarioFormatMap, viewLocationKeywordFilePath, figureMinRepeatNum, keepFigurant);
		boolean findFlag = (Boolean) findTitleMap.get("findFlag");
		List<Map<String, Object>> elementList = new ArrayList<Map<String, Object>>();

		if (findFlag) {
			viewDataMap.put("checkSceTitle", true);
			elementList = (List<Map<String, Object>>) findTitleMap.get("elementList");
			scenarioFormatMap.put("elementList", elementList);

			// 把解析出的结果封装起来
			List<ViewInfoDto> viewInfoDtoList = makeDto(contentList, scenarioFormatMap);
			viewDataMap.put("viewInfoDtoList", viewInfoDtoList);
			viewDataMap.put("scriptRule", scenarioFormatMap.get("scenarioFormat"));
		} else {
			viewDataMap.put("checkSceTitle", false);
		}

		viewDataMap.put("titleWarnMsg", titleWarnMsg);
		viewDataMap.put("titleInfoMsg", titleInfoMsg);
		return viewDataMap;
	}
	
	/**
	 * 把解析出来的数据封装进DTO中
	 * 
	 * @param contentList	剧本内容
	 * @param formatInfoMap	从剧本格式中分析出来的信息
	 * @return
	 */
	private static List<ViewInfoDto> makeDto(List<String> contentList, Map<String, Object> formatInfoMap) {
		List<Map<String, Object>> elementList = (List<Map<String, Object>>) formatInfoMap.get("elementList");
		Map<String, Object> formatInfo = (Map<String, Object>) formatInfoMap.get("formatInfo");
		int lineSeperatorCount = (Integer) formatInfo.get("lineSeperatorCount"); // 自定义格式中换行符的个数
		String title = "";
		List<String> textList = new ArrayList<String>();
		int titleLineCount = 0;// 标题首 所在行数
		List<Integer> titleLineCountList = new ArrayList<Integer>();
		
		ViewInfoDto viewInfoDto = new ViewInfoDto();
		List<ViewInfoDto> playInfoDtoList = new ArrayList<ViewInfoDto>();

		// 先循环一遍，获取每个标题对应正文的行数范围
		for (Map<String, Object> elementMap : elementList) {
			titleLineCount = (Integer) elementMap.get("titleLineCount");
			titleLineCountList.add(titleLineCount);
		}

		textList = getTextFromContent(contentList, titleLineCountList, lineSeperatorCount);

		// 获取每一个标题对应的正文、元素，组装入库Dto;
		String text = ""; // 每场内容
		String foreword = "";// 前言，第一个标题前的内容
		for (int i = 0; i < elementList.size(); i++) {
			Map<String, Object> elementMap = elementList.get(i);
			title = (String) elementMap.get("title");
			if (i == 0) {// 第一个标题的正文需要拼接上序言
				foreword = textList.get(0);
				if (RegexUtils.regexFind("[\u4e00-\u9fa5_a-zA-Z0-9]+", foreword)) {// 含有有效字符
					foreword += PlayAnalysisConstants.lineSeprator + "--------------------" + PlayAnalysisConstants.lineSeprator;
				} else {
					foreword = "";
				}
				text = foreword + textList.get(1);
			} else {// 其他标题
				text = textList.get(i + 1);
			}

			viewInfoDto = genPlayInfoDto(elementMap);
			viewInfoDto.setTitle(title);
			viewInfoDto.setContent(text);
			playInfoDtoList.add(viewInfoDto);
		}

		return playInfoDtoList;
	}
	
	/**
	 * 把标题中解析出的结果封装到PlayInfoDto中
	 * @param elementMap	从标题中解析出来的元素
	 * @return
	 */
	private static ViewInfoDto genPlayInfoDto (Map<String, Object> elementMap) {
		
		ViewInfoDto viewInfoDto = new ViewInfoDto();
		String title = (String) elementMap.get("title");
		int seriesNo = -1;	//集次
		String viewNo = "";	//场次
		String season = "";	//季节
		String atmosphere = "";	//气氛
		String site = "";	//内外景
		String scenarioStr = "";  //场景信息  以/隔开
		List<String> addrList = new LinkedList<String>();// 场景地点列表
		List<String> roleNameList = new ArrayList<String>();
		
		if (!StringUtils.isBlank(title)) {				
			if (elementMap.get("seriesNo")!=null&&elementMap.get("seriesNo").toString().length()>0) {		
				seriesNo = Integer.parseInt((String) elementMap.get("seriesNo"));// 集数	
			}
			
			viewNo = (String) elementMap.get("viewNo");	//场次
			
			season = (String) elementMap.get("season");
			atmosphere = (String) elementMap.get("atmosphere");
			site = (String) elementMap.get("site");
			
			String addrStr = (String) elementMap.get("viewLocation");
			if (!StringUtils.isBlank(addrStr)) {
				addrList = Arrays.asList(addrStr.split(","));
			}
			
			viewInfoDto.setSeriesNo(seriesNo);
			viewInfoDto.setViewNo(viewNo);
			viewInfoDto.setAtmosphere(atmosphere);
			viewInfoDto.setSite(site);
			
			if (addrList.size() > 0) {
				viewInfoDto.setFirstLocation(addrList.get(0));
			}
			if (addrList.size() > 1) {
				viewInfoDto.setSecondLocation(addrList.get(1));
			}
			if (addrList.size() > 2) {
				viewInfoDto.setThirdLocation(addrList.get(2));
			}
			
			String figure = (String) elementMap.get("figure");
			if (!StringUtils.isBlank(figure)) {
				roleNameList = Arrays.asList(figure.split(","));
				List<String> matchRoleNameList = new ArrayList<String>();
				//如果roleList中不存在该角色并且数据库中也不存在，那么就添加进去  到时候存入数据库
				for(String name:roleNameList){
					if(name!=null&&name.length()>0){
						if(!matchRoleNameList.contains(name)){
							matchRoleNameList.add(name);
						}
					}					
				}
				viewInfoDto.setMajorRoleNameList(matchRoleNameList);
			}
		}
		return viewInfoDto;
	}
	
	/**
	 * 根据解析出的标题切分整个剧本内容，把每一场的内容计算出来
	 * @param contentList	所有内容
	 * @param titleLineCountList	每一个标题的起始行
	 * @param lineSeperatorCount	换行符的个数
	 * @return
	 */
	public static List<String> getTextFromContent(List<String> contentList, List<Integer> titleLineCountList, int lineSeperatorCount) {
		String text = "";
		List<String> textList = new ArrayList<String>();
		int startLineCount = 0;
		int endLineCount = 0;
		titleLineCountList.add(0, 0);
		titleLineCountList.add(contentList.size() + lineSeperatorCount + 1);
		for (int k = 0; k < titleLineCountList.size() - 1; k++) {
			text = "";
			startLineCount = titleLineCountList.get(k);
			endLineCount = titleLineCountList.get(k + 1) - lineSeperatorCount - 2;

			for (int i = 0; i < contentList.size(); i++) {
				if (i < startLineCount) {
					continue;
				}
				if (i > endLineCount) {
					break;
				}
				if (RegexUtils.regexFind(PlayAnalysisConstants.REGEX_SERIES, contentList.get(i))) {// 行内容为“第N集”的不加入正文
					if (RegexUtils.regexFind(".*[闪回|闪过]+.*第(.+)集.*", contentList.get(i))) {
						text += contentList.get(i) + PlayAnalysisConstants.lineSeprator;
					}
				} else {
					text += contentList.get(i) + PlayAnalysisConstants.lineSeprator;
				}
			}
			textList.add(text.trim());
		}

		return textList;
	}
	
	/**
	 * 根据当前匹配规则找到标题，并对标题元素内容进行检测，以确定当前匹配规则是否适用
	 * @param contentList 剧本内容（按照每行内容拆分成的一个列表）
	 * @param formatInfoMap	从剧本格式中提取出的详细格式信息
	 * @param viewLocationKeywordFilePath 存储拍摄场景关键字的文件路径
	 * @param figureMinRepeatNum 人物最少重复次数
	 * @param keepFigurant	是否保留群众演员
	 * @return
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Map<String, Object> findTitle(List<String> contentList, Map<String, Object> formatInfoMap, String viewLocationKeywordFilePath, int figureMinRepeatNum, boolean keepFigurant) throws ParseException, FileNotFoundException, IOException {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> formatInfo = (Map<String, Object>) formatInfoMap.get("formatInfo");
		List<String> scenarioFormatList = (List<String>) formatInfoMap.get("scenarioFormatList");
		List<Map<String, Object>> elementList = new ArrayList<Map<String, Object>>();
		int tryNum = (Integer) formatInfoMap.get("tryNum");
		// 切割 scenarioFormat 字符串
		String extralSeriesNo = formatInfoMap.get("extralSeriesNo").toString();
		String mainTitleRegex = (String) formatInfo.get("mainTitleRegex"); // 匹配标题的正则表达式
		String separateSceripRegex = (String) formatInfo.get("separateSceripRegex"); // 分割标题中元素的正则表达式
		int lineSeperatorCount = (Integer) formatInfo.get("lineSeperatorCount"); // 自定义格式中换行符的个数
		String seriesViewNoRegex = (String) formatInfo.get("seriesViewNoRegex"); // 只包含集场号的正则表达式


		if (contentList != null && contentList.size() > 0) {
			// 考虑到自定义格式中有换行符,利用这两个变量对行进行打包分析 例如有一个换行符，则对剧本的分析时按照每两行一个单位进行解析
			int groupLineCount = 0; // 表示当前的数据是第几行
			String groupLineValue = ""; // 存储包括当前行，往上lineSeperatorCount+1行的数据
			String setNoFromContent = "";// 从内容中解析出来的当前集次

			// 上一个有效的集次和场次
			int lastSeriesNo = Integer.parseInt(extralSeriesNo);
			int lastViewNo = 1;
			boolean flashBackFlag = false;
			boolean validTitleFlag = false;

			boolean seriesNoLineFlag = false;
			for (String myLineContent : contentList) {
				groupLineCount++;
				groupLineValue += myLineContent + PlayAnalysisConstants.lineSeprator;

				// 遇到"闪回"关键字
				if (RegexUtils.regexFind("闪回", myLineContent)) {
					flashBackFlag = true;
				}

				Pattern p = Pattern.compile(PlayAnalysisConstants.REGEX_SERIES);
				Matcher m = p.matcher(myLineContent);
				if (m.find()) {
					seriesNoLineFlag = true;
					if (RegexUtils.regexFind(".*[闪回|闪过]+.*" + PlayAnalysisConstants.REGEX_SERIES, myLineContent)) {// 忽略"闪回第一集第2场"样式的行
						seriesNoLineFlag = false;
					}
					if (RegexUtils.regexFind(PlayAnalysisConstants.REGEX_SERIES + "团|中", myLineContent)) {// 忽略"第32集团军"样式的行
						seriesNoLineFlag = false;
					}
					if (RegexUtils.regexFind(",|，|\\.|。", myLineContent)) {// 忽略含有逗号和句号的行
						seriesNoLineFlag = false;
					}
					String tempMyLineContent = myLineContent.replaceAll("第([一|二|三|四|五|六|七|八|九|十|百|0-9| | ]+)集", "").replaceAll("《.*》", "")
							.replaceAll("<.*>", "").trim();
					if (tempMyLineContent.length() > 10) {// 忽略除第N集和剧本名称外字数超过10个的行
						seriesNoLineFlag = false;
					}

					if (seriesNoLineFlag) {
						String num = TitlePreDealUtils.genCNViewNo(m.group(1).replaceAll(" +", ""));
						if (!StringUtils.isEmpty(num)) {
							if (StringUtils.isEmpty(setNoFromContent) || Integer.parseInt(setNoFromContent) < Integer.parseInt(num)) {
								setNoFromContent = num;
							}
						}
					}
				}

				if (groupLineCount < lineSeperatorCount + 1) {
					continue;
				}
				if (groupLineCount != lineSeperatorCount + 1) {
					String[] groupLineArr = groupLineValue.split(PlayAnalysisConstants.lineSeprator);
					groupLineValue = "";
					for (int i = 0, len = groupLineArr.length; i < len; i++) {
						if (i != 0) {
							groupLineValue += groupLineArr[i] + PlayAnalysisConstants.lineSeprator;
						}
					}
				}

				String groupLineContent = groupLineValue.trim();
				String dealedGroupLineContent = TitlePreDealUtils.standardTitle(groupLineContent);

				// 本行是否为标题：集-场 主场景 次场景 气氛 角色
				boolean titleFlag = RegexUtils.regexFind(mainTitleRegex, dealedGroupLineContent);
				boolean isTitle = RegexUtils.regexFind(seriesViewNoRegex, dealedGroupLineContent); // 本行是否匹配只含有集场号的标题

				if (titleFlag || isTitle) {
					List<String> titleElementList = new ArrayList<String>();
					String[] titleArray = dealedGroupLineContent
							.split(separateSceripRegex);
					for (String str : titleArray) {
						titleElementList.add(str);
					}

					if (!StringUtils.isEmpty(setNoFromContent)) {// 如果文本内容中获取到集数，则作为参数传递下去
						extralSeriesNo = setNoFromContent;
						if (Integer.parseInt(extralSeriesNo) > lastSeriesNo) {
							lastSeriesNo = Integer.parseInt(extralSeriesNo);
							lastViewNo = 1;
						}
					}

					Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
					elementMap = genTitleElement(dealedGroupLineContent, formatInfoMap, titleElementList, false, extralSeriesNo);
					validTitleFlag = true;
					int seriesNo = Integer.parseInt((String) elementMap.get("seriesNo"));
					int viewNo = Integer.parseInt(((String) elementMap.get("viewNo")).replaceAll("[a-zA-Z]", ""));

					if (seriesNo > 500) {// 集次超过500不作为标题
						validTitleFlag = false;
					}
					if (RegexUtils.regexFind("^[E|e]\\d+", (String) elementMap.get("viewNo"))) {// E36和e36不作为标题
						validTitleFlag = false;
					}
					if (viewNo > 500) {// 场次超过500不作为标题
						validTitleFlag = false;
					}
					if (flashBackFlag) {// 处于闪回状态下，检查当前标题和上一个标题是否存在集次或场次增长
						if (seriesNo > lastSeriesNo || (seriesNo == lastSeriesNo && viewNo >= lastViewNo)) {
							validTitleFlag = true;
							flashBackFlag = false;
						} else {
							validTitleFlag = false;
						}
					}

					if (validTitleFlag) {
						lastSeriesNo = seriesNo;
						lastViewNo = viewNo;
						elementMap.put("title", groupLineContent);
						elementMap.put("titleLineCount", groupLineCount);
						elementMap.put("extralSeriesNo", extralSeriesNo);
						elementList.add(elementMap);
					}
				}

				if (groupLineCount > 200 && elementList.size() == 0) {// 前200行未解析出标题项
					result.put("findFlag", false);
					return result;
				}
			}
		}

		// 增加元素内容检测逻辑
		logger.info("组装元素内容完成，从" + contentList.size() + "行原文中匹配出" + elementList.size() + "个标题项");

		// 第一重过滤：长度合理性过滤，剔除元素或标题字数超长的标题
		elementList = TitleReasonableCheckUtils.lengthFilter(elementList);
		// 第二重过滤：集场次合理性过滤
		elementList = TitleReasonableCheckUtils.seriesViewFilter(elementList);

		if (elementList.size() == 0) {// 未解析出标题项
			result.put("findFlag", false);
			return result;
		}

		List<String> seriesNoList = new ArrayList<String>();
		List<String> viewNoList = new ArrayList<String>();
		List<String> siteList = new ArrayList<String>();
		List<String> atmosphereList = new ArrayList<String>();
		List<String> seasonList = new ArrayList<String>();
		List<String> viewLocationGroupList = new ArrayList<String>();
		List<String> figureGroupList = new ArrayList<String>();
		for (Map<String, Object> elementContentMap : elementList) {
			seriesNoList.add((String) elementContentMap.get("seriesNo"));
			viewNoList.add((String) elementContentMap.get("viewNo"));
			atmosphereList.add((String) elementContentMap.get("atmosphere"));
			siteList.add((String) elementContentMap.get("site"));
			seasonList.add((String) elementContentMap.get("season"));
			viewLocationGroupList.add((String) elementContentMap.get("viewLocation"));
			figureGroupList.add((String) elementContentMap.get("figure"));
		}

		// 第一重检测：元素数量检车
		boolean elementNumCheckFlag = TitleReasonableCheckUtils.elementNumCheck(elementList, tryNum, scenarioFormatList);
		if (!elementNumCheckFlag) {
			result.put("findFlag", false);
			return result;
		}

		// 第二重检测：集场次元素检查
		Map<String, Object> seriesViewCheckResult = TitleReasonableCheckUtils.seriesViewCheck(seriesNoList, viewNoList, tryNum);
		boolean seriesViewCheckFlag = (Boolean) seriesViewCheckResult.get("checkFlag");
		List<String> titleWarnMsgList = (List<String>) seriesViewCheckResult.get("titleWarnMsgList");
		List<String> titleInfoMsgList = (List<String>) seriesViewCheckResult.get("titleInfoMsgList");
		titleWarnMsg.addAll(titleWarnMsgList);
		titleInfoMsg.addAll(titleInfoMsgList);
		
		if (!seriesViewCheckFlag) {
			result.put("findFlag", false);
			return result;
		}

		// 第三重检测：内外景、氛围、季节三元素校验
		boolean atmosphereCheckFlag = TitleReasonableCheckUtils.keywordCheck(elementList, "atmosphere", tryNum, scenarioFormatList.contains("e4"));
		if (!atmosphereCheckFlag) {
			result.put("findFlag", false);
			return result;
		}
		boolean siteCheckFlag = TitleReasonableCheckUtils.keywordCheck(elementList, "site", tryNum, scenarioFormatList.contains("e5"));
		if (!siteCheckFlag) {
			result.put("findFlag", false);
			return result;
		}

		// 第四重检测：场景元素校验
		boolean viewLocationCheckFlag = TitleReasonableCheckUtils.viewLocationCheck(viewLocationGroupList, tryNum, scenarioFormatList.contains("e3"), viewLocationKeywordFilePath);
		if (!viewLocationCheckFlag) {
			result.put("findFlag", false);
			return result;
		}

		// 第五重检测：人物元素校验
		boolean figureCheckFlag = TitleReasonableCheckUtils.figureCheck(figureGroupList, tryNum, scenarioFormatList.contains("e7"), figureMinRepeatNum, keepFigurant, tempFigureList);
		if (!figureCheckFlag) {
			result.put("findFlag", false);
			return result;
		}

		for (int i = 0; i < figureGroupList.size(); i++) {
			elementList.get(i).put("figure", figureGroupList.get(i));
		}

		result.put("findFlag", true);
		result.put("elementList", elementList);
		return result;
	}
	
	/**
	 * 获取标题中的元素
	 * @param title	标题字符串
	 * @param scripteleInfoList	所有元素信息
	 * @param separatorInfoList	所有分隔符信息
	 * @return
	 */
	public static Map<String, Object> genTitleElement(String titleStr, Map<String, Object> formatInfoMap, 
			List<String> titleElementList, boolean saveNotFullMatchData, String extralSeriesNo) {
		String seriesNo = "", viewNo = "", viewLocation = "", atmosphere = "", site = "", season = "", figure = "";
		boolean validTitle = true; // 标题是否有效
		boolean isStandardTitle = true;
		Map<String, Object> elemenetMap = new HashMap<String, Object>();

		// 从formatInfoMap中提取各种表达式和参数
		List<String> scenarioFormatList = (List<String>) formatInfoMap.get("scenarioFormatList");
		List<ScripteleDto> scripteleInfoList = PlayAnalysisConstants.SCRIPTELE_LIST;
		List<SeparatorDto> separatorInfoList = PlayAnalysisConstants.SEPARATOR_LIST;
		String scenarioFormatStr = formatInfoMap.get("scenarioFormat").toString();

		Map<String, Object> formatInfo = (Map<String, Object>) formatInfoMap.get("formatInfo");
		String mainTitleRegex = (String) formatInfo.get("mainTitleRegex"); // 匹配标题的正则表达式
		int lineSeperatorCount = (Integer) formatInfo.get("lineSeperatorCount"); // 自定义格式中换行符的个数
		String seriesViewNoRegex = (String) formatInfo.get("seriesViewNoRegex"); // 只包含集场号的正则表达式
		List<String> seriesViewNoFormatList = (List<String>) formatInfo.get("seriesViewNoFormatList"); // 只包含集场号的剧本格式信息
		String oriSeparateSceripRegex = (String) formatInfo.get("separateSceripRegex"); // 用户自定义标题格式中分隔符信息
		String noFigureRegex = (String) formatInfo.get("noFigureRegex"); // 不带有人物的正则表达式
		List<String> noFigureFormatList = (List<String>) formatInfo.get("noFigureFormatList"); // 不带有人物的剧本格式信息
		int figureLineNum = (Integer) formatInfo.get("figureLineNum"); // 人物元素和其之前的元素之间换行符的数量

		boolean standardTitleFlag = RegexUtils.regexFind(mainTitleRegex, titleStr); // 本行是否为标配的标题
		boolean isTitle = RegexUtils.regexFind(seriesViewNoRegex, titleStr); // 本行是否匹配只含有集场号的标题
		boolean isNoFigure = RegexUtils.regexFind(noFigureRegex, titleStr); // 本行是否匹配只不含有人物及其前面符号的标题格式
		if (!standardTitleFlag && isTitle) {
			if (noFigureFormatList.size() != 0 && isNoFigure) {
				scenarioFormatList = noFigureFormatList;
				String[] titleLineArray = titleStr.split(PlayAnalysisConstants.lineSeprator);
				titleStr = "";
				int size = titleLineArray.length;
				if (lineSeperatorCount == titleLineArray.length - 1) {
					size = titleLineArray.length - figureLineNum;
				}

				for (int i = 0; i < size; i++) {
					titleStr += titleLineArray[i] + "，";
				}

				String[] titleArray = titleStr.split(oriSeparateSceripRegex);
				titleElementList.clear();
				for (String str : titleArray) {
					titleElementList.add(str);
				}
			} else {
				scenarioFormatList = seriesViewNoFormatList;
			}
			isStandardTitle = false;
		}

		String beforeFigureSeparatorId = ""; // 人物元素之前的符号ID
		String beforeFigureSeparatorName = ""; // 人物元素之前的符号名称
		String preFormatAtomic = ""; // 上一个自定义格式元素
		for (int i = 0, len = scenarioFormatList.size(); i < len; i++) {
			String allFormat = scenarioFormatList.get(i);
			// 获取人物元素之前的符号
			if (!StringUtils.isBlank(preFormatAtomic)
					&& allFormat.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE)) {
				beforeFigureSeparatorId = preFormatAtomic;
				break;
			}
			preFormatAtomic = allFormat;
		}

		if (!StringUtils.isBlank(beforeFigureSeparatorId)) {
			for (SeparatorDto separator : separatorInfoList) {
				if (separator.getId().equals(beforeFigureSeparatorId)) {
					beforeFigureSeparatorName = separator.getName();
					break;
				}
			}

			// 换行做特殊处理
			if (beforeFigureSeparatorName.equals("/r/n")) {
				beforeFigureSeparatorName = "\r\n";
			}

			int titleFigureIndex = titleStr.lastIndexOf(beforeFigureSeparatorName);

			if (titleFigureIndex > 0) {// 含有人物元素之前的符号

				String exceptFigureTitle = titleStr.substring(0, titleFigureIndex);
				String figureTitle = titleStr.substring(titleFigureIndex, titleStr.length());
				int formatFigureIndex = scenarioFormatStr.lastIndexOf(beforeFigureSeparatorId);
				String exceptFigureFormat = scenarioFormatStr.substring(0, formatFigureIndex);

				List<String> exceptFigureScenarioFormatList = genScenarioFormatList(exceptFigureFormat);
				Map<String, Object> formInfoMap = genFormatInfo(scripteleInfoList, separatorInfoList, exceptFigureScenarioFormatList);
				String separateSceripRegex = (String) formInfoMap.get("separateSceripRegex");

				String[] titleArray = exceptFigureTitle.split(separateSceripRegex);
				titleElementList.clear();
				for (String str : titleArray) {
					titleElementList.add(str);
				}

				int exceptFigureElementNum = 0;
				for (String eleAndSep : exceptFigureScenarioFormatList) {
					if (!"e7".equals(eleAndSep) && eleAndSep.startsWith("e")) {
						exceptFigureElementNum++;
					}
				}

				if (exceptFigureElementNum > titleElementList.size()) {
					int missNum = exceptFigureElementNum - titleElementList.size();
					for (int i = 0; i < missNum; i++) {// 元素数量不足的，以空值填充
						titleElementList.add("");
					}
				}

				// 解析figureTitle，把人物值取到
				figureTitle = figureTitle
						.replace(beforeFigureSeparatorName, "");
				figureTitle = figureTitle.replace("\r\n", "");
				String[] figureArray = figureTitle.split(PlayAnalysisConstants.COMMON_SEPARATOR + "|" + oriSeparateSceripRegex);
				for (String myFigure : figureArray) {
					if (StringUtils.isBlank(myFigure)) {
						continue;
					}
					figure += myFigure + ",";
				}
				if (figure.length() > 0) {
					figure = figure.substring(0, figure.length() - 1);
				}
				titleElementList.add(figure);
			}

		}

		int extralElementNum = 0; // 实际的标题比自定义格式的标题多出来的元素数量
		int firstViewLocationIndex = -1; // 主场景下标
		String[] scenarioFormatArray = new String[scenarioFormatList.size()];

		// 去掉标题中的符号信息格式列表
		for (int i = 0, len = scenarioFormatList.size(); i < len; i++) {
			String allFormat = scenarioFormatList.get(i);
			for (ScripteleDto scriptele : scripteleInfoList) {
				String eleId = scriptele.getId();
				if (allFormat.equals(eleId)) {
					scenarioFormatArray[i] = eleId;
				}
			}
		}

		List<String> excepSepFormatList = new ArrayList<String>();
		for (int i = 0, len = scenarioFormatArray.length; i < len; i++) {
			if (StringUtils.isBlank(scenarioFormatArray[i])) {
				continue;
			}
			excepSepFormatList.add(scenarioFormatArray[i]);
		}

		if (excepSepFormatList.size() < titleElementList.size()) {
			extralElementNum = titleElementList.size()
					- excepSepFormatList.size();
		}

		for (int i = 0, len = excepSepFormatList.size(); i < len; i++) {
			if (StringUtils.isBlank(excepSepFormatList.get(i))) {
				continue;
			}
			if (firstViewLocationIndex == -1 && excepSepFormatList.get(i).equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION)) {
				firstViewLocationIndex = i;
				break;
			}
		}

		if (excepSepFormatList.size() > titleElementList.size()) {
			int missNum = excepSepFormatList.size() - titleElementList.size();
			for (int i = 0; i < missNum; i++) {// 元素数量不足的，以空值填充
				titleElementList.add("");
			}
		}
		// 只有标题符合规则的时候再分析
		if (validTitle) {
			for (int s = 0; s < scripteleInfoList.size(); s++) {
				ScripteleDto scriptele = scripteleInfoList.get(s);
				String eleId = scriptele.getId();
				// 获取自定义格式中指定剧本元素符串的index列表

				List<Integer> indexList = new ArrayList<Integer>();
				for (int i = 0, len = excepSepFormatList.size(); i < len; i++) {
					if (StringUtils.isBlank(excepSepFormatList.get(i))) {
						continue;
					}

					if (excepSepFormatList.get(i).equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION)) {
						continue;
					}

					if (!excepSepFormatList.get(i).equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION) && excepSepFormatList.get(i).equals(eleId)) {
						if (i > firstViewLocationIndex && firstViewLocationIndex != -1) {
							i += extralElementNum;
						}
						indexList.add(i);
					}
				}

				if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION) && firstViewLocationIndex != -1) {
					for (int i = firstViewLocationIndex; i <= firstViewLocationIndex + extralElementNum; i++) {
						indexList.add(i);
					}
				}
				String singleValue = "";
				String multiValue = "";

				for (Integer index : indexList) {
					singleValue = titleElementList.get(index);
					multiValue += singleValue + ",";
				}
				if (multiValue.length() > 0) {
					multiValue = multiValue.substring(0, multiValue.length() - 1);
				}
				if (indexList.size() > 0) {
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_SERIESNO)) {
						seriesNo = singleValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_VIEWNO)) {
						viewNo = singleValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION)) {
						viewLocation = multiValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_ATMOSPHERE)) {
						atmosphere = singleValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_SITE)) {
						site = singleValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_SEASON)) {
						season = singleValue;
					}
					if (eleId.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE)) {
						figure = multiValue;
					}
				}

			}
		}
		elemenetMap.put("validTitle", validTitle);
		elemenetMap.put("titleWarnMsg", titleWarnMsg);
		elemenetMap.put("titleInfoMsg", titleInfoMsg);
		elemenetMap.put("seriesNo", seriesNo);
		if (StringUtils.isBlank(seriesNo)) {
			elemenetMap.put("seriesNo", extralSeriesNo);
		} else {
			elemenetMap.put("seriesNo", seriesNo);
		}
		if (!StringUtils.isBlank(viewNo)) {
			elemenetMap.put("viewNo", viewNo.replace("\t", ""));
		}
		if (!StringUtils.isBlank(viewLocation)) {
			elemenetMap.put("viewLocation", viewLocation);
		}

		elemenetMap.put("atmosphere", atmosphere);
		elemenetMap.put("site", site);
		elemenetMap.put("season", season);
		elemenetMap.put("isStandardTitle", isStandardTitle);
		elemenetMap.put("figureLineNum", figureLineNum);
		elemenetMap.put("isTitle", isTitle);
		elemenetMap.put("isNoFigure", isNoFigure);

		elemenetMap.put("figure", figure);

		return elemenetMap;
	}
	
	/**
	 * 把剧本的格式拆分成单个原子的形式 例如e1s3e2s2拆分成[e1, s3, e2, s2] 该方法要求单个原子开头必须是s或e
	 * 
	 * @param scripteleInfoList 剧本元素信息
	 * @param separatorInfoList 剧本符号信息
	 * @param scenarioFormatStr 剧本格式字符串
	 * @return
	 */
	public static List<String> genScenarioFormatList(String scenarioFormatStr) {
		Pattern p = Pattern.compile("[s|e]*[0-9]{1,3}");
		List<String> scenarioFormatList = new ArrayList<String>();
		Matcher m = p.matcher(scenarioFormatStr);
		while (m.find()) {
			for (int j = 0; j <= m.groupCount(); j++)
				scenarioFormatList.add(m.group(j).trim());
		}
		return scenarioFormatList;
	}

	/**
	 * 根据自定义的格式获取分析剧本时需要的剧本格式信息
	 * @param scripteleInfoList		剧本元素
	 * @param separatorInfoList		元素分隔符
	 * @param scenarioFormat
	 * @return 格式Map
	 * 当key为mainTitleRegex时，value表示主标题的正则表达式
	 * seriesViewNoRegex  --  只带有集场的标题的正则表达式
	 * seriesViewNoFormatList  -- 只带有集场的元素列表
	 * noFigureRegex  --  不带有人物的标题的正则表达式
	 * noFigureFormatList  --  不带有人物的标题的元素列表
	 * figureLineNum --  人物元素占据的行数
	 * minCount  --  标题中最小元素数量
	 * lineSeperatorCount -- 换行符的数量
	 * separateSceripRegex  --  分割标题的正则表达式
	 */
	public static Map<String, Object> genFormatInfo(List<ScripteleDto> scripteleInfoList, List<SeparatorDto> separatorInfoList, List<String> scenarioFormatList) {
		Map<String, Object> formmatMap = new HashMap<String, Object>();
		
		//标准标题
		List<String> mainTitleRegexList = new ArrayList<String>(scenarioFormatList.size());
		
		//只带有集场号的标题
		List<String> seriesViewNoRegexList = new ArrayList<String>(scenarioFormatList.size());
		List<String> seriesViewNoFormatList = new ArrayList<String>();
		
		//只不带人物及其之前符号的标题
		List<String> noFigureRegexList = new ArrayList<String>();
		List<String> noFigureFormatList = new ArrayList<String>();
		
		String[] separateRegexList = new String[scenarioFormatList.size() + 2];
		
		int minCount = 0;			//标题中元素的最少数量
		int lineSeperatorCount = 0;	//换行符的数量

		String mainTitleRegex = "";		//拼接标准标题的正则表达式
		String seriesViewNoRegex = "";	//只识别集场号的正则表达式
		
		String noFigureRegex = "";	//识别除了人物及其前面的符号外的正则表达式
		String separateSceripRegex = "(";		//分割元素的正则表达式
		int lineNum = 0;	//人物元素和其之前的元素之间换行符的数量
		
		String preFormatAtomic = "";	//上一个自定义格式元素
		boolean hasFigure = false;
		boolean hasViewLocation = false;
		
		int separatorNum = 0;	//两个元素之间的分隔符数目
		List<String> separatorList = new ArrayList<String>();	//两个元素之间分隔符列表
		
		
		for (int i = 0, len = scenarioFormatList.size(); i < len; i++) {
			String allFormat = scenarioFormatList.get(i);
			
			if (noFigureFormatList.size()==0 && noFigureRegexList.size()==0 && allFormat.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE)) {
				for (String beforeFigureSepa : separatorList) {
					if (beforeFigureSepa.equals(PlayAnalysisConstants.SEPARATOR_LINE)) {
						lineNum++;
					}
				}
				
				for (int j = 0; j < i-separatorNum; j++) {
					noFigureFormatList.add(scenarioFormatList.get(j));
				}
				for (int j = 0; j < i-separatorNum; j++) {
					noFigureRegexList.add(mainTitleRegexList.get(j));
				}
			}
			
			for (ScripteleDto scriptele : scripteleInfoList) {
				String eleId = scriptele.getId();
				String eleRegex = scriptele.getRegex();
				if (allFormat.equals(eleId)) {
					mainTitleRegexList.add(i, eleRegex);
					
					//忽略"人物"元素的个数
					if (!allFormat.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE)) {
						minCount ++;
					}
					
					separatorNum = 0;
					separatorList.clear();
				}
			}
			
			for (SeparatorDto separator : separatorInfoList) {
				String sepaId = separator.getId();
				String sepRegex = separator.getRegex();
				
				if (allFormat.equals(sepaId)) {
					if (StringUtils.isBlank(preFormatAtomic) || (!StringUtils.isBlank(preFormatAtomic) && !preFormatAtomic.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE))) {
						mainTitleRegexList.add(i, sepRegex);
					} else {
						mainTitleRegexList.add(i, "");
					}
					
					//只带有集场号和后面第一个符号的正则表达式
					if (preFormatAtomic.equals(PlayAnalysisConstants.SCRIPTELE_SERIESNO) || preFormatAtomic.equals(PlayAnalysisConstants.SCRIPTELE_VIEWNO)) {
						seriesViewNoRegexList.clear();
						for (String notAllRegex : mainTitleRegexList) {
							seriesViewNoRegexList.add(notAllRegex);
						}
						seriesViewNoFormatList = scenarioFormatList.subList(0, i);
					}
					separateRegexList[i] = sepRegex;
					
					separatorNum++;
					separatorList.add(sepaId);
				}
			}

			if (allFormat.equals(PlayAnalysisConstants.SEPARATOR_LINE)) {
				lineSeperatorCount++;
			}
			if (!hasFigure && allFormat.equals(PlayAnalysisConstants.SCRIPTELE_FIGURE)) {
				hasFigure = true;
			}
			if (!hasViewLocation && allFormat.equals(PlayAnalysisConstants.SCRIPTELE_VIEWLOCATION)) {
				hasViewLocation = true;
			}
			
			preFormatAtomic = allFormat;
		}
		
		if (hasFigure) {
			separateRegexList[scenarioFormatList.size()] = PlayAnalysisConstants.COMMON_SEPARATOR;
		}
		if (hasViewLocation) {
			separateRegexList[scenarioFormatList.size() + 1] = PlayAnalysisConstants.COMMON_SEPARATOR;
		}
		
		
		//主标题的正则表达式
		mainTitleRegex += "^";
		for (int i = 0, len = mainTitleRegexList.size(); i < len; i++) {
			String singleTitleRegex = mainTitleRegexList.get(i);
			if (!StringUtils.isBlank(singleTitleRegex)) {
				mainTitleRegex += singleTitleRegex;
			}
		}
		mainTitleRegex += "$";
		
		
		//只带有集场号的正则表达式
		seriesViewNoRegex += "^";
		for (int i = 0, len = seriesViewNoRegexList.size(); i < len; i++) {
			String mySeriesViewNoRegex = seriesViewNoRegexList.get(i);
			if (!StringUtils.isBlank(mySeriesViewNoRegex)) {
				seriesViewNoRegex += mySeriesViewNoRegex;
			}
		}
		//.*正则匹配不了\r\n字符
		for (int i = 0; i < lineSeperatorCount; i++) {
			seriesViewNoRegex += ".*(\r\n)*";
		}
		seriesViewNoRegex += ".*$";

		
		//不带有人物的正则表达式
		noFigureRegex += "^";
		for (int i = 0; i < noFigureRegexList.size(); i++) {
			noFigureRegex += noFigureRegexList.get(i);
		}
		//.*正则匹配不了\r\n字符
		for (int i = 0; i < lineSeperatorCount; i++) {
			noFigureRegex += ".*(\r\n)*";
		}
		noFigureRegex += ".*$";
		
		
		for (String singleSepRegex : separateRegexList) {
			if (!StringUtils.isBlank(singleSepRegex)) {
				separateSceripRegex += singleSepRegex + "|";
			}
		}
		
		formmatMap.put("mainTitleRegex", mainTitleRegex);
		
		formmatMap.put("seriesViewNoRegex", seriesViewNoRegex);
		formmatMap.put("seriesViewNoFormatList", seriesViewNoFormatList);
		
		formmatMap.put("noFigureRegex", noFigureRegex);
		formmatMap.put("noFigureFormatList", noFigureFormatList);
		formmatMap.put("figureLineNum", lineNum);
		
		formmatMap.put("minCount", minCount);
		formmatMap.put("lineSeperatorCount", lineSeperatorCount);
		
		separateSceripRegex = separateSceripRegex.substring(0, separateSceripRegex.length() - 1);
		separateSceripRegex += ")";
		formmatMap.put("separateSceripRegex", separateSceripRegex);
		
		return formmatMap;
	}
	
}
