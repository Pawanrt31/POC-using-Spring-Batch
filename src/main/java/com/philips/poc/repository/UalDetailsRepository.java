package com.philips.poc.repository;

import com.philips.poc.entity.DistributionRule;
import com.philips.poc.entity.UalDetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UalDetailsRepository extends PagingAndSortingRepository<UalDetails, Long>{
	
	Page<UalDetails> findAllByDistributionRule(DistributionRule distributionRule, Pageable pageable);
	
	@Query("select Count(DISTINCT ualDetails.id) from UalDetails ualDetails inner join ualDetails.distributionRule distributionRule where distributionRule.id = :ruleId ")
    int totalRecordsOfUalDetails(@Param("ruleId") int ruleId);

}
