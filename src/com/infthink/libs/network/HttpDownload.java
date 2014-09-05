package com.infthink.libs.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.http.HttpResponseCache;
import android.util.Log;

import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.IOUtils;

/**
 * <pre>
 * 基于Http1.1协议
 * http://www.ietf.org/rfc/rfc2616.txt
 * 
 * {@linkplain HttpURLConnection}
 * Authenticator.setDefault(new Authenticator() {
 *     protected PasswordAuthentication getPasswordAuthentication() {
 *         return new PasswordAuthentication(username, password.toCharArray());
 *     }
 * });
 * 
 * CookieManager cookieManager = new CookieManager();
 * CookieHandler.setDefault(cookieManager);
 * 
 * For example, to receive www.twitter.com in French:
 *
 * HttpCookie cookie = new HttpCookie("lang", "fr");
 * cookie.setDomain("twitter.com");
 * cookie.setPath("/");
 * cookie.setVersion(0);
 * cookieManager.getCookieStore().add(new URI("http://twitter.com/"), cookie);
 * 
 * {@linkplain HttpResponseCache}
 *
 * protected void onCreate(Bundle savedInstanceState) {
 *     ...
 *
 *     try {
 *         File httpCacheDir = new File(context.getCacheDir(), "http");
 *         long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
 *         HttpResponseCache.install(httpCacheDir, httpCacheSize);
 *     } catch (IOException e) {
 *         Log.i(TAG, "HTTP response cache installation failed:" + e);
 *     }
 * }
 *
 * protected void onStop() {
 *     ...
 *
 *     HttpResponseCache cache = HttpResponseCache.getInstalled();
 *     if (cache != null) {
 *         cache.flush();
 *     }
 * }
 *
 * </pre>
 */
public class HttpDownload implements IDebuggable {

    private static final String TAG = HttpDownload.class.getSimpleName();

    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_TRACE = "TRACE";
    public static final String METHOD_CONNECT = "CONNECT";

    /**
     * <pre>
     * 通用信息头
     * 请求:用于请求时表示客户与服务器之间的代理服务器如何使用已缓存的界面
     * 响应:通知客户机和代理服务器如何缓存该页面
     * 取值为public,private,no-cache,no-store,no-transform,must-ridate,proxy-ridate,max-age,s-maxage
     * 一个cache-control可以设置多个字段,各字段间以逗号分割如
     * Cache-Control:no-store,no-cache
     * </pre>
     */
    public static final String HEAD_Cache_Control = "Cache-Control";

    /**
     * <pre>
     * 通用信息头
     * 用于指示处理完本次请求/响应后,客户端与服务器是否继续保持连接
     * 取值为Keep-Alive,close.默认为Keep-Alive
     * </pre>
     */
    public static final String HEAD_Connection = "Connection";

    /**
     * <pre>
     * 通用信息头
     * 用于表示Http消息产生的时间,格式必须是GMT格式,如
     * Date: True,11 Jul 2000 18:23:16 GMT
     * </pre>
     */
    public static final String HEAD_Date = "Date";

    /**
     * <pre>
     * 通用信息头
     * 与Cache-Control类似,但值只能固定为no-cache
     * </pre>
     */
    public static final String HEAD_Pragma = "Pragma";

    /**
     * <pre>
     * 通用信息头
     * 有些消息头可以放在消息头中,也可以放在实体内容之后.对于后者需在Trailer字段中指定,如
     * Trailer:Date
     * </pre>
     */
    public static final String HEAD_Trailer = "Trailer";

    /**
     * <pre>
     * 通用信息头
     * Transfer-Encoding
     * 如何实体部分采用某种编码方式传输,则该字段指定该编码方式,目前的值只能为chuncked
     * </pre>
     */
    public static final String HEAD_Transfer_Encoding = "Transfer-Encoding";

    /**
     * <pre>
     * 通用信息头
     * Upgrade字段允许客户端指定它所支持兵希望将当前换到的通讯协议对于101状态码,服务器必须使用Upgrade头字段指定切换的协议,如
     * Upgrade:Http/2.0 SHTTP/1.3
     * </pre>
     */
    public static final String HEAD_Upgrade = "Upgrade";

    /**
     * <pre>
     * 通用信息头
     * Via头字段用于指定Http消息途径的中介代理服务器名称和所使用的协议,这个头字段由代理服务器产生,每个代理服务器必须把他的信息追加到via字段的最后,以反应http消息途径的多个代理服务器的顺序
     * Via: Http/1.1 Proxy1 Http/1.1 Proxy2
     * </pre>
     */
    public static final String HEAD_Via = "Via";

    /**
     * <pre>
     * 通用信息头
     * Warning头字段主要用于说明其他头字段和状态码不能表示的一些其它警告信息,例如,返回的实体信息可能已经过时
     * </pre>
     */
    public static final String HEAD_Warning = "Warning";

    /**
     * <pre>
     * 请求头
     * 用于指定客户端可以接受的MIME类型,如
     * Accept:text/html,image/*
     * </pre>
     */
    public static final String HEAD_Accept = "Accept";

    /**
     * <pre>
     * 请求头
     * 用于指定客户端可以使用的字符集
     * Accept-Charset:ISO-8859-1,UTF-8
     * </pre>
     */
    public static final String HEAD_Accept_Charset = "Accept-Charset";

    /**
     * <pre>
     * 请求头
     * 用于指定客户机能够进行解码的数据编码方式,通常指某种压缩方式,如gzip,compress,deflate
     * </pre>
     */
    public static final String HEAD_Accept_Encoding = "Accept-Encoding";

    /**
     * <pre>
     * 请求头
     * 用于指定客户端期望服务器返回哪个国家语言的文档,可以指定多个.
     * Accept-Language:en-gb,zh-cn
     * </pre>
     */
    public static final String HEAD_Accept_Language = "Accept-Language";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_Authorization = "Authorization";

    /**
     * <pre>
     * 请求头
     * 用于指定客户机请求服务器采取的特殊行为,目前只设置100-continue,
     * 用于询问服务器是否可以在随后的请求中发送一个附加文档.
     * </pre>
     */
    public static final String HEAD_Except = "Expect";

    /**
     * <pre>
     * 请求头
     * 用于指定请求发送者的Email,它只被一些特殊的客户端程序使用,浏览器不使用
     * </pre>
     */
    public static final String HEAD_From = "From";

    /**
     * <pre>
     * 请求头
     * 用于指定资源所在的主机和端口号(可省略)
     * GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1
     * 与如下是等价的
     * GET /pub/WWW/TheProject.html HTTP/1.1
     * Host:www.w3.org
     * </pre>
     */
    public static final String HEAD_Host = "Host";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_If_Match = "If-Match";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_If_Modified_Since = "If-Modified-Since";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_If_None_Match = "If-None-Match";

    /**
     * <pre>
     * 请求头
     * 只能跟Range一起使用
     * </pre>
     */
    public static final String HEAD_If_Range = "If-Range";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_If_Unmodified_Since = "If-Unmodified-Since";

    /**
     * <pre>
     * 请求头
     * Max-Forwards
     * 用于指定当前请求可以途径的代理服务器数,
     * 每经过一个代理服务器,值减少1.减到0时,所在的代理服务器负责处理请求.
     * </pre>
     */
    public static final String HEAD_Max_Forwards = "Max-Forwards";

    /**
     * <pre>
     * 请求头
     * 除了针对代理服务器的用户认证信息外,用法与Authorization相同
     * </pre>
     */
    public static final String HEAD_Proxy_Authorization = "Proxy-Authorization";

    /**
     * <pre>
     * 请求头
     * 用于指定服务器只需返回的部分内容及内容范围.这样可以实现打文件内容的分段传输.
     * 有几种格式,如
     * 1)Range:bytes=100-500  100到500
     * 2)Range:bytes=100-     100以上
     * 3)Range:bytes=-100     100以下
     * </pre>
     */
    public static final String HEAD_Range = "Range";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_Referer = "Referer";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_TE = "TE";

    /**
     * <pre>
     * 请求头
     * 
     * </pre>
     */
    public static final String HEAD_User_Agent = "User-Agent";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Accept_Ranges = "Accept-Ranges";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Age = "Age";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_ETag = "ETag";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Location = "Location";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Proxy_Authenticate = "Proxy-Authenticate";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Retry_After = "Retry-After";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Server = "Server";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_Vary = "Vary";

    /**
     * <pre>
     * 响应头
     * 
     * </pre>
     */
    public static final String HEAD_WWW_Authenticate = "WWW-Authenticate";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Allow = "Allow";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Encoding = "Content-Encoding";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Language = "Content-Language";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Length = "Content-Length";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Location = "Content-Location";
    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_MD5 = "Content-MD5";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Range = "Content-Range";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Content_Type = "Content-Type";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Expires = "Expires";

    /**
     * <pre>
     * 实体头
     * 
     * </pre>
     */
    public static final String HEAD_Last_Modified = "Last-Modified";

    /**
     * @param method {@linkplain #METHOD_DELETE}, {@linkplain #METHOD_GET},
     *                {@linkplain #METHOD_HEAD}, {@linkplain #METHOD_OPTIONS},
     *                {@linkplain #METHOD_POST}, {@linkplain #METHOD_PUT}, {@linkplain #METHOD_TRACE}
     * @param httpUrl 如果是GET请求,需要将请求参数拼接在此地址中.
     * @param range 断点续传, 如果range > 0,则发送range请求
     * @param onAddRequestProperties
     * @param useCaches 是否使用缓存.
     * @param onDoOutput 如果不为空,则允许在请求body中输入请求数据.
     * @param onDoInput 如果不为空,则允许读取网络响应body,如果网络响应body为空,则返回一个长度为0的输入流.
     * @param onErrorInput 读取错误流, 当服务器返回错误信息的时候. 只有当onDoInput不为空时,此参数才有效.
     */
    public static void download(String method, String httpUrl, long range, IOnAddRequestProperties onAddRequestProperties, boolean useCaches, IOnDoOutput onDoOutput, IOnDoInput onDoInput,
            IOnErrorInput onErrorInput) {
        if (DEBUG) {
            Log.d(TAG, String.format("download %s %s useCaches:%s", new Object[] { method, httpUrl, useCaches }));
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(60000);
            if (onAddRequestProperties != null) {
                onAddRequestProperties.onAddRequestProperties(connection);
            }
            connection.setDoOutput(onDoOutput != null);
//          if (onDoOutput != null) {
//              connection.setChunkedStreamingMode(0);
//          }

            if (range > 0) {
                String property = "bytes=" + range + "-";
                connection.setRequestProperty(HEAD_Range, property);
            }
            connection.setDoInput(onDoInput != null);
            connection.setUseCaches(false);
            connection.setRequestMethod(method);
            if (onDoOutput != null) {
                OutputStream os = null;
                try {
                    os = connection.getOutputStream();
                    onDoOutput.onDoOutput(connection, os);
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                } finally {
                    if (os != null) {
                        os.flush();
                    }
                    IOUtils.close(os);
                }
            }
            if (onDoInput != null) {
                InputStream is = null;
                try {
                    is = connection.getInputStream();
                    onDoInput.onDoInput(connection, is);
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (is == null) {
                        if (DEBUG)
                            Log.d(TAG, String.format("读取输入流失败 method:%s, url:%s, responseCode:%s", new Object[] { method, httpUrl, connection.getResponseCode() }));
                        if (onErrorInput != null) {
                            try {
                                is = connection.getErrorStream();
                                onErrorInput.onErrorInput(connection, is);
                            } catch (Exception ex) {
                                if (DEBUG) {
                                    ex.printStackTrace();
                                    Log.d(TAG, String.format("读取错误流失败 method:%s, url:%s, responseCode:%s", new Object[] { method, httpUrl, connection.getResponseCode() }));
                                }
                            }
                        }
                    }
                } finally {
                    IOUtils.close(is);
                }
            }
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
            if (onErrorInput != null) {
                try {
                    onErrorInput.onErrorInput(connection, null);
                } catch (Exception ex) {
                    if (DEBUG) {
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * @param method {@linkplain #METHOD_DELETE}, {@linkplain #METHOD_GET},
     *                {@linkplain #METHOD_HEAD}, {@linkplain #METHOD_OPTIONS},
     *                {@linkplain #METHOD_POST}, {@linkplain #METHOD_PUT}, {@linkplain #METHOD_TRACE}
     * @param httpUrl 如果是GET请求,需要将请求参数拼接在此地址中.
     * @param onAddRequestProperties
     * @param useCaches 是否使用缓存.
     * @param onDoOutput 如果不为空,则允许在请求body中输入请求数据.
     * @param onDoInput 如果不为空,则允许读取网络响应body,如果网络响应body为空,则返回一个长度为0的输入流.
     * @param onErrorInput 读取错误流, 当服务器返回错误信息的时候. 只有当onDoInput不为空时,此参数才有效.
     */
    protected static void download(String method, String httpUrl, IOnAddRequestProperties onAddRequestProperties, boolean useCaches, IOnDoOutput onDoOutput, IOnDoInput onDoInput,
            IOnErrorInput onErrorInput) {
        download(method, httpUrl, -1, onAddRequestProperties, useCaches, onDoOutput, onDoInput, onErrorInput);
    }

    public interface IOnAddRequestProperties {

        /**
         * {@linkplain HttpURLConnection#addRequestProperty(String, String)}
         */
        public void onAddRequestProperties(HttpURLConnection connection);

    }

    public interface IOnDoOutput {

        /**
         * {@linkplain HttpURLConnection#addRequestProperty(String, String)}
         * @param connection
         * @param os
         */
        public void onDoOutput(HttpURLConnection connection, OutputStream os);
    }

    public interface IOnDoInput {

        /**
         * {@linkplain HttpURLConnection#getResponseCode()}
         * {@linkplain HttpURLConnection#getInputStream()}
         */
        public void onDoInput(HttpURLConnection connection, InputStream is);

    }

    public interface IOnErrorInput {

        /**
         * {@linkplain HttpURLConnection#getResponseCode()}
         * {@linkplain HttpURLConnection#getErrorStream()}
         */
        public void onErrorInput(HttpURLConnection connection, InputStream is);

    }

}
