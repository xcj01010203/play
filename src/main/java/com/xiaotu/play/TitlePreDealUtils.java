package com.xiaotu.play;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.xiaotu.play.constants.PlayAnalysisConstants;
import com.xiaotu.play.util.MyStringUtils;
import com.xiaotu.play.util.RegexUtils;

/**
 * 标题预处理工具类
 * 对剧本标题进行预处理，使其规范化
 * @author xuchangjian 2017-6-15下午3:44:32
 */
public class TitlePreDealUtils {

	/**
	 * 标准化标题，以便于后续的标题解析
	 * @param groupLineContent
	 * @return
	 */
	public static String standardTitle(String groupLineContent) {
		String regexSite = PlayAnalysisConstants.REGEX_SITE;
		String regexAtmosphere = PlayAnalysisConstants.REGEX_ATMOSPHERE;
		String commonSeparator = PlayAnalysisConstants.COMMON_SEPARATOR;
		String regexMeasureword = PlayAnalysisConstants.REGEX_MEASUREWORD;

		// 英文符号替换为中文符号
		groupLineContent = MyStringUtils.EnToCHSeparator(groupLineContent);

		// 标题和正文通用替换逻辑
		// ?开头的行去掉开头的?
		groupLineContent = groupLineContent.replaceAll("^？+", "");
		// "（1）"类型场次的处理：前括号去掉，后括号替换为空格
		if (RegexUtils.regexFind("^（*\\d+）+", groupLineContent)) {
			groupLineContent = groupLineContent.replaceAll("（", "").replaceAll(
					"）", " ");
		}

		// S1或s#1#类型场次的处理：Ss#去掉
		if (RegexUtils.regexFind("^[S|s]*#*\\d+#*", groupLineContent)) {
			groupLineContent = groupLineContent.replaceAll("#", "")
					.replaceAll("[S|s]", "").trim();
		}
		// * 1 *类型场次的处理：*去掉
		if (RegexUtils.regexFind("^(\\*)*.*\\d+.*(\\*)*", groupLineContent)) {
			groupLineContent = groupLineContent.replaceAll("\\*", "");
		}

		if (RegexUtils.regexFind("(，|、|-|—|/|；|：| |\\.|\t| |．|。|－)*(共|总共|合计)+([零|一|二|两|三|四|五|六|七|八|九|十|百|千]+)场(，|、|-|—|/|；|：| |\\.|\t| |．|。|－)*", groupLineContent)) {
			String tem1String = "";
			String tem2String = "";
			Matcher m = Pattern.compile("(共|总|合计)*([零|一|二|两|三|四|五|六|七|八|九|十|百|千]+)场(，|、|-|—|/|；|：| |\\.|\t| |．|。|－)*").matcher(groupLineContent);
			while (m.find()) {
				tem1String = m.group(0);
				tem2String = " ";
			}
			groupLineContent = groupLineContent.replaceFirst(tem1String, tem2String);
		}

		if (RegexUtils.regexFind("^第*([零|一|二|三|四|五|六|七|八|九|十|百|千]+)场(，|、|-|—|/|；|：| |\\.|\t| |．|。|－|\r\n)+", groupLineContent)) {
			String tem1String = "";
			String tem2String = "";
			Matcher m = Pattern.compile("第*([零|一|二|三|四|五|六|七|八|九|十|百|千]+)场")
					.matcher(groupLineContent);
			while (m.find()) {
				tem1String = m.group(1);
				tem2String = genCNViewNo(tem1String.trim()) + " ";
			}
			groupLineContent = groupLineContent.replaceFirst("第*" + tem1String + "场", tem2String);
		}

		String temGroupLineContent = replaceKeywords(groupLineContent);
		if (RegexUtils.regexFind("^\\d", temGroupLineContent)) {
			groupLineContent = temGroupLineContent;
		}

		// 替换标题中字符
		if (RegexUtils.regexFind("^\\d", groupLineContent)) {
			// 氛围元素和内外景元素紧挨的，中间加空格
			if (RegexUtils.regexFind("^\\d.*[" + regexAtmosphere + "]["
					+ regexSite + "].*|^\\d.*[" + regexSite + "]["
					+ regexAtmosphere + "].*", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("([" + regexAtmosphere + "])([" + regexSite + "])|([" + regexSite + "])(["+ regexAtmosphere + "])").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					String[] group = tem1String.split("");
					for (int i = 0; i < group.length; i++) {
						if (StringUtils.isBlank(group[i])) {
							continue;
						}
						tem2String += " " + group[i];
					}
				}
				groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
			}

			// 10. 氛围元素和内外景元素至少一方带括号且紧挨的，括号替换为空格
			if (RegexUtils.regexFind("（*[" + regexAtmosphere + "]）*（*[" + regexSite + "]）*|（*[" + regexSite + "]）*（*[" + regexAtmosphere + "]）*", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("（*[" + regexAtmosphere + "]）*（*[" + regexSite + "]）*|（*[" + regexSite + "]）*（*[" + regexAtmosphere + "]）*").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					tem2String = tem1String.replaceAll("（", " ").replaceAll("）", " ");
					groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
				}
			}

			// 11.
			// 氛围元素和内外景元素被一对括号括起来的，如果括号内只有内外景元素和氛围元素相关汉字，不含其它汉字也不含数字和字母，则括号替换为空格，否则不替换
			if (RegexUtils.regexFind("（.*）", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				String tem3String = "";
				Matcher m = Pattern.compile("（.*）").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					tem3String = tem1String.replaceAll("（", "").replaceAll("）",
							"");
					tem3String = tem3String.replaceAll(regexSite, "").replaceAll(regexAtmosphere, "").replaceAll(" ", "");
					if (!RegexUtils.regexFind("[\u4e00-\u9fa5_a-zA-Z0-9]", tem3String)) {// 括号内只有内外景元素和氛围元素相关汉字，不含其它汉字也不含数字和字母
						tem2String = tem1String.replaceAll("（", " ").replaceAll("）", " ");
						groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
					}
				}
			}

			// 12. 双氛围元素且不紧挨的，去掉中间的空格或符号
			if (RegexUtils.regexFind("[^\u4e00-\u9fa5](" + regexAtmosphere + ")[ | |\\.|/|\\|\\|]+(" + regexAtmosphere + ")[^\u4e00-\u9fa5]", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("(" + regexAtmosphere + ")[ | |\\.|/|\\|\\|]+(" + regexAtmosphere + ")").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					tem2String = tem1String.replaceAll("[ | |\\.|/|\\|\\|]+", "");
					groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
				}
			}

			// 13. 双内外景元素且不紧挨的，去掉中间的空格或符号
			if (RegexUtils.regexFind("[^\u4e00-\u9fa5](" + regexSite + ")[ | |\\.|/|\\|\\|]+(" + regexSite + ")([^\u4e00-\u9fa5]|$)", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("[^\u4e00-\u9fa5](" + regexSite + ")[ | |\\.|/|\\|\\|]+(" + regexSite + ")([^\u4e00-\u9fa5]|$)").matcher(
						groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					m = Pattern.compile("(" + regexSite + ")[ | |\\.|/|\\|\\|]+(" + regexSite + ")").matcher(tem1String);
					while (m.find()) {
						tem1String = m.group();
						tem2String = tem1String.replaceAll("[ | |\\.|/|\\|\\|]+", "");
						groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
					}
				}
			}

			// 6、29—10 判断这种类型
			if (RegexUtils.regexFind("^(\\d+" + commonSeparator + "){2,}\\d+", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("^(\\d+" + commonSeparator + "){2,}\\d+").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					String tem3String = "";
					if (groupLineContent.endsWith(tem1String)) {// 6、29—10
						tem2String = "(" + tem1String + ")";
						groupLineContent = groupLineContent.replaceAll(
								tem1String, tem2String);
					} else {
						m = Pattern.compile("^(\\d+" + commonSeparator + "){2,}\\d+.")
								.matcher(groupLineContent);
						while (m.find()) {
							tem3String = m.group().substring(m.group().length() - 1);
							if (!RegexUtils.regexFind(regexMeasureword, tem3String)) {// 6、29—10 判断
								tem2String = "(" + tem1String + ")";
								groupLineContent = groupLineContent.replaceAll(
										tem1String, tem2String);
							}
						}
					}
				}
			}

			// 1-2车站 1-2a车站
			if (RegexUtils.regexFind("^(\\d+" + commonSeparator + "){0,1}\\d+[A-Za-z]{0,1}[\u4e00-\u9fa5]", groupLineContent)) {
				String tem1String = "";
				String tem2String = "";
				Matcher m = Pattern.compile("^(\\d+" + commonSeparator + "){0,1}\\d+[A-Za-z]{0,1}[\u4e00-\u9fa5]")
						.matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					m = Pattern.compile("[\u4e00-\u9fa5]").matcher(tem1String);
					while (m.find()) {
						if (!RegexUtils.regexFind(regexMeasureword, m.group())) {// 1.4号车厢这种情况不加空格
							// System.out.println(regex_measureword);
							tem2String = tem1String.replaceAll(m.group(), " " + m.group());
							groupLineContent = groupLineContent.replaceAll(tem1String, tem2String);
						}
					}
				}
			}

			// 5.6.场合并 6、 7、（合并） 市中心咖啡厅 日外
			if (RegexUtils.regexFind("^(\\d+" + commonSeparator + "){2}（*[\u4e00-\u9fa5]+）*", groupLineContent)) {
				String tem1String = "";
				Matcher m = Pattern.compile("^(\\d+" + commonSeparator + "){2}（*[\u4e00-\u9fa5]+）*").matcher(groupLineContent);
				while (m.find()) {
					tem1String = m.group();
					m = Pattern.compile("\\d+" + commonSeparator + "（*[\u4e00-\u9fa5]+）*").matcher(tem1String);
					while (m.find()) {
						tem1String = m.group();
						if (RegexUtils.regexFind("合并", tem1String)) {
							groupLineContent = groupLineContent.replaceAll(tem1String, "");
						}
					}
				}
			}
		}
		
		groupLineContent = groupLineContent.replaceAll("(，|、|-|—|/|；|：| |\\.|	| |．|。|－)*\r\n(，|、|-|—|/|；|：| |\\.|	| |．|。|－)*", "\r\n");
		
		// 替换日期开头的行 TODO 可能无用
		if (RegexUtils.regexFind(PlayAnalysisConstants.REGEX_DATE, groupLineContent)) {
			groupLineContent = groupLineContent.replaceAll(" +", "");
		}

		return groupLineContent;
	}
	
	
	/**
	 * 统一代表人物、场景的关键字
	 * @param groupLineContent	标题包
	 * @return
	 */
	private static String replaceKeywords(String groupLineContent) {
		String regexSpecial = PlayAnalysisConstants.REGEX_SPECIAL;
		String regexSite = PlayAnalysisConstants.REGEX_SITE;
		String regexAtmosphere = PlayAnalysisConstants.REGEX_ATMOSPHERE;
		String regexFigure = PlayAnalysisConstants.REGEX_FIGURE;
		String notNeedCheckRegexSpace = PlayAnalysisConstants.NOT_NEED_CHECK_REGEX_SPACE;
		String needCheckRegexSpace = PlayAnalysisConstants.NEED_CHECK_REGEX_SPACE;
		String notNeedCheckRegexAtmosphere = PlayAnalysisConstants.NOT_NEED_CHECK_REGEX_ATMOSPHERE;
		String needCheckRegexAtmosphere = PlayAnalysisConstants.NEED_CHECK_REGEX_ATMOSPHERE;

		String temGroupLineContent = groupLineContent.replaceAll(" +：", "：");
		if (RegexUtils.regexFind(regexSpecial, temGroupLineContent)) {// 含有"闪回|回忆|梦境|空镜"等关键字
			String temGroupLineContent2 = temGroupLineContent.replaceAll(
					"（*(" + regexSpecial + ")：*）*", "").trim();
			if (RegexUtils.regexFind("^\\d+", temGroupLineContent2)) {// 去掉关键字后以数字开头，可能成为标题
				if (RegexUtils.regexFind(regexSite, temGroupLineContent2)
						|| RegexUtils.regexFind(regexAtmosphere,
								temGroupLineContent2)) {// 去掉关键字后包含氛围或内外景关键字，确定为标题
					temGroupLineContent = temGroupLineContent2;
				}
			}
		}

		//处理人物关键字
		if (RegexUtils.regexFind(regexFigure, temGroupLineContent)) {
			String temGroupLineContent_before = "";
			String temGroupLineContent_after = temGroupLineContent;
			int index = 0;
			Matcher m = Pattern.compile(regexFigure).matcher(temGroupLineContent_after);
			while (m.find()) {
				index = temGroupLineContent_after.indexOf(m.group());
				temGroupLineContent_before += temGroupLineContent_after.substring(0, index);
				temGroupLineContent_after = temGroupLineContent_after.substring(index + m.group().length());
				if (index > 0) {
					String before = temGroupLineContent_before.substring(
							temGroupLineContent_before.length() - 1,
							temGroupLineContent_before.length());
					
					//如果人物关键字的前一位时汉字，则不处理，否则，把人物关键字用换行符替代
					if (RegexUtils.regexFind("[\u4e00-\u9fa5]", before)) {
						temGroupLineContent_before += m.group();
					} else if (RegexUtils.regexFind("\n", before)) {// 前一位是换行符号，替换为空,即不拼接该人物关键字
						temGroupLineContent_before += "";
					} 
					else {
						temGroupLineContent_before += m.group().replaceFirst(m.group(), "角色：");
//						temGroupLineContent_before += "\r\n";
					}
				}
			}
			temGroupLineContent = temGroupLineContent_before + temGroupLineContent_after;
		}
		
		//处理气氛关键字
		if (RegexUtils.regexFind(notNeedCheckRegexAtmosphere, temGroupLineContent)) {
			temGroupLineContent = temGroupLineContent.replaceAll(notNeedCheckRegexAtmosphere, " ");
		}
		if (RegexUtils.regexFind(needCheckRegexAtmosphere, temGroupLineContent)) {
			String temGroupLineContent_before = "";
			String temGroupLineContent_after = temGroupLineContent;
			int index = 0;
			Matcher m = Pattern.compile(needCheckRegexAtmosphere).matcher(temGroupLineContent_after);
			while (m.find()) {
				index = temGroupLineContent_after.indexOf(m.group());
				if (index > 0) {
					temGroupLineContent_before += temGroupLineContent_after.substring(0, index);
					temGroupLineContent_after = temGroupLineContent_after.substring(index + 1);
					String before = temGroupLineContent_before.substring(
							temGroupLineContent_before.length() - 1,
							temGroupLineContent_before.length());
					
					//如果前一个字符不是汉字，则把气氛关键字替换为空格，否则保持原样
					if (RegexUtils.regexFind("[^\u4e00-\u9fa5]", before)) {
						temGroupLineContent_before += m.group().replaceFirst(m.group(), " ");
					} else {
						temGroupLineContent_before += m.group();
					}
				}
			}
			temGroupLineContent = temGroupLineContent_before + temGroupLineContent_after;
		}
		
		
		

		//处理场景关键字
		if (RegexUtils.regexFind(notNeedCheckRegexSpace, temGroupLineContent)) {
			temGroupLineContent = temGroupLineContent.replaceAll(notNeedCheckRegexSpace, " ");
		}
		if (RegexUtils.regexFind(needCheckRegexSpace, temGroupLineContent)) {
			String temGroupLineContent_before = "";
			String temGroupLineContent_after = temGroupLineContent;
			int index = 0;
			Matcher m = Pattern.compile(needCheckRegexSpace).matcher(temGroupLineContent_after);
			while (m.find()) {
				index = temGroupLineContent_after.indexOf(m.group());
				if (index > 0) {
					temGroupLineContent_before += temGroupLineContent_after.substring(0, index);
					temGroupLineContent_after = temGroupLineContent_after.substring(index + 1);
					String before = temGroupLineContent_before.substring(
							temGroupLineContent_before.length() - 1,
							temGroupLineContent_before.length());
					
					//如果前一个字符不是汉字，则把场景关键字替换为空格，否则保持原样
					if (RegexUtils.regexFind("[^\u4e00-\u9fa5]", before)) {
						temGroupLineContent_before += m.group().replaceFirst(m.group(), " ");
					} else {
						temGroupLineContent_before += m.group();
					}
				}
			}
			temGroupLineContent = temGroupLineContent_before + temGroupLineContent_after;
		}

		return temGroupLineContent;
	}
	
	/**
	 * 获取字符串表示的中文场次信息，示例：
	 * “三十”：场次为“30”
	 * “三十扉页”：场次为“30扉页”
	 * “三十ins”： 场次为“30ins”
	 * “三十2ins”：不识别为场次
	 * “三2十”：不识别为场次
	 * “这三十”：不识别为场次
	 * @param str
	 * @return 如果返回为空串，表示不识别的场次
	 */
	public static String genCNViewNo(String str) {
		boolean upperViewNo = RegexUtils.regexFind("^(零|一|二|三|四|五|六|七|八|九|十)+.*", str);
		boolean lowerViewNo = RegexUtils.regexFind("^\\d+.*", str);
		
		String result = "";
		if (lowerViewNo) {
			result = str;
		}
		if (upperViewNo) {
			//解决(三六九：你好)之类，数值是人物名字的情况
			if(RegexUtils.regexFind("^(零|一|二|三|四|五|六|七|八|九|十)+[:|：|：]+",str)){
				return str;
			}
			
			String [] strArray = str.split("");
			List<String> numberCNList = Arrays.asList("零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "百", "千");
			
			String numberCn = "";
			String restStr = "";
			for (int i = 0; i < strArray.length; i++) {
				if(StringUtils.isBlank(strArray[i])){
					continue;
				}
				if (numberCNList.contains(strArray[i])) {
					if (i == strArray.length - 1) {
						numberCn = str;
						restStr = "";
					}
					continue;
				} else {
					numberCn = str.substring(0, i-1);
					restStr = str.substring(i-1, str.length());
					break;
				}
			}
			
			//如果碰到“三十2”这种类型的场次，标识为不识别的场次
			if (!StringUtils.isBlank(restStr) && RegexUtils.regexFind("^\\d+.*", restStr)) {
				restStr = "";
				numberCn = "";
			}
			
			if (!StringUtils.isBlank(numberCn)) {
				result = MyStringUtils.toLowerNumber(numberCn) + restStr;
			}
		}
		
		return result;
	}
}
