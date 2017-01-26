package com.example.lucasfernandez.practicarest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import models.MovieModel;

public class MainActivity extends AppCompatActivity {

    ListView lvMovies;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Cargando...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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


        lvMovies = (ListView)findViewById(R.id.lvMovies);
    }


    public class  JSONTask  extends AsyncTask<String,String,List<MovieModel>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<MovieModel> doInBackground(String...params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson =  buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("movies");

                List<MovieModel> movieModelList = new ArrayList<>();

                for(int i=0; i<parentArray.length();i++ ){

                    JSONObject finalObject = parentArray.getJSONObject(i);

                    MovieModel movieModel = new MovieModel();

                    movieModel.setMovie(finalObject.getString("movie"));
                    movieModel.setYear(finalObject.getInt("year"));
                    movieModel.setRating((float)finalObject.getDouble("rating"));
                    movieModel.setDuration(finalObject.getString("duration"));
                    movieModel.setDirector(finalObject.getString("director"));
                    movieModel.setTagline(finalObject.getString("tagline"));
                    movieModel.setImage(finalObject.getString("image"));
                    movieModel.setStory(finalObject.getString("story"));

                    List<MovieModel.Cast> castList = new ArrayList<>();

                    for (int j=0;j<finalObject.getJSONArray("cast").length();j++){
                        MovieModel.Cast cast = new MovieModel.Cast();
                        cast.setName(finalObject.getJSONArray("cast").getJSONObject(j).getString("name"));
                        castList.add(cast);
                    }
                    movieModel.setCastList(castList);
                    movieModelList.add(movieModel);

                    //finalBufferData.append(movieName + "-" + year + "\n");
                }
                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
                try {if (reader!=null){
                    reader.close();
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(List<MovieModel> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(),R.layout.row,result);
                lvMovies.setAdapter(adapter);
            }else {
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server, please check url.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class MovieAdapter extends ArrayAdapter{

        private List <MovieModel> movieModelList1;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(Context context, int resource, List<MovieModel> objects) {
            super(context, resource, objects);
            movieModelList1 = objects;
            this.resource = resource;
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

           if (convertView == null){

               holder = new ViewHolder();
               convertView = inflater.inflate(resource,null);

               holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
               holder.tvMoive = (TextView)convertView.findViewById(R.id.tvMovie);
               holder.tvTagline = (TextView)convertView.findViewById(R.id.tvTagline);
               holder.tvYear = (TextView)convertView.findViewById(R.id.tvYear);
               holder.tvDuration = (TextView)convertView.findViewById(R.id.tvDuration);
               holder.tvDirector = (TextView)convertView.findViewById(R.id.tvDirector);
               holder.rbMovie = (RatingBar)convertView.findViewById(R.id.rbMovie);
               holder.tvCast = (TextView)convertView.findViewById(R.id.tvCast);
               holder.tvStory = (TextView)convertView.findViewById(R.id.tvStory);

               convertView.setTag(holder);
           }else {
               holder = (ViewHolder)convertView.getTag();
           }


            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            // Then later, when you want to display image
            ImageLoader.getInstance().displayImage(movieModelList1.get(position).getImage(), holder.ivIcon, new ImageLoadingListener() {
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
            }); // Default options will be used

            holder.tvMoive.setText(movieModelList1.get(position).getMovie());
            holder.tvTagline.setText(movieModelList1.get(position).getTagline());
            holder.tvYear.setText(movieModelList1.get(position).getYear()+"");
            holder.tvDuration.setText(movieModelList1.get(position).getDuration());
            holder.tvDirector.setText(movieModelList1.get(position).getDirector()+"");
            holder.tvStory.setText(movieModelList1.get(position).getStory());

            StringBuffer stringBuffer = new StringBuffer();

            for(MovieModel.Cast Cast: movieModelList1.get(position).getCastList()){
                stringBuffer.append(Cast.getName() + ", ");
            }

            holder.tvCast.setText(stringBuffer);
            holder.rbMovie.setRating(movieModelList1.get(position).getRating()/2);
            return convertView;
        }

        class ViewHolder{
            private ImageView ivIcon;
            private TextView  tvMoive;
            private TextView  tvTagline;
            private TextView  tvYear;
            private TextView  tvDuration;
            private TextView  tvDirector;
            private RatingBar rbMovie;
            private TextView  tvCast;
            private TextView  tvStory;
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

            new JSONTask().execute("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
