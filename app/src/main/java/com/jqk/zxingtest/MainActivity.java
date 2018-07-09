package com.jqk.zxingtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan = (Button) findViewById(R.id.scan);

        scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                Intent intent = new Intent();
                intent.setClass(this, CaptureActivity.class);
                startActivityForResult(intent, Constants.SCAN_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.SCAN_RESULT_CODE && requestCode == Constants.SCAN_REQUEST_CODE) {
            Toast.makeText(this, data.getCharSequenceExtra("scanResult"), Toast.LENGTH_SHORT).show();
        }
    }
}
