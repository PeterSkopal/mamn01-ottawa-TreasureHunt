package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ottawa.treasurehunt.treasurehunt.R;

import java.util.HashMap;
import java.util.Map;

public class QuizFragment extends Fragment {
    private static final String QUESTION = "QUESTION";
    private static final String ANSWERS = "ANSWERS";
    private static final String QUIZ_NUM = "QUIZ_NUM";
    private static final String QUIZ_TOTAL = "QUIZ_TOTAL";

    private String question;
    private HashMap<String, Boolean> answers;
    private int quizNumber;
    private int quizNumberTotal;

    private IResultCallback resCallback;

    TextView txtQuizProgress;
    ProgressBar progressTimeLeft;
    Handler progressTimeHandler;
    Runnable progressTimeRunnable;
    TextView txtQuestion;
    Button[] btns;

    View.OnClickListener onAnswerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressTimeHandler.removeCallbacks(progressTimeRunnable);
            if (answers.get(((Button) v).getText()))
                resCallback.callback(true); // answer was correct
            else
                resCallback.callback(false);
        }
    };


    public QuizFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param question Question to be answered
     * @param answers  4 answers with one or multiple true answers
     * @return A new instance of fragment QuizFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizFragment newInstance(String question,
                                           HashMap<String, Boolean> answers,
                                           int quizNumber,
                                           int quizNumberTotal) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION, question);
        args.putSerializable(ANSWERS, answers);
        args.putInt(QUIZ_NUM, quizNumber);
        args.putInt(QUIZ_TOTAL, quizNumberTotal);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = getArguments().getString(QUESTION);
            answers = (HashMap<String, Boolean>) getArguments().getSerializable(ANSWERS);
            quizNumber = getArguments().getInt(QUIZ_NUM);
            quizNumberTotal = getArguments().getInt(QUIZ_TOTAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        txtQuizProgress = (TextView) view.findViewById(R.id.txtQuizProgress);
        progressTimeLeft = (ProgressBar) view.findViewById(R.id.progressTimeLeft);
        txtQuestion = (TextView) view.findViewById(R.id.txtQuestion);
        btns = new Button[]{
                (Button) view.findViewById(R.id.btnAns1),
                (Button) view.findViewById(R.id.btnAns2),
                (Button) view.findViewById(R.id.btnAns3),
                (Button) view.findViewById(R.id.btnAns4)
        };

        txtQuizProgress.setText(String.format("%d / %d", quizNumber, quizNumberTotal));
        txtQuestion.setText(question);

        int i = 0;
        for (Map.Entry<String, Boolean> entry : answers.entrySet()) {
            btns[i].setOnClickListener(onAnswerClick);
            btns[i++].setText(entry.getKey());
        }

        progressTimeHandler = new Handler();
        progressTimeRunnable = new Runnable() { // Progress timer, if progress = 100 then start next quiz
            int count = 0;

            @Override
            public void run() {
                if (count <= 100) {
                    progressTimeLeft.setProgress(count++);
                    progressTimeHandler.postDelayed(this, 50);
                } else {
                    progressTimeHandler.removeCallbacks(this);
                    resCallback.callback(false);
                }
            }
        };

        progressTimeRunnable.run();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IResultCallback) {
            resCallback = (IResultCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IResultCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resCallback = null;
        progressTimeHandler.removeCallbacks(progressTimeRunnable);
    }
}
