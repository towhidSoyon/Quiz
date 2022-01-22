package com.example.quiz.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.quiz.R;
import com.example.quiz.model.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String quizId;

    private String quizName;
    private String currentUserId;

    private NavController navController;

    public static final String TAG = "QUIZ_FRAGMENT_LOG";

    private TextView quizTitle;
    private Button optionOneButton;
    private Button optionTwoButton;
    private Button optionThreeButton;
    private Button nextButton;
    private ImageButton closeButton;
    private TextView questionFeedback;
    private TextView questionText;
    private TextView questionTime;
    private ProgressBar questionProgress;
    private TextView questionNumber;

    private List<QuestionModel> allQuestionList = new ArrayList<>();
    private long totalQuestionToAnswer = 5;
    private List<QuestionModel> questionsToAnswer = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private int currentQuestion = 0;

    private boolean canAnswer= false;

    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);

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

        quizTitle = view.findViewById(R.id.quizTitle);
        optionOneButton = view.findViewById(R.id.quizOptionOne);
        optionTwoButton = view.findViewById(R.id.quizOptionTwo);
        optionThreeButton = view.findViewById(R.id.quizOptionThree);
        nextButton = view.findViewById(R.id.quizNextButton);
        questionFeedback = view.findViewById(R.id.quizQuestionFeedback);
        questionText = view.findViewById(R.id.quizQuestion);
        questionTime = view.findViewById(R.id.quizTime);
        questionProgress = view.findViewById(R.id.quizQuestionProgressbar);
        questionNumber = view.findViewById(R.id.quizQuestionNumber);

        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        totalQuestionToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        firebaseFirestore.collection("QuizList").document(quizId).collection("Questions")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //add all questions to list
                    allQuestionList = task.getResult().toObjects(QuestionModel.class);
                    pickQuestion();
                    loadUI();
                } else {
                    quizTitle.setText("Error Loading Data");
                }
            }
        });

        optionOneButton.setOnClickListener(this);
        optionTwoButton.setOnClickListener(this);
        optionThreeButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
    }

    private void loadUI() {
        quizTitle.setText(quizName);
        questionText.setText("Load First Question");

        enableOptions();

        loadQuestion(1);
    }

    private void loadQuestion(int questNum) {

        questionNumber.setText(questNum+"");

        questionText.setText(questionsToAnswer.get(questNum-1).getQuestion());

        optionOneButton.setText(questionsToAnswer.get(questNum-1).getOption_a());
        optionTwoButton.setText(questionsToAnswer.get(questNum-1).getOption_b());
        optionThreeButton.setText(questionsToAnswer.get(questNum-1).getOption_c());

        canAnswer = true;
        currentQuestion = questNum;
        
        startTimer(questNum);
    }

    private void startTimer(int questionNumber) {

        final Long timeToAnswer = questionsToAnswer.get(questionNumber-1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        questionProgress.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer * 1000, 10) {
            @Override
            public void onTick(long l) {
                questionTime.setText(l / 1000 + "");

                Long percent = l/(timeToAnswer*10);
                questionProgress.setProgress(percent.intValue());
            }

            @SuppressLint("NewApi")
            @Override
            public void onFinish() {
                canAnswer = false;

                questionFeedback.setText("Time Up! No answer was submitted.");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                notAnswered++;
                showNextButton();
            }
        };

        countDownTimer.start();
    }

    private void enableOptions() {

        optionOneButton.setVisibility(View.VISIBLE);
        optionTwoButton.setVisibility(View.VISIBLE);
        optionThreeButton.setVisibility(View.VISIBLE);

        optionOneButton.setEnabled(true);
        optionTwoButton.setEnabled(true);
        optionThreeButton.setEnabled(true);

        questionFeedback.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setEnabled(true);
    }

    private void pickQuestion() {
        for (int i=0; i<totalQuestionToAnswer; i++){
            int randomNumber = getRandomInteger(allQuestionList.size(), 0 );
            questionsToAnswer.add(allQuestionList.get(randomNumber));
            allQuestionList.remove(randomNumber);
        }
    }

    public static int getRandomInteger(int maximum, int minimum){
        return ((int) (Math.random()*(maximum-minimum))) + minimum;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.quizOptionOne:
                verifyAnswer(optionOneButton);
                break;
            case R.id.quizOptionTwo:
                verifyAnswer(optionTwoButton);
                break;
            case R.id.quizOptionThree:
                verifyAnswer(optionThreeButton);
                break;
            case R.id.quizNextButton:
                if (currentQuestion == totalQuestionToAnswer) {
                    submitResults();
                } else{
                    currentQuestion++;
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
                break;
        }
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct", correctAnswers);
        resultMap.put("wrong", wrongAnswers);
        resultMap.put("unanswered", notAnswered);
        firebaseFirestore.collection("QuizList").document(quizId).collection("Results").document(currentUserId).set(resultMap)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizId(quizId);
                    navController.navigate(action);
                } else {
                    quizTitle.setText(task.getException().getMessage());
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void resetOptions() {
        optionOneButton.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionTwoButton.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionThreeButton.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));

        optionOneButton.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionTwoButton.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionThreeButton.setTextColor(getResources().getColor(R.color.colorLightText, null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setEnabled(false);

    }

    @SuppressLint("NewApi")
    private void verifyAnswer(Button selectedAnswerButton) {

        if (canAnswer){

            selectedAnswerButton.setTextColor(getResources().getColor(R.color.colorDark, null));
            if (questionsToAnswer.get(currentQuestion-1).getAnswer().equals(selectedAnswerButton.getText())){
                correctAnswers++;
                selectedAnswerButton.setBackground(getResources().getDrawable(R.drawable.correct_ans_button_bg, null));

                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }else {
                wrongAnswers++;
                selectedAnswerButton.setBackground(getResources().getDrawable(R.drawable.wrong_ans_button_bg, null));

                questionFeedback.setText("Wrong Answer \n \n Correct Answer : " + questionsToAnswer.get(currentQuestion-1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent, null));
            }
            canAnswer = false;

            countDownTimer.cancel();
            
            showNextButton();
        }
    }

    private void showNextButton() {
        if (currentQuestion == totalQuestionToAnswer){
            nextButton.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        nextButton.setEnabled(true);
    }
}