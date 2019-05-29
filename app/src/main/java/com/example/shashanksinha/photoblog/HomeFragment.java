package com.example.shashanksinha.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private List<BlogPost> blog_list2;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private List<User> user_list;
    private List<User> user_list2;

    private List<String> blogPostId2;
    private List<String> blogPostId3;

    private DocumentSnapshot lastDocumentSnapshot;

    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);


        blog_list = new ArrayList<>();
        blog_list2 = new ArrayList<>();


        blog_list_view = view.findViewById(R.id.blog_list_view);
        user_list = new ArrayList<>();
        user_list2 = new ArrayList<>();

        blogPostId2 = new ArrayList<>();
        blogPostId3 = new ArrayList<>();


        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseFirestore = FirebaseFirestore.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        loadMorePost();

                    }

                }
            });


            blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list, blogPostId2);
            blog_list_view.setAdapter(blogRecyclerAdapter);





            Query query;

            if (lastDocumentSnapshot == null) {
                query = firebaseFirestore.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(3);
            } else {

                query = firebaseFirestore.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDocumentSnapshot)
                        .limit(3);
            }


            query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                                    blogPost.setDocumentId(doc.getDocument().getId());
                                    final String blogId = blogPost.getDocumentId();

                                    String blogUserId = doc.getDocument().getString("user_id");


                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {
                                                User user = task.getResult().toObject(User.class);

                                                user_list.add(user);
                                                blog_list.add(blogPost);
                                                blogPostId2.add(blogId);

                                                Log.d("xlr8_xyU: ", String.valueOf(user_list.get(user_list.size() - 1).getName()));
                                                Log.d("xlr8_xyB: ", String.valueOf(blog_list.get(blog_list.size() - 1).getImage_thumb()));
                                                Log.d("xlr8_xyBI: ", String.valueOf(blogPostId2.get(blogPostId2.size() - 1)));

                                            }
                                            blogRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    });

                                }
                                if(queryDocumentSnapshots.size() > 0)
                                {
                                    lastDocumentSnapshot = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                                }
                            }

                        }
                    }
                }
            });






/*


            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING )
                    .limit(1);



            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    Log.d("xlr8_cDocumentSnapshot1",String.valueOf(queryDocumentSnapshots));

                    if(queryDocumentSnapshots != null)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            if(isFirstPageFirstLoad) {
                                // Getting last document when first query is loaded..(only run once, not in loop)
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                Log.d("xlr8_cLastVisible:", String.valueOf(lastVisible));

                            }


                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges())
                            {
                                if(doc.getType() == DocumentChange.Type.ADDED)
                                {
                                    //Getting Id of documents...
                                    //Now we need to send this id to our modal class
                                    String blogPostId = doc.getDocument().getId();
                                    Log.d("xlr8_cBlogPostId:", String.valueOf(blogPostId));



                                    final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);

                                    blogPost.setDocumentId(doc.getDocument().getId());

                                    final String id = blogPost.getDocumentId();


                                    String blogUserId = doc.getDocument().getString("user_id");
                                    Log.d("xlr8_c_BlogPostUserId:", String.valueOf(blogUserId));


                                    firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful())
                                            {
                                                User user = task.getResult().toObject(User.class);

                                                if(isFirstPageFirstLoad) {
                                                    user_list.add(user);
                                                    blog_list.add(blogPost);
                                                    blogPostId2.add(id);
                                                    Log.d("xlr8_cBLOG_POST_1:", String.valueOf(blogPost));
                                                    Log.d("xlr8_cBlogList1",String.valueOf(blog_list));


                                                } else {
                                                    user_list.add(0,user);
                                                    // ADD new post at top of recycler view...
                                                    blog_list.add(0,blogPost);
                                                    blogPostId2.add(0,id);
                                                    Log.d("xlr8_cBLOG_POST_2:", String.valueOf(blogPost));
                                                    Log.d("xlr8_cBlogList2",String.valueOf(blog_list));

                                                }

                                                blogRecyclerAdapter.notifyDataSetChanged();

                                            }
                                        }
                                    });


                                }
                            }

                            Log.d("xlr8_cBoolean",String.valueOf(isFirstPageFirstLoad));
                            isFirstPageFirstLoad = false;
                            Log.d("xlr8_cBoolean",String.valueOf(isFirstPageFirstLoad));


                        }

                    }

                }
            });




*/


        }


        // Inflate the layout for this fragment
        return view;
    }
    public void loadMorePost()
    {

        Query query;

        if (lastDocumentSnapshot == null) {
            query = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(2);
        } else {

            query = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastDocumentSnapshot)
                    .limit(2);
        }


        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                                blogPost.setDocumentId(doc.getDocument().getId());
                                final String blogId = blogPost.getDocumentId();

                                String blogUserId = doc.getDocument().getString("user_id");


                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            User user = task.getResult().toObject(User.class);

                                            user_list.add(user);
                                            blog_list.add(blogPost);
                                            blogPostId2.add(blogId);

                                            Log.d("xlr8_xyU: ", String.valueOf(user_list.get(user_list.size() - 1).getName()));
                                            Log.d("xlr8_xyB: ", String.valueOf(blog_list.get(blog_list.size() - 1).getImage_thumb()));
                                            Log.d("xlr8_xyBI: ", String.valueOf(blogPostId2.get(blogPostId2.size() - 1)));


                                        }
                                        blogRecyclerAdapter.notifyDataSetChanged();

                                    }
                                });

                            }
                            if(queryDocumentSnapshots.size() > 0)
                            {
                                lastDocumentSnapshot = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            }
                        }

                    }
                }
            }
        });

    }


}
