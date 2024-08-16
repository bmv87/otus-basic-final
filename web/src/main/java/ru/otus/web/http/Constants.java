package ru.otus.web.http;

public class Constants {
    public static final String ANY_VALUE = "*";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";

    public static class Headers {
        public static final String CONTENT_DISPOSITION = "Content-Disposition";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String TOKEN = "Token";
        public static final String ACCEPT = "Accept";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONNECTION = "Connection";
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
        public static final String VARY = "Vary";
    }

    public static class MimeTypes {
        public static final String TEXT = "text/html";
        public static final String JSON = "application/json";
        public static final String FORM_DATA = "multipart/form-data";
    }
}
