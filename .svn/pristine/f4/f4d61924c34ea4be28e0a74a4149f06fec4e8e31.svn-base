package com.switek.netseed.server.bean;

import java.util.ArrayList;
import java.util.List;

public class DeviceTypeDef {

	public DeviceTypeDef() {
		// TODO Auto-generated constructor stub
	}

	int deviceType;
	String name = "";
	String logoUrl = "";
	List<Brand> brands = new ArrayList<>();

	/**
	 * @return the brands
	 */
	public List<Brand> getBrands() {
		return brands;
	}
	
	public Brand getBrand(String brandCode){
		for (Brand brand : brands) {
			if (brand.getBrandCode().equalsIgnoreCase(brandCode)){
				return brand;
			}
		}
		
		return null;
	}

	/**
	 * @param brands the brands to set
	 */
	public void setBrands(List<Brand> brands) {
		this.brands = brands;
	}
	
	public void addBrand(Brand brand){
		this.brands.add(brand);
	}

	/**
	 * @return the deviceType
	 */
	public int getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType
	 *            the deviceType to set
	 */
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the logoUrl
	 */
	public String getLogoUrl() {
		return logoUrl;
	}

	/**
	 * @param logoUrl
	 *            the logoUrl to set
	 */
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

}
