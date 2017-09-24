package focus.flashcardwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nealroessler on 8/16/17.
 */

public class SimpleWidgetProvider extends AppWidgetProvider {

    private static String TAG = "FLASHCARD_WIDGET";
    private static ArrayList<Flashcard> wordsList = new ArrayList<>();
    private static boolean firstTime = true;
    private static int wordIndex = 0;
    private static boolean starOnly = false;
    private static String buttonkey = "a";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (firstTime == true) {

            firstTime = false;
            try {
                wordsList = readFile(context);
                wordIndex = getWordIndex(context);
                ArrayList<Integer> starsIndexes = getStarIndexes(context);

//                if (wordIndex >= wordsList.size() || Collections.max(starsIndexes) >= wordsList.size()) {
//                    wordIndex = 0;
//                    starsIndexes = new ArrayList<>();
//                }

                setStars(starsIndexes);
                //Collections.shuffle(wordsList);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        final int count = appWidgetIds.length;



        for (int i = 0; i < count; i++) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
            int widgetId = appWidgetIds[i];

            if (buttonkey != null && buttonkey.equals("staronly")) {
                starOnly = !starOnly;
                if (!isThereAtLeastOneStarredFlashcard(wordsList)) {
                    starOnly = false;
                    // Do a toast and say only can turn on when there are starred things
                } else if (starOnly == true){
                    flipAllToFront(wordsList);
                    wordIndex = getNextStarOnList(wordIndex, wordsList, true);
                } else {
                    flipAllToFront(wordsList);
                }
            }


            if ( buttonkey != null && buttonkey.equals("back")) {
                if (wordsList.get(wordIndex).isDisplayFront()) {

                    if (!starOnly) {
                        wordIndex--;
                    } else {
                        wordIndex = getNextStarOnList(wordIndex, wordsList, false);
                    }

                    if(wordIndex <= 0) {
                        flipAllToFront(wordsList);
                        wordIndex = wordsList.size() -1;
                    }
                } else {
                    // Flip to front if it is on back
                    wordsList.get(wordIndex).setDisplayFront(true);
                }
            }

            if ( buttonkey != null && buttonkey.equals("next")) {

                // Flip to back if is front
                if(wordsList.get(wordIndex).isDisplayFront()) {
                    wordsList.get(wordIndex).setDisplayFront(false);
                } else {

                    if (!starOnly) {
                        wordIndex++;
                    } else {
                        wordIndex = getNextStarOnList(wordIndex, wordsList, true);
                    }

                    if(wordIndex >= wordsList.size()) {
                        flipAllToFront(wordsList);
                        wordIndex = 0;
                    }
                }
            }

            // Set current flashcard!
            Flashcard currentFlashcard = wordsList.get(wordIndex);

            if ( buttonkey != null && buttonkey.equals("star")) {
                currentFlashcard.setStarred(!currentFlashcard.isStarred());
                try {
                    saveStarInedexes(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!isThereAtLeastOneStarredFlashcard(wordsList)) {
                    starOnly = false;
                    flipAllToFront(wordsList);
                }
            }

            // Update text
            String displayText = currentFlashcard.isDisplayFront() ? currentFlashcard.getFront() : currentFlashcard.getBack();
            remoteViews.setTextViewText(R.id.textView, displayText);
            if (currentFlashcard.isDisplayFront()) {
                remoteViews.setFloat(R.id.textView, "setTextSize", 44);
            } else {
                remoteViews.setFloat(R.id.textView, "setTextSize", 24);
            }


            // Update star
            if (currentFlashcard.isStarred()) {
                remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.newstaron);
            } else {
                remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.newstaroff);
            }

            // Update staronly
            if (starOnly) {
                remoteViews.setImageViewResource(R.id.starsonly, R.drawable.newstaron);
            } else {
                remoteViews.setImageViewResource(R.id.starsonly, R.drawable.newstaroff);
            }

            // Save word index
            try {
                saveWordIndex(context, wordIndex);
            } catch (IOException e) {
                Log.i(TAG, "Trouble Saving Word Index " + e);
            }

            registerOnclickListeners(context, appWidgetIds, remoteViews, appWidgetManager, widgetId);
        }
    }

    private boolean isThereAtLeastOneStarredFlashcard(ArrayList<Flashcard> flashcards) {
        for (Flashcard flashcard : flashcards) {
            if (flashcard.isStarred()) {
                return  true;
            }
        }
        return false;
    }

    private int getNextStarOnList(int wordCount, ArrayList<Flashcard> flashcards, boolean forwards) {
        if (forwards) {
            int counter = 0;
            for (Flashcard flashcard : flashcards) {
                Log.v(TAG, wordCount + " counter: " + counter);
                if (flashcard.isStarred() && counter > wordCount) {
                    return counter;
                }
                counter ++;
            }

            // if we made it this far the starred card must be up front
            // Lets flip cards again
            flipAllToFront(flashcards);
            counter = 0;
            for (Flashcard flashcard : flashcards) {
                if (flashcard.isStarred()) {
                    return counter;
                }
                counter ++;
            }
        } else {
            int counter = flashcards.size()-1;
            for (int i = flashcards.size()-1; i >= 0; i--) {
                Flashcard flashcard = flashcards.get(i);
                Log.v(TAG, wordCount + " counter: " + counter);
                if (flashcard.isStarred() && counter < wordCount) {
                    return counter;
                }
                counter --;
            }

            // if we made it this far the starred card must be up front
            // Lets flip cards again
            flipAllToFront(flashcards);
            counter = flashcards.size()-1;
            for (int i = flashcards.size()-1; i >= 0; i--) {
                Flashcard flashcard = flashcards.get(i);
                if (flashcard.isStarred()) {
                    return counter;
                }
                counter --;
            }
        }

        return -1;
    }

    private void registerOnclickListeners(Context context, int[] appWidgetIds, RemoteViews remoteViews, AppWidgetManager appWidgetManager, int widgetId) {
        // Register an onClickListener for 1st button
        Intent intent = new Intent(context, SimpleWidgetProvider.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra("buttonkey", "next");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.forward, pendingIntent);

        // Register an onClickListener for 2nd button............
        Intent intent2 = new Intent(context, SimpleWidgetProvider.class);

        intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent2.putExtra("buttonkey", "star");

        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context,
                1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.toggleStar, pendingIntent2);


        // Register an onClickListener for Third button............
        Intent intent3 = new Intent(context, SimpleWidgetProvider.class);

        intent3.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent3.putExtra("buttonkey", "back");

        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context,
                2, intent3, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.back, pendingIntent3);


        // Register an onClickListener for Fourth button............
        Intent intent4 = new Intent(context, SimpleWidgetProvider.class);

        intent4.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent4.putExtra("buttonkey", "staronly");

        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(context,
                3, intent4, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.starsonly, pendingIntent4);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle b =intent.getExtras();
        try {
            buttonkey = b.getString("buttonkey");
        } catch (Exception e) {
            // TODO: handle exception
        }

        super.onReceive(context, intent);

    }

    private void flipAllToFront(ArrayList<Flashcard> cards) {
        //cards.stream().map(card->card.setDisplayFront(true));
        for (Flashcard card : cards) {
            card.setDisplayFront(true);
        }
    }

    private static void resetStars(ArrayList<Flashcard> cards) {
        for (Flashcard card : cards) {
            card.setStarred(false);
        }
    }

    private void setStars(ArrayList<Integer> starsIndexes) {
        for (int i = 0; i < starsIndexes.size(); i++) {
            wordsList.get(i).setStarred(true);
        }
    }

    private static void saveStarInedexes(Context context) throws IOException {
        File file = new File(context.getFilesDir().toString() + "/star-index");
        file.delete();

        ArrayList<Integer> stars = new ArrayList<>();
        for (int i = 0; i < wordsList.size(); i++) {
            if(wordsList.get(i).isStarred()) {
                stars.add(i);
            }
        }

        //OutputStream outputStream = new ObjectOutputStream(new FileOutputStream(context.getFilesDir().toString() + "/star-index"));
        //outputStream.w

        Writer fileWriter = new FileWriter(context.getFilesDir().toString() + "/star-index");
        for (Integer star : stars) {
            fileWriter.write(star+"\n");
        }
        fileWriter.close();
    }

    private ArrayList<Integer> getStarIndexes(Context context) {
        Log.i(TAG, "Start getStarIndexes");
        Reader fileReader;
        ArrayList<Integer> starIndexes = new ArrayList<>();
        try {
            fileReader = new FileReader(context.getFilesDir().toString() + "/star-index");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            //wordIndexString = bufferedReader.readLine();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                starIndexes.add(Integer.valueOf(line));
            }

            fileReader.close();
        } catch (Exception e) {
            Log.i(TAG, "error getting word index " + e.toString());
            return new ArrayList<>();
        }

        Log.i(TAG, "END getStarIndexes with - " + starIndexes);
        return starIndexes;
    }

    private static void saveWordIndex(Context context, Integer currentWordIndex) throws IOException {
        //FileOutputStream outputStream = new FileOutputStream(new File(context.getFilesDir().toString() + "/word-index"));
        File file = new File(context.getFilesDir().toString() + "/word-index");
        file.delete();

        Writer fileWriter = new FileWriter(context.getFilesDir().toString() + "/word-index");
        fileWriter.write("" + currentWordIndex);
        fileWriter.close();
    }


    private int getWordIndex(Context context) throws IOException {

        Log.i(TAG, "Start getWordIndex");
        Reader fileReader;
        String wordIndexString = "0";
        try {
            fileReader = new FileReader(context.getFilesDir().toString() + "/word-index");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            wordIndexString = bufferedReader.readLine();
            fileReader.close();
        } catch (Exception e) {
            Log.i(TAG, "error getting word index " + e.toString());
            return  0;
        }

        Log.i(TAG, "END getWordIndex with - " + wordIndexString);

        return wordIndexString == null || !isNumeric(wordIndexString) ? 0 : new Integer(wordIndexString);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private ArrayList<Flashcard> readFile(Context context) throws FileNotFoundException {
        Log.i("WIDGET FILE DIR", context.getFilesDir().toString());
        //FileInputStream inputStream = new FileInputStream(new File(context.getFilesDir().toString() + "/chinese-test"));


        InputStream inputStream;

        if (new File(context.getFilesDir().toString() + "/" + MainActivity.CHOSENFILENAME).exists()) {
            Log.i(TAG, context.getFilesDir().toString());
            inputStream = new FileInputStream(new File(context.getFilesDir().toString() + "/" + MainActivity.CHOSENFILENAME));
        } else if (new File(context.getFilesDir().toString() + "/" + MainActivity.DEFAULT_DEFINITIONS_FILENAME).exists()) {
            Log.i(TAG, context.getFilesDir().toString());
            inputStream = new FileInputStream(new File(context.getFilesDir().toString() + "/" + MainActivity.DEFAULT_DEFINITIONS_FILENAME));
        } else {
            // No file exist, create DEFAULT
            Log.i(TAG, context.getFilesDir().toString());
            String string = MainActivity.getIntegratedChinese();
            inputStream = new ByteArrayInputStream(string.getBytes());
        }

        ArrayList<Flashcard> words = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                String front = RowData[0];
                String back = RowData[1];

                Flashcard flashcard = new Flashcard(front.replaceAll("[0-9]", "").replace(".",""), back, true);

                words.add(flashcard);
                // do something with "data" and "value"
            }
        }
        catch (IOException ex) {
            // handle exception
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                // handle exception
            }
        }

        return words;
    }


    public static void reset(Context context, String flashcardSet) {
        try {
            if (wordsList != null) {
                firstTime = true;
                saveWordIndex(context, 0);
                //reset stars
                resetStars(wordsList);
                saveStarInedexes(context);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
                remoteViews.setTextViewText(R.id.textView, flashcardSet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


