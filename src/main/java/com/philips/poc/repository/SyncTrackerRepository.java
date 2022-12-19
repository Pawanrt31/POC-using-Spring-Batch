package com.philips.poc.repository;

import com.philips.poc.entity.SyncTracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncTrackerRepository extends JpaRepository<SyncTracker, Integer>{

	SyncTracker findByRuleName(String ruleName);

}
