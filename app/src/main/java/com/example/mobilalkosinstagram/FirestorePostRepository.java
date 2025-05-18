package com.example.mobilalkosinstagram;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirestorePostRepository {
    private static final String TAG = "FirestorePostRepo";
    private static final String COLLECTION_NAME = "posts";

    private final CollectionReference postsRef;

    public FirestorePostRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        postsRef = db.collection(COLLECTION_NAME);
    }

    public void addPost(Post post, AppCompatActivity activity) {
        postsRef.add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Post added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding post", e);
                    Toast.makeText(activity, "Hiba történt a poszt hozzáadása közben", Toast.LENGTH_SHORT).show();
                });
    }

    public void deletePost(String postId) {
        postsRef.document(postId).delete()
                .addOnSuccessListener(unused -> Log.d(TAG, "Post deleted"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting post", e));
    }

    public void updatePost(Post post, Runnable onSuccess, Consumer<Exception> onError) {
        postsRef.document(post.getId()).set(post)
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e));
    }

    public CollectionReference getPostsCollection() {
        return postsRef;
    }

    public void getAllPosts(OnSuccessListener<List<Post>> onSuccess, OnFailureListener onFailure) {
        postsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> postList = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            postList.add(post);
                        }
                    }
                    onSuccess.onSuccess(postList);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Hely szerint rendezett posztok lekérése (egyszerű változat, timestamp nélkül)
     */
    public void getPostsOrderedByLocation(Consumer<List<Post>> onSuccess, Consumer<Exception> onError) {
        postsRef.orderBy("location")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);
                    onSuccess.accept(posts);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Minimális hosszúságú leírással rendelkező posztok szűrése
     * @param minLength Minimális karakterhossz
     */
    public void getPostsByDescriptionLength(int minLength, Consumer<List<Post>> onSuccess, Consumer<Exception> onError) {
        // Először szűrés a leírás hossza szerint
        postsRef.whereGreaterThanOrEqualTo("description", String.format("%"+minLength+"s", ""))
                .orderBy("description")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);
                    // Pontos szűrés (mert Firestore csak prefix alapján szűr)
                    posts.removeIf(post -> post.getDescription().length() < minLength);
                    onSuccess.accept(posts);
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Rövid (100 karakternél rövidebb) leírással rendelkező posztok szűrése
     */
    public void getPostsWithShortDescriptions(Consumer<List<Post>> onSuccess, Consumer<Exception> onError) {
        // Először minden posztot lekérünk, mert Firestore-ben nincs "less than" operátor string hosszra
        postsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);
                    // Szűrjük a rövid leírású posztokat
                    posts.removeIf(post ->
                            post.getDescription() == null ||
                                    post.getDescription().length() >= 100
                    );
                    onSuccess.accept(posts);
                })
                .addOnFailureListener(onError::accept);
    }
}