package org.mlayer.batterydrain;

import android.annotation.SuppressLint;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import timber.log.Timber;

public class ActMain extends AppCompatActivity implements View.OnClickListener {

    private Button btnStop;
    private Button btnStart;

    private RequestRunner requestRunner;
    private EditText edtPeriod;
    private EditText edtReqCount;
    private EditText edtMaxReqCount;

    private TextView tvCurrentIteration;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        requestRunner = App.getInstance().getRequestRunner();
        storage = App.getInstance().getStorage();

        btnStart = findViewById(R.id.act_main__btn_start);
        btnStop = findViewById(R.id.act_main__btn_stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        updateButtonsState();

        edtPeriod = findViewById(R.id.act_main__edt_period);
        edtReqCount = findViewById(R.id.act_main__edt_req_count);
        edtMaxReqCount = findViewById(R.id.act_main__edt_max_req_count);

        tvCurrentIteration = findViewById(R.id.act_main_tv_current_iteration);
    }

    private void updateButtonsState() {
        if (App.getInstance().getRequestRunner().isScheduled()) {
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        } else {
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        updateButtonsState();
        tvCurrentIteration.setText("" + storage.getCurrentNum());
        edtPeriod.setText("" + storage.getPeriod());
        edtReqCount.setText("" + storage.getRequestsCount());
        edtMaxReqCount.setText("" + storage.getMaxRequestsCount());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startTest() {
        Timber.d("startTest");
        if (!Utils.hasNetworkConnection(getApplicationContext())) {
            Utils.showShort(getApplicationContext(), "No network connection");
        } else {
            requestRunner.start(
                    Integer.valueOf(edtPeriod.getText().toString()),
                    Integer.valueOf(edtReqCount.getText().toString()),
                    Integer.valueOf(edtMaxReqCount.getText().toString())
            );
            updateButtonsState();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.act_main__btn_start) {
            startTest();
        } else if (view.getId() == R.id.act_main__btn_stop) {
            stopTest();
        }
    }

    private void stopTest() {
        Timber.d("stopTest");
        requestRunner.stop();
        updateButtonsState();
    }
}
