package com.xiaotu.play.dto;

/**
 * 剧本标题中的元素
 * @author xuchangjian 2017-6-15上午11:55:26
 */
public class ScripteleDto {
	
	private String id;
	
	/**
	 * 名称
	 */
	private String name;
	
	/**
	 * 元素示例
	 */
	private String example;
	
	/**
	 * 元素对应的正则
	 */
	private String regex;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExample() {
		return this.example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getRegex() {
		return this.regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
}
