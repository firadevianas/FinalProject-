package com.example.firadevianas.deteksitext;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;

public class Detail extends AppCompatActivity {

    TextView TVdate,TVamount,TVdesc,TVcat,TVtitle;
    ImageButton deletebtn;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        getIncomingIntent();

        id = getIntent().getStringExtra("id");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("ExpenseDB").child(id);


        deletebtn= findViewById(R.id.deletebtn);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Detail.this);
                builder.setCancelable(true);
                builder.setTitle("Delete Expense");
                builder.setMessage("are you sure delete this expense?");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Detail.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                        mPostReference.removeValue();
                        finish();
                    }
                });
                builder.show();
            }
        });
    }

    private void getIncomingIntent() {
        if(getIntent().hasExtra("date")&& getIntent().hasExtra("amount")&& getIntent().hasExtra("desc")
                && getIntent().hasExtra("category")&& getIntent().hasExtra("title")&&getIntent().hasExtra("id"))
        {
            String id = getIntent().getStringExtra("id");
            String date = getIntent().getStringExtra("date");
            String amount = getIntent().getStringExtra("amount");
            String desc = getIntent().getStringExtra("desc");
            String cat = getIntent().getStringExtra("category");
            String title = getIntent().getStringExtra("title");

            TVdate = findViewById(R.id.dateview);
            TVamount = findViewById(R.id.amountview);
            TVdesc = findViewById(R.id.desciptionview);
            TVcat = findViewById(R.id.categoryview);
            TVtitle = findViewById(R.id.nameTrans);

            TVdate.setText(date);
            DecimalFormat formater = new DecimalFormat("#,###,###");
            String formatAmount = formater.format(Integer.parseInt(amount.toString()));
            TVamount.setText(formatAmount);
            TVdesc.setText(desc);
            TVcat.setText(cat);
            TVtitle.setText(title);


        }
    }


}
