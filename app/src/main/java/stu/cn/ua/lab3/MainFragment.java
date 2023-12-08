package stu.cn.ua.lab3;

import static stu.cn.ua.lab3.LoadService.settingsLatch;
import static stu.cn.ua.lab3.LoadService.wordsLatch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MainFragment extends BaseFragment {

    public static final String DIFFICULTY_SAVED = "DIFFICULTY_SAVED";
    public static final String TIMER_SAVED = "TIMER_SAVED";

    private int difficulty = 0;
    private int timer_minutes = 2;
    private TextView settingsTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null){
            difficulty = savedInstanceState.getInt(DIFFICULTY_SAVED, 0);
            timer_minutes = savedInstanceState.getInt(TIMER_SAVED, 2);

        }

        view.findViewById(R.id.PlayButton).setOnClickListener(v -> {
            GameFragment fragment = new GameFragment();
            fragment.setTargetFragment(this,0);
            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragmentContainer,fragment)
                    .commit();
        });

        view.findViewById(R.id.settingsButton).setOnClickListener(v -> {
            SettingsFragment fragment = new SettingsFragment();
            fragment.setTargetFragment(this,0);
            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragmentContainer,fragment)
                    .commit();
        });
        settingsTextView = view.findViewById(R.id.LoadText);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DIFFICULTY_SAVED,difficulty);
        outState.putInt(TIMER_SAVED,timer_minutes);
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
        String current_settings = getString(R.string.current_settings,settings.get(0),settings.get(1));
        settingsTextView.setText(current_settings);
    }
}
