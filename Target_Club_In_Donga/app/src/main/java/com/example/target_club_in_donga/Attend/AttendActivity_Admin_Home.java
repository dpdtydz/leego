package com.example.target_club_in_donga.Attend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.target_club_in_donga.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.target_club_in_donga.MainActivity.clubName;

public class AttendActivity_Admin_Home extends AppCompatActivity {
    private RecyclerView activity_attend_admin_information_home_recyclerview_main_list;
//    private List<Attend_Admin_Information_Search_Item> searchList = new ArrayList<>();
    private List<Attend_Admin_Information_Item> userList = new ArrayList<>();
    private List<Attend_Admin_Item> attenditems = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private List<Attend_Admin_Information_Item> listItem = new ArrayList<>();

    private ArrayList<String> listStartTime = new ArrayList<>();
    private List<String> memberList;

    private FirebaseDatabase database;
    private FirebaseAuth auth;

    private Button activity_attend_admin_information_home_category;

    private String startTime, getTardyTimeLimit, nowtardyTimeLimit, getState;
    private int listSize = 0, flag = 0, admin, flag2 = 0, flag3 = 0;
    private int searchFlag = 0, searchFlag2 = 0;

    private TextView activity_attend_admin_information_item_textview_phone_number;
    private EditText attend_admin_information_home_edittext_search;
    private long now;

    public static String uidAdminPath;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_admin_information_home);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        activity_attend_admin_information_home_category = (Button) findViewById(R.id.activity_attend_admin_information_home_category);
        activity_attend_admin_information_item_textview_phone_number = (TextView) findViewById(R.id.activity_attend_admin_information_item_textview_phone_number);

        activity_attend_admin_information_home_recyclerview_main_list = (RecyclerView) findViewById(R.id.activity_attend_admin_information_home_recyclerview_main_list);
        activity_attend_admin_information_home_recyclerview_main_list.setLayoutManager(new LinearLayoutManager(this));

//        final Button attend_admin_information_home_button_search = (Button) findViewById(R.id.attend_admin_information_home_button_search);

        final AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter attendAdminHomeActivity_recyclerViewAdapter = new AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter();

        activity_attend_admin_information_home_recyclerview_main_list.setAdapter(attendAdminHomeActivity_recyclerViewAdapter);
        attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();

        database.getReference().child("EveryClub").child(clubName).child("Attend").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot2 : dataSnapshot.getChildren()) {
                    getTardyTimeLimit = snapshot2.child("tardyTimeLimit").getValue(String.class);
                    if (getTardyTimeLimit != null) {
                        now = System.currentTimeMillis();
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(now);
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        nowtardyTimeLimit = simpleDateFormat.format(date);
                        Date d2 = simpleDateFormat.parse(nowtardyTimeLimit, new ParsePosition(0));
                        Date d1 = simpleDateFormat.parse(getTardyTimeLimit, new ParsePosition(0));
                        long diff = d1.getTime() - d2.getTime();
                        if (diff < 0) {
                            database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("Attend_Certification_Number").removeValue();
                            database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("attendTimeLimit").removeValue();
                            database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("tardyTimeLimit").removeValue();

                            database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("User_State").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        getState = snapshot.child("attend_state").getValue().toString();
                                        if (getState.equals("미출결")) {
                                            database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("User_State").child(snapshot.getKey()).child("attend_state").setValue("결석");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(final DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });

        database.getReference().child("EveryClub").child(clubName).child("User").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                admin = Integer.parseInt(dataSnapshot.child("admin").getValue().toString());
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });

        if (flag == 0) {
            database.getReference().child("EveryClub").child(clubName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("realNameSystem").getValue().equals(false)) {
                        userList.clear();
                        uidLists.clear();
                        memberList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.child("User").getChildren()) {
                            Attend_Admin_Information_Item attendItem = snapshot.getValue(Attend_Admin_Information_Item.class);
                            String uidKey = snapshot.getKey();
                            if (snapshot.child("phone").getValue() == null) {
//                                attendItem.phone = "전화번호 없음";
                                flag2 = 1;
                            }
                            memberList.add(attendItem.name);
                            userList.add(0, attendItem);
                            uidLists.add(0, uidKey);
                        }
                        attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
                    } else {
                        database.getReference().child("AppUser").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                userList.clear();
                                uidLists.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Attend_Admin_Information_Item attendItem = snapshot.getValue(Attend_Admin_Information_Item.class);
                                    String uidKey = snapshot.getKey();
                                    userList.add(0, attendItem);
                                    uidLists.add(0, uidKey);
                                }
                                attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
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

        activity_attend_admin_information_home_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final PopupMenu popup = new PopupMenu(AttendActivity_Admin_Home.this, v);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.attend_admin_information_user:

                                flag = 0;
                                activity_attend_admin_information_home_category.setText("회원 별");
                                attend_admin_information_home_edittext_search.setVisibility(View.VISIBLE);
                                database.getReference().child("EveryClub").child(clubName).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child("realNameSystem").getValue().equals(false)) {
                                            userList.clear();
                                            uidLists.clear();
                                            for (DataSnapshot snapshot : dataSnapshot.child("User").getChildren()) {
                                                Attend_Admin_Information_Item attendItem = snapshot.getValue(Attend_Admin_Information_Item.class);
                                                String uidKey = snapshot.getKey();
                                                if (snapshot.child("phone").getValue() == null) {
//                                                    attendItem.phone = "전화번호 없음";
                                                    flag2 = 1;
                                                }
                                                userList.add(0, attendItem);
                                                uidLists.add(0, uidKey);
                                            }
                                            attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
                                        } else {
                                            database.getReference().child("AppUser").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                                    userList.clear();
                                                    uidLists.clear();
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Attend_Admin_Information_Item attendItem = snapshot.getValue(Attend_Admin_Information_Item.class);
                                                        String uidKey = snapshot.getKey();
                                                        userList.add(0, attendItem);
                                                        uidLists.add(0, uidKey);
                                                    }
                                                    attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
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

                                popup.dismiss();
                                return true;

                            case R.id.attend_admin_information_date:

                                flag = 1;
                                activity_attend_admin_information_home_category.setText("일자 별");
                                attend_admin_information_home_edittext_search.setVisibility(View.INVISIBLE);
                                database.getReference().child("EveryClub").child(clubName).child("Attend").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        attenditems.clear();
                                        uidLists.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Attend_Admin_Item attendItem = snapshot.getValue(Attend_Admin_Item.class);
                                            String uidKey = snapshot.getKey();
                                            attenditems.add(0, attendItem);
                                            uidLists.add(0, uidKey);
                                        }
                                        attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(final DatabaseError databaseError) {

                                    }
                                });

                                popup.dismiss();
                                return true;

                            default:
                                return false;
                        }
                        //return false;
                    }
                });

                popup.inflate(R.menu.attend_admin_home_popup);

                popup.setGravity(Gravity.RIGHT); //오른쪽 끝에 뜨게
                popup.show();

            }
        });
        attend_admin_information_home_edittext_search = (AutoCompleteTextView) findViewById(R.id.attend_admin_information_home_edittext_search);
        attend_admin_information_home_edittext_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                String text = attend_admin_information_home_edittext_search.getText().toString();
                search(text);
            }

            private void search(final String charText) {
                activity_attend_admin_information_home_category.setText("카테고리");

                if (searchFlag == 0) {
                    listItem.clear();
                    listItem.addAll(userList);
                    searchFlag = 1;
//                    flag3 = 0;
                }

                // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
                userList.clear();

                // 문자 입력이 없을때는 모든 데이터를 보여준다.
                if (charText.length() == 0) {
                    userList.addAll(listItem);
//                    flag2 = 1;
                }
                // 문자 입력을 할때..
                else {
                    // 리스트의 모든 데이터를 검색한다.
                    for (int i = 0; i < listItem.size(); i++) {
                        // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                        if (listItem.get(i).name.toLowerCase().contains(charText)) {
                            // 검색된 데이터를 리스트에 추가한다.
                            userList.add(listItem.get(i));
                        }
                    }
//                    flag2 = 0;
                }
                // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
                attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();

/*                attend_admin_information_home_button_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {*/






/*                        database.getReference().child("EveryClub").child(clubName).child("Attend").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                searchList.clear();
                                uidLists.clear();
                                listSize = 0;
                                for (final DataSnapshot snapshot2 : dataSnapshot.getChildren()) {
                                    database.getReference().child("EveryClub").child(clubName).child("Attend").child(snapshot2.getKey()).child("User_State").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(final DataSnapshot dataSnapshot) {
                                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.child("name").getValue(String.class).toLowerCase().contains(charText) && charText.length() != 0) {
                                                    startTime = snapshot2.child("startTime").getValue().toString();
                                                    listStartTime.add(startTime);
                                                    Attend_Admin_Information_Search_Item attendAdminInformationItem = snapshot.getValue(Attend_Admin_Information_Search_Item.class);
                                                    String uidKey = snapshot.getKey();
                                                    searchList.add(0, attendAdminInformationItem);
//                                                    attendAdminItems.get(i).attendTimeLimit = startTime;
                                                    uidLists.add(0, uidKey);
                                                    listSize++;
                                                }
                                            }

                                            for (int i = 0; i < listSize; i++) {
                                                searchList.get(i).attendTimeLimit = listStartTime.get(listSize - 1 - i);
                                            }

                                            // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
                                            attendAdminHomeActivity_recyclerViewAdapter.notifyDataSetChanged();
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
                        });*/






/*                    }
                });*/

            }
        });

        AutoCompleteTextView attend_admin_home_autocompletetextview = (AutoCompleteTextView) findViewById(R.id.attend_admin_information_home_edittext_search);
        attend_admin_home_autocompletetextview.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, memberList));
    }

    // AttendAdminHomeActivity 어댑터

    class AttendAdminHomeActivity_RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            LinearLayout activity_attend_admin_information_item_linearlayout;
            Button activity_attend_admin_information_home_category;
            TextView activity_attend_admin_information_item_textview_name;
            TextView activity_attend_admin_information_item_textview_phone_number;

            public CustomViewHolder(View view) {
                super(view);

                activity_attend_admin_information_item_linearlayout = (LinearLayout) view.findViewById(R.id.activity_attend_admin_information_item_linearlayout);
                activity_attend_admin_information_home_category = (Button) view.findViewById(R.id.activity_attend_admin_information_home_category);
                activity_attend_admin_information_item_textview_name = (TextView) view.findViewById(R.id.activity_attend_admin_information_item_textview_name);
                activity_attend_admin_information_item_textview_phone_number = (TextView) view.findViewById(R.id.activity_attend_admin_information_item_textview_phone_number);

            }

        }

        private class CustomViewHolder2 extends RecyclerView.ViewHolder {

            LinearLayout activity_attend_home_item_linearlayout;
            TextView activity_attend_home_item_textview_recyclerview_start_time;
            TextView activity_attend_home_item_recyclerview_attend_time_limit_tilte;
            TextView activity_attend_home_item_recyclerview_attend_time_limit;
            TextView activity_attend_home_item_textview_recyclerview_tardy_time_limit_title;
            TextView activity_attend_home_item_textview_recyclerview_tardy_time_limit;

            public CustomViewHolder2(View view) {
                super(view);

                activity_attend_home_item_linearlayout = (LinearLayout) view.findViewById(R.id.activity_attend_home_item_linearlayout);
                activity_attend_home_item_textview_recyclerview_start_time = (TextView) view.findViewById(R.id.activity_attend_home_item_textview_recyclerview_start_time);
                activity_attend_home_item_recyclerview_attend_time_limit_tilte = (TextView) view.findViewById(R.id.activity_attend_home_item_recyclerview_attend_time_limit_tilte);
                activity_attend_home_item_recyclerview_attend_time_limit = (TextView) view.findViewById(R.id.activity_attend_home_item_recyclerview_attend_time_limit);
                activity_attend_home_item_textview_recyclerview_tardy_time_limit = (TextView) view.findViewById(R.id.activity_attend_home_item_textview_recyclerview_tardy_time_limit);
                activity_attend_home_item_textview_recyclerview_tardy_time_limit_title = (TextView) view.findViewById(R.id.activity_attend_home_item_textview_recyclerview_tardy_time_limit_title);

            }

        }

/*        private class CustomViewHolder3 extends RecyclerView.ViewHolder {

            LinearLayout activity_attend_admin_information_search_item_linearlayout;
            TextView activity_attend_admin_information_search_item_textview_name;
            TextView activity_attend_admin_information_search_item_textview_attend_state;
            TextView activity_attend_admin_information_search_item_textview_phone_number;
            TextView activity_attend_admin_information_search_item_textview_date;

            public CustomViewHolder3(View view) {
                super(view);

                activity_attend_admin_information_search_item_linearlayout = (LinearLayout) view.findViewById(R.id.activity_attend_admin_information_search_item_linearlayout);
                activity_attend_admin_information_search_item_textview_name = (TextView) view.findViewById(R.id.activity_attend_admin_information_search_item_textview_name);
                activity_attend_admin_information_search_item_textview_attend_state = (TextView) view.findViewById(R.id.activity_attend_admin_information_search_item_textview_attend_state);
                activity_attend_admin_information_search_item_textview_date = (TextView) view.findViewById(R.id.activity_attend_admin_information_search_item_textview_date);
                activity_attend_admin_information_search_item_textview_phone_number = (TextView) view.findViewById(R.id.activity_attend_admin_information_search_item_textview_phone_number);

            }

        }*/

        public void PopupMenu(final AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder2 viewholder, final int position) {
            viewholder.activity_attend_home_item_linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popup = new PopupMenu(view.getContext(), view);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {

                                case R.id.attend_detail:

                                    Intent intent = new Intent(AttendActivity_Admin_Home.this, AttendActivity_Detail_Information.class);
                                    uidAdminPath = database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).getKey();
                                    intent.putExtra("uidAdminPath", uidAdminPath);
                                    intent.putExtra("checkPage", 0);

/*                                    Bundle bundle = new Bundle();
                                    bundle.putString("uidAdminPath", uidAdminPath);
                                    Fragment fragment = new AttendActivity_Fragment();
                                    fragment.setArguments(bundle);*/

                                    startActivity(intent);

                                    return true;

                                case R.id.attend_delete:

                                    AlertDialog.Builder builder = new AlertDialog.Builder(AttendActivity_Admin_Home.this);

                                    View view = LayoutInflater.from(AttendActivity_Admin_Home.this)
                                            .inflate(R.layout.activity_attend_admin_delete, null, false);
                                    builder.setView(view);

                                    final Button confirmButton = (Button) view.findViewById(R.id.activity_attend_admin_delete_item_button_confirm);
                                    final Button cancelButton = (Button) view.findViewById(R.id.activity_attend_admin_delete_item_button_cancel);

                                    final AlertDialog dialog = builder.create();

                                    confirmButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(final View v) {
                                            Toast.makeText(AttendActivity_Admin_Home.this, "출석이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                            delete_content(position);
                                            attenditems.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, attenditems.size());
                                            notifyItemRangeChanged(position, uidLists.size());
                                            dialog.dismiss();
                                        }
                                    });

                                    cancelButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(final View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                    return true;

                                default:
                                    return false;
                            }
                            //return false;
                        }
                    });

                    popup.inflate(R.menu.attend_home_popup);

                    popup.getMenu().getItem(0).setVisible(false);

                    popup.setGravity(Gravity.RIGHT); //오른쪽 끝에 뜨게
                    popup.show();
                }
            });
        }

        @Override
        public int getItemViewType(final int position) {
/*            if (flag == 2) {
                return 2;
            } else */if (flag == 1) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

/*            if (flag == 2) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.activity_attend_admin_information_search_item, viewGroup, false);
                return new AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder3(view);
            } else */if (flag == 1) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.activity_attend_home_item, viewGroup, false);
                return new AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder2(view);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.activity_attend_admin_information_item, viewGroup, false);
                return new AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder(view);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
            switch (viewholder.getItemViewType()) {
                case 0:
                    final AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder customViewHolder = ((AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder) viewholder);
                    customViewHolder.activity_attend_admin_information_item_textview_name.setGravity(Gravity.LEFT);
                    customViewHolder.activity_attend_admin_information_item_textview_phone_number.setGravity(Gravity.LEFT);

                    customViewHolder.activity_attend_admin_information_item_textview_name.setText(userList.get(position).name);
                    customViewHolder.activity_attend_admin_information_item_textview_phone_number.setVisibility(View.INVISIBLE);

                    customViewHolder.activity_attend_admin_information_item_linearlayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            Intent intent = new Intent(AttendActivity_Admin_Home.this, AttendActivity_Admin_Detail_Information.class);
                            intent.putExtra("userName", userList.get(position).name);
                            intent.putExtra("userPhone", userList.get(position).phone);
                            startActivity(intent);
                        }
                    });

                    break;

                case 1:
                    final AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder2 customViewHolder2 = ((AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder2) viewholder);
                    customViewHolder2.activity_attend_home_item_textview_recyclerview_start_time.setGravity(Gravity.LEFT);
                    customViewHolder2.activity_attend_home_item_recyclerview_attend_time_limit.setGravity(Gravity.LEFT);
                    customViewHolder2.activity_attend_home_item_textview_recyclerview_tardy_time_limit.setGravity(Gravity.LEFT);

                    customViewHolder2.activity_attend_home_item_textview_recyclerview_start_time.setText(attenditems.get(position).startTime);
                    customViewHolder2.activity_attend_home_item_recyclerview_attend_time_limit.setText(attenditems.get(position).attendTimeLimit);
                    customViewHolder2.activity_attend_home_item_textview_recyclerview_tardy_time_limit.setText(attenditems.get(position).tardyTimeLimit);

                    PopupMenu(customViewHolder2, position);

                    database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).child("Attend_Certification_Number").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                customViewHolder2.activity_attend_home_item_recyclerview_attend_time_limit.setVisibility(View.GONE);
                                customViewHolder2.activity_attend_home_item_textview_recyclerview_tardy_time_limit.setVisibility(View.GONE);
                                customViewHolder2.activity_attend_home_item_recyclerview_attend_time_limit_tilte.setVisibility(View.GONE);
                                customViewHolder2.activity_attend_home_item_textview_recyclerview_tardy_time_limit_title.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(final DatabaseError databaseError) {

                        }
                    });

                    break;

/*                case 2:
                    final AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder3 customViewHolder3 = ((AttendActivity_Admin_Home.AttendAdminHomeActivity_RecyclerViewAdapter.CustomViewHolder3) viewholder);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_name.setGravity(Gravity.LEFT);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_attend_state.setGravity(Gravity.LEFT);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_phone_number.setGravity(Gravity.LEFT);

                    customViewHolder3.activity_attend_admin_information_search_item_textview_name.setText(searchList.get(position).name);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_attend_state.setText(searchList.get(position).attend_state);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_date.setText(searchList.get(position).attendTimeLimit);
                    customViewHolder3.activity_attend_admin_information_search_item_textview_phone_number.setText(searchList.get(position).phone);

                    break;*/
            }

        }

        private void delete_content(final int position) {

            database.getReference().child("EveryClub").child(clubName).child("Attend").child(uidLists.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    Toast.makeText(AttendActivity_Admin_Home.this, "삭제가 완료되었습니다", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull final Exception e) {

                }
            });

        }

        @Override
        public int getItemCount() {
/*            if (flag == 2) {
                return searchList.size();
            } else */if (flag == 1) {
                return attenditems.size();
            } else {
                return userList.size();
            }
        }

    }
}
