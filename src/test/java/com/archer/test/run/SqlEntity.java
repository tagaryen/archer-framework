package com.archer.test.run;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.archer.framework.datasource.annotation.Entity;

@Entity(tableName = "alice_cat")
public class SqlEntity {
	
	private Long id;
	
	private Long isCat;

	private Double tailLength;

	private Integer color;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIsCat() {
		return isCat;
	}

	public void setIsCat(Long isCat) {
		this.isCat = isCat;
	}

	public Double getTailLength() {
		return tailLength;
	}

	public void setTailLength(Double tailLength) {
		this.tailLength = tailLength;
	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
	}

}
