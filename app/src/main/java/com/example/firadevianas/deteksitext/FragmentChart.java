package com.example.firadevianas.deteksitext;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class FragmentChart extends Fragment {
    Button janBtn, febBtn, marBtn, aprBtn, mayBtn, junBtn, julBtn, augBtn, sepBtn, octBtn, novBtn, decBtn;
    Button dailyBtn,weeklyBtn,monthlyBtn;
    private static String TAG = "FragmentChart";
    private List<Float> dataY = new ArrayList<>();
    private List<String> dataX = new ArrayList<>();
    float total = 0;
    List<Integer> amountCategory = new ArrayList<>();
    int expense = 0;
    PieDataSet pieDataSet;
    RelativeLayout relativeLayout,relativeLayoutSpend,relativeLayoutWeekly,relativeLayoutDaily,relativeLayoutTotal;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Transaction> options;
    FirebaseRecyclerAdapter<Transaction, Recycler_view> adapter;
    TextView tvExpense,tvExpenseMonth,tvTotal,tvWeekStart,tvWeekEnd,tvWeeklyTotal;

    ImageView leftArrowWeek,rightArrowWeek,dayLeftArrow,dayRightArrow;
    PieChart pieChart;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView todayExpense,todayDate;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    String stringDate;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chart, container, false);


        janBtn = view.findViewById(R.id.jan);
        janBtn.setOnClickListener(mListener);
        febBtn = view.findViewById(R.id.feb);
        febBtn.setOnClickListener(mListener);
        marBtn = view.findViewById(R.id.mar);
        marBtn.setOnClickListener(mListener);
        aprBtn = view.findViewById(R.id.Apr);
        aprBtn.setOnClickListener(mListener);
        mayBtn = view.findViewById(R.id.May);
        mayBtn.setOnClickListener(mListener);
        junBtn = view.findViewById(R.id.Jun);
        junBtn.setOnClickListener(mListener);
        julBtn = view.findViewById(R.id.jul);
        julBtn.setOnClickListener(mListener);
        augBtn = view.findViewById(R.id.Aug);
        augBtn.setOnClickListener(mListener);
        sepBtn = view.findViewById(R.id.Sep);
        sepBtn.setOnClickListener(mListener);
        octBtn = view.findViewById(R.id.Oct);
        octBtn.setOnClickListener(mListener);
        novBtn = view.findViewById(R.id.Nov);
        novBtn.setOnClickListener(mListener);
        decBtn = view.findViewById(R.id.Dec);
        decBtn.setOnClickListener(mListener);


        todayDate = view.findViewById(R.id.TVcurrentDate);
        tvWeeklyTotal = view.findViewById(R.id.weeklytotal);
        tvWeekStart = view.findViewById(R.id.dateStartWeek);
        tvWeekEnd = view.findViewById(R.id.dateEndWeek);
        leftArrowWeek = view.findViewById(R.id.leftArrow);
        rightArrowWeek = view.findViewById(R.id.rightArrow);
        dayLeftArrow = view.findViewById(R.id.dayleftArrow);
        dayRightArrow = view.findViewById(R.id.dayRightArrow);
        todayExpense = view.findViewById(R.id.TVcurrentExpense);
        tvTotal = view.findViewById(R.id.TVtotalExpense);
        tvExpenseMonth = view.findViewById(R.id.TVcurrentMonth);
        dailyBtn = view.findViewById(R.id.btn_daily);
        weeklyBtn=view.findViewById(R.id.btn_weekly);
        monthlyBtn = view.findViewById(R.id.btn_monthly);
        relativeLayoutDaily = view.findViewById(R.id.dailyInside);
        relativeLayoutWeekly = view.findViewById(R.id.relativeLayoutdateWeekly);
        relativeLayoutTotal = view.findViewById(R.id.relativeLayoutTotal);



        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        stringDate = format2.format(calendar.getTime());


        monthlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relativeLayout.getVisibility() == View.VISIBLE) {
                    relativeLayout.setVisibility(View.GONE);

                } else {
                    relativeLayout.setVisibility(View.VISIBLE);
                    relativeLayoutDaily.setVisibility(View.GONE);
                    relativeLayoutWeekly.setVisibility(View.GONE);
                    relativeLayoutSpend.setVisibility(View.GONE);
                    relativeLayoutTotal.setVisibility(View.GONE);
                }
            }
        });

        weeklyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relativeLayoutWeekly.getVisibility() == View.VISIBLE) {
                    relativeLayoutWeekly.setVisibility(View.GONE);

                } else {
                    relativeLayoutWeekly.setVisibility(View.VISIBLE);
                    relativeLayoutDaily.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.GONE);
                    relativeLayoutSpend.setVisibility(View.GONE);
                    relativeLayoutTotal.setVisibility(View.GONE);
                    String start = tvWeekStart.getText().toString();
                    String end = tvWeekEnd.getText().toString();
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    query(start,end);
                    pieChart.clear();
                }
            }
        });

        dailyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (relativeLayoutDaily.getVisibility() == View.VISIBLE) {
                    relativeLayoutDaily.setVisibility(View.GONE);
                } else {
                    relativeLayoutDaily.setVisibility(View.VISIBLE);
                    relativeLayoutWeekly.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.GONE);
                    relativeLayoutSpend.setVisibility(View.GONE);
                    relativeLayoutTotal.setVisibility(View.GONE);
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    String start = format1.format(date);
                    String end = format1.format(date);
                    query(start,end);
                    pieChart.clear();
                    todayDate.setText(start);
                }
            }
        });


        tvExpense = view.findViewById(R.id.expenserp);
        relativeLayout = view.findViewById(R.id.relativeLayoutdate);
        relativeLayoutSpend = view.findViewById(R.id.relativeSpending);

        relativeLayoutWeekly.setVisibility(View.GONE);
        relativeLayoutDaily.setVisibility(View.GONE);
        relativeLayoutSpend.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);

        Log.d(TAG, "onCreate: starting to create chart");
        pieChart = view.findViewById(R.id.idPieChart);
        Description description = new Description();
        description.setText("Expense by category");
        pieChart.setDescription(description);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleAlpha(40);
        pieChart.setCenterTextSize(8);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(user.getUid()).child("ExpenseDB");
        databaseReference.keepSynced(true);
        final String Start = stringDate;
        final String End = stringDate;
        query(Start, End);


        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar weekStartDate = Calendar.getInstance();
        weekStartDate.set(Calendar.DAY_OF_WEEK,weekStartDate.getActualMinimum(Calendar.DAY_OF_WEEK));
        final Calendar weekEndDate = Calendar.getInstance();
        weekEndDate.set(Calendar.DAY_OF_WEEK,weekEndDate.getActualMaximum(Calendar.DAY_OF_WEEK));

        final Calendar monthStartDate = Calendar.getInstance();
        monthStartDate.set(Calendar.DAY_OF_MONTH,monthStartDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        final Calendar monthEndDate = Calendar.getInstance();
        monthEndDate.set(Calendar.DAY_OF_MONTH,monthEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        final Calendar dayCurrentDate = Calendar.getInstance();

        dayLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayCurrentDate.add(Calendar.DATE,-1);
                Date yesterday = dayCurrentDate.getTime();
                todayDate.setText(sdf.format(yesterday));
                String start = todayDate.getText().toString();
                String end = todayDate.getText().toString();
                dataY.clear();
                dataX.clear();
                total = 0;
                query(start,end);
                pieChart.clear();
            }
        });
        dayRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayCurrentDate.add(Calendar.DATE,1);
                Date tomorrow = dayCurrentDate.getTime();
                todayDate.setText(sdf.format(tomorrow));
                String start = todayDate.getText().toString();
                String end = todayDate.getText().toString();
                dataY.clear();
                dataX.clear();
                total = 0;
                query(start,end);
                pieChart.clear();
            }
        });

        leftArrowWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekStartDate.add(Calendar.DAY_OF_MONTH, -7);
                Date firstDayOfWeek = weekStartDate.getTime();
                tvWeekStart.setText(sdf.format(firstDayOfWeek));
                weekEndDate.add(Calendar.DAY_OF_MONTH, -7);
                Date lastDayOfWeek = weekEndDate.getTime();
                tvWeekEnd.setText(sdf.format(lastDayOfWeek));
                String start = tvWeekStart.getText().toString();
                String end = tvWeekEnd.getText().toString();
                dataY.clear();
                dataX.clear();
                total = 0;
                query(start,end);
                pieChart.clear();
            }
        });
        rightArrowWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekStartDate.add(Calendar.DAY_OF_MONTH, 7);
                Date firstDayOfWeek = weekStartDate.getTime();
                tvWeekStart.setText(sdf.format(firstDayOfWeek));
                weekEndDate.add(Calendar.DAY_OF_MONTH, 7);
                Date lastDayOfWeek = weekEndDate.getTime();
                tvWeekEnd.setText(sdf.format(lastDayOfWeek));
                String start = tvWeekStart.getText().toString();
                String end = tvWeekEnd.getText().toString();
                dataY.clear();
                dataX.clear();
                total = 0;
                query(start,end);
                pieChart.clear();
            }
        });



        Date firstDayOfWeek = weekStartDate.getTime();
        tvWeekStart.setText(sdf.format(firstDayOfWeek));
        Date lastDayOfWeek = weekEndDate.getTime();
        tvWeekEnd.setText(sdf.format(lastDayOfWeek));


        return view;
    }

    public void query(String startDate, String endDate) {
        expense = 0;
        DecimalFormat formater = new DecimalFormat("#,###,###");
        String formatAmount = formater.format(Integer.parseInt(String.valueOf(expense)));
        tvExpense.setText(formatAmount);
        tvWeeklyTotal.setText(formatAmount);
        todayExpense.setText(formatAmount);
        Query query = databaseReference.orderByChild("date").startAt(startDate).endAt(endDate);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Transaction transaction = dataSnapshot.getValue(Transaction.class);

                int expenseAmount = Integer.parseInt(transaction.getAmount());
                expense += expenseAmount;

                DecimalFormat formater = new DecimalFormat("#,###,###");
                String formatExpense = formater.format(Integer.parseInt(String.valueOf(expense)));


                todayExpense.setText(formatExpense);
                String category = transaction.getCategory();
                float amount = Float.parseFloat(transaction.getAmount());

                tvExpense.setText(formatExpense);
                tvWeeklyTotal.setText(formatExpense);
                tvTotal.setText(formatExpense);

                if (!isCategoryExist(category)) {
                    dataX.add(transaction.getCategory());
                    Integer cat_idx = dataX.indexOf(category);
                    dataY.add(cat_idx, (float) 0.0);
                    amountCategory.add(cat_idx, 0);
                    float current_amount = (float) amountCategory.get(cat_idx);
                    float amountCat = amount + current_amount;
                    amountCategory.set(cat_idx, (int) amountCat);
                    reCalculate(amount, cat_idx, amountCat);
                } else {
                    Integer cat_idx = dataX.indexOf(category);
                    Integer current_amount = amountCategory.get(cat_idx);
                    if (current_amount == null) {
                        current_amount = 0;
                    }
                    float amountCat = amount + current_amount;
                    reCalculate(amount, cat_idx, amountCat);
                }
                addDataSet();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        options =
                new FirebaseRecyclerOptions.Builder<Transaction>()
                        .setQuery(databaseReference.orderByChild("date").startAt(startDate).endAt(endDate), Transaction.class)
                        .build();

        if (getActivity() != null) {
            adapter =
                    new FirebaseRecyclerAdapter<Transaction, Recycler_view>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull Recycler_view holder, int position, @NonNull final Transaction model) {
                            holder.text_date.setText(model.getDate());
                            DecimalFormat formater = new DecimalFormat("#,###,###");
                            String formatAmount = formater.format(Integer.parseInt(model.getAmount().toString()));
                            holder.text_amount.setText(formatAmount);
                            holder.text_category.setText(model.getCategory());
                            holder.text_title.setText(model.getTitle());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    String[] options = {"Details", "Update"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                //detail activity
                                                Intent intent = new Intent(getActivity(), Detail.class);
                                                intent.putExtra("date", model.getDate());
                                                intent.putExtra("amount", model.getAmount());
                                                intent.putExtra("desc", model.getDesc());
                                                intent.putExtra("category", model.getCategory());
                                                intent.putExtra("title", model.getTitle());
                                                intent.putExtra("id", model.getId());
                                                startActivity(intent);
                                            }
                                            if (which == 1) {
                                                //update
                                                Intent intent = new Intent(getActivity(), EditTransaction.class);
                                                intent.putExtra("cdate", model.getDate());
                                                intent.putExtra("camount", model.getAmount());
                                                intent.putExtra("cdesc", model.getDesc());
                                                intent.putExtra("ccategory", model.getCategory());
                                                intent.putExtra("ctitle", model.getTitle());
                                                intent.putExtra("cid", model.getId());
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    builder.create().show();

                                }

                            });


                        }

                        @NonNull
                        @Override
                        public Recycler_view onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                            View itemView = LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.transaction_item, viewGroup, false);
                            return new Recycler_view(itemView);
                        }


                    };
            adapter.startListening();
            recyclerView.setAdapter(adapter);
        }

    }

    private final View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.jan:
                    String jan ="2019-01-01";
                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar = Calendar.getInstance();
                    Date date = null;
                    try {
                        date = sdf.parse(jan);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    calendar.setTime(date);
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.add(Calendar.DATE, -1);
                    Date lastDayOfMonth = calendar.getTime();
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    expense=0;
                    String startJan = sdf.format(date);
                    String endJan = sdf.format(lastDayOfMonth);
                    query(startJan,endJan);
                    pieChart.clear();
                    tvExpenseMonth.setText("January");
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "January", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.feb:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    expense=0;
                    pieChart.clear();
                    String startFeb = "2019-02-01";
                    String endFeb = "2019-02-28";
                    query(startFeb,endFeb);
                    tvExpenseMonth.setText("February");
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "February", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.mar:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    expense = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("March");
                    String startMar = "2019-03-01";
                    String endMar = "2019-03-31";
                    query(startMar,endMar);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "March", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Apr:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    tvExpenseMonth.setText("April");
                    pieChart.clear();
                    String startApr = "2019-04-01";
                    String endApr = "2019-04-30";
                    query(startApr,endApr);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "April", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.May:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("May");
                    String startMay = "2019-05-01";
                    String endMay = "2019-05-31";
                    query(startMay,endMay);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "May", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Jun:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("June");
                    String startJun = "2019-06-01";
                    String endJun = "2019-06-30";
                    query(startJun,endJun);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "June", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.jul:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("July");
                    String startJul = "2019-07-01";
                    String endJul = "2019-07-31";
                    query(startJul,endJul);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "July", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Aug:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    tvExpenseMonth.setText("August");
                    pieChart.clear();
                    String startAug = "2019-08-01";
                    String endAug = "2019-08-31";
                    query(startAug,endAug);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "August", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Sep:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    tvExpenseMonth.setText("September");
                    pieChart.clear();
                    String startSep = "2019-09-01";
                    String endSep = "2019-09-31";
                    query(startSep,endSep);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "September", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Oct:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("October");
                    String startOct = "2019-10-01";
                    String endOct = "2019-10-31";
                    query(startOct,endOct);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "October", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Nov:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    pieChart.clear();
                    tvExpenseMonth.setText("November");
                    String startNov = "2019-11-01";
                    String endNov = "2019-11-31";
                    query(startNov,endNov);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "November", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Dec:
                    dataY.clear();
                    dataX.clear();
                    total = 0;
                    tvExpenseMonth.setText("December");
                    pieChart.clear();
                    String startDec = "2019-12-01";
                    String endDec = "2019-12-31";
                    query(startDec,endDec);
                    if (relativeLayoutSpend.getVisibility() == View.VISIBLE) {
                        relativeLayoutSpend.setVisibility(View.GONE);
                    } else {
                        relativeLayoutSpend.setVisibility(View.VISIBLE);
                        relativeLayout.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "December", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void reCalculate(float amount, Integer cat_idx, float amountCat) {
        total += amount;

        for (int i = 0; i< dataY.size(); i++){
            if(i==cat_idx){
                dataY.set(cat_idx,(float) amountCat/total * 100);
            }else{
                dataY.set(i,(float) amountCategory.get(i)/total * 100);
            }
        }
    }

    private boolean isCategoryExist(String category) {
        boolean exist = false;
        for(int i=0; i< dataX.size(); i++){
            if(category.equals(dataX.get(i))){
                exist = true;
                break;
            }
        }
        return exist;
    }

    private void addDataSet() {

        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < dataY.size(); i++){
            yEntrys.add(new PieEntry(dataY.get(i), dataX.get(i)));
        }
        for(int i = 1; i < dataX.size(); i++){
            xEntrys.add(dataX.get(i));

        }

        //create the data set
        pieDataSet = new PieDataSet(yEntrys, "Expenses");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        pieDataSet.setSliceSpace(2f);
        pieDataSet.setSelectionShift(4f);
        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }

}