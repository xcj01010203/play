package com.xiaotu.play.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlException;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.xiaotu.play.constants.PlayAnalysisConstants;

public class OfficeUtils {
	
	/**
	 * 读取word文件内容
	 * 
	 * @param fileStorePath
	 * @return
	 * @throws IOException
	 * @throws XmlException
	 * @throws OpenXML4JException
	 */
	public static String readWordFile(String fileStorePath, String officeHome) throws IOException,
			XmlException, OpenXML4JException {
		String text = null;
		String ext = fileStorePath.substring(fileStorePath.lastIndexOf("."));
		if (ext.equalsIgnoreCase(".doc")) {
			text = readDocFile(fileStorePath, officeHome);
		} else if (ext.equalsIgnoreCase(".docx")) {
			text = readDocxFile(fileStorePath, officeHome);
		} else {
			throw new IllegalArgumentException("不支持的文件格式");
		}
		return text;
	}
	
	/**
	 * 读取.docx格式的word文件
	 * 
	 * @param filePath
	 * @return
	 * @throws XmlException
	 * @throws OpenXML4JException
	 * @throws IOException
	 */
	public static String readDocxFile(String filePath, String officeHome) throws XmlException,
			OpenXML4JException, IOException {
		String text = "";
		
		try {
			text = readFromConvertedTxt(filePath, officeHome);
		} catch (Exception e) {
			e.printStackTrace();
			StringBuilder builder=new StringBuilder();
			FileInputStream in = new FileInputStream(new File(filePath));
			XWPFDocument xwpfd=new XWPFDocument(in);
			List<XWPFParagraph> xwpfpList=xwpfd.getParagraphs();
			if(xwpfpList!=null && xwpfpList.size()>0){
				XWPFParagraph xwpfp=null;
				String line=null;
				for(int p=0;p<xwpfpList.size();p++){
					xwpfp=xwpfpList.get(p);
					line=xwpfp.getText();
					line=line.replaceAll("", PlayAnalysisConstants.lineSeprator);//将软回车替换为回车符
					builder.append(line);
					builder.append(PlayAnalysisConstants.lineSeprator);
				}
			}
			in.close();
			text=builder.toString();
		}
		
		return text;
	}

	/**
	 * 读取.doc格式的word文件
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String readDocFile(String filePath, String officeHome) throws IOException {
		String text = "";
		
		try {
			text = readFromConvertedTxt(filePath, officeHome);
		} catch (Exception e) {
			e.printStackTrace();
			FileInputStream in = new FileInputStream(new File(filePath));
			WordExtractor extractor = new WordExtractor(in);

			text = extractor.getText();

			text = text.replaceAll("[\\n\\r]", PlayAnalysisConstants.lineSeprator);// 将软回车替换为回车符
			in.close();
		}
		
		return text;
	}
	
	/**
	 * 从转换的txt文件中读取文档内容
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFromConvertedTxt(String filePath, String officeHome) throws FileNotFoundException, IOException {
		String fileNameWithSuffix = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		String fileName = fileNameWithSuffix.substring(0, fileNameWithSuffix.lastIndexOf("."));

		//转换后的文件存储路径
		String convertDir = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1) + "convert" + File.separator;
		
		String convertedFilePath = convertDir + fileName + ".txt";
		word2Format(officeHome, filePath, convertedFilePath);
		
		String text = readTxt(convertedFilePath);
		
		return text;
	}
	
	/**
	 * 将Office文档转换为其他格式. 运行该函数需要用到OpenOffice
	 * 通过指定outputFilePath文件后缀，该方法亦可实现将Office文档转换为TXT、PDF等格式.
	 * 运行该函数需要用到OpenOffice,需要在服务器上安装OpenOffice 文件转换成功与否以异常的形式抛出
	 * 
	 * @param inputFilePath
	 *            源文件,绝对路径. 可以是Office2003-2007全部格式的文档, Office2010的没测试. 包括.doc,
	 *            .docx, .xls, .xlsx, .ppt, .pptx等.
	 * @param outputFilePath
	 *            转换后文件输出路径
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String word2Format(String officeHome, String inputFilePath, String outputFilePath)
			throws FileNotFoundException, IOException {
		

		OfficeManager officeManager = MyOfficeManager.getInstance(officeHome);

		try {
			OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
			File inputFile = new File(inputFilePath);
			if (inputFile.exists()) {// 找不到源文件, 则返回
				File outputFile = new File(outputFilePath);
				if (!outputFile.getParentFile().exists()) { // 假如目标路径不存在, 则新建该路径
					outputFile.getParentFile().mkdirs();
				}
				converter.convert(inputFile, outputFile);
			} else {
				throw new IllegalArgumentException("找不到需要转换的文件");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return outputFilePath;
	}
	
	/**
	 * 读取txt文件内容
	 * 
	 * @throws IOException
	 */
	public static String readTxt(String filepath) throws IOException {
		File file = new File(filepath);
		InputStreamReader read =  new InputStreamReader(new FileInputStream(file), "UTF-8");
		
		BufferedReader reader = new BufferedReader(read);

		String content = "";
		while (reader.ready()) {
			content += reader.readLine() + PlayAnalysisConstants.lineSeprator;
		}
		reader.close();
		
		content = content.replaceAll("", PlayAnalysisConstants.lineSeprator);
		return content;
	}
}
