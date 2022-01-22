package com.example.quiz.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.quiz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResultFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private NavController navController;

    private String currentUserId;

    private String quizId;

    private TextView resultCorrect;
    private TextView resultWrong;
    private TextView resultMissed;

    private TextView resultPercent;
    private ProgressBar resultProgress;

    private Button resultHomeButton;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        } else {

        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizId = ResultFragmentArgs.fromBundle(getArguments()).getQuizId();

        resultCorrect = view.findViewById(R.id.resultCorrectText);
        resultWrong = view.findViewById(R.id.resultWrongText);
        resultMissed = view.findViewById(R.id.resultMissedText);

        resultHomeButton = view.findViewById(R.id.resultHomeButton);
        resultHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_resultFragment_to_listFragment);
            }
        });
        resultPercent = view.findViewById(R.id.resultPercent);
        resultProgress = view.findViewById(R.id.resultProgressBar);

        firebaseFirestore.collection("QuizList").document(quizId)
                .collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();

                    Long correct = result.getLong("correct");
                    Long wrong = result.getLong("wrong");
                    Long missed = result.getLong("unanswered");

                    resultCorrect.setText(correct.toString());
                    resultWrong.setText(wrong.toString());
                    resultMissed.setText(missed.toString());

                    Long total = correct + wrong + missed;

                    Long percent = (correct*100)/total;

                    resultPercent.setText(percent + "%");
                    resultProgress.setProgress(percent.intValue());
                }
            }
        });
    }
}