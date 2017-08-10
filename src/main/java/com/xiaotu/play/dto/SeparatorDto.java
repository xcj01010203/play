package com.xiaotu.play.dto;

/**
 * 剧本标题中的元素
 * @author xuchangjian 2017-6-15下午12:04:47
 */
public class SeparatorDto {
	
	/**
	 * 分隔符的id
	 */
	private String id;
	
	/**
	 * 分隔符名称
	 */
	private String name;
	
	/**
	 * 分隔符描述
	 */
	private String description;
	
	/**
	 * 对应的正则表达式
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

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRegex() {
		return this.regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
}
