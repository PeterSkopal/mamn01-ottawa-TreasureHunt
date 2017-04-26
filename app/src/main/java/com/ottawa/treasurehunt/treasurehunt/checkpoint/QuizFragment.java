package com.ottawa.treasurehunt.treasurehunt.checkpoint;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ottawa.treasurehunt.treasurehunt.R;

import java.util.HashMap;
import java.util.Map;

public class QuizFragment extends Fragment {
    private static final String QUESTION = "QUESTION";
    private static final String ANSWERS = "ANSWERS";

    private String question;
    private HashMap<String, Boolean> answers;

    private OnFragmentInteractionListener mListener;

    TextView txtQuestion;
    Button[] btns;

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
    public static QuizFragment newInstance(String question, HashMap<String, Boolean> answers) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION, question);
        args.putSerializable(ANSWERS, answers);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        txtQuestion = (TextView) view.findViewById(R.id.txtQuestion);
        btns = new Button[]{
                (Button) view.findViewById(R.id.btnAns1),
                (Button) view.findViewById(R.id.btnAns2),
                (Button) view.findViewById(R.id.btnAns3),
                (Button) view.findViewById(R.id.btnAns4)
        };

        txtQuestion.setText(question);

        int i = 0;
        for (Map.Entry<String, Boolean> entry : answers.entrySet()) {
            btns[i++].setText(entry.getKey());
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
