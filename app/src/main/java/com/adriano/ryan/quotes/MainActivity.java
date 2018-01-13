package com.adriano.ryan.quotes;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentListenOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String QUOTE_KEY = "quote";
    public static final String AUTHOR_KEY = "author";
    public static final String TAG = "Inspiring Quotes";

    private TextView mTextView;

    private DocumentReference mDocumentReference = FirebaseFirestore.getInstance().document("sampleData/inspiration");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textQuoteDisplay);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        DocumentListenOptions verboseOptions = new DocumentListenOptions();
        verboseOptions.includeMetadataChanges();

        mDocumentReference.addSnapshotListener(this, verboseOptions, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    Log.w(TAG, "Got a " + (documentSnapshot.getMetadata().hasPendingWrites() ? "local" : "server" + " update!") , e);

                    InspiringQuote quote = documentSnapshot.toObject(InspiringQuote.class);
                    String quoteText = "\"" + quote.getQuote() + "\" --" + quote.getAuthor();
                    mTextView.setText(quoteText);
                } else {
                    Log.w(TAG, "Got an exception!", e);
                }
            }
        });
    }

    public void onFetchClick(View view) {
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    InspiringQuote quote = documentSnapshot.toObject(InspiringQuote.class);
                    String quoteText = "\"" + quote.getQuote() + "\" --" + quote.getAuthor();
                    mTextView.setText(quoteText);
                }
            }
        });
    }

    public void onSaveClick(View view) {
        EditText quoteView = findViewById(R.id.textQuote);
        EditText authorView = findViewById(R.id.textAuthor);

        String quoteText = quoteView.getText().toString();
        String authorText = authorView.getText().toString();

        if (quoteText.isEmpty() || authorText.isEmpty()) {
            return;
        }

        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(QUOTE_KEY, quoteText);
        dataToSave.put(AUTHOR_KEY, authorText);

        mDocumentReference.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(TAG, "Document has been saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Document was not saved");
            }
        });
    }
}
