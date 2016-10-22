package org.bambrikii.md.converter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

import static org.bambrikii.md.converter.ViewStorageTransformer.CHARSET_NAME;
import static org.bambrikii.md.converter.ViewStorageTransformer.transformViewStorage;

/**
 * Created by Alexander Arakelyan on 22.10.16 14:51.
 */
public class DownloadTest {
	private static final Logger logger = LoggerFactory.getLogger(DownloadTest.class);

	private String username;
	private String password;
	private String hostUrl;
	private String space;

	@Before
	public void before() {
		username = System.getProperty("md-user");
		password = System.getProperty("md-password");
		hostUrl = System.getProperty("md-host-url");
		space = System.getProperty("md-space");
	}

	@Test
	public void testDownload() throws IOException, TransformerException, ParserConfigurationException, SAXException {
		Crawler crawler = new Crawler(hostUrl, "target");
		crawler.login(username, password);
		crawler.downloadSpace(space);
	}

	@Test
	public void testTransform() throws IOException, TransformerException, SAXException, ParserConfigurationException {
		try (InputStream is = DownloadTest.class.getResourceAsStream("/viewstoragecontent.xml")) {
			String content = IOUtils.toString(is, CHARSET_NAME);
			String content2 = transformViewStorage(content);
			logger.debug(content2);
		}
	}
}
