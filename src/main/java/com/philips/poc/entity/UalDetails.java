package com.philips.poc.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


import lombok.experimental.Accessors;


@Entity
@Table(name = "tbl_ual_details")
public class UalDetails {

	
    private long id;
	private DistributionRule distributionRule;
    private String equipmentNumber;
    private String status;
    private String createdBy;
    private Date ualCreationTime;

    public UalDetails() {
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
    public long getId() {
    	return this.id;
    }
    
    public void setId(long id) {
    	this.id = id;
    }
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rule_id")
    public DistributionRule getDistributionRule() {
        return this.distributionRule;
    }

    public void setDistributionRule(DistributionRule distributionRule) {
        this.distributionRule = distributionRule;
    }

    
    @Column(name = "equipment_number", nullable = false, length = 100)
    public String getEquipmentNumber() {
        return this.equipmentNumber;
    }

    public void setEquipmentNumber(String equipmentNumber) {
        this.equipmentNumber = equipmentNumber;
    }

    @Column(name = "status", nullable = false, length = 100)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "created_by", nullable = false, length = 500)
    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false, length = 35)
    public Date getPackageCreationTime() {
        return this.ualCreationTime;
    }

    public void setPackageCreationTime(Date ualCreationTime) {
        this.ualCreationTime = ualCreationTime;
    }
}
