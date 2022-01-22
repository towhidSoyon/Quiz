package com.example.quiz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quiz.R;
import com.example.quiz.model.QuizListModel;

import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private List<QuizListModel> quizListModels;
    private OnQuizListItemClicked onQuizListItemClicked;

    public void setQuizListModels(List<QuizListModel> quizListModels) {
        this.quizListModels = quizListModels;
    }

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked) {
        this.onQuizListItemClicked = onQuizListItemClicked;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);

        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        holder.listTitle.setText(quizListModels.get(position).getName());

        String imageUrl = quizListModels.get(position).getImage();

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.listImage);


        String lD = quizListModels.get(position).getDesc();
        if (lD.length() > 150){
            lD = lD.substring(0,150);
        }

        holder.listDesc.setText(lD+ "...");
        holder.listLevel.setText(quizListModels.get(position).getLevel());

    }

    @Override
    public int getItemCount() {
        if (quizListModels == null){
            return 0;
        }
        else {
            return quizListModels.size();
        }
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        private ImageView listImage;
        private TextView listTitle;
        private TextView listDesc;
        private TextView listLevel;
        private Button listButton;
        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.listImage);
            listTitle = itemView.findViewById(R.id.listTitle);
            listDesc = itemView.findViewById(R.id.listDesc);
            listLevel = itemView.findViewById(R.id.listDifficulty);
            listButton = itemView.findViewById(R.id.listButton);

            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onQuizListItemClicked.onItemClicked(getAdapterPosition());
                }
            });
        }
    }
    public interface  OnQuizListItemClicked{
        void onItemClicked(int position);
    }
}
