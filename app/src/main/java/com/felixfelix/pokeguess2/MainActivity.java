package com.felixfelix.pokeguess2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> pokeURLs = new ArrayList<String>();
    ArrayList<String> expertURLs = new ArrayList<String>();
    ArrayList<String> pokeNames = new ArrayList<String>();
    int chosenPokemon = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];
    Boolean expert = false;

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    CheckBox expertBox;



    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data !=-1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read(); // move to next character in stream
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    public void chooseAnswer(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! It was " + pokeNames.get(chosenPokemon), Toast.LENGTH_SHORT).show();
        }
        createNewQuestion();
    }

    public void changeExpert(View view) {
        expert = !expert;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        expertBox = (CheckBox) findViewById(R.id.expert);

        String result = null;
        DownloadTask task = new DownloadTask();

        try {
            result = task.execute("https://pokeapi.co/api/v2/generation/1/").get();

            JSONObject jsonObject = new JSONObject(result);

            String pokemonList = jsonObject.getString("pokemon_species");

            JSONArray pokemons = new JSONArray(pokemonList);

            for (int i = 0; i < pokemons.length(); i++) {
                JSONObject jsonPart = pokemons.getJSONObject(i);

                String name = jsonPart.getString("name");
                String url = jsonPart.getString("url");

                String imageUrl = "https://img.pokemondb.net/artwork/" + name + ".jpg";
//                String expertImageUrl = "https://img.pokemondb.net/sprites/black-white/anim/back-normal/" + name + ".gif";
                String expertImageUrl = "https://img.pokemondb.net/sprites/red-blue/back-normal/" + name + ".png";



                expertURLs.add(expertImageUrl);
                pokeURLs.add(imageUrl);
                pokeNames.add(name);
            }

            createNewQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    public void createNewQuestion() {

        Random random = new Random();
        chosenPokemon = random.nextInt(pokeURLs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap pokeImage;

        try {
            if (expert) {
                pokeImage = imageTask.execute(expertURLs.get(chosenPokemon)).get();

            } else {
                pokeImage = imageTask.execute(pokeURLs.get(chosenPokemon)).get();

            }
//            pokeImage = imageTask.execute(pokeURLs.get(chosenPokemon)).get();
            imageView.setImageBitmap(pokeImage);


            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;
            for (int i=0; i < 4;  i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = pokeNames.get(chosenPokemon);
                } else {
                    incorrectAnswerLocation = random.nextInt(pokeURLs.size());
                    answers[i] = pokeNames.get(incorrectAnswerLocation);

                    while (incorrectAnswerLocation == chosenPokemon) {
                        answers[i] = pokeNames.get(incorrectAnswerLocation);
                    }
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
