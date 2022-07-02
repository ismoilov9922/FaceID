package uz.faceid.faceidcompany.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import uz.faceid.faceidcompany.common.TransitionsLibrary;
import uz.faceid.faceidcompany.libs.benchmark.BenchmarkLayout;
import uz.faceid.faceidcompany.libs.benchmark.LayoutClassInterface;


public class BenchmarkModeActivity extends AppCompatActivity {
    private LayoutClassInterface benchmarkLayout;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        benchmarkLayout = new BenchmarkLayout(this);
        benchmarkLayout.makeActive();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        TransitionsLibrary.executeToRightTransition(this);
    }
}