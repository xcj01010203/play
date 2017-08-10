package com.xiaotu.play.dto;

import java.util.List;

/**
 * 场景信息
 * @author xuchangjian 2017-6-19下午1:54:15
 */
public class ViewInfoDto {

	/**
	 * 集次
	 */
	private Integer seriesNo;
	
	/**
	 * 场次
	 */
	private String viewNo;
	
	/**
	 * 季节
	 */
	private Integer season;
	
	/**
	 * 气氛
	 */
	private String atmosphere;
	
	/**
	 * 内外景
	 */
	private String site;
	
	/**
	 * 每一场的标题
	 */
	private String title;
	
	/**
	 * 内容
	 */
	private String content;
	
	/**
	 * 主要演员
	 */
	private List<String> majorRoleNameList;
	
	/**
	 * 主场景
	 */
	private String firstLocation;
	
	/**
	 * 次场景
	 */
	private String secondLocation;
	
	/**
	 * 三级场景
	 */
	private String thirdLocation;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((seriesNo == null) ? 0 : seriesNo.hashCode());
		result = prime * result + ((viewNo == null) ? 0 : viewNo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		ViewInfoDto playInfo = (ViewInfoDto) o;
		if (playInfo.getSeriesNo().equals(this.seriesNo) && playInfo.getViewNo().equals(this.viewNo)) {
			return true;
		} else {
			return false;
		}
	}

	public Integer getSeriesNo() {
		return this.seriesNo;
	}

	public void setSeriesNo(Integer seriesNo) {
		this.seriesNo = seriesNo;
	}

	public String getViewNo() {
		return this.viewNo;
	}

	public void setViewNo(String viewNo) {
		this.viewNo = viewNo;
	}

	public Integer getSeason() {
		return this.season;
	}

	public void setSeason(Integer season) {
		this.season = season;
	}

	public String getAtmosphere() {
		return this.atmosphere;
	}

	public void setAtmosphere(String atmosphere) {
		this.atmosphere = atmosphere;
	}

	public String getSite() {
		return this.site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getMajorRoleNameList() {
		return this.majorRoleNameList;
	}

	public void setMajorRoleNameList(List<String> majorRoleNameList) {
		this.majorRoleNameList = majorRoleNameList;
	}

	public String getFirstLocation() {
		return this.firstLocation;
	}

	public void setFirstLocation(String firstLocation) {
		this.firstLocation = firstLocation;
	}

	public String getSecondLocation() {
		return this.secondLocation;
	}

	public void setSecondLocation(String secondLocation) {
		this.secondLocation = secondLocation;
	}

	public String getThirdLocation() {
		return this.thirdLocation;
	}

	public void setThirdLocation(String thirdLocation) {
		this.thirdLocation = thirdLocation;
	}
}
