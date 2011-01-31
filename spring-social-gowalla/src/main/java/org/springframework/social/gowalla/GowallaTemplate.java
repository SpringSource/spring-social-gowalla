/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.gowalla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.social.intercept.ExtendedRestTemplate;
import org.springframework.social.oauth2.OAuth2RequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

/**
 * <p>
 * The central class for interacting with TripIt.
 * </p>
 * 
 * <p>
 * TripIt operations require OAuth 1 authentication. Therefore TripIt template
 * must be given the minimal amount of information required to sign requests to
 * the TripIt API with an OAuth <code>Authorization</code> header.
 * </p>
 * 
 * @author Craig Walls
 */
public class GowallaTemplate implements GowallaOperations {

	RestOperations restOperations;

	/**
	 * Constructs a GowallaTemplate with the minimal amount of information
	 * required to sign requests with an OAuth <code>Authorization</code>
	 * header.
	 * 
	 * @param accessToken
	 *            An access token granted to the application after OAuth
	 *            authentication.
	 */
	public GowallaTemplate(String accessToken) {
		ExtendedRestTemplate restTemplate = new ExtendedRestTemplate();
		restTemplate.addInterceptor(OAuth2RequestInterceptor.draft8(accessToken));
		restOperations = restTemplate;
	}

	public String getProfileId() {
		HttpEntity<?> requestEntity = new HttpEntity<String>(buildBaseHeaders());
		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> response = restOperations.exchange(PROFILE_URL, HttpMethod.GET, requestEntity, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, ?> profileInfo = response.getBody();
		return String.valueOf(profileInfo.get("username"));
	}

	public String getProfileUrl() {
		return "http://www.gowalla.com/users/" + getProfileId();
	}
	
	public List<Checkin> getTopCheckins(String userId) {
		HttpEntity<?> requestEntity = new HttpEntity<String>(buildBaseHeaders());
		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> response = restOperations.exchange(TOP_CHECKINS_URL, HttpMethod.GET, requestEntity,
				Map.class, userId);

		@SuppressWarnings("unchecked")
		List<Map<String, ?>> checkinsList = (List<Map<String, ?>>) response.getBody().get("top_spots");
		List<Checkin> checkins = new ArrayList<Checkin>();
		for (Map<String, ?> checkinMap : checkinsList) {
			checkins.add(new Checkin((String) checkinMap.get("name"), (Integer) checkinMap.get("user_checkins_count")));
		}
		return checkins;
	}

	private MultiValueMap<String, String> buildBaseHeaders() {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Accept", "application/json");
		return headers;
	}

	static final String PROFILE_URL = "https://api.gowalla.com/users/me";
	static final String TOP_CHECKINS_URL = "https://api.gowalla.com/users/{userId}/top_spots";
}
