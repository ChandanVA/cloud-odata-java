package com.sap.core.odata.processor.fit.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_MATERIAL")
public class Material {

	public Material() {
		//No arg Constructor
	}
	
	public Material(String materialName, String typeCode, double price,
			String measurementUnit) {
		super();
		this.materialName = materialName;
		this.typeCode = typeCode;
		this.price = price;
		this.measurementUnit = measurementUnit;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MATERIAL_ID")
	private long materialId;
	
	@Column(name = "MATERIAL_NAME")
	private String materialName;
	
	@Column(name = "TYPE_CODE")
	private String typeCode;
	
	@Column(name = "PRICE")
	private double price;
	
	@Column(name = "MEASUREMENT_UNIT")
	private String measurementUnit;
	
	@ManyToMany
	private List<Store> stores = new ArrayList<Store>();

	public long getMaterialId() {
		return materialId;
	}

	public void setMaterialId(long materialId) {
		this.materialId = materialId;
	}

	public String getMaterialName() {
		return materialName;
	}

	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getMeasurementUnit() {
		return measurementUnit;
	}

	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}	
	
	public List<Store> getStores() {
		return stores;
	}

	public void setStores(List<Store> stores) {
		this.stores = stores;
		Iterator<Store> itr = stores.iterator();
		while(itr.hasNext()) {
			itr.next().getMaterials().add(this);
		}
	}
}
