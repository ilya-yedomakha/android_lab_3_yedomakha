package stu.cn.ua.lab3;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadService extends Service {

    private LoadBinder binder = new LoadBinder();
    private SharedPreferences preferences;
    public static final String GAME_SETTINGS = "GAME_SETTINGS";
    public static final String DIFFICULTY = "DIFFICULTY";
    public static final String TIMER_MINUTES = "TIMER_MINUTES";
    public static final String HARD_WORDS = "HARD_WORDS";
    public static final String NORMAL_WORDS = "NORMAL_WORDS";
    private Set<SettingsListener> listeners = new HashSet<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    ArrayList<Integer> settings_arr = new ArrayList<>();
    ArrayList<String> normalWords = new ArrayList<>();
    ArrayList<String> hardWords = new ArrayList<>();
    public final static CountDownLatch settingsLatch = new CountDownLatch(1);
    public final static CountDownLatch wordsLatch = new CountDownLatch(2);

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(GAME_SETTINGS,MODE_PRIVATE);

        ArrayList<String> normalWords = new ArrayList<>(Arrays.asList("мама", "тато", "сонце", "радіо", "фото", "риба", "ложка", "хліб", "миша", "кінь", "плащ"));
        ArrayList<String> hardWords = new ArrayList<>(Arrays.asList("горизонт", "завдання", "календар", "магістр", "розповідь", "сімейство", "функція", "характер", "швидкість", "чернігів", "фотограф"));

        Set<String> normalWordsSet = new HashSet<>(normalWords);
        Set<String> hardWordsSet = new HashSet<>(hardWords);
        preferences.edit()
                .putStringSet(NORMAL_WORDS, normalWordsSet)
                .putStringSet(HARD_WORDS, hardWordsSet)
                .apply();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        listeners.clear();
    }

    public void saveSettingsAsync(int difficulty, int timer_minutes) {
        executorService.submit(() -> {
            String currentThreadName = Thread.currentThread().getName();
            long currentThreadId = Thread.currentThread().getId();

            saveSettings(difficulty, timer_minutes);
            Log.d("LoadService", "Thread Name: " + currentThreadName + " Thread ID: " + currentThreadId + "........................................Settings saved in a separate thread.");
            settingsLatch.countDown();
        });
    }

    public ArrayList<Integer> getSettingsAsync() {

        executorService.submit(() -> {
            String currentThreadName = Thread.currentThread().getName();
            long currentThreadId = Thread.currentThread().getId();
            settings_arr.clear();
            settings_arr.addAll(getSettings());
            Log.d("LoadService", "Thread Name: " + currentThreadName + " Thread ID: " + currentThreadId + "........................................Settings retrieved in a separate thread.");
            settingsLatch.countDown();
        });
        return settings_arr;
    }

    public ArrayList<String> getNormalWordsAsync() {
        executorService.submit(() -> {
            String currentThreadName = Thread.currentThread().getName();
            long currentThreadId = Thread.currentThread().getId();
            normalWords = getNormalWords();
            Log.d("LoadService", "Thread Name: " + currentThreadName + " Thread ID: " + currentThreadId + "........................................Normal words retrieved in a separate thread.");
            wordsLatch.countDown();
        });
        return normalWords;
    }

    public ArrayList<String> getHardWordsAsync() {
        executorService.submit(() -> {
            String currentThreadName = Thread.currentThread().getName();
            long currentThreadId = Thread.currentThread().getId();
            hardWords = getHardWords();
            Log.d("LoadService", "Thread Name: " + currentThreadName + " Thread ID: " + currentThreadId + "........................................Hard words retrieved in a separate thread.");
            wordsLatch.countDown();
        });
        return hardWords;
    }

    public void saveSettings(int difficulty, int timer_minutes) {
        preferences.edit()
                .putInt(DIFFICULTY, difficulty)
                .putInt(TIMER_MINUTES, timer_minutes)
                .apply();
    }



    public ArrayList<Integer> getSettings() {
        ArrayList<Integer> settings = new ArrayList<>();
        settings.add(preferences.getInt(DIFFICULTY, 0));
        settings.add(preferences.getInt(TIMER_MINUTES, 2));
        return settings;
    }

    public ArrayList<String> getNormalWords() {
        Set<String> normalWordsSet = preferences.getStringSet(NORMAL_WORDS, null);
        return new ArrayList<>(normalWordsSet);
    }

    public ArrayList<String> getHardWords() {
        Set<String> hardWordsSet = preferences.getStringSet(HARD_WORDS, null);
        return new ArrayList<>(hardWordsSet);
    }

    public void addSettingsListener(SettingsListener listener) {
        this.listeners.add(listener);
        listener.onGotSettings(getSettingsAsync(), getNormalWordsAsync(), getHardWordsAsync());
    }

    public void removeSettingsListener(SettingsListener listener) {
        this.listeners.remove(listener);
    }


    interface SettingsListener {
        void onGotSettings(ArrayList<Integer> settings, ArrayList<String> normal_words, ArrayList<String> hard_words);
    }

    class LoadBinder extends Binder {
        public LoadService getService() {
            return LoadService.this;
        }
    }
}
