package com.example.firadevianas.deteksitext;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditTransaction extends AppCompatActivity {

    EditText ETdate,ETamount,ETdesc;
    ImageButton btnsave;
    TextView TVtitle,TVid;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String id;

    Spinner spinCat;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        spinCat = findViewById(R.id.category_spinner);

        adapter = ArrayAdapter.createFromResource(EditTransaction.this,R.array.categories,R.layout.spinner_textview);
        adapter.setDropDownViewResource(R.layout.spinner_textview);
        spinCat.setAdapter(adapter);


        getIncomingIntent();

        btnsave = findViewById(R.id.btn_save);

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateExpense();
            }
        });

    }
    public void UpdateExpense(){
        final String date = ETdate.getText().toString();
        final String amount = ETamount.getText().toString();
        final String desc = ETdesc.getText().toString();
        final String title = TVtitle.getText().toString();
        final String category = spinCat.getSelectedItem().toString();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(user.getUid()).child("ExpenseDB");

        Query query = databaseReference.orderByChild("id").equalTo(TVid.getText().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot datas : dataSnapshot.getChildren()){
                    datas.getRef().child("date").setValue(date);
                    datas.getRef().child("title").setValue(title);
                    datas.getRef().child("amount").setValue(amount);
                    datas.getRef().child("desc").setValue(desc);
                    datas.getRef().child("category").setValue(category);
                }
                Toast.makeText(EditTransaction.this,"Expense Updated",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void getIncomingIntent() {
        if(getIntent().hasExtra("cdate")
                && getIntent().hasExtra("camount")
                && getIntent().hasExtra("cdesc")
                && getIntent().hasExtra("ccategory")
                && getIntent().hasExtra("ctitle")
                && getIntent().hasExtra("cid"))
        {
            String id = getIntent().getStringExtra("cid");
            String date = getIntent().getStringExtra("cdate");
            String amount = getIntent().getStringExtra("camount");
            String desc = getIntent().getStringExtra("cdesc");
            String cat = getIntent().getStringExtra("ccategory");
            String title = getIntent().getStringExtra("ctitle");

            spinCat = findViewById(R.id.category_spinner);
            ETdate = findViewById(R.id.Etdate);
            ETamount = findViewById(R.id.Etamount);
            ETdesc = findViewById(R.id.Etdesc);
            TVtitle = findViewById(R.id.TVTitle);
            TVid = findViewById(R.id.TVId);

            spinCat.setSelection(adapter.getPosition(cat));
            ETdate.setText(date);
            ETamount.setText(amount);
            ETdesc.setText(desc);
            TVtitle.setText(title);
            TVid.setText(id);
        }
    }

}
