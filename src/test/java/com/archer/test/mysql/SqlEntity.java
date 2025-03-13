package com.archer.test.mysql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SqlEntity {
	
	private Long id;
	
	private Long columnA;

	private Integer columnB;

	private Float columnC;

	private Double columnD;

	private String columnE;

	private String columnF;

	private LocalDate columnG;

	private LocalTime columnH;

	private LocalDateTime columnI;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getColumnA() {
		return columnA;
	}

	public void setColumnA(Long columnA) {
		this.columnA = columnA;
	}

	public Integer getColumnB() {
		return columnB;
	}

	public void setColumnB(Integer columnB) {
		this.columnB = columnB;
	}

	public Float getColumnC() {
		return columnC;
	}

	public void setColumnC(Float columnC) {
		this.columnC = columnC;
	}

	public Double getColumnD() {
		return columnD;
	}

	public void setColumnD(Double columnD) {
		this.columnD = columnD;
	}

	public String getColumnE() {
		return columnE;
	}

	public void setColumnE(String columnE) {
		this.columnE = columnE;
	}

	public String getColumnF() {
		return columnF;
	}

	public void setColumnF(String columnF) {
		this.columnF = columnF;
	}

	public LocalDate getColumnG() {
		return columnG;
	}

	public void setColumnG(LocalDate columnG) {
		this.columnG = columnG;
	}

	public LocalTime getColumnH() {
		return columnH;
	}

	public void setColumnH(LocalTime columnH) {
		this.columnH = columnH;
	}

	public LocalDateTime getColumnI() {
		return columnI;
	}

	public void setColumnI(LocalDateTime columnI) {
		this.columnI = columnI;
	}

}
