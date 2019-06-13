package com.example.firadevianas.deteksitext;

import android.app.DatePickerDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class FragmentAddTransaction extends Fragment {

    EditText etDate, etAmount, etDesc, etTitle;
    Spinner spinner;
    Button btnSave;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseRecyclerAdapter<Transaction, Recycler_view> adapter;

    ArrayAdapter<CharSequence> adapterArray;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addtransaction, container, false);

        etDate= view.findViewById(R.id.Etdate);
        etDesc = view.findViewById(R.id.Etdesc);
        etTitle = view.findViewById(R.id.Ettitle);
        spinner= view.findViewById(R.id.category_spinner);
        btnSave = view.findViewById(R.id.btn_save);
        etAmount = view.findViewById(R.id.Etamount);

        if(getArguments()!= null){
            //   String getCat = getArguments().getString("getCat").toString();
            String getDate = getArguments().getString("getDate").toString();
            String getBrand = getArguments().getString("getBrand").toString();
            String getAmount = getArguments().getString("getAmount").toString();
            etAmount.setText(getAmount);
            etTitle.setText(getBrand);
            etDate.setText(getDate);
            // spinner.setSelection(adapterArray.getPosition(getCat));
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(user.getUid()).child("ExpenseDB");
        databaseReference.keepSynced(true);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExpense();
            }
        });

        adapterArray = ArrayAdapter.createFromResource(getActivity(),R.array.categories,R.layout.spinner_textview);
        adapterArray.setDropDownViewResource(R.layout.spinner_textview);
        spinner.setAdapter(adapterArray);

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mdatePickerDialog;
                mdatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR,year);
                        calendar.set(Calendar.MONTH,month);
                        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String formatted = format1.format(calendar.getTime());
                        etDate.setText(formatted);
                    }
                },mYear,mMonth,mDay);
                mdatePickerDialog.setTitle("Select Date");
                mdatePickerDialog.show();

            }
        });


        return view;
    }

    private void AddExpense() {
        String date = etDate.getText().toString();
        String amount = etAmount.getText().toString();
        String desc = etDesc.getText().toString();
        String title = etTitle.getText().toString();
        String category = spinner.getSelectedItem().toString();
        String id = databaseReference.push().getKey();
        Transaction transaction = new Transaction(id,date,amount,desc,category,title);

        if(TextUtils.isEmpty(date)){
            Toast.makeText(getContext().getApplicationContext(),"Please Enter the date ",Toast.LENGTH_SHORT).show();
            return;
        }if(TextUtils.isEmpty(desc)){
            Toast.makeText(getContext().getApplicationContext(),"Please Enter the description ",Toast.LENGTH_SHORT).show();
            return;
        }if(TextUtils.isEmpty(category)){
            Toast.makeText(getContext().getApplicationContext(),"Please Enter the category ",Toast.LENGTH_SHORT).show();
            return;
        }if(TextUtils.isEmpty(title)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the title ", Toast.LENGTH_SHORT).show();
            return;
        }if(TextUtils.isEmpty(amount)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the amount ", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(id).setValue(transaction).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getActivity(),"failed"+task.getException(),Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"Saved",Toast.LENGTH_SHORT).show();
                    FragmentChart newFragmentChart = new FragmentChart();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    getFragmentManager().popBackStack();
                    fragmentTransaction.replace(R.id.fragment_container, newFragmentChart);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });

    }
}