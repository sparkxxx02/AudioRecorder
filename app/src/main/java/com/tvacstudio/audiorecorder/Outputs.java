package com.tvacstudio.audiorecorder;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class Outputs extends AppCompatActivity {

    LineChartView lineChartView;
    /*String[] axisData = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept",
            "Oct", "Nov", "Dec","Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept",
            "Oct", "Nov", "Dec"};
    int[] yAxisData = {50, 20, 15, 30, 20, 60, 15, 40, 45, 10, 90, 18,50, 20, 15, 30, 20, 60, 15, 40, 45, 10, 90, 18};
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output);

        lineChartView = findViewById(R.id.chart);

         if(getIntent().getStringArrayExtra("XAXIS").length ==0)
             Toast.makeText(Outputs.this, "please do some rec", Toast.LENGTH_SHORT).show();
        else
        startProcess(getIntent().getStringArrayExtra("XAXIS"),
                      getIntent().getFloatArrayExtra("YAXIS"));

}
    private void startProcess(String[]  axisData , float[] yAxisData)
    {
        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();
        Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));

        for (int i = 0; i < axisData.length; i++) {
            axisValues.add(i, new AxisValue(i).setLabel(axisData[i]));
        }

        for (int i = 0; i < yAxisData.length; i++) {
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setTextSize(16);
        axis.setTextColor(Color.parseColor("#03A9F4"));
        data.setAxisXBottom(axis);

        Axis yAxis = new Axis();
        yAxis.setName("Sales in millions");
        yAxis.setTextColor(Color.parseColor("#03A9F4"));
        yAxis.setTextSize(16);
        data.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 110;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
        calcavg(yAxisData);
    }
    void calcavg(float[] str)
    {
        String predict="LOUD";
        float sum=0,avg;
        for(int i=0;i<str.length;i++)
        {
            sum+=str[i];
        }
        avg= sum/str.length;
        TextView avg_text= findViewById(R.id.AVG);
        avg_text.setText("Average :-"+String.valueOf(avg)+" dB");
        if(avg <=60)
            predict="SOFT";
        avg_text.append("\n Noise level="+predict);


    }

}
