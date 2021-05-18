package com.example.contactutils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class CustomDialog extends AppCompatActivity {

    TextView tv_client;
    String phone_no;
    Button dialog_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this, R.style.MyAlertDialogTheme);

            builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

            //Setting message manually and performing action on button click
            builder.setMessage(getIntent().getExtras().getString("phone_no") +
                    " " +
                    getIntent().getExtras().getString("name"))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                            Toast.makeText(getApplicationContext(), "you choose Ok action for alertbox",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            alert.setTitle("Contact Alert Dialog");
            alert.show();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            e.printStackTrace();
        }
    }
}
