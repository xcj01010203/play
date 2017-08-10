package com.xiaotu.play.dto;

import java.util.List;

public class PlayTitleMsgDto {

	/**
	 * 剧本名称
	 */
	private String playName;
	
	/**
	 * 标题提示信息列表，多条以\r\n隔开
	 */
	private String titleInfoMsgs;
	
	/**
	 * 标题警告信息列表，多条以\r\n隔开
	 */
	private String titleWarnMsgs;
	
	/**
	 * 标题错误信息列表，多条以\r\n隔开
	 */
	private String titleErrorMsgs;
	
	/**
	 * 匹配规则
	 */
	private String scriptRule;

	public String getScriptRule() {
		return scriptRule;
	}

	public void setScriptRule(String scriptRule) {
		this.scriptRule = scriptRule;
	}

	public String getPlayName() {
		return this.playName;
	}

	public void setPlayName(String playName) {
		this.playName = playName;
	}

	public String getTitleInfoMsgs() {
		return this.titleInfoMsgs;
	}

	public void setTitleInfoMsgs(String titleInfoMsgs) {
		this.titleInfoMsgs = titleInfoMsgs;
	}

	public String getTitleWarnMsgs() {
		return this.titleWarnMsgs;
	}

	public void setTitleWarnMsgs(String titleWarnMsgs) {
		this.titleWarnMsgs = titleWarnMsgs;
	}

	public String getTitleErrorMsgs() {
		return this.titleErrorMsgs;
	}

	public void setTitleErrorMsgs(String titleErrorMsgs) {
		this.titleErrorMsgs = titleErrorMsgs;
	}
}
