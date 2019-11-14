package com.example.target_club_in_donga.Attend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.target_club_in_donga.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendActivity_Admin_Detail_Information extends AppCompatActivity {
    private Button activity_attend_detail_button_admin, activity_attend_detail_button_attendance, activity_attend_detail_button_cancel, activity_attend_detail_button_attend_state;
    private TextView activity_attend_detail_textview_certification_number_name, activity_attend_detail_textview_certification_number, activity_attend_detail_textview_attend;

    private PieChart activity_attend_piechart;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    private String userName, userPhone, userId;
    private int attendCount = 0, tardyCount = 0, unsentCount = 0, absentCount = 0, flag = 0, absent;

    private RecyclerView activity_attend_detail_recyclerview_main_list;
    List<Attend_Information_Item> attendAdminItems = new ArrayList<>();
    List<String> uidLists = new ArrayList<>();

    private ArrayList<String> listStartTime = new ArrayList<>();

    private int menu_count = 0, listSize = 0;

    private String startTime;
    private String userFlag;

    private String clubName = "TCID";
    // 임시로 바꾼 부분

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_detail);

        Intent intent = getIntent();
        userName = intent.getExtras().getString("userName");
        userPhone = intent.getExtras().getString("userPhone");

        activity_attend_detail_textview_certification_number_name = (TextView) findViewById(R.id.activity_attend_detail_textview_certification_number_name);
        activity_attend_detail_textview_certification_number = (TextView) findViewById(R.id.activity_attend_detail_textview_certification_number);
        activity_attend_detail_button_attend_state = (Button) findViewById(R.id.activity_attend_detail_button_attend_state);
        activity_attend_detail_textview_attend = (TextView) findViewById(R.id.activity_attend_detail_textview_attend);

        activity_attend_piechart = (PieChart) findViewById(R.id.activity_attend_piechart);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        activity_attend_detail_textview_certification_number_name.setVisibility(View.GONE);
        activity_attend_detail_textview_certification_number.setVisibility(View.GONE);
        activity_attend_detail_button_attend_state.setVisibility(View.GONE);


        activity_attend_detail_recyclerview_main_list = (RecyclerView) findViewById(R.id.activity_attend_detail_recyclerview_main_list);
        activity_attend_detail_recyclerview_main_list.setLayoutManager(new LinearLayoutManager(AttendActivity_Admin_Detail_Information.this));

        final AttendActivity_Admin_Detail_Information.AttendAdminInformationActivity_AdminRecyclerViewAdapter attendAdminInformationActivity_adminRecyclerViewAdapter = new AttendActivity_Admin_Detail_Information.AttendAdminInformationActivity_AdminRecyclerViewAdapter();

        activity_attend_detail_recyclerview_main_list.setAdapter(attendAdminInformationActivity_adminRecyclerViewAdapter);
        attendAdminInformationActivity_adminRecyclerViewAdapter.notifyDataSetChanged();

        database.getReference().child("EveryClub").child(clubName).child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    database.getReference().child("EveryClub").child(clubName).child("User").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("name").getValue().equals(userName) && dataSnapshot.child("phone").getValue().equals(userPhone)) {
                                userId = dataSnapshot.getKey();

                                activity_attend_piechart.setUsePercentValues(true);
                                activity_attend_piechart.getDescription().setEnabled(true);
                                activity_attend_piechart.setExtraOffsets(5, 10, 5, 5);

                                activity_attend_piechart.setDragDecelerationFrictionCoef(0.95f);

                                activity_attend_piechart.setDrawHoleEnabled(true);
                                activity_attend_piechart.setHoleColor(Color.WHITE);
                                activity_attend_piechart.setTransparentCircleRadius(61f);

                                final ArrayList<PieEntry> pieEntries = new ArrayList<>();

                                database.getReference().child("EveryClub").child(clubName).child("Attend").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.child("User_State").child(userId).child("attend_state").getValue(String.class).equals("출석")) {
                                                attendCount++;
                                            } else if (snapshot.child("User_State").child(userId).child("attend_state").getValue(String.class).equals("지각")) {
                                                tardyCount++;
                                            } else if (snapshot.child("User_State").child(userId).child("attend_state").getValue(String.class).equals("미출결")) {
                                                unsentCount++;
                                            } else {
                                                absentCount++;
                                            }
                                        }

                                        if (attendCount > 0) {
                                            pieEntries.add(new PieEntry(attendCount, "출석"));
                                        }
                                        if (tardyCount > 0) {
                                            pieEntries.add(new PieEntry(tardyCount, "지각"));
                                        }
                                        if (unsentCount > 0) {
                                            pieEntries.add(new PieEntry(unsentCount, "미출결"));
                                        }
                                        if (absentCount > 0) {
                                            pieEntries.add(new PieEntry(absentCount, "결석"));
                                        }

                                        Description description = new Description();
                                        description.setText("출석률");
                                        description.setTextSize(15);
                                        activity_attend_piechart.setDescription(description);

                                        activity_attend_piechart.animateY(1000, Easing.EasingOption.EaseInOutCubic);

                                        PieDataSet pieDataSet = new PieDataSet(pieEntries, "%");
                                        pieDataSet.setSliceSpace(3f);
                                        pieDataSet.setSelectionShift(4f);
                                        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
//                                        pieDataSet.setColors(new int[]{R.drawable.border_green, R.drawable.border_orange, R.drawable.border_gray});

                                        PieData pieData = new PieData((pieDataSet));
                                        pieData.setValueTextSize(15f);
                                        pieData.setValueTextColor(Color.WHITE);

                                        activity_attend_piechart.setData(pieData);

                                    }

                                    @Override
                                    public void onCancelled(final DatabaseError databaseError) {

                                    }
                                });

                                database.getReference().child("EveryClub").child(clubName).child("Attend").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        attendAdminItems.clear();
                                        uidLists.clear();
                                        listSize = 0;
                                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.child("User_State").child(userId).getKey().equals(auth.getCurrentUser().getUid())) {
                                                startTime = snapshot.child("startTime").getValue().toString();
                                                listStartTime.add(startTime);
                                                Attend_Information_Item attendAdminInformationItem = snapshot.child("User_State").child(userId).getValue(Attend_Information_Item.class);
                                                String uidKey = snapshot.getKey();
                                                attendAdminItems.add(0, attendAdminInformationItem);
                                                uidLists.add(0, uidKey);
                                                listSize++;

                                                for (int i = 0; i < listSize; i++) {
                                                    attendAdminItems.get(i).attendTimeLimit = listStartTime.get(listSize - 1 - i);
                                                }

                                                // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
                                                attendAdminInformationActivity_adminRecyclerViewAdapter.notifyDataSetChanged();
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(final DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(final DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });

    }

    class AttendAdminInformationActivity_AdminRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            LinearLayout activity_attend_information_item_linearlayout;
            TextView activity_attend_information_item_textview_date;
            TextView activity_attend_information_item_textview_attend_state;

            public CustomViewHolder(View view) {
                super(view);

                activity_attend_information_item_linearlayout = (LinearLayout) view.findViewById(R.id.activity_attend_information_item_linearlayout);
                activity_attend_information_item_textview_date = (TextView) view.findViewById(R.id.activity_attend_information_item_textview_date);
                activity_attend_information_item_textview_attend_state = (TextView) view.findViewById(R.id.activity_attend_information_item_textview_attend_state);

            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.activity_attend_information_item, viewGroup, false);

            return new AttendActivity_Admin_Detail_Information.AttendAdminInformationActivity_AdminRecyclerViewAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
            final AttendActivity_Admin_Detail_Information.AttendAdminInformationActivity_AdminRecyclerViewAdapter.CustomViewHolder customViewHolder = ((AttendActivity_Admin_Detail_Information.AttendAdminInformationActivity_AdminRecyclerViewAdapter.CustomViewHolder) viewholder);
            customViewHolder.activity_attend_information_item_textview_date.setGravity(Gravity.LEFT);

            customViewHolder.activity_attend_information_item_textview_attend_state.setText(attendAdminItems.get(position).attend_state);
            customViewHolder.activity_attend_information_item_textview_date.setText(attendAdminItems.get(position).attendTimeLimit);

            customViewHolder.activity_attend_information_item_linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final PopupMenu popup = new PopupMenu(AttendActivity_Admin_Detail_Information.this, v);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {

                                case R.id.attend_state_information_attend:

                                    database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).child("User_State").child(userId).child("attend_state").setValue("출석");
                                    popup.dismiss();

                                    return true;

                                case R.id.attend_state_information_tardy:

                                    database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).child("User_State").child(userId).child("attend_state").setValue("지각");
                                    popup.dismiss();

                                    return true;

                                case R.id.attend_state_information_absent:

                                    database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).child("User_State").child(userId).child("attend_state").setValue("결석");
                                    popup.dismiss();

                                    return true;

                                default:
                                    return false;
                            }
                            //return false;
                        }
                    });

                    popup.inflate(R.menu.attend_state_information_popup);

                    popup.setGravity(Gravity.RIGHT); //오른쪽 끝에 뜨게
                    popup.show();
                }

            });

        }

        @Override
        public int getItemCount() {
            return attendAdminItems.size();
        }

    }
}