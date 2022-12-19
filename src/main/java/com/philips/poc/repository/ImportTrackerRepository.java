package com.philips.poc.repository;

import com.philips.poc.entity.ImportTracker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportTrackerRepository extends JpaRepository<ImportTracker, String>{
	

	ImportTracker findByRuleNameAndRegion(String ruleName, String region);
}
