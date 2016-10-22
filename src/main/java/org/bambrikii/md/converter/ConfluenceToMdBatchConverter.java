package org.bambrikii.md.converter;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alexander Arakelyan on 22.10.16 13:18.
 */
public class ConfluenceToMdBatchConverter {

	private static final Logger logger = LoggerFactory.getLogger(ConfluenceToMdBatchConverter.class);

	public static final String USERNAME = "u";
	public static final String PASSWORD = "p";
	public static final String HOST_URL = "h";
	public static final String CONFLUENCE_SPACE = "s";
	public static final String TARGET_DIR = "t";

	public static void main(String[] args) throws ParseException, IOException, TransformerException, ParserConfigurationException, SAXException {
		Options options = new Options();

		options.addOption(Option.builder(USERNAME).required().hasArg().desc("Logon username").build());
		options.addOption(Option.builder(PASSWORD).required().hasArg().desc("Logon password").build());
		options.addOption(Option.builder(HOST_URL).required().hasArg().desc("Host URL, for example https://<hostname>:<port>").build());
		options.addOption(Option.builder(CONFLUENCE_SPACE).required().hasArg().desc("Confluence Space to start with.").build());
		options.addOption(Option.builder(TARGET_DIR).required().hasArg().desc("Target directory to store downloaded and parsed files into.").build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		String username = cmd.getOptionValue(USERNAME);
		String password = cmd.getOptionValue(PASSWORD);
		String hostUrl = cmd.getOptionValue(HOST_URL);
		String space = cmd.getOptionValue(CONFLUENCE_SPACE);
		String targetDir = cmd.getOptionValue(TARGET_DIR);

		Crawler crawler = new Crawler(hostUrl, StringUtils.defaultIfEmpty(targetDir, "MD"));
		crawler.login(username, password);
		Pattern pattern = Pattern.compile("([0-9]+)(,[0-9])?");
		Map<String, String> pageIds = new HashMap<>();
		Matcher matcher = pattern.matcher(space);
		while (matcher.find()) {
			String pageId = matcher.group(1);
			pageIds.put(pageId, pageId);
		}
		if (pageIds.size() > 0) {
			logger.info("Starting to download pages: " + pageIds.entrySet().stream().map(stringStringEntry -> stringStringEntry.getKey()).reduce((s, s2) -> s + s2));
			crawler.downloadPages(pageIds);
		} else {
			logger.info("Starting to download space: " + space);
			crawler.downloadSpace(space);
		}
	}
}
