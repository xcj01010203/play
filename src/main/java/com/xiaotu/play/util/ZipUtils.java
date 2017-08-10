package com.xiaotu.play.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ZipUtils.class);

	/**
	 * 通过控制台解压缩文件
	 * @param rar
	 * @param unRarPath
	 * @param winrarInstallPath
	 * @return
	 */
	public static List<String> unRarByCmd(File rar, String unRarPath, String winrarInstallPath) {
		Runtime rt = Runtime.getRuntime();
		if (!new File(unRarPath).exists()) {
			new File(unRarPath).mkdirs();
		}

		try {
			String OS = System.getProperty("os.name").toLowerCase();
			String cmd = "";
			if (OS.contains("windows")) {
				DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
				cmd = winrarInstallPath + " x -o- " + rar.getPath() + " " + unRarPath;
			}

			if (OS.contains("linux")) {
				if (rar.getAbsolutePath().toLowerCase().contains("rar")) {
					cmd = "rar x -o- " + rar.getPath() + " " + unRarPath;
				} else {
					cmd = "unzip -o " + rar.getPath() + " -d " + unRarPath;
				}
			}

			if (!StringUtils.isBlank(cmd)) {
				Process p = rt.exec(cmd);
				p.waitFor();
			}

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		}

		List<String> fileList = new ArrayList<String>();
		fileList = traverseFolder(unRarPath, fileList);
		return fileList;
	}

	/**
	 * 把文件放入对应的文件夹中
	 * @param path
	 * @param fileList
	 * @return
	 */
	public static List<String> traverseFolder(String path, List<String> fileList) {
		File file = new File(path);
		if (file.exists()) {
			File[] files = file.listFiles();
			if (files.length == 0) {
				return fileList;
			} else {
				for (File file2 : files) {
					if (RegexUtils.regexFind("^(\\.|_)", file2.getName())) {
						continue;
					}
					if (file2.isDirectory()) {
						traverseFolder(file2.getAbsolutePath(), fileList);
					} else {
						fileList.add(file2.getAbsolutePath());
					}
				}
			}
		}

		return fileList;
	}
}
