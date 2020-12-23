package com.tvacstudio.audiorecorder;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    static ArrayList<Float> yAxisValues = new ArrayList<Float>();
    static ArrayList axisValues = new ArrayList();

    private ImageButton listBtn;
    static final private double EMA_FILTER = 0.6;

    private ImageButton recordBtn;
    private Button btn;
    private TextView filenameText;
    private int str=0;

    private boolean isRecording = false;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    Thread runner;
    final Handler mHandler = new Handler();

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;
    private static double mEMA = 0.0;

    final Runnable updater = new Runnable() {

        public void run() {
           // Log.d(LOG,"Run()");

                updateTv();

        }

        ;
    };



    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intitialize Variables
        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.record_list_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        filenameText = view.findViewById(R.id.record_filename);
        btn= view.findViewById(R.id.button_output);

        /* Setting up on click listener
           - Class must implement 'View.OnClickListener' and override 'onClick' method
         */
        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Outputs.class);
                /*Log.d("Record Intent", "Xvalues = "+axisValues.get(2));
                Log.d("Record Intent", "Yvalues = "+yAxisValues.get(2));*/
                String xaxis[]= (String[]) axisValues.toArray(new String[axisValues.size()]);
                float[] yaxis = new float[yAxisValues.size()];
                int index = 0;
                for (Float value : yAxisValues) {
                    yaxis[index++] = value;
                }
                intent.putExtra("XAXIS", xaxis);
                intent.putExtra("YAXIS",yaxis);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onClick(View v) {
        /*  Check, which button is pressed and do the task accordingly
        */
        switch (v.getId()) {
            case R.id.record_list_btn:
                /*
                Navigation Controller
                Part of Android Jetpack, used for navigation between both fragments
                 */
                if(isRecording){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
                    alertDialog.create().show();
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.record_btn:
                if(isRecording) {
                    //Stop Recording


                        stopRecording();

                        // Change button image and set Recording state to false
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                        isRecording = false;
                        filenameText.setText("Recording Stopped, File Saved : " + recordFile);



                } else {
                    //Check permission to record audio
                    if(checkPermissions()) {
                        //Start Recording
                        startRecording();


                        // Change button image and set Recording state to false
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                        isRecording = true;
                    }
                }

                break;
        }
    }

    private void stopRecording() {
        //Stop Timer, very obvious
        timer.stop();



        //Change text on page to file saved

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        str=0;

    }

    private void startRecording() {
        yAxisValues.clear();
        axisValues.clear();
        //Start timer from 0
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();


        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".3gp";

        //filenameText.setText("Recording, File Name : " + recordFile);

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
        //start calculating
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(1000);
                            //Log.i(LOG, "Tock");
                        } catch (InterruptedException e) {
                        }
                        ;
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            //Log.d(LOG ,"start runner()");
        }

    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
    }
    public double convertdDb(double amplitude) {
        /*
         Cellphones can catch up to 90 db + -
         getMaxAmplitude returns a value between 0-32767 (in most phones). that means that if the maximum db is 90, the pressure
         at the microphone is 0.6325 Pascal.
         it does a comparison with the previous value of getMaxAmplitude.
         we need to divide maxAmplitude with (32767/0.6325)
        51805.5336 or if 100db so 46676.6381
        */
        double EMA_FILTER = 0.6;
        SharedPreferences sp = this.getActivity().getSharedPreferences("device-base", MODE_PRIVATE);
        double amp = (double) sp.getFloat("amplitude", 0);
        double mEMAValue = EMA_FILTER * amplitude + (1.0 - EMA_FILTER) * mEMA;
        Log.d("db", Double.toString(amp));
        //Assuming that the minimum reference pressure is 0.000085 Pascal (on most phones) is equal to 0 db
        // samsung S9 0.000028251
        return 20 * (float) Math.log10((mEMAValue/51805.5336) /0.000028251);
    }


    public double getAmplitude() {
        if (mediaRecorder != null)
            return (mediaRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    private void updateTv() {

            if(mediaRecorder !=null)
            {
                double amplitude = mediaRecorder.getMaxAmplitude();


            //filenameText.setText(Double.toString((getAmplitudeEMA())) + " dB");
            if (amplitude > 0 && amplitude < 1000000) {
                float dbl = (float) convertdDb(amplitude);
                axisValues.add(String.valueOf(str));
                Log.d("Record", "Xvalues = "+axisValues.get(str));
                yAxisValues.add(dbl);
                Log.d("Record", "Yvalues = "+yAxisValues.get(str));

                filenameText.setText("Cuurent value:-"+Double.toString(dbl) + "dB");
                str++;
            }
            }

    }
}
