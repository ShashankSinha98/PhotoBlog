package com.example.shashanksinha.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public CommentsRecyclerAdapter(List<Comments> commentsList)
    {
        this.commentsList = commentsList;
        Log.d("xlr8_comment3:", String.valueOf(commentsList));

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        Log.d("xlr8_comment4: ", String.valueOf(commentsList));


        String commentMessage = commentsList.get(position).getMessage();
        holder.setCommentMessage(commentMessage);
        String user_id = commentsList.get(position).getUser_id();



        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            if (task.getResult().exists())
                            {

                                String username = task.getResult().getString("name");
                                holder.setCommentUsername(username);

                                String image = task.getResult().getString("image");
                                String thumb_image = task.getResult().getString("thumb_image");
                                holder.setUserImage(image,thumb_image);
                            }
                        }
                    }
                });

    }

    @Override
    public int getItemCount() {

        if(commentsList != null)
        {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        private TextView comment_message;
        private TextView comment_username;
        private ImageView comment_userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentMessage(String message)
        {
            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);
        }

        public void setCommentUsername(String username)
        {
            comment_username = mView.findViewById(R.id.comment_username);
            comment_username.setText(username);
        }

        public void setUserImage(String imageUri, String thumbUri)
        {
            comment_userImage = mView.findViewById(R.id.comment_image);
            Glide.with(context).load(imageUri).thumbnail(Glide.with(context).load(thumbUri)).into(comment_userImage);
        }
    }
}
