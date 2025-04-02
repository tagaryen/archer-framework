package com.archer.framework.datasource.mysql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.archer.framework.datasource.exceptions.SqlException;

public class MySQLUrl {
	

    private static final Pattern CONNECTION_STRING_PTRN = Pattern.compile("(?<scheme>[\\w\\+:%]+)\\s*" // scheme: required; alphanumeric, plus, colon or percent
            + "(?://(?<authority>[^/?#]*))?\\s*" // authority: optional; starts with "//" followed by any char except "/", "?" and "#"
            + "(?:/(?!\\s*/)(?<path>[^?#]*))?" // path: optional; starts with "/" but not followed by "/", and then followed by by any char except "?" and "#"
            + "(?:\\?(?!\\s*\\?)(?<query>[^#]*))?" // query: optional; starts with "?" but not followed by "?", and then followed by by any char except "#"
            + "(?:\\s*#(?<fragment>.*))?"); // fragment: optional; starts with "#", and then followed by anything
    private static final Pattern IP_PTRN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String scheme;
    private String authority;
    private String database;
    private String query;
    private String host;
    private int port;
    
    

	public void parseConnectionUrl(String url) {

        Matcher matcher = CONNECTION_STRING_PTRN.matcher(url);
        if (!matcher.matches()) {
            throw new SqlException("Invalid mysql connection url '" + url + "'");
        }
        this.scheme = matcher.group("scheme");
        this.authority = matcher.group("authority");
        try {
			this.database = matcher.group("path") == null ? null : URLDecoder.decode(matcher.group("path"), StandardCharsets.UTF_8.name()).trim();
		} catch (UnsupportedEncodingException e) {
			throw new SqlException(e);
		}
        this.query = matcher.group("query");
        
        parseAuthoritySection(url);
	}
	

    private void parseAuthoritySection(String url) throws SqlException {
    	String[] ipAndPort = this.authority.split(":");
    	if(ipAndPort.length != 2) {
            throw new SqlException("Invalid mysql connection url '" + url + "'");
    	}
    	if(!IP_PTRN.matcher(ipAndPort[0]).matches()) {
            throw new SqlException("Invalid mysql connection url '" + url + "'");
    	}
    	this.host = ipAndPort[0];
    	try {
    		this.port = Integer.parseInt(ipAndPort[1]);
    	} catch(Exception e) {
            throw new SqlException("Invalid mysql connection url '" + url + "'");
    	}
    	
    }


	public static Pattern getConnectionStringPtrn() {
		return CONNECTION_STRING_PTRN;
	}


	public static Pattern getIpPtrn() {
		return IP_PTRN;
	}


	public String getScheme() {
		return scheme;
	}


	public String getAuthority() {
		return authority;
	}


	public String getDatabase() {
		return database;
	}


	public String getQuery() {
		return query;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}
	
	public String getDatabseUrl() {
		return database + "/" + query;
	}
}
