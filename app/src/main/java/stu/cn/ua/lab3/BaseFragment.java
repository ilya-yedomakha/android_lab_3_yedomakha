package stu.cn.ua.lab3;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;


public class BaseFragment extends Fragment implements LoadService.SettingsListener{

    private LoadService service;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((LoadService.LoadBinder) iBinder).getService();
            addListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().startService(createServiceIntent());
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(createServiceIntent(),connection,0);
        addListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        service.removeSettingsListener(this);
        getActivity().unbindService(connection);
    }

    Intent createServiceIntent(){
        return new Intent(getActivity(),LoadService.class);
    }

    protected final LoadService getLoadService(){
        return service;
    }

    private void addListener(){
        if (service != null) {
            service.addSettingsListener(this);
        }
    }

    @Override
    public void onGotSettings(ArrayList<Integer> settings,ArrayList<String> normal_words,ArrayList<String> hard_words) {

    }


}
