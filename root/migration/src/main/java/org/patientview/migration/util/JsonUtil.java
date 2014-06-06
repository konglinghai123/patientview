package org.patientview.migration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.Lookup;
import org.patientview.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class JsonUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    public static final String fhirUrl = "http://localhost:8083";
    public static final String pvUrl = "http://localhost:8083";

    private JsonUtil() {}


    public static <T, V extends HttpEntityEnclosingRequestBase> T jsonRequest(String url, Class<T> responseObject, Object requestObject, Class<V> httpMethod) {

        Gson gson = new Gson();
        HttpClient httpClient = getThreadSafeClient();
        V method;

        try {
            Constructor<V> cons = httpMethod.getConstructor(String.class);
            method = cons.newInstance(url);
        } catch (Exception e) {
            LOG.error("Error creating request type {}", e.getCause());
            return null;
        }

        try {
            if (requestObject != null) {

            } else {
                requestObject = new Object();
            }

            String json = gson.toJson(requestObject);
            LOG.info("Adding the following to request: " + json);
            StringEntity puttingString = new StringEntity(json);
            method.setEntity(puttingString);

        } catch (Exception e) {
            LOG.error("Error creating request object {}", e.getCause());
            return null;
        }

        method.setHeader("Content-type", "application/json");
        BufferedReader br;
        StringBuilder output = new StringBuilder();

        try {

            HttpResponse httpResponse = httpClient.execute(method);

            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= 300) {
                LOG.error("Failed to put request for {} response was {}", url, httpResponse.getStatusLine());
                return null;
            }

            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
            br.close();

        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", url, e.getCause());
            e.printStackTrace();
            return null;

        } finally {
            //httpClient.getConnectionManager().shutdown();
        }


        return gson.fromJson(output.toString(), responseObject);

    }



    public static HttpResponse gsonPut(String postUrl, Object object) throws Exception {

        Gson gson = new Gson();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut put = new HttpPut(postUrl);

        if (object != null) {
            String json = gson.toJson(object);
            LOG.debug("Putting the following: " + json);
            StringEntity puttingString = new StringEntity(json);
            put.setEntity(puttingString);
        }

        put.setHeader("Content-type", "application/json");
        return httpClient.execute(put);

    }


    public static  HttpResponse gsonPost(String postUrl, Object object) throws Exception {

        Gson gson = new Gson();

        String json = gson.toJson(object);
        LOG.info("Posting the following: " + json);
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);

        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        return httpClient.execute(post);

    }



    public static <T> List<T> getStaticDataList(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new Gson();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<T> data = gson.fromJson(output.toString(), new TypeToken<List<T>>(){}.getType());

        return data;

    }


    public static List<Lookup> getStaticDataLookups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new Gson();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Lookup> data = gson.fromJson(output.toString(), new TypeToken<List<Lookup>>(){}.getType());

        return data;

    }

    public static List<Feature> getStaticDataFeatures(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Feature> data = gson.fromJson(output.toString(), new TypeToken<List<Feature>>(){}.getType());

        return data;

    }

    public static List<Group> getGroups(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Group> data = gson.fromJson(output.toString(), new TypeToken<List<Group>>(){}.getType());

        return data;

    }

    public static List<Role> getRoles(String getUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        HttpGet get = new HttpGet(getUrl);
        get.addHeader("accept", "application/json");

        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpResponse.getStatusLine().getStatusCode());
        }

        BufferedReader br;

        StringBuilder output = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));

            String s;
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
        } catch (Exception e) {
            LOG.error("Exception trying to get data from: {} cause: {}", getUrl, e.getCause());
            e.printStackTrace();
        }

        List<Role> data = gson.fromJson(output.toString(), new TypeToken<List<Role>>(){}.getType());

        return data;

    }


    public static DefaultHttpClient getThreadSafeClient()  {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }


}
