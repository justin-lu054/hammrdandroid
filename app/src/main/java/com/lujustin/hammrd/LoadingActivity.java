package com.lujustin.hammrd;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingActivity {
    private Activity activity;
    private AlertDialog dialog;

    public LoadingActivity(Activity activity) {
        this.activity = activity;
    }

    public void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_loading_dialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();

    }

    public void dismissDialog() {
        dialog.dismiss();
    }

}
