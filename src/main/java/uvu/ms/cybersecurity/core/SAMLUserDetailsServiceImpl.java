/*
 * Copyright 2018 Vincenzo De Notaris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uvu.ms.cybersecurity.core;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLObjectChildrenList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(SAMLUserDetailsServiceImpl.class);

	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {


		String userID = credential.getNameID().getValue();
		List<Attribute> attributes = credential.getAttributes();
		String attribute = getAttributeAsString("uid", attributes);

		userID = attribute + " - Service Provider (SP) ID :" + userID;
		LOG.info(userID + " is logged in");
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
		authorities.add(authority);

		return new User(userID, "<abc123>", true, true, true, true, authorities);
	}


	public Attribute getAttribute(String name,List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			if (name.equalsIgnoreCase(attribute.getFriendlyName())) {
				return attribute;
			}
		}
		return null;
	}

	public String getAttributeAsString(String name, List<Attribute> attributes) {
		Attribute attribute = getAttribute(name, attributes);
		if (attribute == null) {
			return null;
		}
		List<XMLObject> attributeValues = attribute.getAttributeValues();
		if (attributeValues == null || attributeValues.size() == 0) {
			return null;
		}
		XMLObject xmlValue = attributeValues.iterator().next();
		return getString(xmlValue);
	}

	private String getString(XMLObject xmlValue) {
		if (xmlValue instanceof XSString) {
			return ((XSString) xmlValue).getValue();
		} else if (xmlValue instanceof XSAny) {
			return ((XSAny) xmlValue).getTextContent();
		} else {
			return null;
		}
	}

}
