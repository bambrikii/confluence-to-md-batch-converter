package org.bambrikii.md.converter;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import org.bambrikii.md.converter.api.Downloadable;
import org.bambrikii.md.converter.api.Loginable;
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

	private Set<String> processedLinks = new ConcurrentHashSet<>();
	private WebClient client;
	private final Downloadable downloader;
	private final String dstDir;
	private final ConfluenceUrlBuilder urlBuilder;

	public Crawler(String dstDir, Downloadable downloader, ConfluenceUrlBuilder urlBuilder) throws IOException {
		this.urlBuilder = urlBuilder;
		this.dstDir = dstDir;
		this.downloader = downloader;
	}

	public void login(Loginable loginable) throws IOException {
		URL logonUrl = new URL(urlBuilder.createLogonUrl());
		client = new WebClient();
		client.getOptions().setThrowExceptionOnScriptError(false);
		loginable.login(client, logonUrl);

		downloader.setClient(client);
	}

	public void downloadSpace(String space) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		if (!processedLinks.contains(space)) {
			String displayLink = urlBuilder.createDisplayUrl(space);
			PageLinks links = downloader.download(displayLink);
			processedLinks.add(space);
			download(links);
		}
	}

	private void download(PageLinks links) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		downloadPages(links.getPageLinks());
		downloadSpaces(links.getSpaceLinks());
		downloadAttachments(links.getAttachmentLinks());
	}

	private void downloadAttachments(Map<String, String> attachmentLinks) throws IOException {
		downloader.downloadAttachments(attachmentLinks);
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
		String pageUrl = urlBuilder.createPageUrl(pageId);
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

}
