package org.bambrikii.md.converter;

import java.net.URL;

/**
 * Created by Alexander Arakelyan on 23.10.16 22:12.
 */
public class ConfluenceUrlBuilder {
	private final URL hostUrl;

	public ConfluenceUrlBuilder(URL hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String createLogonUrl() {
		return hostUrl.getProtocol() + "://" + hostUrl.getAuthority() + "/login.action?logout=true";
	}

	public String createDisplayUrl(String space) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/display/" + space;
	}

	public String createPageUrl(String pageId) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/pages/viewpage.action?pageId=" + pageId;
	}

	public String createAttachmentUrl(String id, String name) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/download/attachments/" + id + "/" + name;
	}

	public URL getBaseUrl() {
		return hostUrl;
	}
}
