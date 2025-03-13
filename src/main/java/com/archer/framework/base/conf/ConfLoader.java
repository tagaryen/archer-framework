package com.archer.framework.base.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConfLoader {
	
	public static Conf load() {
		Conf conf;
		URL source = Thread.currentThread().getContextClassLoader().getResource("app.yml");
		if(source == null) {
			System.err.println("can not found app.yml");
			System.exit(0);
		}
		try(InputStream in = source.openStream()) {
			StringBuilder sb = new StringBuilder(in.available());
			int off = 0;
			byte[] buf = new byte[1024];
			while((off = in.read(buf)) > 0) {
				sb.append(new String(Arrays.copyOfRange(buf, 0, off), StandardCharsets.UTF_8));
			}
			conf = ConfParser.parseFromString(sb.toString());
		} catch (IOException e) {
			conf = new Conf();
		}
		return conf;
	}
}
