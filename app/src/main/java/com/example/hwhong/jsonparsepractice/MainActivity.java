package com.example.hwhong.jsonparsepractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.hwhong.jsonparsepractice.models.MovieModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lvMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start

        lvMovies = (ListView) findViewById(R.id.lvMovies);

        new JSONTask().execute("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");

    }

    public class JSONTask extends AsyncTask<String, String, List<MovieModel>> {

        @Override
        protected List<MovieModel> doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;

            //reads text from a character-input stream, buffering characters so as to provide for
            // the efficient reading of characters, arrays, and lines.
            BufferedReader reader = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                //show objects as InputStream
                //so that the inputStream can later be parsed
                InputStream inputStream = urlConnection.getInputStream();
                //initializing the bufferedreader which is used to read the urlconnection
                reader = new BufferedReader(new InputStreamReader(inputStream));

                //used when there is a necessity to make a lot of modifications to Strings of
                // characters, a very flexible string object
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";

                //add the contents from the url to the stringBuffer
                //to be parsed later
                while((line = reader.readLine()) !=  null) {
                    stringBuffer.append(line);
                }

                //this part is passed into onPostExecute method
                String finalJson = stringBuffer.toString();

                //Creating a new JSONObject with name/value mappings from the JSON string
                JSONObject parentJson = new JSONObject(finalJson);
                //retrieve the array from the JSONObject
                JSONArray parentArray = parentJson.getJSONArray("movies");

                List<MovieModel> movieModelList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++) {
                    MovieModel movieModel = new MovieModel();

                    JSONObject childJson = parentArray.getJSONObject(i);

                    //getting the data from the individual JSON objects
                    movieModel.setMovie(childJson.getString("movie"));
                    movieModel.setYear(childJson.getInt("year"));
                    movieModel.setRating((float)childJson.getDouble("rating"));
                    movieModel.setDirector(childJson.getString("director"));
                    movieModel.setDuration(childJson.getString("duration"));
                    movieModel.setTagline(childJson.getString("tagline"));
                    movieModel.setImage(childJson.getString("image"));
                    movieModel.setStory(childJson.getString("story"));

                    List<MovieModel.Cast> castList = new ArrayList<>();
                    for (int k = 0; k < childJson.getJSONArray("cast").length(); k++) {
                        JSONObject jsonObject = childJson.getJSONArray("cast").getJSONObject(k);
                        MovieModel.Cast cast = new MovieModel.Cast();

                        cast.setName(jsonObject.getString("name"));

                        castList.add(cast);
                    }
                    movieModel.setCastList(castList);
                    movieModelList.add(movieModel);
                }

                return movieModelList;

            } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
        }

        //the result from doInBackground() is passed in here as paramater
        @Override
        protected void onPostExecute(List<MovieModel> result) {
            //the ui work that is not background related at all
            super.onPostExecute(result);

            //public MovieAdapter(Context context, int resource, List<MovieModel> objects)
            MovieAdapter movieAdapter = new MovieAdapter(getApplicationContext(),
                    R.layout.row, result);
            lvMovies.setAdapter(movieAdapter);
            //sets data here
        }
    }

    public class MovieAdapter extends ArrayAdapter {

        private List<MovieModel> movieModelList;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(Context context, int resource, List<MovieModel> objects) {
            super(context, resource, objects);
            //the fully parsed JSONobject in the list of MovieModel format
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null ){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.ivMovieIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvMovie = (TextView) convertView.findViewById(R.id.tvMovie);
                holder.tvTagline = (TextView) convertView.findViewById(R.id.tagline);
                holder.tvDuration = (TextView) convertView.findViewById(R.id.duration);
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvYear);
                holder.tvDirector = (TextView) convertView.findViewById(R.id.tvDirector);

                holder.ratingBar = (RatingBar) convertView.findViewById(R.id.ratingBar);
                holder.tvCast = (TextView) convertView.findViewById(R.id.cast);
                holder.tvStory = (TextView) convertView.findViewById(R.id.tvStory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            //if image is not loaded yet operations
            ImageLoader.getInstance().displayImage(movieModelList.
                    get(position).getImage(), holder.ivMovieIcon, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            holder.tvMovie.setText(movieModelList.get(position).getMovie());
            holder.tvTagline.setText(movieModelList.get(position).getTagline());
            holder.tvYear.setText("Year: " + movieModelList.get(position).getYear());
            holder.tvDuration.setText("Duration: " + movieModelList.get(position).getDuration());
            holder.tvDirector.setText("Director: " + movieModelList.get(position).getDirector());
            holder.tvStory.setText("Synopsis: " + movieModelList.get(position).getStory());

            StringBuffer stringBuffer = new StringBuffer();
            for (MovieModel.Cast  cast : movieModelList.get(position).getCastList()) {
                stringBuffer.append(cast.getName() +" , ");
            }
            holder.tvCast.setText("Cast: " + stringBuffer);

            holder.ratingBar.setRating(movieModelList.get(position).getRating()/2);

            return convertView;
        }

        class ViewHolder {
            //viewholder makes performance faster
            //since findViewById is very inefficient and costly
            //viewholder resolves that problem
            private ImageView ivMovieIcon;
            private TextView tvMovie;
            private TextView tvTagline;
            private TextView tvDuration;
            private TextView tvYear;
            private TextView tvDirector;

            private RatingBar ratingBar;
            private TextView tvCast;
            private TextView tvStory;
        }
    }
}
