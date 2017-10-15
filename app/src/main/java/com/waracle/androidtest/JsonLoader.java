package com.waracle.androidtest;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created by rgher on 10/14/2017.
 */

public class JsonLoader extends AsyncTaskLoader<String> {
    Context mContext;
    String mUri;
    String myJsonArray = null;

    public JsonLoader(Context context, String uri) {
        super(context);
        mContext = context;
        mUri = uri;
    }

    @Override
    public String loadInBackground() {
        try {
            myJsonArray = loadData(mUri);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return myJsonArray;
    }

    @Override
    protected void onStartLoading() {
        if (myJsonArray != null) {
            deliverResult(myJsonArray);
        } else {
            forceLoad();
        }
    }

    public void deliverResult(String data) {
        myJsonArray = data;
        super.deliverResult(data);
    }


    private String loadData(String jsonString) throws IOException, JSONException {
        URL url = new URL(jsonString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");

            InputStream in = null;
            if ("gzip".equals(urlConnection.getContentEncoding())) {
                in = new GZIPInputStream(urlConnection.getInputStream());
            }
            else {
                in = urlConnection.getInputStream();
            }


            byte[] bytes = readUnknownFully(in);

            // Read in charset of HTTP content.
            String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, charset);
            // Read string as JSON.
            return jsonText;
        } finally {
            urlConnection.disconnect();
        }
    }


    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }

    public static byte[] readUnknownFully(InputStream stream) throws IOException {
        // Read in stream of bytes
        ArrayList<Byte> data = new ArrayList<>();
        while (true) {
            int result = stream.read();
            if (result == -1) {
                break;
            }
            data.add((byte) result);
        }

        // Convert ArrayList<Byte> to byte[]
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get(i);
        }

        // Return the raw byte array.
        return bytes;
    }


}
