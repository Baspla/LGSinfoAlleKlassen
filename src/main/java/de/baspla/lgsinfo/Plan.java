package de.baspla.lgsinfo;

import org.apache.http.HttpEntity;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Plan {
    private static CacheConfig cacheConfig;
    private static RequestConfig requestConfig;
    private String BASE_URL;

    public Plan(String url) {
        BASE_URL = url;
        cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(1000)
                .setMaxObjectSize(10000)
                .build();
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .build();
    }

    public ArrayList<String> getKlassen() {
        ArrayList<String> list = new ArrayList<>();
        try {
            Document document;
            document = Jsoup.parse(getHTML(BASE_URL + "/stundenplan"));
            Elements elements = document.getElementsByClass("Class");
            if (elements.isEmpty()) {
                System.out.println("Keine Elemente");
                return list;
            }
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).child(0).text());
            }
            return list;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return list;
        }
    }

    private static String getHTML(String url) {
        try {
            UnsafeSSLHelp unsafeSSLHelp = new UnsafeSSLHelp();
            //CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            //credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("", ""));

            CloseableHttpClient httpclient = CachingHttpClientBuilder
                    .create()
                    .setCacheConfig(cacheConfig)
                    .setDefaultRequestConfig(requestConfig)
                    //.setDefaultCredentialsProvider(credentialsProvider)
                    .setSslcontext(unsafeSSLHelp.createUnsecureSSLContext())
                    .setHostnameVerifier(unsafeSSLHelp.getPassiveX509HostnameVerifier())
                    .build();
            HttpCacheContext context = HttpCacheContext.create();
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpGet, context);
            if (context.getCacheResponseStatus() != CacheResponseStatus.CACHE_MISS) {
                System.out.println("DER CACHE GEHT! - " + context.getCacheResponseStatus());
            }
            try {
                String content = "";
                HttpEntity entity = response.getEntity();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                entity.writeTo(outputStream);
                content = outputStream.toString("UTF-8");
                EntityUtils.consume(entity);
                return content;
            } finally {
                response.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<Eintrag> getVetretrungen(String klasse) {
        ArrayList<Eintrag> list = new ArrayList<>();
        try {
            Document document;
            document = Jsoup.parse(getHTML(BASE_URL + "/vertretungsplan"));
            Elements elements = document.getElementsByClass("index");
            if (elements.isEmpty()) {
                System.err.println("Keine Elemente");
                return list;
            }
            Elements klassen = elements.get(0).child(0).child(0).children();
            String link = null;
            for (int i = 0; i < klassen.size(); i++) {
                if (klassen.get(i).child(0).text().equalsIgnoreCase(klasse)) {
                    link = klassen.get(i).child(0).attr("href");
                }
            }
            if (link == null) return list;
            document = Jsoup.parse(getHTML(BASE_URL + "/vertretungsplan/" + link));
            elements = document.getElementsByTag("tbody");
            Elements trs = elements.get(0).children();
            for (int i = 0; i < trs.size(); i++) {
                list.add(new Eintrag(trs.get(i), BASE_URL + "/vertretungsplan/" + link));
            }
            return list;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return list;
        }
    }
}
