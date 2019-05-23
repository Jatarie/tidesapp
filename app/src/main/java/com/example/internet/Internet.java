package com.example.internet;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Internet extends AsyncTask<String, Integer, Integer> {

    private TextView textView;
    private Map<String, Integer> answer;

    Internet(TextView aTextView) {
        textView = aTextView;

    }

    @Override
    protected Integer doInBackground(String... urls) {
        int answer = 0;
        try {
            this.answer = okayMain();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        textView.setText("30");

        return 0;

    }



    @Override
    protected void onPostExecute(Integer result) {
        String initialHeight = "";
        int time = 0;
        for (String s : this.answer.keySet()) {
            initialHeight = s;
            time = answer.get(s);

        }

        if (initialHeight.equals("High")) {
            textView.setTextColor(0xFFc93636);
        }
        if (initialHeight.equals("Low")) {
            textView.setTextColor(0xFF219117);
        }


        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {

                int hours = (int) millisUntilFinished / (1000 * 3600);
                float minutes = ((float) millisUntilFinished / (1000 * 3600) - hours) * 60;
                float seconds = (minutes - (int) minutes) * 60;
                int m = (int) minutes;
                int s = (int) seconds;

                textView.setText(String.format("%02d:%02d:%02d", hours, m, s));
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                new Internet(textView).execute();
            }

        }.start();
    }

    public static Map<String, Integer> okayMain() throws Exception {
        String content = GetHTTP();
        ArrayList<String> days;
        days = Regex(content);

        Map<String, Integer> returnMap = new HashMap<>();

        ArrayList<Long> dateList = new ArrayList<>();
        System.out.println(new Date());


        String initialHeight = "";
        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            day = day.substring(7);

            Pattern pattern = Pattern.compile("\\w+");
            Matcher matcher = pattern.matcher(day);
            if (matcher.find()) {
                day = matcher.group(0);

            }

            pattern = Pattern.compile("[HigLow]{3,4}.+?</tr.", Pattern.DOTALL);
            matcher = pattern.matcher(days.get(i));


            ArrayList<String> data = new ArrayList<>();
            while (matcher.find()) {
                data.add(matcher.group());
            }

            for (String d : data) {

                if (initialHeight == "") {
                    pattern = Pattern.compile("\\w+");
                    matcher = pattern.matcher(d);
                    if (matcher.find()) {
                        initialHeight = matcher.group(0);
                        System.out.println(initialHeight);
                    }
                }

                pattern = Pattern.compile("\\d{4}");
                matcher = pattern.matcher(d);
                while (matcher.find()) {

                    String hour = matcher.group().substring(0, 2);
                    String minute = matcher.group().substring(2, 4);

                    Calendar cal = Calendar.getInstance();
                    int month = cal.get(Calendar.MONTH) + 1;
                    int year = cal.get(Calendar.YEAR);
                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    Date date = new SimpleDateFormat("yyyy-M-dd HH:mm").parse(String.format("%d-%d-%d %s:%s", year, month, dayOfMonth + i, hour, minute));
                    dateList.add(date.getTime());
                }
            }
        }

        ArrayList<Long> averageDateList = new ArrayList<>();
        for (int i = 0; i < dateList.size() - 1; i++) {
            long d1 = dateList.get(i);
            long d2 = dateList.get(i + 1);
            long d3 = (d1 + d2) / 2;
            averageDateList.add(d3);

        }

        ArrayList<String> highLow = new ArrayList<String>();
        highLow.add(initialHeight);
        if (initialHeight.equals("High")){
            highLow.add("Low");
        }
        else{
            highLow.add("High");
        }

        for (int i = 0; i < averageDateList.size(); i++) {
            long d = averageDateList.get(i);
            float timeDelta = d - new Date().getTime();
            if (timeDelta >= 0) {
                long before = d - averageDateList.get(i - 1);
                long after = averageDateList.get(i + 1) - d;

                String rt = "";
                if (before < after){
                    rt = highLow.get(0);
                }
                else{
                    rt = highLow.get(1);
                }

                System.out.println(timeDelta);
                returnMap.put(rt, (int) timeDelta);
                return returnMap;

            }
        }
        return returnMap;
    }


    private static ArrayList<String> Regex(String content) {
        Pattern pattern = Pattern.compile("<div class=\"ui-tabs-panel open\" id=\"tide-details0\">.+<div class=\"tide-graph\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            content = matcher.group(0);
        }

        pattern = Pattern.compile("\"date\".+?</tbody>");
        matcher = pattern.matcher(content);

        ArrayList<String> days = new ArrayList<String>();
        while (matcher.find()) {
            days.add(matcher.group());
        }
        return days;


    }

    private static String GetHTTP() throws IOException {

        URL url = new URL("https://www.bbc.co.uk/weather/coast_and_sea/tide_tables/6/637#tide-details");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        System.out.println("1");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        System.out.println("1");
        String inputLine;
        System.out.println("1");
        StringBuilder content = new StringBuilder();
        System.out.println("1");
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();

    }

}
