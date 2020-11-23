package com.github.anrimian.musicplayer.data.repositories.equalizer;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.BANDS;
import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.BAND_LEVEL;
import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.BAND_NUMBER;
import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.EQUALIZER_STATE;
import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.Constants.SELECTED_PRESET;


public class EqualizerStateRepository {

    interface Constants {
        String PREFERENCES_NAME = "equalizer_preferences";

        String EQUALIZER_STATE = "equalizer_state";

        String SELECTED_PRESET = "selected_preset";
        String BAND_NUMBER = "band_number";
        String BAND_LEVEL = "band_level";
        String BANDS = "bands";
    }

    private final SharedPreferencesHelper preferences;

    public EqualizerStateRepository(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void saveEqualizerState(EqualizerState state) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SELECTED_PRESET, state.getCurrentPreset());
            JSONArray bands = new JSONArray();
            for (Map.Entry<Short, Short> band: state.getBendLevels().entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put(BAND_NUMBER, band.getKey());
                obj.put(BAND_LEVEL, band.getValue());
                bands.put(obj);
            }
            jsonObject.put(BANDS, bands);
            preferences.putString(EQUALIZER_STATE, jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public EqualizerState loadEqualizerState() {
        try {
            String rawData = preferences.getString(EQUALIZER_STATE);
            if (rawData == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject(rawData);
            short currentPreset = (short) jsonObject.getInt(SELECTED_PRESET);

            JSONArray jsonArray = new JSONArray(jsonObject.getJSONObject(BANDS));
            Map<Short, Short> bands = new HashMap<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                short number = (short) obj.getInt(BAND_NUMBER);
                short level = (short) obj.getInt(BAND_LEVEL);
                bands.put(number, level);
            }
            return new EqualizerState(currentPreset, bands);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
