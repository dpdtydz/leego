package com.example.target_club_in_donga.Attend;


import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.target_club_in_donga.MainActivity.clubName;

public class AttendActivity_Home extends AppCompatActivity {

    RecyclerView activity_attend_home_admin_recyclerview_main_list;
    List<Attend_Item> attenditems = new ArrayList<>();
    List<String> uidLists = new ArrayList<>();

    Button activity_attend_home_admin_button_insert;

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private int admin;
    public static String uidAdminPath;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_home);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        activity_attend_home_admin_recyclerview_main_list = (RecyclerView) findViewById(R.id.activity_attend_home_admin_recyclerview_main_list);
        activity_attend_home_admin_recyclerview_main_list.setLayoutManager(new LinearLayoutManager(this));

        final AttendActivity_AdminRecyclerViewAdapter attendActivity_adminRecyclerViewAdapter = new AttendActivity_AdminRecyclerViewAdapter();

        activity_attend_home_admin_recyclerview_main_list.setAdapter(attendActivity_adminRecyclerViewAdapter);
        attendActivity_adminRecyclerViewAdapter.notifyDataSetChanged();

        database.getReference().child(clubName).child("Attend_Admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                attenditems.clear();
                uidLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Attend_Item attendItem = snapshot.getValue(Attend_Item.class);
                    String uidKey = snapshot.getKey();
                    attenditems.add(attendItem);
                    uidLists.add(uidKey);
                }
                attendActivity_adminRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });

        activity_attend_home_admin_button_insert = (Button) findViewById(R.id.activity_attend_home_admin_button_insert);
        activity_attend_home_admin_button_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendActivity_Home.this, AttendActivity_Admin.class);
                startActivity(intent);
            }
        });

        database.getReference().child(clubName).child("User").child(auth.getCurrentUser().getUid()).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                admin = Integer.parseInt(dataSnapshot.getValue().toString());

                if (admin > 0) {
                    activity_attend_home_admin_button_insert.setVisibility(View.VISIBLE);
                } else {
                    activity_attend_home_admin_button_insert.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = storage.getReferenceFromUrl("gs://target-club-in-donga.appspot.com");

        Uri file = Uri.fromFile(new File(getPath(data.getData())));
        StorageReference riversRef = storageRef.child(clubName).child("Attend/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUri = taskSnapshot.getDownloadUrl();

            }
        });
    }

    public String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);

    }

//        <------------------------------------------------------------------------------------------------------------------------------------------>
//        <------------------------------------------------------------------------------------------------------------------------------------------>
//        <------------------------------------------------------------------------------------------------------------------------------------------>
//        <------------------------------------------------------------------------------------------------------------------------------------------>
//        <------------------------------------------------------------------------------------------------------------------------------------------>
//        <------------------------------------------------------------------------------------------------------------------------------------------>


    // AttendActivity 어댑터

    class AttendActivity_AdminRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            LinearLayout activity_attend_home_admin_item_linearlayout;
            TextView activity_attend_home_admin_item_textview_recyclerview_club_name;
            TextView activity_attend_home_admin_item_textview_recyclerview_start_time;
            TextView activity_attend_home_admin_item_recyclerview_attend_time_limit;
            TextView activity_attend_home_admin_item_textview_recyclerview_tardy_time_limit;
            TextView activity_attend_home_admin_item_textview_recyclerview_attend_statue;

            public CustomViewHolder(View view) {
                super(view);

                activity_attend_home_admin_item_linearlayout = (LinearLayout) view.findViewById(R.id.activity_attend_home_admin_item_linearlayout);
                activity_attend_home_admin_item_textview_recyclerview_club_name = (TextView) view.findViewById(R.id.activity_attend_home_admin_item_textview_recyclerview_club_name);
                activity_attend_home_admin_item_textview_recyclerview_attend_statue = (TextView) view.findViewById(R.id.activity_attend_home_admin_item_textview_recyclerview_attend_statue);
                activity_attend_home_admin_item_textview_recyclerview_start_time = (TextView) view.findViewById(R.id.activity_attend_home_admin_item_textview_recyclerview_start_time);
                activity_attend_home_admin_item_recyclerview_attend_time_limit = (TextView) view.findViewById(R.id.activity_attend_home_admin_item_recyclerview_attend_time_limit);
                activity_attend_home_admin_item_textview_recyclerview_tardy_time_limit = (TextView) view.findViewById(R.id.activity_attend_home_admin_item_textview_recyclerview_tardy_time_limit);

            }

        }

        public void PopupMenu(final AttendActivity_Home.AttendActivity_AdminRecyclerViewAdapter.CustomViewHolder viewholder, final int position) {
            viewholder.activity_attend_home_admin_item_linearlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final PopupMenu popup = new PopupMenu(view.getContext(), view);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {

                                case R.id.attend_detail:

                                    Intent intent = new Intent(AttendActivity_Home.this, AttendActivity.class);
                                    uidAdminPath = database.getReference().child(clubName).child("Attend_Admin").child(uidLists.get(position)).getKey();

                                    Bundle bundle = new Bundle();
                                    bundle.putString("uidAdminPath", uidAdminPath);
                                    Fragment fragment = new AttendActivity_Fragment();
                                    fragment.setArguments(bundle);

                                    startActivity(intent);

                                    return true;

                                default:
                                    return false;
                            }
                            //return false;
                        }
                    });

                    popup.inflate(R.menu.attend_home_main_popup);

                    popup.setGravity(Gravity.RIGHT); //오른쪽 끝에 뜨게
                    popup.show();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.activity_attend_home_item, viewGroup, false);

            return new AttendActivity_Home.AttendActivity_AdminRecyclerViewAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
            AttendActivity_Home.AttendActivity_AdminRecyclerViewAdapter.CustomViewHolder customViewHolder = ((AttendActivity_Home.AttendActivity_AdminRecyclerViewAdapter.CustomViewHolder) viewholder);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_club_name.setGravity(Gravity.LEFT);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_attend_statue.setGravity(Gravity.LEFT);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_start_time.setGravity(Gravity.LEFT);
            customViewHolder.activity_attend_home_admin_item_recyclerview_attend_time_limit.setGravity(Gravity.LEFT);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_tardy_time_limit.setGravity(Gravity.LEFT);

            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_club_name.setText(attenditems.get(position).clubName);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_attend_statue.setText(attenditems.get(position).myAttendState);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_start_time.setText(attenditems.get(position).startTime);
            customViewHolder.activity_attend_home_admin_item_recyclerview_attend_time_limit.setText(attenditems.get(position).attendTimeLimit);
            customViewHolder.activity_attend_home_admin_item_textview_recyclerview_tardy_time_limit.setText(attenditems.get(position).tardyTimeLimit);

            PopupMenu(customViewHolder, position);

        }


        @Override
        public int getItemCount() {
            return attenditems.size();
        }

    }

}