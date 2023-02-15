package com.igatn.extranet.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

// It's better to mark this class as @Component rather than @Service
// read more about difference: https://www.baeldung.com/spring-component-repository-service
@Component
public class RestUtils {
    
    @Autowired
    private RestTemplate restTemplate;

    public <T> ResponseEntity<T> prepareGetWS(String query, Map<String, String> queryParams, Class<T> cls) {
        return restTemplate.getForEntity(prepareQuery(query, queryParams), cls);
    }

    // FRE - it seems unused
//    public <T> ResponseEntity<T> prepareGetWS(String query, Map<String, String> queryParams, Class<T> cls, Map<String, String> headers) {
//        return restTemplate.exchange(prepareQuery(query, queryParams), HttpMethod.GET, new HttpEntity<>(prepareHeaders(headers)), cls);
//    }
    
    public <T> ResponseEntity<T> preparePostWS(String query, Map<String, String> queryParams, Class<T> cls, Object obj) {
        return restTemplate.postForEntity(prepareQuery(query, queryParams), new HttpEntity<>(obj), cls);
    }

    public <T> ResponseEntity<T> preparePostWS(String query, Map<String, String> queryParams, Class<T> cls, Object obj, Map<String, String> headers) {
        return restTemplate.postForEntity(prepareQuery(query, queryParams), new HttpEntity<>(obj, prepareHeaders(headers)), cls);
    }
    
    public <T> ResponseEntity<T> prepareGetWs(URI uri, Class<T> cls){
        return restTemplate.exchange(uri, HttpMethod.GET, null, cls);
    }
    
    private HttpHeaders prepareHeaders(Map<String, String> headers) {
        HttpHeaders restHeaders = new HttpHeaders();
        headers.forEach(restHeaders::set);
        
        return restHeaders;
    }
    
    private String prepareQuery(String query, Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(query);
        
        if (!queryParams.isEmpty()) {
            sb.append("?");
            
            Iterator<Map.Entry<String, String>> iterator = queryParams.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                sb.append(entry.getKey()).append("=").append(entry.getValue());

                if (iterator.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        return sb.toString();
    }
}
