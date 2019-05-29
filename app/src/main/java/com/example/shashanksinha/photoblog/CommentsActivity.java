package com.example.shashanksinha.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String blog_post_id;
    private String curr_user_id;
    private RecyclerView comment_list;
    private List<Comments> commentslist;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.action_comments_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        curr_user_id = firebaseAuth.getCurrentUser().getUid();

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_edit_text);
        comment_post_btn = findViewById(R.id.comment_imageview_btn);


        comment_list = findViewById(R.id.comment_recycler_view);

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String comment_msg = comment_field.getText().toString();

                if(!TextUtils.isEmpty(comment_msg))
                {
                    Map<String,Object> commentsMap = new HashMap<>();
                    commentsMap.put("message",comment_msg);
                    commentsMap.put("user_id",curr_user_id);
                    commentsMap.put("timestamp",FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if(!task.isSuccessful())
                            {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this,"COMMENT Error : "+errorMessage,Toast.LENGTH_LONG).show();

                            } else {

                                comment_field.setText("");
                            }
                        }
                    });
                }
            }
        });

        commentslist = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentslist);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);


        Log.d("xlr8_comment1:", String.valueOf(comment_list));

        Query query = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .orderBy("timestamp",Query.Direction.ASCENDING);

        //RecyclerView Firebase List...

                query.addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(queryDocumentSnapshots != null)
                        {
                            if(!queryDocumentSnapshots.isEmpty())
                            {
                                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges())
                                {
                                    if(doc.getType() == DocumentChange.Type.ADDED)
                                    {
                                        Comments comments = doc.getDocument().toObject(Comments.class);
                                        commentslist.add(comments);

                                        commentsRecyclerAdapter.notifyDataSetChanged();
                                        Log.d("xlr8_comment2:", String.valueOf(comment_list));



                                    }
                                }
                            }
                        }

                    }
                });








    }
}
