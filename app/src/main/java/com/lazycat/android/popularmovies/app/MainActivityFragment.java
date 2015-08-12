package com.lazycat.android.popularmovies.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static final String FLAVOR_MOVIE_PARCEL_KEY = "flavorMovie.parcel.key";

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private FlavorMovieAdapter mFlavorMovieAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // create a adapter to establish a bridge between the grid view item and movie array data
        mFlavorMovieAdapter = new FlavorMovieAdapter(getActivity(), R.layout.list_item_poster, new ArrayList<FlavorMovie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mFlavorMovieAdapter);

        // Add onItemClickListener to handle what happen when an item is clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                FlavorMovie flavorMovie = mFlavorMovieAdapter.getItem(position);

                if (flavorMovie != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FLAVOR_MOVIE_PARCEL_KEY, flavorMovie);
                    intent.putExtras(bundle);

                    // start the detail activity
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "flavorMovie is null");
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchMovie();
    }

    private void fetchMovie() {
        // get sort by from share preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPref.getString(
                getString(R.string.pref_order_by_key),
                getString(R.string.pref_order_by_default));

        new FetchMovieTask().execute(sortBy);
    }

    private class FetchMovieTask extends AsyncTask<String, Void, FlavorMovie[]> {
        @Override
        protected FlavorMovie[] doInBackground(String... params) {
            if (params.length == 0)
                return null;

            String sortBy = params[0];
            String apiKey = getString(R.string.themoviedb_api_key);

            // download movies data from themoviedb
            String moviesJsonStr = DownloadUtils.discoverMoviesFromTheMovieDb(apiKey, sortBy);

            // parse the return JSON string to flavor movie object array
            return DownloadUtils.getMovieDataFromJson(moviesJsonStr);
        }

        @Override
        protected void onPostExecute(FlavorMovie[] flavorMovies) {
            super.onPostExecute(flavorMovies);

            mFlavorMovieAdapter.clear();
            for (FlavorMovie flavorMovie : flavorMovies) {
                mFlavorMovieAdapter.add(flavorMovie);
            }
        }
    }
}
