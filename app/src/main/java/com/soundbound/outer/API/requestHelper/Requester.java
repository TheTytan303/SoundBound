package com.soundbound.outer.API.requestHelper;

import com.soundbound.outer.API.requestHelper.Expections.ConnectionException;
import com.soundbound.outer.API.requestHelper.RequestTask;

import java.io.IOException;
import java.util.concurrent.*;

public class Requester {
    private static int PARALLEL_REQUESTS = 16;
    private static ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_REQUESTS);

    public static Future<String> getResponse(RequestTask request){
        Future<String> returnVale = executor.submit(request);
        return returnVale;
    }
}
