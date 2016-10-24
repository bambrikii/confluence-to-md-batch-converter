package org.bambrikii.md.converter.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Alexander Arakelyan on 24.10.16 19:48.
 */
public interface Persistable {
	void persistPage(String name, String content) throws IOException;

	void persistContent(String name, InputStream content) throws IOException;
}
