package com.example.firadevianas.deteksitext;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class FragmentAccount extends Fragment {
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth auth;
    TextView tvBudget, tvBalance, tvExpense, tvDateFrom, tvDateTo;
    EditText etAmount, dateFrom, dateTo, EtAlert1, EtAlert2, EtAlert3;
    Button btnSaveBudget, btnLogout;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReferencebudget, databaseReferenceTrans;
    int totalBudget = 0;
    int balance = 0;
    int expense = 0;
    String dateFromdb = null;
    String dateTodb = null;
    String percen1 = null;
    String percen2 = null;
    String percen3 = null;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RelativeLayout rlBudget = null;
    ImageButton btnBudget;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferencebudget = firebaseDatabase.getReference().child(user.getUid()).child("BudgetPlan");
        databaseReferenceTrans = firebaseDatabase.getReference().child(user.getUid()).child("ExpenseDB");
        databaseReferencebudget.keepSynced(true);
        databaseReferenceTrans.keepSynced(true);

        rlBudget = view.findViewById(R.id.rlBudgetPlan);
        tvBudget = view.findViewById(R.id.budgetrp);

        tvBalance = view.findViewById(R.id.balancerp);
        tvExpense = view.findViewById(R.id.expenserp);
        etAmount = view.findViewById(R.id.editamount);
        dateFrom = view.findViewById(R.id.editDateFrom);
        dateTo = view.findViewById(R.id.editDateTo);

        EtAlert1 = view.findViewById(R.id.percen1);
        EtAlert2 = view.findViewById(R.id.percen2);
        EtAlert3 = view.findViewById(R.id.percen3);

        tvDateFrom = view.findViewById(R.id.BudgetdateFrom);
        tvDateTo = view.findViewById(R.id.BudgetdateTo);


        btnBudget = view.findViewById(R.id.hideShow);
        rlBudget.setVisibility(View.GONE);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlBudget.getVisibility() == View.VISIBLE) {
                    rlBudget.setVisibility(View.GONE);
                } else {
                    rlBudget.setVisibility(View.VISIBLE);
                }
            }
        });
        btnSaveBudget = view.findViewById(R.id.buttonSave);

        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBudget();
            }
        });

        dateTo.setOnClickListener(new View.OnClickListener() {
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
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String formatted = format1.format(calendar.getTime());
                        dateTo.setText(formatted);
                    }
                }, mYear, mMonth, mDay);
                mdatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                mdatePickerDialog.setTitle("Select Date");
                mdatePickerDialog.show();
            }
        });
        dateFrom.setOnClickListener(new View.OnClickListener() {
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
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String formatted = format1.format(calendar.getTime());
                        dateFrom.setText(formatted);
                    }
                }, mYear, mMonth, mDay);
                mdatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                mdatePickerDialog.setTitle("Select Date");
                mdatePickerDialog.show();
            }
        });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getActivity(), Login.class));
                    getActivity().finish();
                }
            }
        };

        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logout();
            }
        });


        Query queryBudget = databaseReferencebudget.orderByChild("date").startAt(dateFromdb).endAt(dateTodb);
        queryBudget.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BudgetPlan budgetPlan = postSnapshot.getValue(BudgetPlan.class);
                    String budget = budgetPlan.getAmount();
                    String fromdate = budgetPlan.getFromDate();
                    String todate = budgetPlan.getToDate();
                    String alert1 = budgetPlan.getAlert1();
                    String alert2 = budgetPlan.getAlert2();
                    String alert3 = budgetPlan.getAlert3();
                    DecimalFormat formater = new DecimalFormat("#,###,###");
                    String formatBudget = formater.format(Integer.parseInt(budget.toString()));
                    tvBudget.setText(formatBudget);
                    tvDateFrom.setText(fromdate);
                    tvDateTo.setText(todate);
                    dateFromdb = fromdate;
                    dateTodb = todate;
                    percen1 = alert1;
                    percen2 = alert2;
                    percen3 = alert3;
                    totalBudget = Integer.parseInt(budget);
                }
                Query query = databaseReferenceTrans.orderByChild("date").startAt(dateFromdb).endAt(dateTodb);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Transaction transaction = postSnapshot.getValue(Transaction.class);
                            int expenseAmount = Integer.parseInt(transaction.getAmount());
                            expense += expenseAmount;
                            balance = totalBudget - expense;
                        }
                        DecimalFormat formater = new DecimalFormat("#,###,###");
                        String formatExpense = formater.format(Integer.parseInt(String.valueOf(expense)));
                        String formatBalance = formater.format(Integer.parseInt(String.valueOf(balance)));
                        tvExpense.setText(formatExpense);
                        tvBalance.setText(formatBalance);

                        if(expense!=0) {
                            double percentUsage = (expense * 100) / totalBudget;


                            if (percentUsage >= Integer.valueOf(percen1) && percentUsage < Integer.valueOf(percen2)) {
                                String firstAlert = "Your spending reach more than " + percen1 + "% of your budget";
                                showAlert(firstAlert);
                            }
                            if (percentUsage >= Integer.valueOf(percen2) && percentUsage < Integer.valueOf(percen3)) {
                                String firstAlert = "Your spending reach more than " + percen2 + "% of your budget";
                                showAlert(firstAlert);
                            }
                            if (percentUsage >= Integer.valueOf(percen3)) {
                                String firstAlert = "Your spending reach more than " + percen3 + "% of your budget";
                                showAlert(firstAlert);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void showAlert(String Message) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Please Spend Wisely!")
                    .setMessage(Message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void addBudget() {
        String id = databaseReferencebudget.push().getKey();
        String fromDate = dateFrom.getText().toString();
        String toDate = dateTo.getText().toString();
        String amount = etAmount.getText().toString();
        String alert1 = EtAlert1.getText().toString();
        String alert2 = EtAlert2.getText().toString();
        String alert3 = EtAlert3.getText().toString();

        BudgetPlan budgetPlan = new BudgetPlan(id, fromDate, toDate, amount, alert1, alert2, alert3);

        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the Amount ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fromDate)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the date ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(toDate)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the date ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(alert1)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the Percentage  ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(alert2)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the Percentage ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.equals(alert1, alert2)) {
            Toast.makeText(getContext().getApplicationContext(), "The percentage can't be same ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.equals(alert1, alert3)) {
            Toast.makeText(getContext().getApplicationContext(), "The percentage can't be same ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.equals(alert2, alert3)) {
            Toast.makeText(getContext().getApplicationContext(), "The percentage can't be same ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(alert3)) {
            Toast.makeText(getContext().getApplicationContext(), "Please Enter the Percentage ", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReferencebudget.child(id).setValue(budgetPlan).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getActivity(), "failed" + task.getException(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(FragmentAccount.this).attach(FragmentAccount.this).commit();

                }
            }
        });
    }

    public void Logout() {
        auth.signOut();
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }


}