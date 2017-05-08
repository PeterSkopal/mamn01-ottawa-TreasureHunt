package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ottawa.treasurehunt.treasurehunt.R;

public class ResultSplash extends Fragment {
    private static final String RESULT = "RESULT";
    private boolean isWin;
    private MediaPlayer mediaPlayer;

    public ResultSplash() {}

    public static ResultSplash newInstance(boolean result) {
        ResultSplash fragment = new ResultSplash();
        Bundle args = new Bundle();
        args.putBoolean(RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isWin = getArguments().getBoolean(RESULT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result_splash, container, false);

        if (isWin) {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.windows_success);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();
            view.findViewById(R.id.ivBackground)
                    .setBackgroundResource(R.drawable.green_gradient);

            ((TextView) view.findViewById(R.id.txtResult)).setText("Success!");
        } else {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.windowsxp_error);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();
            view.findViewById(R.id.ivBackground)
                    .setBackgroundResource(R.drawable.red_gradient);

            ((TextView) view.findViewById(R.id.txtResult)).setText("Failed!");
        }

        return view;
    }
}
