package com.hughtebby.improsoundsystem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
    {

        private final int REQUEST_CODE_PICK_DIR = 1;
        private final int REQUEST_CODE_PICK_FILE = 2;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final Activity context = getActivity();

            Preference storageDir = (Preference) findPreference("storage_dir");
            storageDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent fileExploreIntent = new Intent(
                            FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                            null,
                            context,
                            FileBrowserActivity.class
                    );
                    fileExploreIntent.putExtra(
                            FileBrowserActivity.startDirectoryParameter,
                            Environment.getRootDirectory().getPath() //Here you can add optional start directory parameter, and file browser will start from that directory.
                    );
                    startActivityForResult(
                            fileExploreIntent,
                            REQUEST_CODE_PICK_DIR
                    );

                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            final Activity context = getActivity();

            if (requestCode == REQUEST_CODE_PICK_DIR) {
                if(resultCode == RESULT_OK) {
                    String newDir = data.getStringExtra( FileBrowserActivity.returnDirectoryParameter);
                    Toast.makeText(
                            context,
                            "Set new directory:\n" + newDir +"\n Please refresh.",
                            Toast.LENGTH_LONG).show();

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("storage_dir", newDir);
                    editor.commit();

                } else {
                    Toast.makeText(
                            context,
                            "Received no result from file browser",
                            Toast.LENGTH_LONG).show();
                }
            }

            if (requestCode == REQUEST_CODE_PICK_FILE) {
                if(resultCode == RESULT_OK) {
                    String newFile = data.getStringExtra(FileBrowserActivity.returnFileParameter);
                    Toast.makeText(
                            context,
                            "Received FILE path from file browser:\n"+newFile,
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(
                            context,
                            "Received NO result from file browser",
                            Toast.LENGTH_LONG).show();
                }
            }



            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}