package com.infthink.libs.common.utils;

import java.util.Collection;

import org.apache.http.NameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class Collections {

    public static String deepToString(Collection<NameValuePair> params) {
        if (params == null || params.size() < 1) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (NameValuePair nvp : params) {
            buffer.append("&" + nvp.getName() + "=" + nvp.getValue());
        }
        return buffer.toString().substring(1);
    }

    public static HttpParams deepToHttpParams(Collection<NameValuePair> params) {
        if (params == null || params.size() < 1) {
            return null;
        }
        HttpParams httpParams = new BasicHttpParams();
        for (NameValuePair nvp : params) {
            httpParams.setParameter(nvp.getName(), nvp.getValue());
        }
        return httpParams;
    }

}
