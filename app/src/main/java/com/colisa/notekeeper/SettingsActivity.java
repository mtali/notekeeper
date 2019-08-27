package com.colisa.notekeeper;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "SettingsActivityTitle";
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private CharSequence title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                    .commit();
        } else {
            title = savedInstanceState.getString(TITLE_TAG);
            setTitle(title);
        }
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    setTitle(R.string.settings_title);
                }
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Log.i(TAG, "SettingsActivity.onCreate called");
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, title);
        Log.i(TAG, "SettingsActivity.onSaveInstanceState called");
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        Log.i(TAG, "SettingsActivity.onSupportNavigateUp called");
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate new fragment
        Bundle args = pref.getExtras();
        Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment()
        );
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        // Replace existing fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
        title = pref.getTitle();
        setTitle(title);
        Log.i(TAG, "SettingsActivity.onPreferenceStartFragment called");
        return true;
    }

    /**
     * Root preference fragment that display fragment link to other preference fragment below
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_root, rootKey);
            Log.i(TAG, "SettingsFragment.onCreatePreferences called");
        }
    }

    /**
     * General preference fragment
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);

            EditTextPreference emailPreference = findPreference(getString(R.string.pref_key_email_address));
            if (null != emailPreference){
                emailPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    }
                });
                emailPreference.setSummaryProvider(new Preference.SummaryProvider() {
                    @Override
                    public CharSequence provideSummary(Preference preference) {
                        String text = ((EditTextPreference) preference).getText();
                        if (TextUtils.isEmpty(text)) {
                            return "Not set";
                        }
                        return text;
                    }
                });
            }


//            EditTextPreference displayNamePreference = findPreference("user_display_name");
//            if (null != displayNamePreference) {
//                displayNamePreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
//            }
//
//            ListPreference socialListPreference = findPreference("list_social_media_preference");
//            if (null != socialListPreference) {
//                socialListPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
//            }
            Log.i(TAG, "SettingsFragment.onCreatePreferences called");
        }
    }
}