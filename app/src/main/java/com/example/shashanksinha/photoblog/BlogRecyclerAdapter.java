package com.example.shashanksinha.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public List<User> user_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private TextView blogDeleteTv;
    private List<String> blogPostId2;


    public BlogRecyclerAdapter(List<BlogPost> blog_list, List<User> user_list, List<String> blogPostId2)
    {
        this.blog_list = blog_list;
        this.user_list = user_list;
        this.blogPostId2 = blogPostId2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d("xlr8_xx2: ", String.valueOf(user_list.get(0).getName()));
        Log.d("xlr8_xx2: ", String.valueOf(blog_list.get(0).getImage_thumb()));
        Log.d("xlr8_xx2: ", String.valueOf(blogPostId2.get(0)));

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);



        final String blog_post_id = blogPostId2.get(position);

        Log.d("xlr8xx: ",blog_post_id);

        Log.d("xlr8xx_2: ",blogPostId2.get(position));


        final String curr_user_id = mAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_uri = blog_list.get(position).getImage_uri();
        String thumb_uri = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_uri,thumb_uri);

        String user_id = blog_list.get(position).getUser_id();

        String username = user_list.get(position).getName();
        holder.setUserName(username);


        String userImageUri = user_list.get(position).getImage();
        String thumbImageUri = user_list.get(position).getThumb_image();
        holder.setUserImage(userImageUri,thumbImageUri);



        long millisecond = blog_list.get(position).getTimestamp().getTime();
        Log.d("xlr8_d: ", String.valueOf(millisecond));
       // Date date=new Date(millisecond);
       // SimpleDateFormat sfd = new SimpleDateFormat("dd-MMM, yyyy HH:mm");
       // sfd.format(new Date(millisecond));
        // Log.d("xlr8_date", String.valueOf(date));
        String dateString = new SimpleDateFormat("MMM dd, yyyy").format(new Date(millisecond));
        holder.setDate(String.valueOf(dateString));

        // Get Likes Count...
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                if(queryDocumentSnapshots != null)
                {
                    if(!queryDocumentSnapshots.isEmpty())
                    {

                        int count = queryDocumentSnapshots.size();

                        holder.updateLikesCount(count);

                    } else {

                        holder.updateLikesCount(0);

                    }

                }
            }
        });


        // Get if curr user liked that post...
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(curr_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(documentSnapshot != null)
                {

                    if(documentSnapshot.exists())
                    {
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                    } else {

                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                    }

                }

            }
        });


        // Likes Feature...
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes")
                        .document(curr_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                        if(!task.getResult().exists())
                        {
                            Map<String, Object> likesMap = new HashMap<>();

                            likesMap.put("timestamp",FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts").document(blog_post_id).collection("Likes")
                                    .document(curr_user_id).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts").document(blog_post_id).collection("Likes")
                                    .document(curr_user_id).delete();

                        }
                    }
                });


            }
        });


        // Comment Action...
        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent comment_intent = new Intent(context, CommentsActivity.class);
                comment_intent.putExtra("blog_post_id",blog_post_id);
                context.startActivity(comment_intent);
            }
        });


        // Count Comments...
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                if(queryDocumentSnapshots != null)
                {
                    if(!queryDocumentSnapshots.isEmpty())
                    {

                        int count = queryDocumentSnapshots.size();

                        holder.updateCommentCount(count);

                    } else {

                        holder.updateCommentCount(0);

                    }

                }
            }
        });


        //Delete Post...

        String blog_user_id = blog_list.get(position).getUser_id();
        String current_user_id = mAuth.getCurrentUser().getUid();

        if(blog_user_id.equals(current_user_id)) {

            blogDeleteTv.setVisibility(View.VISIBLE);
        }

        blogDeleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts").document(blog_post_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Post Removed!", Toast.LENGTH_SHORT).show();
                        blog_list.remove(position);
                        user_list.remove(position);

                        MainActivity.activity.recreate();
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }





    // ViewHolder....
    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView usernameTextView;
        private TextView dateTextView;
        private ImageView userImageView;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCountComment;




        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            blogDeleteTv = itemView.findViewById(R.id.delete_post_tv);
        }

        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUri)
        {
            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                    .into(blogImageView);

        }

        public void setUserName(String username)
        {
            usernameTextView = mView.findViewById(R.id.blog_user_name);
            usernameTextView.setText(username);
        }

        public void setDate(String dateString)
        {
            dateTextView = mView.findViewById(R.id.blog_date);
            dateTextView.setText(dateString);
        }

        public void setUserImage(String userImageUri, String thumbImageUri)
        {
            userImageView = mView.findViewById(R.id.blog_user_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(userImageUri)
                    .thumbnail(Glide.with(context).load(thumbImageUri))
                    .into(userImageView);
        }

        public void updateLikesCount(int count)
        {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count+" Likes");
        }

        public void updateCommentCount(int count)
        {
            blogCountComment = mView.findViewById(R.id.comment_count_tv);
            blogCountComment.setText(count+" Comments");
        }
    }
}
