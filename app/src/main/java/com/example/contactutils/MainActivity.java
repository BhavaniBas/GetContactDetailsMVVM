package com.example.contactutils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactutils.databinding.ActivityMainBinding;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private ActionMode actionMode;
    private ContactViewModel contactViewModel;
    private ContactDataAdapter contactDataAdapter;
    private List<Contact> contactArray;
    private RecyclerView recyclerView;
    public static final int REQUEST_READ_CONTACTS = 79;
    private ActionCallback actionCallback;
    private Hashtable<String, Hashtable<String, Object>> contactDetailsHash = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.
                setContentView(this, R.layout.activity_main);

        setSupportActionBar(activityMainBinding.toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.contact_details));
        }

        // bind RecyclerView
        setAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            contactArray = getAllContacts(MainActivity.this);
        } else {
            requestPhone();
            requestPermission();
        }

        if (contactArray != null && !contactArray.isEmpty()) {
            activityMainBinding.progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            contactViewModel.getMutableLiveContactData(contactArray);
        }

        // Delay for Recyclerview Items Its not a mandatory ..
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                contactViewModel.mutableLiveData.observe(MainActivity.this, new Observer<List<Contact>>() {
                    @Override
                    public void onChanged(@Nullable List<Contact> contactItems) {
                        activityMainBinding.progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        if (contactItems != null && !contactItems.isEmpty())
                            contactDataAdapter.setContactList((ArrayList<Contact>) contactItems);
                    }
                });
            }
        }, 0);

        contactDataAdapter.setItemClick(new ContactDataAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, Contact inbox, int position) {
                if (contactDataAdapter.selectedItemCount() > 0) {
                    toggleActionBar(position);
                } else {
                    Toast.makeText(MainActivity.this, "clicked " + inbox.getName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongPress(View view, Contact inbox, int position) {

                toggleActionBar(position);

            }
        });

    }

    private void requestPhone() {
        Permissions.check(this, Manifest.permission.CALL_PHONE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Toast.makeText(MainActivity.this, "Phone granted.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleActionBar(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        contactDataAdapter.toggleSelection(position);
        int count = contactDataAdapter.selectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private void setAdapter() {
        actionCallback = new ActionCallback();
        recyclerView = activityMainBinding.contactList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        activityMainBinding.progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        contactViewModel = ViewModelProviders.of(MainActivity.this).get(ContactViewModel.class);
        contactDataAdapter = new ContactDataAdapter();
        recyclerView.setAdapter(contactDataAdapter);
    }


    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactArray = getAllContacts(MainActivity.this);
            }  // permission denied,Disable the
            // functionality that depends on this permission.

        }
    }

    public List<Contact> getAllContacts(Context ctx) {

        List<Contact> list = new ArrayList<>();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                    Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                    Bitmap photo = null;
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }
                    while (cursorInfo.moveToNext()) {
                        Contact info = new Contact();
                        info.setId(id);
                        info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        info.setMobileNumber(cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        info.setNameSubString(info.getName().substring(0, 1));
                        info.setProfileBitmap(photo);
                        info.setProfileUri(pURI);
                        list.add(info);
                    }
                    // duplicates can be removed ...
                    HashSet<Contact> hashSet = new HashSet<>(list);
                    list.clear();
                    list.addAll(hashSet);

                    // Sorting the list
                    Collections.sort(list, new Comparator<Contact>() {

                        @Override
                        public int compare(Contact lhs, Contact rhs) {
                            //here getName() method return contact name...
                            return lhs.getName().compareTo(rhs.getName());

                        }
                    });
                    cursorInfo.close();
                }
            }
            cursor.close();
        }
        return list;
    }

    private class ActionCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Util.toggleStatusBarColor(MainActivity.this, R.color.blue_grey_700);
            mode.getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.saveItem) {
                saveSelectedContactItems();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            contactDataAdapter.clearSelection();
            actionMode = null;
            Util.toggleStatusBarColor(MainActivity.this, R.color.colorPrimary);

        }
    }

    private void saveSelectedContactItems() {
        List<Integer> selectedItemPositions = contactDataAdapter.getSelectedItems();
        for (int i = 0; i < selectedItemPositions.size(); i++) {
            if (contactArray != null && contactArray.size() > 0) {
                Contact contact = contactArray.get(selectedItemPositions.get(i));
                parseAndSaveSelectedContact(contact, selectedItemPositions.get(i));
            }
        }
        contactDataAdapter.notifyDataSetChanged();
    }

    private void parseAndSaveSelectedContact(Contact contact, Integer integer) {
        Hashtable<String, Object> dataHash = new Hashtable<>();
        dataHash.put(getString(R.string.Contact), contact);
        dataHash.put(getString(R.string.Save_Contact), integer);
        contactDetailsHash.put(contact.getMobileNumber(), dataHash);
        String name = getSeparateName(contact);
        SharedPref.putString(this, contact.getMobileNumber() + getString(R.string.contact_name), name);
        SharedPref.putSelectedSaveContact(this, contact.getMobileNumber(), contactDetailsHash);
    }

    private String getSeparateName(Contact contact) {
        return contact.getName();
    }
}
