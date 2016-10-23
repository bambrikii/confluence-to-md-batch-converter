package org.bambrikii.md.converter;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alexander Arakelyan on 22.10.16 13:23.
 */
public class Crawler {
	private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
	public static final String ATTACHMENTS_PATTERN = "\\/download\\/attachments\\/([0-9]+)\\/([^\\?]+)\\?";
	public static final String PAGES_PATTERN = "/pages/viewpage.action\\?pageId=([0-9]+)[^\\>]+\\>([^\\<]+)\\<";
	private URL hostUrl;

	private Set<String> processedLinks = new ConcurrentHashSet<>();
	private WebClient client;
	private Downloader downloader;
	private final String dstDir;

	public Crawler(String hostUrl, String dstDir) throws IOException {
		this.hostUrl = new URL(hostUrl);
		this.dstDir = dstDir;
	}

	public void login(String username, String password) throws IOException {
		URL logonUrl = new URL(hostUrl.getProtocol() + "://" + hostUrl.getAuthority() + "/login.action?logout=true");
		client = new WebClient();
		downloader = new Downloader(client, hostUrl, dstDir);
		client.getOptions().setThrowExceptionOnScriptError(false);
		HtmlPage page = client.getPage(logonUrl);
		HtmlForm loginForm = page.getFormByName("loginform");
		HtmlInput uname = loginForm.getInputByName("os_username");
		uname.setValueAttribute(username);
		HtmlInput pwd = loginForm.getInputByName("os_password");
		pwd.setValueAttribute(password);
		HtmlInput btn = loginForm.getInputByName("login");
		btn.click();
	}

	private String createDisplayUrl(String space) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/display/" + space;
	}

	public void downloadSpace(String space) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		if (!processedLinks.contains(space)) {
			String displayLink = createDisplayUrl(space);
			PageLinks links = downloader.download(displayLink);
			processedLinks.add(space);
			download(links);
		}
	}

	private void download(PageLinks links) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		downloadPages(links.getPageLinks());
		downloadSpaces(links.getSpaceLinks());
	}

	private void downloadSpaces(Map<String, String> spaceLinks) throws ParserConfigurationException, TransformerException, SAXException, IOException {
		processedLinks.forEach(link -> spaceLinks.remove(link));
		if (spaceLinks.size() > 0) {
			for (Map.Entry<String, String> entry : spaceLinks.entrySet()) {
				String space = entry.getKey();
				processedLinks.add(space);
				downloadSpace(space);
			}
		}
	}

	public void downloadPage(String pageId) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		String pageUrl = createPageUrl(pageId);
		if (!processedLinks.contains(pageId)) {
			try {
				PageLinks links = downloader.download(pageUrl);
				processedLinks.add(pageId);
				download(links);
			} catch (FailingHttpStatusCodeException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private void downloadPages(Map<String, String> childPages) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		processedLinks.forEach((key) -> childPages.remove(key));
		if (childPages.size() > 0) {
			for (Map.Entry<String, String> entry : childPages.entrySet()) {
				String pageId = entry.getKey();
				downloadPage(pageId);
			}
		}
	}

	private String createPageUrl(String pageId) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/pages/viewpage.action?pageId=" + pageId;
	}

}
