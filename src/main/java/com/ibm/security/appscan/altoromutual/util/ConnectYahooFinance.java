package com.ibm.security.appscan.altoromutual.util;

import com.ibm.security.appscan.altoromutual.model.Account;
import com.ibm.security.appscan.altoromutual.model.Holding;
import com.ibm.security.appscan.altoromutual.model.StockData;
import org.json.*;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ConnectYahooFinance{

    public static final String YAHOO_FINANCE_URL_START = "https://query1.finance.yahoo.com/v7/finance/download/";
    public static final String YAHOO_FINANCE_URL_END = "&interval=1d&events=history&includeAdjustedClose=true";

    public static final String YAHOO_FINANCE_SPIDER_URL = "https://yfapi.net/v6/finance/quote?symbols=";
    // signup up https://www.yahoofinanceapi.com and then get a api key
    public static final String api_key = "1CSow6sSst2l6pE9WicGLaZFGEsyKrQE6Lb9j8ko";
    // Amber's api-key: Tq9rY48hUn8yBMNoZyxfZ3OyIiNJx44l9PKqKCMN


    /**
     * Get previous 5-year data
     * @param
     * @return
     */
    public static long GenerateEndTimestamp(){
        //get timestamp
        Date current_date = new Date();
        //long end_date_timestamp = System.currentTimeMillis()/1000;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //calendar.add(Calendar.DAY_OF_MONTH,1);
        String end_date_format = sdf.format(current_date);
        long end_date_timestamp = (long) (Timestamp.valueOf(end_date_format).getTime())/1000;
        return end_date_timestamp;
    }

    public static long GenerateStartTimestamp(){
        Date current_date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar.add(Calendar.YEAR,-5);
        //calendar.add(Calendar.DAY_OF_MONTH,-1);
        Date start_date = calendar.getTime();
        String start_date_format = sdf.format(start_date);
        long start_date_timestamp = (long) (Timestamp.valueOf(start_date_format).getTime())/1000;
        return start_date_timestamp;
    }

    public static long StringtoTimestamp(String Datestr){
        //get timestamp
        String Datestr1 = Datestr + " 00:00:00";
        long timestamp = (long) (Timestamp.valueOf(Datestr1).getTime())/1000;
        return timestamp;
    }

    public static ArrayList<StockData> getHistoryData(String symbol, String startdate, String enddate){
        ArrayList<StockData> history_data = new ArrayList<StockData>();
        long start_date_timestamp = 0;
        long end_date_timestamp = 0;

        if(startdate == null && enddate == null){
            start_date_timestamp = GenerateStartTimestamp();
            end_date_timestamp = GenerateEndTimestamp();
        }
        else if(startdate == null && enddate != null){
            start_date_timestamp = GenerateStartTimestamp();
            end_date_timestamp = StringtoTimestamp(enddate);
        }
        else if(startdate != null && enddate == null){
            start_date_timestamp = StringtoTimestamp(startdate);
            end_date_timestamp = GenerateEndTimestamp();
        }
        else{
            start_date_timestamp = StringtoTimestamp(startdate);
            end_date_timestamp = StringtoTimestamp(startdate);
        }



        //get URL string
        String time_params = "?period1=" + start_date_timestamp + "&period2=" + end_date_timestamp;
        String url = YAHOO_FINANCE_URL_START + symbol + time_params + YAHOO_FINANCE_URL_END;

        URL MyURL = null;
        URLConnection con = null;
        InputStreamReader ins = null;
        BufferedReader in = null;

        try{
            MyURL = new URL(url);
            con = MyURL.openConnection();
            ins = new InputStreamReader(con.getInputStream(), "UTF-8");
            in = new BufferedReader(ins);
            String newLine = in.readLine();

            while((newLine = in.readLine()) != null){
                String stockInfo[] = newLine.trim().split(",");
                StockData hisdata_5years = new StockData();
                hisdata_5years.setSymbol(symbol);
                hisdata_5years.setDate(stockInfo[0]);
                hisdata_5years.setOpen(Float.valueOf(stockInfo[1]));
                hisdata_5years.setHigh(Float.valueOf(stockInfo[2]));
                hisdata_5years.setLow(Float.valueOf(stockInfo[3]));
                hisdata_5years.setClose(Float.valueOf(stockInfo[4]));
                hisdata_5years.setAdj_close(Float.valueOf(stockInfo[5]));
                hisdata_5years.setVolume(Float.valueOf(stockInfo[6]));
                history_data.add(hisdata_5years);

            }
        } catch (Exception e) {
            return null;
        } finally {
            if(in != null){
                try{
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return history_data;

    }

    /**
     * @param stockSymbol
     * @return
     */
    public static JSONObject getLiveObjects(String stockSymbol) throws IOException {
        URL url = new URL(YAHOO_FINANCE_SPIDER_URL + stockSymbol);
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("x-api-key", api_key);

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.connect();

            StringBuffer msg = new StringBuffer();
            InputStream ins = conn.getInputStream();

            if (ins != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
                String text = null;
                while ((text = br.readLine()) != null) {
                    msg.append(text);
                }
            }

            JSONObject object = new JSONObject(msg.toString()).getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(0);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Double> getROR(String stockSymbol, String startDate) {
        ArrayList<Double> ROR = new ArrayList<Double>();
        ArrayList<StockData> stock = getHistoryData(stockSymbol, startDate, null);
        int n = stock.size();
        for(int i=1; i<n; i++){
            double day1 = stock.get(i-1).getAdj_close();
            double day2 = stock.get(i).getAdj_close();
            double ror = (day2 - day1) / day1;
            ROR.add(ror);
        }
        return ROR;
    }

    public static ArrayList<Double> getWeight(Holding[] holdings) {
        double totalValue = 0.0;
        ArrayList<Double> weight = new ArrayList<Double>();
        for(Holding holding: holdings){
            double value = holding.getCostPrice() * holding.getHoldingAmount();
            totalValue += value;
        }

        for(Holding holding: holdings){
            double value = holding.getCostPrice() * holding.getHoldingAmount();
            weight.add(value/totalValue);
        }
        return weight;
    }

    public static double getVolatility(ArrayList<Double> averages) throws IOException {
        double volatility = 0.0;
        double rf = getLiveObjects("%5ETNX").getDouble("regularMarketPrice") / 100;
        ArrayList<Double> excess_ret = new ArrayList<Double>();

        for(int i=0; i<averages.size(); i++){ //get rp-rf
            double rp_rf = averages.get(i) - rf;
            excess_ret.add(rp_rf);
        }

        double tot_excess_ret = 0.0; // get volatility: stdev of rp-rf
        for(int i=0; i<excess_ret.size(); i++){
            tot_excess_ret += excess_ret.get(i);
        }
        double avg_excess_ret = tot_excess_ret / excess_ret.size();
        double sum_diff_mean = 0.0;
        for(int i=0; i<excess_ret.size(); i++){
            sum_diff_mean += (excess_ret.get(i) - avg_excess_ret) * (excess_ret.get(i) - avg_excess_ret);
        }
        volatility = Math.sqrt(sum_diff_mean / excess_ret.size());

        return volatility;
    }


    public static double getSharpeRatio(Account[] accounts) throws SQLException, IOException {
        //only for one account!
        double SharpeRatio = 0.0;
        Holding[] holdings = DBUtil.getHolding(accounts);
        ArrayList<Double> weight = getWeight(holdings);
        System.out.println("weight: " + weight);
        double rf = getLiveObjects("%5ETNX").getDouble("regularMarketPrice") / 100;

        ArrayList<Double> averages = new ArrayList<Double>(); //avg return for all stocks
        for(Holding holding: holdings){
            String startDate = DBUtil.getStartDate(holding.getAccountId(), holding.getStockSymbol());
            System.out.println("Date: " + startDate);
            ArrayList<Double> ror = getROR(holding.getStockSymbol(), startDate);
            double ror_sum = 0.0;
            System.out.println("ror size: " + ror.size());
            for (int i=0; i<ror.size(); i++){
                ror_sum += ror.get(i);
                System.out.println(i + ": " + ror_sum);
            }
            double ror_avg = ror_sum / ror.size(); //for one stock
            averages.add(ror_avg);
        }
        System.out.println("average: " + averages);

        double rp = 0;//expected portfolio return
        for(int i=0; i<weight.size(); i++){
            if (Double.isNaN(averages.get(i))) {
                averages.set(i,0.0);
            }
            rp += weight.get(i) * averages.get(i);
        }
        System.out.println("rp: " + rp);
        System.out.println("rf: " + rf);
        double volatility = getVolatility(averages);
        System.out.println("volatility: " + volatility);
        SharpeRatio = (rp - rf) / volatility;

        return SharpeRatio;
    }


    public static void main(String[] args) throws Exception {
//        JSONObject msg = getLiveObjects("AAPL");
//        System.out.println(msg);

        Account[] accounts = new Account[]{DBUtil.getAccount(800009)};
        Holding[] holdings = DBUtil.getHolding(accounts);
        ArrayList<StockData> list = null;
        ArrayList<Double> ror = new ArrayList<Double>();
        //ror = getROR("AAPL", "2022-03-01");
        //System.out.println(ror);
        //System.out.println(ror.size());
        double sharpe = getSharpeRatio(accounts);
        System.out.println(sharpe);
    }
}

