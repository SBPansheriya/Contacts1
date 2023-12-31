package com.contacts.Fragment;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.contacts.Adapter.HeaderListAdapter;
import com.contacts.Activity.CreateContactActivity;
import com.contacts.Model.Header;
import com.contacts.Model.Users;
import com.contacts.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContactsFragment extends Fragment {

    HeaderListAdapter headerListAdapter;
    RecyclerView recyclerView;
    LinearLayout no_contcat_found_linear, Contact_found_linear;
    ImageView edit, cancel, share, delete;
    Button create_btn;
    TextView selectall, totalcontact;
    FloatingActionButton floatingActionButton;
    ViewGroup viewGroup;
    Context context;
    ArrayList<Users> usersArrayList = new ArrayList<>();
    ArrayList<Object> items = new ArrayList<>();
    int position;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        init(view);

        checkPermission();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateContactActivity.class);
                startActivity(intent);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.GONE);
                selectall.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                share.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateContactActivity.class);
                startActivity(intent);
            }
        });

        if (usersArrayList.isEmpty()) {
            no_contcat_found_linear.setVisibility(View.VISIBLE);
            Contact_found_linear.setVisibility(View.INVISIBLE);
        }


        if (usersArrayList.isEmpty()) {
            totalcontact.setText("0 Conatcts");
        } else {
            totalcontact.setText(usersArrayList.get(position).contactId + " " + "Contacts");
        }


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(ContactsFragment.this.getContext());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dailog_layout);
                dialog.setCancelable(false);
                dialog.show();

                Button cancel = dialog.findViewById(R.id.canceldialog);
                Button movetobin = dialog.findViewById(R.id.movetobin);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                movetobin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        usersArrayList.remove(usersArrayList.get(i).contactId);
                    }
                });
            }
        });
        return view;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            getContactList();
        }
    }

    private void getContactList() {

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String phoneName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                String firstName = "";
                String lastName = "";
                if (!TextUtils.isEmpty(phoneName)) {
                    if (phoneName.contains(" ")) {
                        String currentString = phoneName;
                        String[] separated = currentString.split(" ");
                        firstName = separated[0];
                        lastName = separated[1];
                    } else {
                        firstName = phoneName;
                        lastName = "";
                    }
                }


                // Get phone numbers
                List<String> phoneNumbers = getPhoneNumbers(contentResolver, contactId);
                String phoneNumber = "";
                String officeNumber = "";
                if (phoneNumbers.size() > 0) {
                    if (phoneNumbers.size() > 2) {
                        phoneNumber = phoneNumbers.get(0);
                        officeNumber = phoneNumbers.get(1);
                    } else {
                        phoneNumber = phoneNumbers.get(0);
                        officeNumber = "";
                    }
                }

                // Create a User object with the retrieved data and add it to the ArrayList
                Users user = new Users(contactId, photoUri, firstName, lastName, phoneNumber, officeNumber);
                usersArrayList.add(user);
            }
            cursor.close();
        }


        if (usersArrayList.size() > 0) {
            Comparator<Users> nameComparator = new Comparator<Users>() {
                @Override
                public int compare(Users user1, Users user2) {
                    // Use compareTo() to compare the names in alphabetical order
                    return user1.getFirst().compareTo(user2.getFirst());
                }
            };

            Collections.sort(usersArrayList, nameComparator);
        }

        if (usersArrayList.size() > 0) {
            ArrayList<Header> headerArrayList = new ArrayList<>();

            for (char i = 'A'; i <= 'Z'; i++) {
                Header header = new Header(String.valueOf(i), new ArrayList<>());
                headerArrayList.add(header);
            }
            Header header1 = new Header("#", new ArrayList<>());
            headerArrayList.add(header1);
            for (int i = 0; i < usersArrayList.size(); i++) {
                if (!TextUtils.isEmpty(usersArrayList.get(i).first)) {
                    boolean isMatch = false;
                    for (int i1 = 0; i1 < headerArrayList.size(); i1++) {
                        String header = headerArrayList.get(i1).header;
                        String firstLetter = String.valueOf(usersArrayList.get(i).first.toUpperCase().charAt(0));

                        if (Objects.equals(header, firstLetter)) {
                            isMatch = true;
                            headerArrayList.get(i1).usersList.add(usersArrayList.get(i));
                            break;
                        }

                    }

                    if (!isMatch) {
                        headerArrayList.get(headerArrayList.size() - 1).usersList.add(usersArrayList.get(i));
                    }
                }
            }

            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            headerListAdapter = new HeaderListAdapter(ContactsFragment.this, headerArrayList);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(headerListAdapter);
        }
    }

    private List<String> getPhoneNumbers(ContentResolver contentResolver, String contactId) {
        List<String> phoneNumbers = new ArrayList<>();

        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] phoneProjection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        Cursor phoneCursor = contentResolver.query(phoneUri, phoneProjection, phoneSelection, new String[]{contactId}, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumbers.add(phoneNumber);
            }
            phoneCursor.close();
        }

        return phoneNumbers;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContactList();
        } else {
            Toast.makeText(context, "Permission Denied.", Toast.LENGTH_SHORT).show();
            checkPermission();
        }
    }

//    private void getHeaderListLatter(ArrayList<Header> usersList) {
//
//        Collections.sort(usersList, new Comparator<Header>() {
//            int position = 0;
//
//            @Override
//            public int compare (Header user1, Header user2) {
//                return String.valueOf(user1.header.charAt(0)).toUpperCase().compareTo(String.valueOf(user2.getUsersList().get(position).getFirst().charAt(0)).toUpperCase());
//            }
//        });
//
//        String lastHeader = "";
//
//        int size = usersList.size();
//
//        for (int i = 0; i < size; i++) {
//
//            Header header1 = usersList.get(i);
//            String header = String.valueOf(header1.header.charAt(0)).toUpperCase();
//
//            if (!TextUtils.equals(lastHeader, header)) {
//                lastHeader = header;
//                mSectionList.add(new Header(header,true,usersArrayList));
//            }
//
//            mSectionList.add(header1);
//        }
//    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.show_contact_recyclerview);
        floatingActionButton = view.findViewById(R.id.add_contact);
        edit = view.findViewById(R.id.edit);
        cancel = view.findViewById(R.id.cancel);
        share = view.findViewById(R.id.share);
        delete = view.findViewById(R.id.trash);
        selectall = view.findViewById(R.id.selectall);
        create_btn = view.findViewById(R.id.create_contact);
        viewGroup = view.findViewById(android.R.id.content);
        no_contcat_found_linear = view.findViewById(R.id.no_contcat_found_linear);
        Contact_found_linear = view.findViewById(R.id.Contact_found_linear);
        totalcontact = view.findViewById(R.id.totalcontact);
    }
}