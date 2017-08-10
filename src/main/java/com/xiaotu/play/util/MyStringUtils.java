package com.xiaotu.play.util;


public class MyStringUtils {

	/**
	 * 把英文符号转为中文符号
	 * @param separator
	 * @return
	 */
	public static String EnToCHSeparator(String separator) {
		String changedStr = separator
				.replaceAll(":", "：")
				.replaceAll(",", "，")
				.replaceAll(";", "；")
				.replaceAll("\\)", "）")
				.replaceAll("\\(", "（");
		return changedStr;
	}
	
	/**
	 * 大写数字转换为小写
	 * @param str
	 * @return
	 */
	public static int toLowerNumber(String str) {
		String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
		String[] units = {"十", "百", "千"};
		
		String[] array = str.trim().split("");
		
		int result = 0;
		
		int preNumber = 1;
		for (int i = 0; i < array.length; i++) {
			String a = array[i];
			if (org.apache.commons.lang.StringUtils.isBlank(a)) {
				continue;
			}
			
			for (int j = 0; j < numbers.length; j++) {
				if (a.equals(numbers[j])) {
					if (i == array.length - 1) {
						result += j + 1;
					} else {
						preNumber = j + 1;
					}
					break;
				}
			}
			for (int k = 0; k < units.length; k++) {
				if (a.equals(units[k])) {
					result += (preNumber *= Math.pow(10, (k+1)));
					break;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 全角转半角
	 * @param input
	 * @return 半角字符串
	 */
	public static String ToDBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);
			}
		}
		String returnString = new String(c);
		return returnString;
	}
}
