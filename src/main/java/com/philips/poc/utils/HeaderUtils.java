package com.philips.poc.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HeaderUtils {

	/**
	 * @param <TP>
	 * @param headermap
	 * @param payload
	 * @return
	 */
	public <P> HttpEntity<P> createHttpEntityWithHeader(Map<String, String> headermap, P payload) {
		final HttpHeaders headers = new HttpHeaders();
		for (final Map.Entry<String, String> header : headermap.entrySet()) {
			headers.add(header.getKey(), header.getValue());
		}
		return new HttpEntity<P>(payload, headers);

	}

	/**
	 * Method to set Headers
	 * 
	 * @return
	 */
	public Map<String, String> setHeaders() {
		final Map<String, String> headerMap = new HashMap<>();
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Api-Version", "1.0");
		return headerMap;
	}
}
