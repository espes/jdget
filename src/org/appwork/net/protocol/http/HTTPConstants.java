package org.appwork.net.protocol.http;

public class HTTPConstants {

    /**
     * Content-Types that are acceptable Accept: text/plain
     */
    public static final String HEADER_REQUEST_ACCEPT              = "Accept";
    /**
     * Character sets that are acceptable Accept-Charset: utf-8
     */
    public static final String HEADER_REQUEST_ACCEPT_CHARSET      = "Accept-Charset";
    /**
     * Acceptable encodings Accept-Encoding: <compress | gzip | deflate |
     * identity>
     */
    public static final String HEADER_REQUEST_ACCEPT_ENCODING     = "Accept-Encoding";
    /**
     * Acceptable languages for response Accept-Language: en-US
     */
    public static final String HEADER_REQUEST_ACCEPT_LANGUAGE     = "Accept-Language";
    /**
     * Authentication credentials for HTTP authentication Authorization: Basic
     * QWxhZGRpbjpvcGVuIHNlc2FtZQ="="
     */
    public static final String HEADER_REQUEST_AUTHORIZATION       = "Authorization";
    /**
     * Used to specify directives that MUST be obeyed by all caching mechanisms
     * along the request/response chain Cache-Control: no-cache
     */
    public static final String HEADER_REQUEST_CACHE_CONTROL       = "Cache-Control";
    /**
     * Used to specify directives that MUST be obeyed by all caching mechanisms
     * along the request/response chain Cache-Control: no-cache
     */
    public static final String HEADER_REQUEST_CONNECTION          = "Connection";
    /**
     * an HTTP cookie previously sent by the server with Set-Cookie (below)
     * Cookie: $Version="1; Skin="new;
     */
    public static final String HEADER_REQUEST_COOKIE              = "Cookie";
    /**
     * The length of the request body in octets (8-bit bytes) Content-Length:
     * 348
     */
    public static final String HEADER_REQUEST_CONTENT_LENGTH      = "Content-Length";
    /**
     * The mime type of the body of the request (used with POST and PUT
     * requests) Content-Type: application/x-www-form-urlencoded
     */
    public static final String HEADER_REQUEST_CONTENT_TYPE        = "Content-Type";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_DATE                = "Date";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_EXPECT              = "Expect";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_FROM                = "From";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_HOST                = "Host";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_MATCH            = "If-Match";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_MODIFIED_SINCE   = "If-Modified-Since";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_NON_MATCH        = "If-None-Match";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_ID_RANGE            = "If-Range";
    /**
     * Only send the response if the entity has not been modified since a
     * specific time. If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    public static final String HEADER_REQUEST_ID_MODIFIED_SINCE   = "If-Unmodified-Since";
    /**
     * Only send the response if the entity has not been modified since a
     * specific time. If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    public static final String HEADER_REQUEST_MAX_FORWARDS        = "Max-Forwards";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_PRAGMA              = "Pragma";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_PROXY_AUTHORIZATION = "Proxy-Authorization";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_RANGE               = "Range";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_REFERER             = "Referer";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_TE                  = "TE";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_UPGRADE             = "Upgrade";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_USER_AGENT          = "User-Agent";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_VIA                 = "Via";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_WARNING             = "Warning";
    public static final String HTTP_KEEP_ALIVE                    = "Keep-Alive";
}
