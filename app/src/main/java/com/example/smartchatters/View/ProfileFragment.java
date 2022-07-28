package com.example.smartchatters.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartchatters.R;
import com.example.smartchatters.logic.Singleton;
import com.example.smartchatters.logic.Usernode;


public class ProfileFragment extends Fragment {
    private TextView usernameInfo;
    private TextView firstnameInfo;
    private TextView lastnameInfo;

    public static ProfileFragment newInstance(Usernode user) {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_profile, container, false);
        usernameInfo= (TextView) view.findViewById(R.id.usernameInfo);
        firstnameInfo= (TextView) view.findViewById(R.id.firstnameInfo);
        lastnameInfo= (TextView) view.findViewById(R.id.lastnameInfo);

        usernameInfo.setText(Singleton.getInstance().getUser().getUsername());
        firstnameInfo.setText(Singleton.getInstance().getUser().getFirstname());
        lastnameInfo.setText(Singleton.getInstance().getUser().getLastname());


        return view;
    }
}