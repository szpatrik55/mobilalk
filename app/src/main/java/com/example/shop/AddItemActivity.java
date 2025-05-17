package com.example.shop;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddItemActivity extends AppCompatActivity {

    private EditText nameEditText, descEditText, priceEditText;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        nameEditText = findViewById(R.id.name_input);
        descEditText = findViewById(R.id.desc_input);
        priceEditText = findViewById(R.id.price_input);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
    }

    public void addItem(View view) {
        String name = nameEditText.getText().toString();
        String desc = descEditText.getText().toString();
        String price = priceEditText.getText().toString() + " Ft";

        if (name.isEmpty() || desc.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
            return;
        }

        ShoppingItem item = new ShoppingItem(name, desc, price, 0, R.drawable.dummy, 0);
        mItems.add(item).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Termék hozzáadva!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show();
        });
    }
}
