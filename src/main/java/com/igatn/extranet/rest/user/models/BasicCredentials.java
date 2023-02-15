package com.igatn.extranet.rest.user.models;

// FRE - Java community recommends using records instead of Lombok 
// in case we do not have many fields or inheritance
// read more on : https://www.baeldung.com/java-record-vs-lombok
public record BasicCredentials(String username, String password) { }
