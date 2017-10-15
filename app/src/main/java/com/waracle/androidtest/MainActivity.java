package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";
    private static final int ID_LOADER = 30;
    private static final int ID_IMG = 40;
    private static String BUNDLE_EXTRA = "BUNDLE_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                   .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            getSupportFragmentManager().beginTransaction()
                    .detach(getSupportFragmentManager().findFragmentById(R.id.container))
                    .commit();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment is responsible for loading in some JSON and
     * then displaying a list of cakes with images.
     * Fix any crashes
     * Improve any performance issues
     * Use good coding practices to make code more secure
     */
    public static class PlaceholderFragment extends ListFragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();
        private android.support.v4.app.LoaderManager.LoaderCallbacks<String> callback;
        private Loader<JSONArray> asyncTaskLoader;
        private Bundle queryBundle;
        private Bundle imageBundle;
        private android.support.v4.app.LoaderManager loaderManager;

        private ListView mListView;
        private MyAdapter mAdapter;

        public PlaceholderFragment() { /**/ }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mListView = (ListView) rootView.findViewById(android.R.id.list);

            /*StrictMode Handler for SDK>8

            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            */

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Create and set the list adapter.
            mAdapter = new MyAdapter();
            mListView.setAdapter(mAdapter);

            //Create LoaderCallback and LoaderManager
            callback = this;
            loaderManager = getLoaderManager();

            queryBundle = new Bundle();
            queryBundle.putString(BUNDLE_EXTRA, JSON_URL);

            asyncTaskLoader =  loaderManager.getLoader(ID_LOADER);
            if(asyncTaskLoader == null) {
                loaderManager.initLoader(ID_LOADER, queryBundle, callback);
            } else {
                loaderManager.restartLoader(ID_LOADER, queryBundle, callback);
            }

        }

        //LOADER INTERFACE METHODS

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            if(id==ID_LOADER){
                return new JsonLoader(getContext(), JSON_URL);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            try {
                JSONArray array = new JSONArray(data);
                mAdapter.setItems(array);
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.v("ROMAN", e.getMessage());
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            Toast.makeText(getContext(), "LOADER RESET", Toast.LENGTH_SHORT);
        }



        //ADAPTER

        private class MyAdapter extends BaseAdapter {

            // Can you think of a better way to represent these items???
            //REPLY: Yes, I would Display them on the main activity using a RecyclerView with an Adapter and Picasso library

            private JSONArray mItems;
            private ImageLoader mImageLoader;

            public MyAdapter() {
                this(new JSONArray());
            }

            public MyAdapter(JSONArray items) {
                mItems = items;
                mImageLoader = new ImageLoader();
            }

            @Override
            public int getCount() {
                return mItems.length();
            }

            @Override
            public Object getItem(int position) {
                try {
                    return mItems.getJSONObject(position);
                } catch (JSONException e) {
                    Log.e("", e.getMessage());
                }
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }



            @SuppressLint("ViewHolder")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View root = inflater.inflate(R.layout.list_item_layout, parent, false);
                if (root != null) {
                    TextView title = (TextView) root.findViewById(R.id.title);
                    TextView desc = (TextView) root.findViewById(R.id.desc);
                    ImageView image = (ImageView) root.findViewById(R.id.image);
                    try {

                        JSONObject object = (JSONObject) getItem(position);

                        //Capitalize Title
                        String myTitle = object.getString("title");
                        title.setText(capitalizeWords(myTitle));

                        //Capitalize Description
                        String myDescription = object.getString("desc");
                        myDescription = myDescription.substring(0, 1).toUpperCase() + myDescription.substring(1).toLowerCase();
                        desc.setText(myDescription);

                        mImageLoader.load(object.getString("image"), image);

                        /*Picasso.with(getContext())
                                .load(object.getString("image"))
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .into(image);
                        */

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return root;
            }

            public void setItems(JSONArray items) {
                mItems = items;
            }

            private String capitalizeWords(String word){
                String[] words = word.split(" ");
                StringBuilder sb = new StringBuilder();
                if (words[0].length() > 0) {
                    sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                    for (int i = 1; i < words.length; i++) {
                        sb.append(" ");
                        sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
                    }
                }
                return  sb.toString();
            }
        }
    }
}
