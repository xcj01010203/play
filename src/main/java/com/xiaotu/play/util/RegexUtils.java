package com.xiaotu.play.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

	/**
	 * 判断字符串和指定的正则表达式是否匹配
	 * @param reg 正则表达式
	 * @param str 带匹配的字符串
	 * @return
	 */
	public static boolean regexFind(String reg, String str) {
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}
}
