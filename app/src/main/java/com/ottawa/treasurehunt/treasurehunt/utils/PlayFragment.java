package com.ottawa.treasurehunt.treasurehunt.utils;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottawa.treasurehunt.treasurehunt.R;

public class PlayFragment extends Fragment {
    private static final String INSTRUCTION = "INSTRCUTION";
    private String ins;

    public PlayFragment() {}

    public static PlayFragment newInstance(String intruction) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(INSTRUCTION, intruction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ins = getArguments().getString(INSTRUCTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        if (ins == "quiz") {
            ((TextView) view.findViewById(R.id.instructional_text_mini_game)).setText("Tilt your phone to answer the questions!");
        } else if (ins == "minigame"){
            ((ImageView) view.findViewById(R.id.instruction_image)).setVisibility(View.INVISIBLE);
            ((TextView) view.findViewById(R.id.instructional_text_mini_game)).setText("Prepare yourself for a MiniGame!");
        } else {

        }

        return view;
    }
}
