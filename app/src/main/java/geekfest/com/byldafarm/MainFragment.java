package geekfest.com.byldafarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by prempal on 22/3/15.
 */
public class MainFragment extends Fragment {

    private Button submitButton, buildFarmButton;
    private EditText farmAreaEdTxt, farmLocationEdTxt, farmBudgetEdTxt;

    String stringToPassInSQL = "" ,location = "";
    float maxProfit;


    private ProgressBar progressBar;

    public MainFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        submitButton = (Button) rootView.findViewById(R.id.submit);
        farmAreaEdTxt = (EditText) rootView.findViewById(R.id.farm_area);
        farmBudgetEdTxt = (EditText) rootView.findViewById(R.id.farm_budget);
        farmLocationEdTxt = (EditText) rootView.findViewById(R.id.farm_location);
        buildFarmButton = (Button) rootView.findViewById(R.id.build_a_farm);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        ArrayList<String> cropsGrown = Utils.getSeason();


        for(int i = 0; i < cropsGrown.size(); i++){
            if( i == cropsGrown.size() - 1){
                stringToPassInSQL = stringToPassInSQL + "\'"+cropsGrown.get(i)+"\');";
            } else
            stringToPassInSQL = stringToPassInSQL + "\'"+cropsGrown.get(i)+"\', "; //"\' OR Season LIKE ";
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int farmBudget = Integer.parseInt(farmBudgetEdTxt.getText().toString());
                final int farmArea = Integer.parseInt(farmAreaEdTxt.getText().toString());
                location = farmLocationEdTxt.getText().toString();
                final String sqlInput = "SELECT * FROM `nigga` WHERE District LIKE " + "\'" + location + "\'" + " AND Season IN ("+ stringToPassInSQL;

                Log.d("Raghav", sqlInput);
                new QueryTask(){
                    @Override
                    protected void onPostExecute(String stringResponse) {
                        try {


                            JSONArray jsonArray = new JSONArray(stringResponse);
                            ArrayList<Crop> crop = new ArrayList<Crop>();
                            int i = 0;
                            while(i < jsonArray.length() && i < 3) {
                                crop.add(i, new Crop());
                                crop.get(i).cropName = jsonArray.getJSONObject(i).getString("Crop");
                                crop.get(i).seedCost = jsonArray.getJSONObject(i).getInt("SeedCost");
                                crop.get(i).fertilizerCost = jsonArray.getJSONObject(i).getInt("Fertilizer");
                                crop.get(i).irrigationCost = jsonArray.getJSONObject(i).getInt("Irrigation");
                                crop.get(i).labourCost = jsonArray.getJSONObject(i).getInt("LabourCost");
                                crop.get(i).sellingPrice = jsonArray.getJSONObject(i).getInt("SellingPrice");
                                crop.get(i).costPrice = jsonArray.getJSONObject(i).getInt("CostPrice");
                                i++;
                            }
                            FarmCalculationResult farmCalResult = AlgorithmBadCase.efficientFarm(farmBudget, farmArea, crop);
                            for( i = 0; i < crop.size(); i++){
                                if(i == 0){
                                    crop.get(i).maxArea = farmCalResult.maxAreaCrop1;

                                } else if (i == 1) {
                                   crop.get(i).maxArea = farmCalResult.maxAreaCrop2;

                                } else if (i ==2) {
                                    crop.get(i).maxArea = farmCalResult.maxAreaCrop3;

                                }
                            }
                            maxProfit = farmCalResult.totalProfit;
                            Log.d("Raghav", "" + maxProfit + "" + farmCalResult.maxAreaCrop1+ ""+ farmCalResult.maxAreaCrop2+ "" +farmCalResult.maxAreaCrop3);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                        Log.d("Raghav", stringResponse);
                    }
                }.execute(sqlInput);

                if(checkNotNull()) {
                    final String sqlInput = "SELECT * FROM `TABLE 1` WHERE District LIKE " + location + "AND Season LIKE " + stringToPassInSQL;

                    Log.d("Raghav", sqlInput);
                    new QueryTask() {
                        @Override
                        protected void onPreExecute() {
                            progressBar.setVisibility(View.VISIBLE);
                            super.onPreExecute();
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            progressBar.setVisibility(View.GONE);
                            Log.d("Raghav", s);
                        }
                    }.execute(sqlInput);
                }
            }
        });
        buildFarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(checkNotNull()){
                    Intent intent = new Intent(getActivity().getApplicationContext(), CustomMapActivity.class);
                    intent.putExtra("Area", farmAreaEdTxt.getText().toString());
                    startActivity(intent);
                }
            }
        });


    return rootView;
    }

    public boolean checkNotNull(){
        if(farmAreaEdTxt.getText().toString().equals("")){
            Toast.makeText(getActivity().getApplicationContext(), "Please enter area", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(farmBudgetEdTxt.getText().toString().equals("")){
            Toast.makeText(getActivity().getApplicationContext(), "Please enter budget", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(farmLocationEdTxt.getText().toString().equals("")){
            Toast.makeText(getActivity().getApplicationContext(), "Please enter location", Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.d("Show text", farmAreaEdTxt.getText().toString());

        return true;
    }
}