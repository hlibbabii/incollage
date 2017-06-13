package ua.webchallange.instacollage.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HttpHelper {

    private final static Logger logger = LoggerFactory.getLogger(HttpHelper.class);

    private HttpHelper() {}

    public static String sendPostRequest(String url, Map<String, String> params) throws IOException {
        CloseableHttpClient httpСlient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = params.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));

        try (CloseableHttpResponse response = httpСlient.execute(httpPost)) {
            HttpEntity entity2 = response.getEntity();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(entity2.getContent(), stringWriter, "UTF-8");
            String theString = stringWriter.toString();
            EntityUtils.consume(entity2);
            return theString;
        }
    }

    public static String sendGetRequest(String url, Map<String, String> params) throws IOException {
        try (CloseableHttpResponse response = executeRequest(url, params)) {
            HttpEntity entity = response.getEntity();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(entity.getContent(), stringWriter, "UTF-8");
            String theString = stringWriter.toString();
            EntityUtils.consume(entity);
            return theString;
        }
    }

    public static BufferedImage sendGetRequestGetImage(String url) throws IOException {

        try (CloseableHttpResponse response = executeRequest(url, null)) {
            HttpEntity entity = response.getEntity();
            return ImageIO.read(entity.getContent());
        }
    }

    private static CloseableHttpResponse executeRequest(String url, Map<String, String> params) throws IOException {
        CloseableHttpClient httpСlient = HttpClients.createDefault();
        StringBuilder urlBuilder = new StringBuilder(url);
        if (params != null) {
            urlBuilder.append("?");
            params.entrySet().forEach(e -> urlBuilder.append(e.getKey()).append("=").append(e.getValue()));
        }
        String uri = urlBuilder.toString();
        HttpGet httpGet = new HttpGet(uri);
        if (logger.isTraceEnabled()) {
            logger.trace(uri);
        }
        return httpСlient.execute(httpGet);
    }
}
