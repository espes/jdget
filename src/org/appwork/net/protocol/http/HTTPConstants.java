package org.appwork.net.protocol.http;

public class HTTPConstants {
    /**
     * List of HTTP Response Codes. INcomplete! <br>
     * Check http://en.wikipedia.org/wiki/List_of_HTTP_status_codes to complete
     * this list
     * 
     * @author thomas
     * 
     */
    public static enum ResponseCode {
        /**
         * This means that the server has received the request headers, and that
         * the client should proceed to send the request body (in the case of a
         * request for which a body needs to be sent; for example, a POST
         * request). If the request body is large, sending it to a server when a
         * request has already been rejected based upon inappropriate headers is
         * inefficient. To have a server check if the request could be accepted
         * based on the request's headers alone, a client must send Expect:
         * 100-continue as a header in its initial request[2] and check if a 100
         * Continue status code is received in response before continuing (or
         * receive 417 Expectation Failed and not continue).[2]
         */
        INFORMATION_CONTINUE(100, "Continue"),
        /**
         * Standard response for successful HTTP requests. The actual response
         * will depend on the request method used. In a GET request, the
         * response will contain an entity corresponding to the requested
         * resource. In a POST request the response will contain an entity
         * describing or containing the result of the action.[2]
         */
        SUCCESS_OK(200, "OK"),
        /**
         * The server successfully processed the request, but is not returning
         * any content.[2
         */
        SUCCESS_NO_CONTENT(204, "No Content"),
        /**
         * This is the most popular redirect code[citation needed], but also an
         * example of industrial practice contradicting the standard.[2]
         * HTTP/1.0 specification (RFC 1945) required the client to perform a
         * temporary redirect (the original describing phrase was
         * "Moved Temporarily"),[5] but popular browsers implemented 302 with
         * the functionality of a 303 See Other. Therefore, HTTP/1.1 added
         * status codes 303 and 307 to distinguish between the two behaviours.
         * However, the majority of Web applications and frameworks still use
         * the 302 status code as if it were the 303[6].
         */
        REDIRECT_FOUND(302, "Found"),
        /**
         * http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal
         */
        RESUME_INCOMPLETE(308, "Resume Incomplete"),
        /**
         * The request cannot be fulfilled due to bad syntax.[2
         */
        ERROR_BAD_REQUEST(400, "Bad Request"),

        ERROR_UNAUTHORIZED(401, "Unauthorized"),
        /**
         * The request was a legal request, but the server is refusing to
         * respond to it.[2] Unlike a 401 Unauthorized response, authenticating
         * will make no difference.[2
         */
        ERROR_FORBIDDEN(403, "Forbidden"),

        PROXY_AUTH_REQUIRED(407, "Forbidden"),
        /**
         * The requested resource could not be found but may be available again
         * in the future.[2] Subsequent requests by the client are permissible.
         */
        ERROR_NOT_FOUND(404, "Not Found"),

        ERROR_RANGE_NOT_SUPPORTED(416, "Range requests not supported"),
        /**
         * A generic error message, given when no more specific message is
         * suitable.[2
         */
        SERVERERROR_INTERNAL(500, "Internal Server Error"),
        /**
         * The server either does not recognise the request method, or it lacks
         * the ability to fulfill the request.[2
         */
        SERVERERROR_NOT_IMPLEMENTED(501, "Not Implemented"),
        /**
         * The server was acting as a gateway or proxy and received an invalid
         * response from the upstream server.[2
         */
        SERVERERROR_BAD_GATEWAY(502, "Bad Gateway"),
        /**
         * The server is currently unavailable (because it is overloaded or down
         * for maintenance).[2] Generally, this is a temporary state.
         */
        SERVERERROR_SERVICE_UNAVAILABLE(503, "Service Unavailable"),

        GATEWAY_TIMEOUT(504, "Gateway Time-out"),

        SUCCESS_PARTIAL_CONTENT(206, "Partial Content");
        /**
         * @param responseCode
         * @return
         */
        public static ResponseCode get(final int responseCode) {
            for (final ResponseCode rc : ResponseCode.values()) {
                if (responseCode == rc.getCode()) { return rc; }
            }
            return null;

        }

        private final int    code;
        private final String description;

        private final byte[] bytes;

        private ResponseCode(final int code, final String desc) {
            this.code = code;
            this.description = desc;
            this.bytes = (code + " " + desc).getBytes();
        }

        /**
         * @return
         */
        public byte[] getBytes() {
            return this.bytes;
        }

        public int getCode() {
            return this.code;
        }

        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Content-Types that are acceptable Accept: text/plain
     */
    public static final String HEADER_REQUEST_ACCEPT                        = "Accept";
    /**
     * Character sets that are acceptable Accept-Charset: utf-8
     */
    public static final String HEADER_REQUEST_ACCEPT_CHARSET                = "Accept-Charset";
    /**
     * Acceptable encodings Accept-Encoding: <compress | gzip | deflate |
     * identity>
     */
    public static final String HEADER_REQUEST_ACCEPT_ENCODING               = "Accept-Encoding";
    /**
     * Acceptable languages for response Accept-Language: en-US
     */
    public static final String HEADER_REQUEST_ACCEPT_LANGUAGE               = "Accept-Language";
    /**
     * Authentication credentials for HTTP authentication Authorization: Basic
     * QWxhZGRpbjpvcGVuIHNlc2FtZQ="="
     */
    public static final String HEADER_REQUEST_AUTHORIZATION                 = "Authorization";
    /**
     * Used to specify directives that MUST be obeyed by all caching mechanisms
     * along the request/response chain Cache-Control: no-cache
     */
    public static final String HEADER_REQUEST_CACHE_CONTROL                 = "Cache-Control";
    /**
     * Used to specify directives that MUST be obeyed by all caching mechanisms
     * along the request/response chain Cache-Control: no-cache
     */
    public static final String HEADER_REQUEST_CONNECTION                    = "Connection";
    /**
     * an HTTP cookie previously sent by the server with Set-Cookie (below)
     * Cookie: $Version="1; Skin="new;
     */
    public static final String HEADER_REQUEST_COOKIE                        = "Cookie";
    /**
     * The length of the request body in octets (8-bit bytes) Content-Length:
     * 348
     */
    public static final String HEADER_REQUEST_CONTENT_LENGTH                = "Content-Length";
    /**
     * The mime type of the body of the request (used with POST and PUT
     * requests) Content-Type: application/x-www-form-urlencoded
     */
    public static final String HEADER_REQUEST_CONTENT_TYPE                  = "Content-Type";

    public static final String HEADER_REQUEST_CONTENT_RANGE                 = "Content-Range";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_DATE                          = "Date";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_EXPECT                        = "Expect";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_FROM                          = "From";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_HOST                          = "Host";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_MATCH                      = "If-Match";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_MODIFIED_SINCE             = "If-Modified-Since";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_IF_NON_MATCH                  = "If-None-Match";
    /**
     * The date and time that the message was sent Date: Tue, 15 Nov 1994
     * 08:12:31 GMT
     */
    public static final String HEADER_REQUEST_ID_RANGE                      = "If-Range";
    /**
     * Only send the response if the entity has not been modified since a
     * specific time. If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    public static final String HEADER_REQUEST_ID_MODIFIED_SINCE             = "If-Unmodified-Since";
    /**
     * Only send the response if the entity has not been modified since a
     * specific time. If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    public static final String HEADER_REQUEST_MAX_FORWARDS                  = "Max-Forwards";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_PRAGMA                        = "Pragma";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_PROXY_AUTHORIZATION           = "Proxy-Authorization";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_RANGE                         = "Range";

    public static final String HEADER_ETAG                                  = "ETag";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_REFERER                       = "Referer";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_TE                            = "TE";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_UPGRADE                       = "Upgrade";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_USER_AGENT                    = "User-Agent";
    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_VIA                           = "Via";
    public static final String HEADER_REQUEST_LOCATION                      = "Location";
    public static final String HEADER_REQUEST_X_FORWARDED_FOR               = "X-Forwarded-For";
    public static final String HEADER_REQUEST_X_CLIENT_IP                   = "X-Client-IP";

    /**
     * Implementation-specific headers that may have various effects anywhere
     * along the request-response chain. Pragma: no-cache
     */
    public static final String HEADER_REQUEST_WARNING                       = "Warning";
    public static final String HTTP_KEEP_ALIVE                              = "Keep-Alive";
    /**
     * The mime type of the body of the request (used with POST and PUT
     * requests) Content-Type: application/x-www-form-urlencoded
     */
    public static final String HEADER_RESPONSE_CONTENT_TYPE                 = "Content-Type";
    /**
     * In case we have dynamic content-length or content-length is not known in
     * advance
     */
    public static final String HEADER_RESPONSE_TRANSFER_ENCODING            = "Transfer-Encoding";
    public static final String HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED    = "chunked";
    public static final String HEADER_REQUEST_ACCEPT_RANGES                 = "Accept-Ranges";
    public static final String HEADER_RESPONSE_CONTENT_ENCODING             = "Content-Encoding";
    public static final String HEADER_RESPONSE_LOCATION                     = "Location";
    public static final String HEADER_RESPONSE_SERVER                       = "Server";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN  = "Access-Control-Allow-Origin";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_MAX_AGE       = "Access-Control-Max-Age";
}
