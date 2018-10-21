package org.blazers.spotthatfire;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnReportFire, btnSafetyTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnReportFire = findViewById(R.id.btn_report_fire);
        btnSafetyTips = findViewById(R.id.btn_safety_tips);


        btnReportFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reportIntent = new Intent(MainActivity.this,ReportFireActivity.class);
                MainActivity.this.startActivity(reportIntent);
            }
        });

        btnSafetyTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to safety tips activity
            }
        });


    }

    private void init() {
        btnReportFire = (Button) findViewById(R.id.btn_report_fire);
        btnSafetyTips = (Button) findViewById(R.id.btn_safety_tips);
    }
}
