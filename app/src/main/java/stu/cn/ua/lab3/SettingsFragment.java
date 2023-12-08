package stu.cn.ua.lab3;

import static stu.cn.ua.lab3.LoadService.settingsLatch;
import static stu.cn.ua.lab3.LoadService.wordsLatch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SettingsFragment extends BaseFragment {
    public static final String SET_DIFFICULTY = "SET_DIFFICULTY";

    public static final String SET_TIMER_MINUTES = "SET_TIMER_MINUTES";

    private static final String DIFFICULTY = "DIFFICULTY";
    private static final String TIMER_MINUTES = "TIMER_MINUTES";

    private static int difficulty = 0;
    private static int timer_minutes = 2;

    private Bundle lastSavedState;

    private RadioGroup difficultyGroup;
    private RadioGroup timerGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastSavedState = savedInstanceState;

        difficultyGroup = view.findViewById(R.id.difficultyGroup);
        timerGroup = view.findViewById(R.id.timer_difficultyGroup);


        view.findViewById(R.id.saveButton).setOnClickListener(v -> {
            try {
                settingsLatch.await();
                wordsLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            getLoadService().saveSettingsAsync(difficulty,timer_minutes);
            getActivity().onBackPressed();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DIFFICULTY,difficulty);
        outState.putInt(TIMER_MINUTES,timer_minutes);
    }

    private void updateView(RadioGroup difficultyGroup, RadioGroup timerGroup, int difficulty, int timer_minutes) {
        if (difficulty == 0) {
            difficultyGroup.check(R.id.normal_difficulty);
        } else {
            difficultyGroup.check(R.id.hard_difficulty);
        }


        if (timer_minutes == 1) {
            timerGroup.check(R.id.hard_timer);
        } else if (timer_minutes == 2) {
            timerGroup.check(R.id.normal_timer);
        } else {
            timerGroup.check(R.id.easy_timer);
        }
    }

    @Override
    public void onGotSettings(ArrayList<Integer> settings,ArrayList<String> normal_words,ArrayList<String> hard_words) {

        try {
            settingsLatch.await();
            wordsLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        super.onGotSettings(settings,normal_words,hard_words);
        if (lastSavedState != null){
            difficulty = lastSavedState.getInt(DIFFICULTY,0);
            timer_minutes = lastSavedState.getInt(TIMER_MINUTES, 2);
            updateView(difficultyGroup, timerGroup, difficulty, timer_minutes);
        }else {
            difficulty = settings.get(0);
            timer_minutes = settings.get(1);
            updateView(difficultyGroup, timerGroup, difficulty, timer_minutes);
        }


        difficultyGroup.setOnCheckedChangeListener((difgroup, checkedId) -> {
            if (checkedId == R.id.normal_difficulty) {
                difficulty = 0;
            } else if (checkedId == R.id.hard_difficulty) {
                difficulty = 1;
            }
        });

        timerGroup.setOnCheckedChangeListener((timegroup, checkedId) -> {
            if (checkedId == R.id.easy_timer) {
                timer_minutes = 5;
            } else if (checkedId == R.id.normal_timer) {
                timer_minutes = 2;
            } else if (checkedId == R.id.hard_timer) {
                timer_minutes = 1;
            }
        });
    }
}
