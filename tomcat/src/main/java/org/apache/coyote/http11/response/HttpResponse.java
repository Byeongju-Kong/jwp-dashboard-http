package org.apache.coyote.http11.response;

import static org.apache.coyote.http11.response.header.HttpStatusCode.FOUND;
import static org.apache.coyote.http11.response.header.HttpStatusCode.NOT_FOUND;
import static org.apache.coyote.http11.response.header.HttpStatusCode.OK;
import static org.apache.coyote.http11.util.StringUtils.NEW_LINE;

import org.apache.coyote.http11.response.header.ContentLength;
import org.apache.coyote.http11.response.header.ContentType;
import org.apache.coyote.http11.response.header.Cookie;
import org.apache.coyote.http11.response.header.HttpResponseHeader;
import org.apache.coyote.http11.response.header.HttpStatusCode;
import org.apache.coyote.http11.response.header.Location;
import org.apache.coyote.http11.response.header.ResponseHeaders;

public class HttpResponse {

    private static final String HTTP_VERSION = "HTTP/1.1 ";

    private HttpStatusCode httpStatusCode;
    private ResponseBody responseBody;
    private ResponseHeaders responseHeaders;

    public HttpResponse(HttpStatusCode httpStatusCode, ResponseHeaders responseHeaders, ResponseBody responseBody) {
        this.httpStatusCode = httpStatusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public static HttpResponse getNew() {
        return new HttpResponse(OK, new ResponseHeaders(), ResponseBody.None());
    }

    public static HttpResponse ok(String responseBody, ContentType contentType) {
        return new HttpResponse(
                OK,
                ResponseHeaders.from(contentType, ContentLength.from(responseBody)),
                new ResponseBody(responseBody)
        );
    }

    public static HttpResponse notFound(String responseBody) {
        return new HttpResponse(
                NOT_FOUND,
                ResponseHeaders.from(ContentType.TEXT_HTML, ContentLength.from(responseBody)),
                new ResponseBody(responseBody)
        );
    }

    public static HttpResponse found(String redirectUri) {
        return new HttpResponse(
                FOUND,
                ResponseHeaders.from(new Location(redirectUri)),
                ResponseBody.None()
        );
    }

    public static HttpResponse found(String redirectUri, Cookie cookie) {
        return new HttpResponse(
                FOUND,
                ResponseHeaders.from(new Location(redirectUri), cookie),
                ResponseBody.None()
        );
    }

    public void setHttpStatusCode(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = new ResponseBody(responseBody);
        responseHeaders.add(ContentLength.from(responseBody));
    }

    public String getResponse() {
        return String.join(
                NEW_LINE,
                HTTP_VERSION + httpStatusCode.getValue(),
                responseHeaders.toResponseFormat(),
                responseBody.getValue());
    }

    public void addHeader(HttpResponseHeader contentType) {
        responseHeaders.add(contentType);
    }
}
