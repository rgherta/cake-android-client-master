package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by rgher on 10/15/2017.
 */

public class ImageRequest extends AsyncTask<String, Void, Bitmap> {
    private ImageView myImageView;

    public ImageRequest(ImageView imageView){
        this.myImageView = imageView;
        }

    @Override
    protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(params[0]).openConnection();
                connection.setRequestProperty("Accept-Encoding", "gzip");

                InputStream stream = null;
                if ("gzip".equals(connection.getContentEncoding())) {
                    stream = new GZIPInputStream(connection.getInputStream());
                }
                else {
                    stream = connection.getInputStream();
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 7;
                options.inBitmap = bitmap;

                bitmap = BitmapFactory.decodeStream(stream, null, options);

                connection.disconnect();
                stream.close();

            } catch (IOException e) {
            e.printStackTrace();
            }
            return bitmap;
            }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(bitmap == null) {
            myImageView.setImageResource(R.mipmap.ic_launcher);
        } else if(myImageView.getVisibility() == View.VISIBLE){
            myImageView.setImageBitmap(bitmap);
        }
    }
}
