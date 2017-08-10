package com.xiaotu.play.util;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

public class PDFUtils {

	public static String readPDFFile(String fileStorePath) throws IOException,
			XmlException, OpenXML4JException {
		String docText = "";
		try {
			FileInputStream fis = new FileInputStream(fileStorePath);
			COSDocument cosDoc = null;
			PDFParser parser = new PDFParser(fis);
			parser.parse();
			cosDoc = parser.getDocument();
			PDFTextStripper stripper = new PDFTextStripper();
			docText = stripper.getText(new PDDocument(cosDoc));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return docText;
	}
}
