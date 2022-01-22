package com.example.quiz.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quiz.R;
import com.example.quiz.model.QuizListModel;
import com.example.quiz.viewModel.QuizListViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class DetailsFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private QuizListViewModel quizListViewModel;
    private int position;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private ImageView detailsImage;
    private TextView detailsTitle;
    private TextView detailsDesc;
    private TextView detailsDiff;
    private TextView detailsQuestion;
    private TextView detailsScore;

    private Button startButton;
    private String quizId;
    private long totalQuestions = 0;

    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth =FirebaseAuth.getInstance();

        navController = Navigation.findNavController(view);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        detailsImage = view.findViewById(R.id.detailsImage);
        detailsTitle = view.findViewById(R.id.detailsTitle);
        detailsDesc = view.findViewById(R.id.detailsDesc);
        detailsDiff = view.findViewById(R.id.detailsDifficultyText);
        detailsQuestion = view.findViewById(R.id.detailsQuestionText);
        detailsScore = view.findViewById(R.id.detailsScoreText);

        startButton = view.findViewById(R.id.detailsStartButton);
        startButton.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {

                Glide.with(getContext()).load(quizListModels.get(position)).centerCrop().placeholder(R.drawable.placeholder_image).into(detailsImage);

                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsDiff.setText(quizListModels.get(position).getLevel());
                detailsQuestion.setText(quizListModels.get(position).getQuestions()+"");

                quizId = quizListModels.get(position).getQuizId();
                totalQuestions = quizListModels.get(position).getQuestions();

                loadResultsData();
            }
        });
    }

    private void loadResultsData() {

        firebaseFirestore.collection("QuizList").document(quizId)
                .collection("Results")
                .document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document != null && document.exists()){
                        Long correct = document.getLong("correct");
                        Long wrong = document.getLong("wrong");
                        Long missed = document.getLong("unanswered");

                        Long total = correct + wrong + missed;

                        Long percent = (correct*100)/total;

                        detailsScore.setText(percent + "%");
                    } else {

                    }


                }
            }
        });
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.detailsStartButton:
               DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment actionDetailsFragmentToQuizFragment = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
               actionDetailsFragmentToQuizFragment.setTotalQuestions(totalQuestions);
               actionDetailsFragmentToQuizFragment.setQuizId(quizId);
                navController.navigate(actionDetailsFragmentToQuizFragment);
               break;
       }
    }
}