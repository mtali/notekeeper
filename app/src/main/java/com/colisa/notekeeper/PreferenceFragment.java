package com.colisa.notekeeper;

import android.annotation.SuppressLint;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PreferenceFragment extends PreferenceFragmentCompat {

    private void setAllPreferencesToAvoidHavingExtraSpace(Preference preference) {
        if (preference != null) {
            preference.setIconSpaceReserved(false);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup group = ((PreferenceGroup) preference);
                int count = group.getPreferenceCount();
                for (int i = 0; i < count; i++) {
                    setAllPreferencesToAvoidHavingExtraSpace(group.getPreference(i));
                }
            }
        }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen);
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            public void onPreferenceHierarchyChange(Preference preference) {
                setAllPreferencesToAvoidHavingExtraSpace(preference);
                super.onPreferenceHierarchyChange(preference);
            }
        };
    }
}
