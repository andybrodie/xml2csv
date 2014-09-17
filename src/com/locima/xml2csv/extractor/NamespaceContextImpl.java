package com.locima.xml2csv.extractor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bare-bones implementation of {@NamespaceContext} to support XPath queries.
 */
public class NamespaceContextImpl implements NamespaceContext {

	private static final Logger LOG = LoggerFactory.getLogger(NameToXPathMappings.class);

	private Map<String, String> prefixUriMap;

	public NamespaceContextImpl(Map<String, String> prefixUriMap) {
		this.prefixUriMap = prefixUriMap;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		String namespaceUri;
		if (this.prefixUriMap.containsKey(prefix)) {
			namespaceUri = this.prefixUriMap.get(prefix);
		} else {
			namespaceUri = XMLConstants.NULL_NS_URI;
		}
		LOG.trace("Resolved \"{}\" to \"{}\"", prefix, namespaceUri);
		return namespaceUri;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (this.prefixUriMap.containsValue(namespaceURI)) {
			for (Map.Entry<String, String> entry : this.prefixUriMap.entrySet()) {
				if (namespaceURI.equals(entry.getValue())) {
					String prefix = entry.getKey();
					LOG.trace("getPrefix returning prefix \"{}\" for URI \"{}\"", prefix, namespaceURI);
					return entry.getKey();
				}
			}
		}
		return null;
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		Set<String> set = new HashSet<String>();
		if (this.prefixUriMap.containsValue(namespaceURI)) {
			for (Map.Entry<String, String> entry : this.prefixUriMap.entrySet()) {
				if (namespaceURI.equals(entry.getValue())) {
					set.add(entry.getKey());
				}
			}
		}
		return set.iterator();
	}

}
