package com.philips.poc.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.philips.poc.entity.DistributionRule;
import com.philips.poc.entity.UalDetails;

@Repository
public interface PagingRepository extends PagingAndSortingRepository<UalDetails, Long> {
	
	Page<UalDetails> findAllByDistributionRule(DistributionRule distributionRule, Pageable pageable);
	
	List<UalDetails> findAllByDistributionRule(DistributionRule distributionRule);
	
	

}
