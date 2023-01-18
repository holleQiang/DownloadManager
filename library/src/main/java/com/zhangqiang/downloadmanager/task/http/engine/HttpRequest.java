package com.zhangqiang.downloadmanager.task.http.engine;

import com.zhangqiang.downloadmanager.task.http.utils.FiledSetter;

import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {
     private final String url;
     private final Map<String,String> headers;

     public HttpRequest(String url, Map<String, String> headers) {
          this.url = url;
          this.headers = headers;
     }

     public String getUrl() {
          return url;
     }

     public Map<String, String> getHeaders() {
          return headers;
     }

     public static class  Builder implements FiledSetter {
          private String url;
          private Map<String,String> headers;

          public Builder setUrl(String url) {
               this.url = url;
               return this;
          }
          public HttpRequest build(){
               return  new HttpRequest(url,headers);
          }

          @Override
          public void setField(String key, String value) {
               if (headers == null) {
                    headers = new HashMap<>();
               }
               headers.put(key, value);
          }
     }
}
