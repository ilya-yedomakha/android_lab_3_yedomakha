package stu.cn.ua.lab3;

import static stu.cn.ua.lab3.LoadService.settingsLatch;
import static stu.cn.ua.lab3.LoadService.wordsLatch;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameFragment extends BaseFragment {
    TextView timerText;
    Button topButton1;
    Button topButton2;
    Button topButton3;
    Button topButton4;
    Button topButton5;
    Button topButton6;
    Button topButton7;
    Button topButton8;
    Button topButton9;

    Button bottomButton1;
    Button bottomButton2;
    Button bottomButton3;
    Button bottomButton4;
    Button bottomButton5;
    Button bottomButton6;
    Button bottomButton7;
    Button bottomButton8;
    Button bottomButton9;
    View horizontalLine;

    CountDownTimer timer;

    TextView textView;


    private int difficulty = 0;
    private int timer_minutes = 2;
    long timeRemaining = 0;
    private String word = "";
    private static Random random = new Random();
    private ArrayList<Button> topButtons = new ArrayList<>();
    private ArrayList<Button> bottomButtons = new ArrayList<>();
    private ArrayList<String> topButtonsText = new ArrayList<>();
    private ArrayList<String> guessed_words = new ArrayList<>();
    private ArrayList<String> bottomButtonsText = new ArrayList<>();

    private HashMap<Integer, Integer> top_bottom_switch = new HashMap<>();
    private boolean timer_finished = false;
    private boolean words_done = false;

    public static final String DIFFICULTY = "DIFFICULTY";
    public static final String TIMER_MINUTES = "TIMER_MINUTES";
    public static final String WORD = "WORD";
    public static final String TOP_BUTTONS_TEXT = "TOP_BUTTONS_TEXT";
    public static final String GUESSED_WORDS = "GUESSED_WORDS";
    public static final String BOTTOM_BUTTONS_TEXT = "BOTTOM_BUTTONS_TEXT";
    public static final String NORMAL_WORDS = "NORMAL_WORDS";
    public static final String HARD_WORDS = "HARD_WORDS";
    public static final String TIMER_STATE = "TIMER_STATE";

    public static final String BOTTOM_INDEXES = "BOTTOM_INDEXES";
    public static final String TOP_INDEXES = "TOP_INDEXES";
    public static final String TIMER_FINISHED = "TIMER_FINISHED";
    public static final String ALL_WORDS_DONE = "ALL_WORDS_DONE";

    public ArrayList<String> normal_words = new ArrayList<>();
    public ArrayList<String> hard_words = new ArrayList<>();

    private Bundle lastSavedState;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DIFFICULTY, difficulty);
        outState.putInt(TIMER_MINUTES, timer_minutes);
        outState.putString(WORD, word);
        outState.putStringArrayList(NORMAL_WORDS, normal_words);
        outState.putStringArrayList(HARD_WORDS, hard_words);
        outState.putStringArrayList(TOP_BUTTONS_TEXT, topButtonsText);
        outState.putStringArrayList(BOTTOM_BUTTONS_TEXT, bottomButtonsText);
        outState.putStringArrayList(GUESSED_WORDS, guessed_words);
        outState.putLong(TIMER_STATE, timeRemaining);
        ArrayList<Integer> topIndexes = new ArrayList<>();
        ArrayList<Integer> bottomIndexes = new ArrayList<>();
        for (int i = 0; i < topButtonsText.size(); i++) {
            if (!topButtonsText.get(i).equals("_")) {
                topIndexes.add(i);
                bottomIndexes.add(top_bottom_switch.get(i));
            }
        }
        outState.putIntegerArrayList(BOTTOM_INDEXES, bottomIndexes);
        outState.putIntegerArrayList(TOP_INDEXES, topIndexes);
        outState.putBoolean(TIMER_FINISHED,timer_finished);
        outState.putBoolean(ALL_WORDS_DONE,words_done);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeOnCreate(view);
        lastSavedState = savedInstanceState;

    }

    @Override
    public void onGotSettings(ArrayList<Integer> settings,ArrayList<String> normal_words_settings,ArrayList<String> hard_words_settings) {
        try {
            settingsLatch.await();
            wordsLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        super.onGotSettings(settings,normal_words_settings,hard_words_settings);
        if (lastSavedState != null) {
            difficulty = lastSavedState.getInt(DIFFICULTY);
            timer_minutes = lastSavedState.getInt(TIMER_MINUTES);
            word = lastSavedState.getString(WORD);
            normal_words.clear();
            normal_words.addAll(lastSavedState.getStringArrayList(NORMAL_WORDS));
            hard_words.clear();
            hard_words.addAll(lastSavedState.getStringArrayList(HARD_WORDS));
            topButtonsText.clear();
            topButtonsText.addAll(lastSavedState.getStringArrayList(TOP_BUTTONS_TEXT));
            bottomButtonsText.clear();
            bottomButtonsText.addAll(lastSavedState.getStringArrayList(BOTTOM_BUTTONS_TEXT));
            guessed_words.clear();
            guessed_words.addAll(lastSavedState.getStringArrayList(GUESSED_WORDS));
            timeRemaining = lastSavedState.getLong(TIMER_STATE);

            ArrayList<Integer> topIndexes = new ArrayList<>();
            topIndexes.addAll(lastSavedState.getIntegerArrayList(TOP_INDEXES));

            ArrayList<Integer> bottomIndexes = new ArrayList<>();
            bottomIndexes.addAll(lastSavedState.getIntegerArrayList(BOTTOM_INDEXES));
            top_bottom_switch.clear();
            for (int i = 0; i < bottomIndexes.size(); i++) {
                top_bottom_switch.put(topIndexes.get(i), bottomIndexes.get(i));
            }
            words_done = lastSavedState.getBoolean(ALL_WORDS_DONE);
            timer_finished = lastSavedState.getBoolean(TIMER_FINISHED);

            if(timer_finished){
                timerText.setText("Done!");
                hideTheButtons();
                horizontalLine.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Your result is: " + guessed_words.size() + "/11");
            } else if (words_done) {
                hideTheButtons();
                horizontalLine.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("You guessed all the words!");
                timerText.setText("Done!");
            }else {
                startTimer(timeRemaining);
                restoreState();
            }
        }else {
            normal_words.addAll(normal_words_settings);
            hard_words.addAll(hard_words_settings);
            difficulty = settings.get(0);
            timer_minutes = settings.get(1);
            words_done = false;
            timer_finished = false;
            for (int i = 0; i < 9; i++) {
                bottomButtonsText.add("_");
                topButtonsText.add("_");
            }

            startTimer(Integer.toUnsignedLong(timer_minutes * 60000));
            hideTheButtons();
            publishWord();
        }
    }

    private void startTimer(long mills) {

        timer = new CountDownTimer(mills, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                timerText.setText("Time remaining: " + minutes + ":" + seconds);
            }

            public void onFinish() {
                timer_finished =true;
                timerText.setText("Done!");
                hideTheButtons();
                horizontalLine.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Your result is: " + guessed_words.size() + "/11");
            }
        }.start();
    }

    private void topButtonListener(Button topButton, int top_index) {
        topButton.setOnClickListener(view -> {
            String textButton = topButtonsText.get(top_index);


            int bottom_index = top_bottom_switch.get(top_index);

            bottomButtons.get(bottom_index).setVisibility(View.VISIBLE);
            bottomButtonsText.set(bottom_index, textButton);
            bottomButtons.get(bottom_index).setText(textButton);


            top_bottom_switch.remove(top_index);

            topButtonsText.set(top_index, "_");
            topButtons.get(top_index).setText("_");
            topButtons.get(top_index).setVisibility(View.INVISIBLE);
        });
    }

    private void bottomButtonListener(Button bottomButton, int bottom_index) {
        bottomButton.setOnClickListener(view -> {
            String textButton = bottomButtonsText.get(bottom_index);


            int top_index;
            for (top_index = 0; top_index < topButtonsText.size(); top_index++) {
                if (topButtonsText.get(top_index).equals("_")) {
                    topButtonsText.set(top_index, textButton);
                    break;
                }
            }

            topButtons.get(top_index).setVisibility(View.VISIBLE);
            topButtons.get(top_index).setText(textButton);

            top_bottom_switch.put(top_index, bottom_index);
            bottomButtonsText.set(bottom_index, "_");
            bottomButtons.get(bottom_index).setText("_");
            bottomButtons.get(bottom_index).setVisibility(View.INVISIBLE);


            StringBuilder cleanWord = new StringBuilder();
            for (String st : topButtonsText) {
                if (!st.equals("_")) {
                    cleanWord.append(st);
                }
            }
            if (cleanWord.length() == word.length()) {
                if (cleanWord.toString().equals(word)) {
                    Toast.makeText(getActivity(), "Correct word!", Toast.LENGTH_SHORT).show();
                    guessed_words.add(cleanWord.toString());
                    if (passedWordDelete(cleanWord.toString())) {
                        publishWord();
                    } else {
                        hideTheButtons();
                        horizontalLine.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("You guessed all the words!");
                        timer.cancel();
                        timerText.setText("Done!");
                        words_done = true;
                    }
                } else {
                    Toast.makeText(getActivity(), "Word is incorrect!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


    private boolean passedWordDelete(String cleanWord) {
        if (difficulty == 0) {
            normal_words.remove(cleanWord);
            return normal_words.size() > 0;
        } else {
            hard_words.remove(cleanWord);
            return hard_words.size() > 0;
        }
    }

    private void publishWord() {
        if (difficulty == 0) {
            word = randomStringFromArray(normal_words);
        } else {
            word = randomStringFromArray(hard_words);
        }
        fillTheBottomRow(randomizeString(word));
    }

    private void fillTheBottomRow(String word) {
        showXBottomletters(word.length());
        for (int i = 0; i < word.length(); i++) {
            bottomButtonsText.set(i, String.valueOf(word.charAt(i)));
            bottomButtons.get(i).setText(String.valueOf(word.charAt(i)));
        }
        for (int i = 0; i < 9; i++) {
            topButtonsText.set(i, "_");
            topButtons.get(i).setText("_");
            topButtons.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void restoreState() {
        for (int i = 0; i < bottomButtons.size();i++){
            if(!bottomButtonsText.get(i).equals("_")){
                bottomButtons.get(i).setText(bottomButtonsText.get(i));
                bottomButtons.get(i).setVisibility(View.VISIBLE);
            }else bottomButtons.get(i).setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < topButtons.size();i++){
            if(!topButtonsText.get(i).equals("_")){
                topButtons.get(i).setText(topButtonsText.get(i));
                topButtons.get(i).setVisibility(View.VISIBLE);
            }else topButtons.get(i).setVisibility(View.INVISIBLE);
        }
    }


    public static String randomStringFromArray(ArrayList<String> strings) {
        int randomIndex = random.nextInt(strings.size());
        return strings.get(randomIndex);
    }

    public static String randomizeString(String string) {
        List<Character> characters = new ArrayList<Character>();
        for (char c : string.toCharArray()) {
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(string.length());
        while (characters.size() != 0) {
            int randPicker = (int) (Math.random() * characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }


    private void hideTheButtons() {
        for (Button button : topButtons) {
            button.setVisibility(View.INVISIBLE);
        }

        for (Button button : bottomButtons) {
            button.setVisibility(View.INVISIBLE);
        }
    }


    private void showXBottomletters(int letters) {
        for (int i = 0; i < letters; i++) {
            bottomButtons.get(i).setVisibility(View.VISIBLE);
        }
    }

    private void fillTheButtonsArrays() {
        topButtons.add(topButton1);
        topButtons.add(topButton2);
        topButtons.add(topButton3);
        topButtons.add(topButton4);
        topButtons.add(topButton5);
        topButtons.add(topButton6);
        topButtons.add(topButton7);
        topButtons.add(topButton8);
        topButtons.add(topButton9);

        bottomButtons.add(bottomButton1);
        bottomButtons.add(bottomButton2);
        bottomButtons.add(bottomButton3);
        bottomButtons.add(bottomButton4);
        bottomButtons.add(bottomButton5);
        bottomButtons.add(bottomButton6);
        bottomButtons.add(bottomButton7);
        bottomButtons.add(bottomButton8);
        bottomButtons.add(bottomButton9);
    }

    private void initializeOnCreate(View view) {
        horizontalLine = view.findViewById(R.id.horizontalLine);
        textView = view.findViewById(R.id.testTextWord);
        topButton1 = view.findViewById(R.id.topButton1);
        topButton2 = view.findViewById(R.id.topButton2);
        topButton3 = view.findViewById(R.id.topButton3);
        topButton4 = view.findViewById(R.id.topButton4);
        topButton5 = view.findViewById(R.id.topButton5);
        topButton6 = view.findViewById(R.id.topButton6);
        topButton7 = view.findViewById(R.id.topButton7);
        topButton8 = view.findViewById(R.id.topButton8);
        topButton9 = view.findViewById(R.id.topButton9);


        bottomButton1 = view.findViewById(R.id.bottomButton1);
        bottomButton2 = view.findViewById(R.id.bottomButton2);
        bottomButton3 = view.findViewById(R.id.bottomButton3);
        bottomButton4 = view.findViewById(R.id.bottomButton4);
        bottomButton5 = view.findViewById(R.id.bottomButton5);
        bottomButton6 = view.findViewById(R.id.bottomButton6);
        bottomButton7 = view.findViewById(R.id.bottomButton7);
        bottomButton8 = view.findViewById(R.id.bottomButton8);
        bottomButton9 = view.findViewById(R.id.bottomButton9);
        timerText = view.findViewById(R.id.timerText);

        fillTheButtonsArrays();
        bottomButtonListener(bottomButton1, 0);
        bottomButtonListener(bottomButton2, 1);
        bottomButtonListener(bottomButton3, 2);
        bottomButtonListener(bottomButton4, 3);
        bottomButtonListener(bottomButton5, 4);
        bottomButtonListener(bottomButton6, 5);
        bottomButtonListener(bottomButton7, 6);
        bottomButtonListener(bottomButton8, 7);
        bottomButtonListener(bottomButton9, 8);

        topButtonListener(topButton1, 0);
        topButtonListener(topButton2, 1);
        topButtonListener(topButton3, 2);
        topButtonListener(topButton4, 3);
        topButtonListener(topButton5, 4);
        topButtonListener(topButton6, 5);
        topButtonListener(topButton7, 6);
        topButtonListener(topButton8, 7);
        topButtonListener(topButton9, 8);
    }
}
