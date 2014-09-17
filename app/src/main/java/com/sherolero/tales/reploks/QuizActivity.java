package com.sherolero.tales.reploks;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class QuizActivity extends Activity {

    //constantes
    private static final String TAG = "QuizActivity";

    //membros privados
    private ArrayList<Questions> perguntas;
    private long seed;
    private int perguntaAtual;
    private int respostasCorretas;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        perguntas = new ArrayList<Questions>();

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("seed")){
                seed = savedInstanceState.getLong("seed");
            }else{
                seed = new Random().nextLong();
            }

            if(savedInstanceState.containsKey("perguntaAtual")){
                perguntaAtual = savedInstanceState.getInt("perguntaAtual");
            }else{
                perguntaAtual = 0;
            }

            if(savedInstanceState.containsKey("respostasCorretas")){
                respostasCorretas = savedInstanceState.getInt("respostasCorretas");
            }else{
                respostasCorretas = 0;
            }
        }else{
            //Sem nenhuma instancia salva
            seed = new Random().nextLong();
            perguntaAtual = 0;
            respostasCorretas = 0;
        }

        new LoadQuestionsTask().execute("perguntas");
        //setContentView(R.layout.activity_quiz);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState){
        outState.putLong("seed", seed);
        outState.putInt("perguntaAtual", perguntaAtual);
        outState.putInt("respostasCorretas", respostasCorretas);
        super.onSaveInstanceState(outState);
    }

    //TODO: Esse trecho de código possui muito codigo dentro do try/catch. Pensar o que pode ficar de fora
    public ArrayList<String> loadQuestions(final String questionFilePath){
        final ArrayList<String> perguntas = new ArrayList<String>();
        try{
            for(final String fileName: getAssets().list(questionFilePath)){
                final InputStream input = getAssets().open(questionFilePath+"/"+fileName);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                //Popula a lista de perguntas
                String inputLine;
                while((inputLine = reader.readLine()) != null){
                    //Ignorar comentarios no arquivo das perguntas
                    if(!inputLine.startsWith("//") && !(inputLine.length() == 0)){
                        perguntas.add(inputLine);
                    }
                }
            }
        }catch (final IOException e){
            Log.e(TAG, "IOException while reading questions from file.", e);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.question_reading_exception), Toast.LENGTH_LONG).show();
        }
        return perguntas;
    }

    private class LoadQuestionsTask extends AsyncTask<String, Integer, Void> {

        //TODO: Mover trecho de código do LoadQuestions para cá. LoadQuestions deve tratar arquivos individuais
        @Override
        protected Void doInBackground(String... params){
            final ArrayList<String> questionsTemp = new ArrayList<String>();
            for(String path: params){
                questionsTemp.addAll(loadQuestions(path));
            }

            //Randomiza a ordem das perguntas
            final Random rand = new Random(seed);
            Collections.shuffle(questionsTemp, rand);

            //TODO: Corrigir código duplicado de forma porca
            int failedParses = 0;
            perguntas = new ArrayList<Questions>();
            for(String s: questionsTemp){
                try{
                    perguntas.add(Questions.parse(s));
                }catch(final IllegalArgumentException e) {
                    failedParses++;
                    Log.e(TAG, "Unable to parse question: " + s, e);
                }
            }
            if(failedParses > 0){
                Toast.makeText(getApplicationContext(), String.format(getResources().getQuantityString(R.plurals.question_reading_parse_fail_number, failedParses), failedParses), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            //Se nenhuma pergunta for carregada, fecha a Activity
            if(perguntas.isEmpty()){
                QuizActivity.this.finish();
            }else{
                displayQuestion(perguntaAtual);
            }
        }

        private void displayQuestion(final int questionID){
            final TextView quizQuestion = (TextView)findViewById(R.id.quiz_question);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
