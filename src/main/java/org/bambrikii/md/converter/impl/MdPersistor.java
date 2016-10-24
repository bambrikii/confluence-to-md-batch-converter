package org.bambrikii.md.converter.impl;

import org.apache.commons.io.IOUtils;
import org.bambrikii.md.converter.api.Persistable;

import java.io.*;
import java.nio.charset.Charset;

import static org.bambrikii.md.converter.impl.ViewStorageTransformer.CHARSET_NAME;

/**
 * Created by Alexander Arakelyan on 22.10.16 19:29.
 */
public class MdPersistor implements Persistable {
	private File targetDir;

	public MdPersistor(String dir) {
		targetDir = new File(dir);
		targetDir.mkdirs();
	}

	@Override
	public void persistPage(String name, String content) throws IOException {
		try (OutputStream os = new FileOutputStream(new File(targetDir, name.replaceAll("\\/", " - ")))) {
			IOUtils.write(content, os, Charset.forName(CHARSET_NAME));
		}
	}

	@Override
	public void persistContent(String name, InputStream content) throws IOException {
		try (OutputStream os = new FileOutputStream(new File(targetDir, name))) {
			IOUtils.copy(content, os);
		}
	}
}
