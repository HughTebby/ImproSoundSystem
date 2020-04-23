package com.hughtebby.improsoundsystem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.commons.io.comparator.NameFileComparator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    public static float SCALE;
    private static String PATH;
    protected ArrayList<ArrayList<Sample>> mSamples;
    protected ArrayList<Sample> mSample;
    protected ArrayList<SeekBar> volumeSliders;
    protected List<String> tab_list;
    private SampleAdapter mAdapter;
    private ArrayList<GridView> mSampleList;
    private TabHost myTabHost;
    private String tabName;
    private String buttonSize;
    private FrameLayout drawerLayout;
    private boolean pathError = false;
    private GlobalClass globalVariable;
    private Uri uri;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalVariable = (GlobalClass) getApplicationContext();

        SCALE = getApplicationContext().getResources().getDisplayMetrics().density;

        volumeSliders = new ArrayList<SeekBar>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PATH = sharedPref.getString("storage_dir", Environment.getExternalStorageDirectory() + "/ImproSoundSystem");

        //set volume visibility by default
        Boolean showVolumeDefault = sharedPref.getBoolean("show_volume_default", false);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("show_volume",showVolumeDefault);
        editor.commit();

        //check if directory still exists

        boolean firstUse = sharedPref.getBoolean("first_use", true);
        if (firstUse) {
            sharedPref.edit().putBoolean("first_use", false).apply();
        }
        boolean keepScreenOn = sharedPref.getBoolean("keep_screen_on", true);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        buttonSize = sharedPref.getString("button_size", "1");
        int maxTabs = Integer.parseInt(sharedPref.getString("max_tabs", "10"));
        int maxSounds = Integer.parseInt(sharedPref.getString("max_sounds", "100"));

        myTabHost = (TabHost) this.findViewById(R.id.tabHost);
        myTabHost.setup();

        tab_list = new ArrayList<String>();

        try {
            new File(PATH).mkdirs();
            int tabCounter = 0;
            for (File file : new File(PATH).listFiles()) {
                if (file.isDirectory() && tabCounter < maxTabs) {
                    tab_list.add(file.getName());
                    tabCounter++;
                }
            }
        } catch (Exception name) {
            pathError = true;
        }
        File varTmpDir = new File(PATH);
        if (!varTmpDir.exists()) {
            pathError = true;
        }

        if ( tab_list.size() != 0) {

            mSamples = new ArrayList<ArrayList<Sample>>();
            mSampleList = new ArrayList<GridView>();

            for (int i = 0; i < tab_list.size(); i++) {
                tabName = tab_list.get(i);
                final TabSpec x = myTabHost.newTabSpec("x");

                x.setIndicator(tabName);

                mSample = new ArrayList<Sample>();
                mSamples.add(mSample);

                File[] files = new File(PATH + "/" + tabName).listFiles();
                Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

                int soundCounter = 0;

                for (File file : files) {
                    //   for (File file : new File(PATH + "/" + tabName).listFiles()) {


                    if (file.isDirectory()){
                        //TODO if Directory list files and create list view inside tab...

                        File[] subfiles = file.listFiles();


                    } else {
                        String fileName = file.getName();
                        if (fileName.endsWith(".wav") || fileName.endsWith(".mp3") || fileName.endsWith(".m4a") || fileName.endsWith(".ogg")) {

                            //check if file is readable with MediaPlayer then create Sample
                            uri = Uri.fromFile(new File(file.getAbsolutePath()));
                            //Log.e("filename",fileName);
                            MediaPlayer tmpPlayer = MediaPlayer.create(this, uri);
                            //TODO avoid crash with corrupted files...
                            if (tmpPlayer != null) {
                                int duration = tmpPlayer.getDuration();
                                mSamples.get(i).add(new Sample(fileName. substring(0, fileName.length() - 4), file, duration, this, "none"));
                                soundCounter++;
                            } else {
                                //TODO maybe log something or show error message?
                            }
                            tmpPlayer.release();
                        }
                    }
                    if (soundCounter == maxSounds) {
                        break;
                    }
                }

                GridView grid = new GridView(this);
                grid.setNumColumns(GridView.AUTO_FIT);
                grid.setColumnWidth((int) (150 * SCALE * Float.parseFloat(buttonSize) + 0.5f)); //130 no volume
                grid.setHorizontalSpacing(0);
                grid.setVerticalSpacing((int) (10 * SCALE * Float.parseFloat(buttonSize) + 0.5f));
                grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                mSampleList.add(grid);

                mAdapter = new SampleAdapter(this, mSamples.get(i), false);
                mSampleList.get(i).setAdapter(mAdapter);
                final int j = i;
                x.setContent(new TabHost.TabContentFactory() {
                    public View createTabContent(String arg) {
                        return mSampleList.get(j);
                    }
                });

                myTabHost.addTab(x);



            }
        } else {
                //add demo creation button
                Button demoButton = new Button(this);
                demoButton.setText("Generate demo content (for testing)");
                demoButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                       generateDemoContent();
                    }
                });
                FrameLayout mainLayout = (FrameLayout)findViewById(android.R.id.content);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                mainLayout.addView(demoButton, lp);

                //Add explanation text
                TextView tutorial = new TextView(this);
                tutorial.setText(Html.fromHtml("<br><br><br> <b>The selected directory does not contain any sub-directories or is not readable.</b> "
                        + "<br><br> Create sub-directories and add files in: <b>" + PATH + "</b>"
                        + "<br> Or select other storage directory in application settings."
                        + "<br><br> The sound files need to be located in subdirectories of the storage directory. "
                        + "<br> The subdirectories will be used as tabs."
                        + "<br><br> The maximum number of tabs and sounds per tab can be changed in the application settings (if the app is slow or you encounter crashes, lowering those numbers could help)."
                ));
                mainLayout.addView(tutorial, lp);
                // Toast.makeText(this, "Create sub-directories and add files in " + PATH, Toast.LENGTH_LONG).show();
        }

        //history
        ArrayList<Sample> history = globalVariable.getHistory();
        if (history != null) {
            updateHistoryView(history);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_volume: // Refresh all local sounds on external storage.
                showVolume();
                return true;
            case R.id.action_refresh: // Refresh all local sounds on external storage.
                refreshRecordings();
                return true;
            case R.id.action_stop: // Stops all looped play backs.
                stopAll();
                return true;
            case R.id.action_settings: // Open settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopAll();
    }

    public void addToHistory(Sample sample) {
        //Get and update history

        ArrayList<Sample> history = globalVariable.getHistory();
        history.add(sample);
        globalVariable.setHistory(history);

        updateHistoryView(history);

    }

    public void updateHistoryView(ArrayList<Sample> history) {

        GridView gridHistory = new GridView(this);
        gridHistory.setNumColumns(GridView.AUTO_FIT);
        gridHistory.setColumnWidth((int) (150 * SCALE * Float.parseFloat(buttonSize) + 0.5f)); //130 no volume
        gridHistory.setHorizontalSpacing(0);
        gridHistory.setHorizontalSpacing(0);
        gridHistory.setVerticalSpacing((int) (10 * SCALE * Float.parseFloat(buttonSize) + 0.5f));
        gridHistory.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridHistory.setAdapter(new SampleAdapter(this, history, true));
        drawerLayout = (FrameLayout) this.findViewById(R.id.drawer_content);
        drawerLayout.addView(gridHistory);

    }

    public void clearHistory() {

        ArrayList<Sample> history = new ArrayList<>();
        globalVariable.setHistory(history);
        updateHistoryView(history);

    }

    private void showVolume() {
        if (volumeSliders != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean showVolume = sharedPref.getBoolean("show_volume", false);
            SharedPreferences.Editor editor = sharedPref.edit();

            if(showVolume) {
                editor.putBoolean("show_volume",false);
            } else {
                editor.putBoolean("show_volume",true);
            }
            editor.commit();

            for (SeekBar volumeSlider : volumeSliders) {

                if(showVolume) {
                    volumeSlider.setVisibility(View.GONE);
                } else {
                    volumeSlider.setVisibility(View.VISIBLE);
                }
            }
        }
        //refreshRecordings();
    }

    public void addVolumeSlider(SeekBar volumeSlider) {
        volumeSliders.add(volumeSlider);
    }

    private void refreshRecordings() {
        recreate();
        clearHistory();
        Toast.makeText(this, "Sounds refreshed", Toast.LENGTH_SHORT).show();
    }

    public void generateDemoContent() {

        //create folders for tabs;
        pathError = false;
        try {
            new File(PATH + "/tab1").mkdirs();
            new File(PATH + "/tab2").mkdirs();
            new File(PATH + "/tab3").mkdirs();
        } catch (Exception name) {
            pathError = true;
        }

        if(pathError == false){
            //copy sound files to folder
            try {
                copyAsset("bach_gavotte.mp3", "Bach gavotte.mp3", PATH + "/tab1");
                copyAsset("walloon_lilli.mp3", "Walloon Lilli.mp3", PATH + "/tab1");
                copyAsset("bach_gavotte.mp3", "Bach gavotte.mp3", PATH + "/tab1");
                copyAsset("purcell_song_spin.mp3", "Purcell Song Full.mp3", PATH + "/tab2");
                copyAsset("purcell_song_spin.mp3", "Purcell Skip 13s#130.mp3", PATH + "/tab2");
            } catch (IOException e){

            }

        }
        refreshRecordings();

    }

    private void copyAsset(String in, String out, String path) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream is = assetManager.open(in);
        File outputFile = new File(path, out);
        OutputStream os = new FileOutputStream(outputFile);

        byte[] buffer = new byte[1024];
        int read;
        while((read = is.read(buffer)) != -1){
            os.write(buffer, 0, read);
        }
    }

    public void stopAll() {
        if (mSamples != null) {
            for (ArrayList<Sample> samples : mSamples) {
                for (Sample sample : samples) {
                    try {
                        sample.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.hughtebby.improsoundsystem/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.hughtebby.improsoundsystem/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
