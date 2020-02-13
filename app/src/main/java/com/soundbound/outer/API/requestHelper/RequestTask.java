package com.soundbound.outer.API.requestHelper;


import com.soundbound.outer.API.requestHelper.Expections.ConnectionException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class RequestTask implements Callable<String> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public enum RequestType{GET, POST, PUT, DELETE}
    private Request request;
    private OkHttpClient client;

    public RequestTask(RequestType type, String url,String body, Map<String, String> headers, Map<String, String> queryParams){
        client = new OkHttpClient();
        System.out.println(url);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        HttpUrl URL = urlBuilder.build();
        if(queryParams != null)
        for(Map.Entry<String, String> entry: queryParams.entrySet()){
            urlBuilder.addQueryParameter(entry.getKey(),entry.getValue());
        }
        Request.Builder requestBuilder;
        switch (type){
            case POST:
                requestBuilder = new Request.Builder().url(URL).post(RequestBody.create(JSON, body));
                break;
            default:
            case GET:
                requestBuilder = new Request.Builder().url(URL).get();
                break;
            case PUT:
                requestBuilder = new Request.Builder().url(URL).put(RequestBody.create(JSON, body));
                break;
            case DELETE:
                requestBuilder = new Request.Builder().url(URL).delete(RequestBody.create(JSON, body));
                break;
        }
        if(headers != null)
        for(Map.Entry<String, String> entry: headers.entrySet()){
            requestBuilder.addHeader(entry.getKey(),entry.getValue());
        }
        this.request = requestBuilder.build();
    }
    @Override
    public String call() throws ConnectionException, IOException {
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            return Objects.requireNonNull(response.body()).string();
        }else {
            ConnectionException exception = new ConnectionException("response Code: " + response.code() + "\n message: " + response.body().string());
            throw exception;
        }
    }
}
