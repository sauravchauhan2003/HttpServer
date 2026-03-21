package org.example;

public enum RequestType {
    GET,
    POST,
    UPDATE,
    DELETE,
    PATCH,
    PUT
}
@interface Endpoint{
    String path();
    RequestType requesttype();
}
