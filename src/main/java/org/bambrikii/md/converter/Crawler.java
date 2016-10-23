package org.bambrikii.md.converter;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bambrikii.md.converter.ViewStorageTransformer.CHARSET_NAME;
import static org.bambrikii.md.converter.ViewStorageTransformer.transformViewStorage;

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
	private ViewStorageTransformer viewStorageTransformer;
	private MdPersistor persistor;

	public Crawler(String hostUrl, String dstDir) throws IOException {
		this.persistor = new MdPersistor(dstDir);
		this.hostUrl = new URL(hostUrl);
	}

	public void login(String username, String password) throws IOException {
		URL logonUrl = new URL(hostUrl.getProtocol() + "://" + hostUrl.getAuthority() + "/login.action?logout=true");
		client = new WebClient();
		client.getOptions().setThrowExceptionOnScriptError(false);
		HtmlPage page = client.getPage(logonUrl);
		HtmlForm loginForm = page.getFormByName("loginform");
		HtmlInput uname = loginForm.getInputByName("os_username");
		uname.setValueAttribute(username);
		HtmlInput pwd = loginForm.getInputByName("os_password");
		pwd.setValueAttribute(password);
		HtmlInput btn = loginForm.getInputByName("login");
		btn.click();
		viewStorageTransformer = new ViewStorageTransformer(hostUrl, client);
	}

	private Map<String, String> download(String url) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		HtmlPage page1 = client.getPage(url);
		String pageContent = page1.getWebResponse().getContentAsString();
		logger.info(pageContent);

		try {
			// Fetch PageId
			HtmlHiddenInput treePageId = page1.getElementByName("treePageId");
			String pageId = treePageId.getValueAttribute();

			// Download page using ViewStorage plugin
			String viewStorageContent = viewStorageTransformer.downloadViewStorage(pageId);

			// Transform the page to MD format
			String transformedStorageContent = transformViewStorage(viewStorageContent);
			logger.info(transformedStorageContent);
			persistor.persistPage(page1.getTitleText() + ".md", transformedStorageContent);
		} catch (ElementNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		}

		// List the files and download them
		downloadAttachments(pageContent);

		// List the pages and return them
		Pattern pagesPattern = Pattern.compile(PAGES_PATTERN);
		Matcher pagesMatcher = pagesPattern.matcher(pageContent);
		Map<String, String> links = new LinkedHashMap<>();
		while (pagesMatcher.find()) {
			links.put(pagesMatcher.group(1), pagesMatcher.group(2));
		}
		return links;
	}

	private void downloadAttachments(String pageContent) throws IOException {
		Pattern attachmentsPattern = Pattern.compile(ATTACHMENTS_PATTERN);
		Matcher attachmentsMatcher = attachmentsPattern.matcher(pageContent);
		while (attachmentsMatcher.find()) {
			String id = attachmentsMatcher.group(1);
			String name = attachmentsMatcher.group(2);
			String attachmentUrl = createAttachmentUrl(id, name);

			try {
				Page attachmentPage = client.getPage(attachmentUrl);
				try (InputStream inputStream = attachmentPage.getWebResponse().getContentAsStream()) {
					persistor.persistContent(URLDecoder.decode(name, CHARSET_NAME), inputStream);
				}
			} catch (FailingHttpStatusCodeException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private String createAttachmentUrl(String id, String name) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/download/attachments/" + id + "/" + name;
	}

	private String createDisplayUrl(String space) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/display/" + space;
	}

	public void downloadSpace(String space) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		String displayLink = createDisplayUrl(space);
		Map<String, String> pageLinks = download(displayLink);
		processedLinks.add(displayLink);
		downloadPages2(pageLinks);
	}

	public void downloadPages(Map<String, String> pageLinks) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		for (Map.Entry<String, String> entry : pageLinks.entrySet()) {
			String pageId = entry.getKey();
			downloadPage(pageId);
		}
	}

	public void downloadPage(String pageId) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		String pageUrl = createPageUrl(pageId);
		if (!checkProcessed(pageUrl)) {
			try {
				Map<String, String> childPages = download(pageUrl);
				addProcessed(pageUrl);
				downloadPages2(childPages);
			} catch (FailingHttpStatusCodeException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private void downloadPages2(Map<String, String> childPages) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		processedLinks.forEach(childPages::remove);
		if (childPages.size() > 0) {
			downloadPages(childPages);
		}
	}

	private String createPageUrl(String pageId) {
		return this.hostUrl.getProtocol() + "://" + this.hostUrl.getAuthority() + "/pages/viewpage.action?pageId=" + pageId;
	}

	private void addProcessed(String link) {
		processedLinks.add(link);
	}

	private boolean checkProcessed(String link) {
		return processedLinks.contains(link);
	}

}
