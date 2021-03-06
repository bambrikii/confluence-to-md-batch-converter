package org.bambrikii.md.converter.impl;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.bambrikii.md.converter.ConfluenceUrlBuilder;
import org.bambrikii.md.converter.PageLinks;
import org.bambrikii.md.converter.api.Downloadable;
import org.bambrikii.md.converter.api.Persistable;
import org.bambrikii.md.converter.api.Transformable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bambrikii.md.converter.impl.ViewStorageTransformer.CHARSET_NAME;

/**
 * Created by Alexander Arakelyan on 23.10.16 21:22.
 */
public class Downloader implements Downloadable {
	public static final String ATTACHMENTS_PATTERN = "\\/download\\/attachments\\/([0-9]+)\\/([^\\?]+)\\?";
	public static final String PAGES_PATTERN = "/pages/viewpage.action\\?pageId=([0-9]+)[^\\>]+\\>([^\\<]+)\\<";
	private static final String SPACE_PATTERN = "\\/display\\/([^\\>\"]+).*\\>([^\\<]+)\\<";

	private static final Logger logger = LoggerFactory.getLogger(Downloader.class);
	private WebClient client;
	private final Transformable transformable;
	private final Persistable persistor;
	private final ConfluenceUrlBuilder urlBuilder;

	public Downloader(Transformable transformable, Persistable persistor, ConfluenceUrlBuilder urlBuilder) {
		this.transformable = transformable;
		this.persistor = persistor;
		this.urlBuilder = urlBuilder;
	}

	public void setClient(WebClient client) {
		this.client = client;
		this.transformable.setWebClient(client);
	}

	public PageLinks download(String url) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		HtmlPage page1 = client.getPage(url);
		String pageContent = page1.getWebResponse().getContentAsString();
		logger.info(pageContent);

		try {
			// Download page using ViewStorage plugin
			String viewStorageContent = transformable.retrieveContent(page1);

			// Transform the page to MD format
			String transformedStorageContent = transformable.transformContent(viewStorageContent);
			logger.info(transformedStorageContent);
			persistor.persistPage(page1.getTitleText() + ".md", transformedStorageContent);
		} catch (ElementNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		}

		// List the files and download them
		Map<String, String> spaceLinks1 = parseOutSpaceLinks(pageContent);
		Map<String, String> pageLinks1 = parseOutPageLinks(pageContent);
		Map<String, String> attachmentLinks1 = parseOutAttachments(pageContent);
		PageLinks pageLinks = new PageLinks(pageLinks1, spaceLinks1, attachmentLinks1);
		return pageLinks;
	}


	private Map<String, String> parseOutPageLinks(String pageContent) {
		// List the pages and return them
		return parseOutLinksByPattern(pageContent, PAGES_PATTERN);
	}

	private Map<String, String> parseOutLinksByPattern(String pageContent, String pattern1) {
		Pattern pagesPattern = Pattern.compile(pattern1);
		Matcher pagesMatcher = pagesPattern.matcher(pageContent);
		Map<String, String> links = new LinkedHashMap<>();
		while (pagesMatcher.find()) {
			links.put(pagesMatcher.group(1), pagesMatcher.group(2));
		}
		return links;
	}

	private Map<String, String> parseOutSpaceLinks(String pageContent) {
		// List the spaces and return them
		return parseOutLinksByPattern(pageContent, SPACE_PATTERN);
	}

	public void downloadAttachments(Map<String, String> attachmentLinks) throws IOException {
		for (Entry<String, String> entry : attachmentLinks.entrySet()) {
			String id = entry.getKey();
			String name = entry.getValue();
			String attachmentUrl = urlBuilder.createAttachmentUrl(id, name);

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

	private Map<String, String> parseOutAttachments(String pageContent) throws IOException {
		return parseOutLinksByPattern(pageContent, ATTACHMENTS_PATTERN);
	}

}
