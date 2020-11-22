package com.alien.crack_wechat_robot.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {

    public static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(3000, TimeUnit.MILLISECONDS)
            .build();

    public enum HttpMethod {
        POST, GET
    }

    public Response httpResponeBody(String url, HttpMethod httpMethod) {
        return httpResponeBody(url, httpMethod, null);
    }

    public Response httpResponeBody(String url, HttpMethod httpMethod, Map<Object, Object> paramster) {
        return httpResponeBody(url, httpMethod, paramster, null);
    }

    public Response httpResponeBody(String url, HttpMethod httpMethod, Map<Object, Object> paramster, Map<Object, Object> header) {
        try {
            Request request = builderRequest(url, httpMethod, paramster, header);
            Response execute = okHttpClient.newCall(request).execute();
            return execute;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String http(String url, HttpMethod httpMethod) {
        return http(url, httpMethod, null, null);
    }

    public String http(String url, HttpMethod httpMethod, Map<Object, Object> paramster) {
        return http(url, httpMethod, paramster, null);
    }

    public String http(String url, HttpMethod httpMethod, Map<Object, Object> paramster, Map<Object, Object> header) {
        Response response = httpResponeBody(url, httpMethod, paramster, header);
        try {
            String string = response.body().string();
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    public String http(String url, String context, MediaType mediaType, Map<Object, Object> header) {
        try {
            RequestBody body = RequestBody.create(mediaType, context);
            Request.Builder post = new Request.Builder()
                    .url(url)
                    .post(body);
            if (header != null && header.size() != 0) {
                Set<Map.Entry<Object, Object>> entries = header.entrySet();
                for (Map.Entry<Object, Object> entry : entries) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    // todo 这里
                    if (key == null || value == null) continue;
                    post.addHeader(key, value);
                }
            }
            Request build = post.build();
            Response execute = okHttpClient.newCall(build).execute();
            return execute.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String http(String url, String context, MediaType mediaType) {
        return http(url, context, mediaType, null);
    }

    private Request builderRequest(String url, HttpMethod httpMethod, Map<Object, Object> paramster, Map<Object, Object> header) {
        Request.Builder builder = new Request.Builder();
        if (httpMethod == HttpMethod.POST) {
            //POST 请求
            FormBody.Builder formBody = new FormBody.Builder();
            forEachMap(paramster, ((key, value) -> formBody.add(key, value)));
            builder.post(formBody.build()).url(url);
        } else if (httpMethod == HttpMethod.GET) {
            //GET 请求
            if (paramster != null && paramster.size() != 0) {
                StringBuffer sf = new StringBuffer("?");
                forEachMap(paramster, ((key, value) -> sf.append(key).append("=").append(value).append("&")));
                url += sf.toString();
            }
            builder.url(url);
        }
        //统一添加请求头
        forEachMap(header, (key, value) -> builder.addHeader(key, value));
        return builder.build();
    }

    private void forEachMap(Map<Object, Object> map, MapForInterface mapForInterface) {
        if (map != null && map.size() != 0) {
            Set<Map.Entry<Object, Object>> entries = map.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                mapForInterface.result(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }


    interface MapForInterface {
        void result(String key, String value);
    }

}
