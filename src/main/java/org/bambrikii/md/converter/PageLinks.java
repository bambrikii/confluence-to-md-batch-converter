package org.bambrikii.md.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander Arakelyan on 23.10.16 19:58.
 */
public class PageLinks {
	private final Map<String, String> pageLinks;
	private final Map<String, String> spaceLinks;

	public PageLinks() {
		pageLinks = new HashMap<>();
		spaceLinks = new HashMap<>();
	}

	public PageLinks(Map<String, String> pageLinks, Map<String, String> spaceLinks) {
		this.pageLinks = pageLinks;
		this.spaceLinks = spaceLinks;
	}

	public Map<String, String> getPageLinks() {
		return pageLinks;
	}

	public Map<String, String> getSpaceLinks() {
		return spaceLinks;
	}
}
