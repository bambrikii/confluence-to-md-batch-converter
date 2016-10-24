package org.bambrikii.md.converter.impl;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.bambrikii.md.converter.api.Transformable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by Alexander Arakelyan on 24.10.16 20:25.
 */
public class HtmTransformer implements Transformable {
	private WebClient client;

	@Override
	public void setWebClient(WebClient client) {
		this.client = client;
	}

	@Override
	public String retrieveContent(HtmlPage pageId) throws IOException {
		return null;
	}

	@Override
	public String transformContent(String viewStorageContent) throws ParserConfigurationException, IOException, SAXException, TransformerException {
		return null;
	}
}
