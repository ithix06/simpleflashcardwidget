package focus.flashcardwidget;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by nealroessler on 8/16/17.
 */

public class SimpleWidgetProvider extends AppWidgetProvider {

    private static ArrayList<Flashcard> wordsList = new ArrayList<>();
    private static boolean firstTime = true;
    private static int wordCount = 0;
    private static String buttonkey = "a";



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        boolean star = true;

        if (firstTime == true) {
            firstTime = false;
            try {
                wordsList = readFile();
                Collections.shuffle(wordsList);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        final int count = appWidgetIds.length;



        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            //String number = String.format("%03d", (new Random().nextInt(900) + 100));
            Flashcard currentFlashcard;
            if(wordsList.get(wordCount).isDisplayFront()) {
                currentFlashcard = wordsList.get(wordCount);
                
                if ( buttonkey != null && buttonkey.equals("next")) {
                    wordsList.get(wordCount).setDisplayFront(false);
                }
                //wordsList.remove(0)
            } else {

                if ( buttonkey != null && buttonkey.equals("next")) {
                    wordCount++;
                }

                if(wordCount >= wordsList.size()) {
                    flipToFront(wordsList);
                    wordCount = 0;
                }
                currentFlashcard = wordsList.get(wordCount);
            }

            String displayText = currentFlashcard.isDisplayFront() ? currentFlashcard.getFront() : currentFlashcard.getBack();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);
            remoteViews.setTextViewText(R.id.textView, displayText);



            if ( buttonkey != null && buttonkey.equals("star")) {
                currentFlashcard.setStarred(!currentFlashcard.isStarred());
                if (currentFlashcard.isStarred()) {
                    remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.staron);
                } else {
                    remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.staroff);
                }

                Intent intent = new Intent(context, SimpleWidgetProvider.class);

                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                intent.putExtra("buttonkey", "next");

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

                // Register an onClickListener for 2nd button............
                Intent intent2 = new Intent(context, SimpleWidgetProvider.class);

                intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                intent2.putExtra("buttonkey", "star");

                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context,
                        1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                remoteViews.setOnClickPendingIntent(R.id.toggleStar, pendingIntent2);

                appWidgetManager.updateAppWidget(widgetId, remoteViews);

                return;

            }

            // update star image regardless
            if (currentFlashcard.isStarred()) {
                remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.staron);
            } else {
                remoteViews.setImageViewResource(R.id.toggleStar, R.drawable.staroff);
            }

//            Intent intent = new Intent(context, SimpleWidgetProvider.class);
//            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
//
//
//
//
//            remoteViews.setOnClickPendingIntent(R.id.toggleStar, pendingIntent);
//            appWidgetManager.updateAppWidget(widgetId, remoteViews);

            // Register an onClickListener for 1st button
            Intent intent = new Intent(context, SimpleWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.putExtra("buttonkey", "next");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            // Register an onClickListener for 2nd button............
            Intent intent2 = new Intent(context, SimpleWidgetProvider.class);

            intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent2.putExtra("buttonkey", "star");

            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context,
                    1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.toggleStar, pendingIntent2);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
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

    private void flipToFront(ArrayList<Flashcard> cards) {
        //cards.stream().map(card->card.setDisplayFront(true));
        for (Flashcard card : cards) {
            card.setDisplayFront(true);
        }
    }

    private ArrayList<Flashcard> readFile() throws FileNotFoundException {
        //--- Suppose you have input stream `is` of your csv file then:

        FileInputStream inputStream = new FileInputStream(new File("/sdcard/Flashcard/chinese-all.csv"));
        //FileInputStream inputStream = new FileInputStream(new File("/sdcard/Flashcard/1000-popular-chinese"));

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


}


