package com.oilpalm3f.mainapp.weighbridge;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.oilpalm3f.mainapp.BuildConfig;
import com.oilpalm3f.mainapp.CollectionwithoutPlotDbModles.CollectionWithOutPlot;
import com.oilpalm3f.mainapp.R;
import com.oilpalm3f.mainapp.cloudhelper.Log;
import com.oilpalm3f.mainapp.collectioncenter.CCDataAccessHandler;
import com.oilpalm3f.mainapp.collectioncenter.FarmersDetailsScreen;
import com.oilpalm3f.mainapp.collectioncenter.PrintReceipt;
import com.oilpalm3f.mainapp.collectioncenter.WeighbridgeCC;
import com.oilpalm3f.mainapp.collectioncenter.collectioncentermodels.Collection;
import com.oilpalm3f.mainapp.collectioncenter.collectioncentermodels.CollectionCenter;
import com.oilpalm3f.mainapp.common.CommonConstants;
import com.oilpalm3f.mainapp.common.CommonUtils;
import com.oilpalm3f.mainapp.common.FiscalDate;
import com.oilpalm3f.mainapp.common.InputFilterMinMax;
import com.oilpalm3f.mainapp.database.DataAccessHandler;
import com.oilpalm3f.mainapp.database.Queries;
import com.oilpalm3f.mainapp.datasync.helpers.DataManager;
import com.oilpalm3f.mainapp.utils.ImageUtility;
import com.oilpalm3f.mainapp.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static com.oilpalm3f.mainapp.datasync.helpers.DataManager.COLLECTION_CENTER_DATA;


public class tareWeighCaliculation extends Fragment {
    EditText tokenNoEt, number_of_bunches, number_of_bunches_rejected, number_of_bunches_accepted,
            remarks, grader_name;
    static EditText tare_weight;
    Button grossWeightBtn;
    TextView netWeightTxt, collection_ID;
    EditText date_and_time, vehicle_number, vehicle_driver, gross_weight, operator_name, commentsEdit, post_date_and_time;
    ImageView slip_image, slip_icon;
    View view;
    String WeighbridgeId;
    LinearLayout tareLayout;
    DataAccessHandler dataAccessHandler;
    TokenTable mtokenTable;
    Button generateReceipt;
    private int typeSelected;
    private CollectionCenter selectedCollectionCenter;
    private String mCurrentPhotoPath;
    private Bitmap currentBitmap = null;
    private static final int CAMERA_REQUEST = 1888;
    private String ColFarmerWithOutplot,totalBunches ;
    private CCDataAccessHandler ccDataAccessHandler;
    private String days = "";
    public int financialYear;

    Spinner isloosefruitavailable_spinner;

    private EditText harvestername,harvestermobilenumber,harvestervillage, harvestermandal, unripen, underripe, ripen, overripe, diseased,
            emptybunches, longstalk, mediumstalk, shortstalk, optimum, loosefruitweight;
    private LinearLayout loosefruitweightLL;
    private LinearLayout newlyt;
    LinearLayout avehiclenumberlyt;

    private double GrossWeightD = 0.0, TareWeightD = 0.0;


    private String vehicleNumber, vehicleDriverName, grossWeight, tareWeight, netWeight, operatorName, bunchesNumber, rejectedBunches, remarksS, graderName;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_CAM_PERMISSIONS = 1;
    public static final int OWN_WEIGHBRIDGE = 24;
    public static final int PRIVATE_WEIGHBRIDGE = 25;
    public static final int NO_WEIGHBRIDGE = 26;
    private static final String LOG_TAG = tareWeighCaliculation.class.getName();
    private int rejectedBunches_converted, totalBunches_converted;
    public tareWeighCaliculation() {

    }



    //Initializing the Class
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_tare_weigh_caliculation, container, false);
        final Calendar calendar = Calendar.getInstance();

        final FiscalDate fiscalDate = new FiscalDate(calendar);
        financialYear = fiscalDate.getFiscalYear();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            String currentdate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_3);
            String financalDate = "01/04/"+String.valueOf(financialYear);
            Date date1 = dateFormat.parse(currentdate);
            Date date2 = dateFormat.parse(financalDate);
            long diff = date1.getTime() - date2.getTime();
            String noOfDays = String.valueOf(TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS)+1);
            days = StringUtils.leftPad(noOfDays,3,"0");
            Log.v(LOG_TAG,"days -->"+days);

        }catch (Exception e){
            e.printStackTrace();
        }

        initViews();
        return view;
    }


    //Initializing the UI
    private void initViews() {
        dataAccessHandler = new DataAccessHandler(getContext());
        ccDataAccessHandler = new CCDataAccessHandler(getActivity());
        ColFarmerWithOutplot = FarmersDetailsScreen.firstthree;
        CommonConstants.flowFrom="Tare_Weight";

        tokenNoEt = view.findViewById(R.id.tokenNoEt);
        grossWeightBtn=view.findViewById(R.id.grossWeightBtn);
        tare_weight = view.findViewById(R.id.tare_weight);
        netWeightTxt = view.findViewById(R.id.netWeightTxt);
        number_of_bunches = view.findViewById(R.id.number_of_bunches);
        number_of_bunches_rejected = view.findViewById(R.id.number_of_bunches_rejected);
        number_of_bunches_accepted = view.findViewById(R.id.number_of_bunches_accepted);
        remarks = view.findViewById(R.id.remarks);
        grader_name = view.findViewById(R.id.grader_name);
        slip_icon = view.findViewById(R.id.slip_icon);
        slip_image = view.findViewById(R.id.slip_image);
        date_and_time = view.findViewById(R.id.date_and_time);
        vehicle_number = view.findViewById(R.id.vehicle_number);
        vehicle_driver = view.findViewById(R.id.vehicle_driver);
        gross_weight = view.findViewById(R.id.gross_weight);
        operator_name = view.findViewById(R.id.operator_name);
        commentsEdit = view.findViewById(R.id.commentsEdit);
        post_date_and_time = view.findViewById(R.id.post_date_and_time);
        collection_ID = view.findViewById(R.id.collection_ID);
        tareLayout = view.findViewById(R.id.tareLayout);
        generateReceipt = view.findViewById(R.id.generateReceipt);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        avehiclenumberlyt =  view.findViewById(R.id.avehiclenumberlyt);
        newlyt = view.findViewById(R.id.newlyt);
        harvestername =  view.findViewById(R.id.harvestername_et);
        harvestermobilenumber =  view.findViewById(R.id.harvestermobileNumber_et);
        harvestervillage =  view.findViewById(R.id.harvestervillage_et);
        harvestermandal =  view.findViewById(R.id.harvestermandal_et);
        unripen =  view.findViewById(R.id.unripen);
        underripe =  view.findViewById(R.id.underripe);
        ripen =  view.findViewById(R.id.ripen);
        overripe =  view.findViewById(R.id.overipe);
        diseased =  view.findViewById(R.id.diseased);
        emptybunches =  view.findViewById(R.id.emptybunch);
        longstalk =  view.findViewById(R.id.longstake);
        mediumstalk =  view.findViewById(R.id.mediumstake);
        shortstalk =  view.findViewById(R.id.shortstake);
        optimum =  view.findViewById(R.id.optimum);
        loosefruitweight = view.findViewById(R.id.loosefruitweight);
        loosefruitweightLL = view.findViewById(R.id.loosefruitweightLL);
        isloosefruitavailable_spinner = view.findViewById(R.id.isloosefruitavailable_spinner);

        //Binding Data to Loose Fruit Spinner & on Item Selected

        String[] isloosefruitavailableArray = getResources().getStringArray(R.array.yesOrNo_values);
        List<String> isloosefruitavailableList = Arrays.asList(isloosefruitavailableArray);
        ArrayAdapter<String> isloosefruitavailableAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, isloosefruitavailableList);
        isloosefruitavailableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        isloosefruitavailable_spinner.setAdapter(isloosefruitavailableAdapter);

        isloosefruitavailable_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                    loosefruitweightLL.setVisibility(View.VISIBLE);
                    loosefruitweight.setText("");
                } else {
                    loosefruitweightLL.setVisibility(View.GONE);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        unripen.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        underripe.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        ripen.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        overripe.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        diseased.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        emptybunches.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        longstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        mediumstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        shortstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        optimum.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});

        //Getting Selected Collection Center Type
        selectedCollectionCenter = (CollectionCenter) DataManager.getInstance().getDataFromManager(COLLECTION_CENTER_DATA);
        typeSelected = selectedCollectionCenter.getWeighBridgeTypeId();

        if (selectedCollectionCenter.getStateId() != 1){
            newlyt.setVisibility(View.GONE);
        }else{
            newlyt.setVisibility(View.VISIBLE);
        }

        if (typeSelected == OWN_WEIGHBRIDGE) {
           // toolbar.setTitle(R.string.new_collection_own);
            toolbar.setTitle("Weight Calculation");

        } else if (typeSelected == PRIVATE_WEIGHBRIDGE) {
          //  toolbar.setTitle(R.string.new_collection_priv);
            toolbar.setTitle("Weight Calculation");

        } else {
          //  toolbar.setTitle(R.string.new_collection_no_weighbridge);
            toolbar.setTitle("Weight Calculation");
        }


        //Tokennumber field on Text Changed Listener
        tokenNoEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() >= 5) {


                    mtokenTable = dataAccessHandler.getgrossWeightDetails(Queries.getInstance()
                            .getGrossWeightDetails(Integer.parseInt(s.toString())));

                    if (mtokenTable != null) {
                        if (mtokenTable.getVehicleNumber().equalsIgnoreCase("null")){

                            avehiclenumberlyt.setVisibility(View.GONE);
                        }else{

                            avehiclenumberlyt.setVisibility(View.VISIBLE);
                        }

                        tareLayout.setVisibility(View.VISIBLE);
                        collection_ID.setText("" + mtokenTable.getCollId());
                        date_and_time.setText("" + mtokenTable.getWeighingDate());
                        vehicle_number.setText("" + mtokenTable.getVehicleNumber());
                        vehicle_driver.setText("" + mtokenTable.getDriverName());
                        gross_weight.setText("" + mtokenTable.getGrossWeight());
                        operator_name.setText("" + mtokenTable.getOperatorName());
                        commentsEdit.setText("" + mtokenTable.getComments());
                        if(mtokenTable.getPostingDate()!=null) {

                           Calendar c = Calendar.getInstance();
                           SimpleDateFormat objdateformat = new SimpleDateFormat(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS, Locale.US);
                         String  postedDateAndTimeStr = objdateformat.format(c.getTime());
                            post_date_and_time.setText(postedDateAndTimeStr);

                        }
                        WeighbridgeId = mtokenTable.getCollectionCenterCode();
                        Log.v("@@@W","Id"+WeighbridgeId);
                    }else{

                        UiUtils.showCustomToastMessage("Token Doesn't Exist", getActivity(), 0);
                    }
                }

            }
        });

        //Gross Weight/Read Weight On Click Listener
        grossWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), New_WeighbridgeActivity.class));

            }
        });

        //NumberofBunches field on Text Changed Listener
        number_of_bunches.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                totalBunches = number_of_bunches.getText().toString();
                if (TextUtils.isEmpty(totalBunches) || !validateBunchValues()) {
                    number_of_bunches_rejected.setText("");
                    number_of_bunches_accepted.setText("");
                }
            }
        });

//NumberofBunchesRejected field on Text Changed Listener
        number_of_bunches_rejected.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                rejectedBunches = number_of_bunches_rejected.getText().toString();
                if (!validateBunchValues()) {
                    if (!TextUtils.isEmpty(totalBunches)) {
                        number_of_bunches_rejected.setText(rejectedBunches.substring(0, rejectedBunches.length() - 1));
                        number_of_bunches_rejected.setSelection(rejectedBunches.length());
                    }
                }
            }
        });

//TareWeight field on Text Changed Listener
        tare_weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tareWeight = tare_weight.getText().toString();
                if (!validateDoubles()) {
                    if (!TextUtils.isEmpty(grossWeight)) {
                        tare_weight.setText(tareWeight.substring(0, tareWeight.length() - 1));
                        tare_weight.setSelection(tareWeight.length());
                    }
                }
            }
        });

        //Image on Click Listener
        slip_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (!CommonUtils.isPermissionAllowed(getActivity(), Manifest.permission.CAMERA))) {
                    Log.v(LOG_TAG, "Location Permissions Not Granted");
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            PERMISSIONS_STORAGE,
                            REQUEST_CAM_PERMISSIONS
                    );
                } else {
//                    Intent Camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(Camera, CAMERA_REQUEST);
                    dispatchTakePictureIntent(CAMERA_REQUEST);
                }
            }
        });

//GenerateReceipt on Click Listener
        generateReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                injectData();
                if (typeSelected == OWN_WEIGHBRIDGE && !validationsInOwWbCase(true)) {
                    return;
                } else if (typeSelected == PRIVATE_WEIGHBRIDGE && !validationsInPrivateWbCase()) {
                    return;
                } else if (!validationsInNoWbCase()) {
                    return;
                }
                gotoPreviewScreen();
            }
        });


    }

    //Validation for Bunches
    public boolean validateBunchValues() {
        if (!TextUtils.isEmpty(rejectedBunches) && !rejectedBunches.equalsIgnoreCase(".") && !TextUtils.isEmpty(totalBunches) && !totalBunches.equalsIgnoreCase(".")) {
            try {
                rejectedBunches_converted = Integer.parseInt(rejectedBunches);
                totalBunches_converted = Integer.parseInt(totalBunches);
                if (totalBunches_converted >= rejectedBunches_converted) {
                    getAreaLeft(totalBunches_converted, rejectedBunches_converted);
                } else {
                    UiUtils.showCustomToastMessage("Rejected bunches should not exceed total bunches", getActivity(), 1);
                    return false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "" + e.getMessage());
            }
        } else if (!TextUtils.isEmpty(rejectedBunches) && !TextUtils.isEmpty(totalBunches) && !totalBunches.equalsIgnoreCase(".")) {
            Log.v(LOG_TAG, "@@@ check 1 " + totalBunches);
            totalBunches_converted = Integer.parseInt(totalBunches);
            getAreaLeft(totalBunches_converted, 0);
        }
        return true;
    }

    //Validation for Weight
    public boolean validateDoubles() {
        grossWeight = gross_weight.getText().toString();

        if (!TextUtils.isEmpty(grossWeight) && !grossWeight.equalsIgnoreCase(".") && !TextUtils.isEmpty(tareWeight) && !tareWeight.equalsIgnoreCase(".")) {
            try {
                GrossWeightD = Double.parseDouble(grossWeight);
                TareWeightD = Double.parseDouble(tareWeight);
                if (GrossWeightD >= TareWeightD) {
                    getNetWeight(GrossWeightD, TareWeightD);
                } else {
                    UiUtils.showCustomToastMessage("Tare weight  should not exceed gross weight", getActivity(), 1);
                    return false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "" + e.getMessage());
            }
        } else if (!TextUtils.isEmpty(grossWeight) && !grossWeight.equalsIgnoreCase(".")) {
            try {
                GrossWeightD = Double.parseDouble(grossWeight);
                getNetWeight(GrossWeightD, 0.0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "" + e.getMessage());
            }
        } else if (!TextUtils.isEmpty(tareWeight) && !tareWeight.equalsIgnoreCase(".")) {
            try {
                TareWeightD = Double.parseDouble(tareWeight);
                getNetWeight(TareWeightD, 0.0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "" + e.getMessage());
            }
        }
        return true;
    }

    //Set Number Bunches Accepted
    public double getAreaLeft(final int plotAreaDouble, final int totalAreaDouble) {
        double diff = plotAreaDouble - totalAreaDouble;
        number_of_bunches_accepted.setText("" + diff);
        return diff;
    }

    //Set Netweight
    public double getNetWeight(final Double plotAreaDouble, final Double totalAreaDouble) {
        double diff = plotAreaDouble - totalAreaDouble;
        netWeightTxt.setVisibility(View.VISIBLE);
        netWeightTxt.setText("" + diff);
        return diff;
    }


    //Navigate to Confirm Receipt Screen
    public void gotoPreviewScreen() {
        PrintReceipt printReceipt = new PrintReceipt();
        if (ColFarmerWithOutplot.equalsIgnoreCase("CCFARMERW")||ColFarmerWithOutplot.equalsIgnoreCase("FFBFARMER")){
            bindCollectionFarmerWOPData();
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelable("collection_data", bindCollectionFarmerWOPData());
            printReceipt.setArguments(dataBundle);
        }else {
            bindData();
            Bundle dataBundle = new Bundle();
            dataBundle.putParcelable("collection_data", bindData());
            printReceipt.setArguments(dataBundle);
        }

        String backStateName = printReceipt.getClass().getName();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ftransaction = fm.beginTransaction();
        ftransaction.replace(android.R.id.content, printReceipt).commit();
        ftransaction.addToBackStack(backStateName);
    }

    //Bind Collection Data for Farmer Without Plot
    public CollectionWithOutPlot bindCollectionFarmerWOPData(){
        CollectionWithOutPlot wbWOPCollection = new CollectionWithOutPlot();

        wbWOPCollection.setWeighingDate(date_and_time.getText().toString());
        wbWOPCollection.setCollectioncentercode(selectedCollectionCenter.getCode());
        wbWOPCollection.setCode(ccDataAccessHandler.getGeneratedCollectionCodeWithOutPlotFarmers(selectedCollectionCenter.getCode(), ccDataAccessHandler.COLLECTION_CENTER_CODE_WithOutPlot_INITIAL,
                ccDataAccessHandler.TABLE_COLLECTION_WithOutPlot,days));

        if (mtokenTable.getVehicleNumber().equalsIgnoreCase("null")){

            wbWOPCollection.setVehiclenumber("null");
        }else{
            wbWOPCollection.setVehiclenumber(vehicleNumber.toUpperCase());

        }

       // wbWOPCollection.setVehiclenumber(vehicleNumber.toUpperCase());
        wbWOPCollection.setDrivername(vehicleDriverName);
        wbWOPCollection.setGrossweight(CommonUtils.isValidDouble(grossWeight) ? Double.parseDouble(grossWeight) : 0);
        wbWOPCollection.setNetweight(CommonUtils.isValidDouble(netWeight) ? Double.parseDouble(netWeight) : 0);
        wbWOPCollection.setTareweight(CommonUtils.isValidDouble(tareWeight) ? Double.parseDouble(tareWeight) : 0);
        wbWOPCollection.setOperatorname(operatorName);
        wbWOPCollection.setTokenNo(Integer.parseInt(tokenNoEt.getText().toString()));
        wbWOPCollection.setRecieptGeneratedDate(post_date_and_time.getText().toString());
        if (!TextUtils.isEmpty(bunchesNumber) && bunchesNumber.length() > 0) {
            wbWOPCollection.setTotalbunches(Integer.parseInt(bunchesNumber));
        }
        if (!TextUtils.isEmpty(rejectedBunches) && rejectedBunches.length() > 0) {
            wbWOPCollection.setRejectedbunches(Integer.parseInt(rejectedBunches));
        }
        if (typeSelected == PRIVATE_WEIGHBRIDGE || typeSelected == NO_WEIGHBRIDGE) {
            if (WeighbridgeId!=null) {

                String wbId = WeighbridgeId;
                Log.v(LOG_TAG, "@@@ selected wb center id " + wbId);

                wbWOPCollection.setWeighbridgecenterid(Integer.parseInt(wbId));
            } else {
                wbWOPCollection.setWeighbridgecenterid(null);
            }
        } else {
            wbWOPCollection.setWeighbridgecenterid(null);
        }
        wbWOPCollection.setRemarks(remarksS);
        wbWOPCollection.setGradername(graderName);
        wbWOPCollection.setComments(commentsEdit.getText().toString());
        wbWOPCollection.setRecieptlocation(mCurrentPhotoPath);
        wbWOPCollection.setRecieptextension(".jpg");

        wbWOPCollection.setVehicleTypeId(mtokenTable.getVehicleTypeId());

        if (!TextUtils.isEmpty(harvestername.getText().toString())) {
            wbWOPCollection.setName(harvestername.getText().toString());
        }else {
            wbWOPCollection.setName("");
        }

        if (!TextUtils.isEmpty(harvestermobilenumber.getText().toString())) {
            wbWOPCollection.setMobileNumber(harvestermobilenumber.getText().toString());
        }else{
            wbWOPCollection.setMobileNumber("");
        }

        if (!TextUtils.isEmpty(harvestervillage.getText().toString())) {
            wbWOPCollection.setVillage(harvestervillage.getText().toString());
        }else{
            wbWOPCollection.setVillage("");
        }

        if (!TextUtils.isEmpty(harvestermandal.getText().toString())) {
            wbWOPCollection.setMandal(harvestermandal.getText().toString());
        }else{
            wbWOPCollection.setMandal("");
        }

        if (selectedCollectionCenter.getStateId() == 1) {

            wbWOPCollection.setUnRipen(Integer.parseInt(unripen.getText().toString()));
            wbWOPCollection.setUnderRipe(Integer.parseInt(underripe.getText().toString()));
            wbWOPCollection.setRipen(Integer.parseInt(ripen.getText().toString()));
            wbWOPCollection.setOverRipe(Integer.parseInt(overripe.getText().toString()));
            wbWOPCollection.setDiseased(Integer.parseInt(diseased.getText().toString()));
            wbWOPCollection.setEmptyBunches(Integer.parseInt(emptybunches.getText().toString()));
            wbWOPCollection.setFFBQualityLong(Integer.parseInt(longstalk.getText().toString()));
            wbWOPCollection.setFFBQualityMedium(Integer.parseInt(mediumstalk.getText().toString()));
            wbWOPCollection.setFFBQualityShort(Integer.parseInt(shortstalk.getText().toString()));
            wbWOPCollection.setFFBQualityOptimum(Integer.parseInt(optimum.getText().toString()));

            int isfruitavailablee = 0;

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                isfruitavailablee = 1;
            } else if (isloosefruitavailable_spinner.getSelectedItemPosition() == 2) {
                isfruitavailablee = 0;
            }

            wbWOPCollection.setLooseFruit(isfruitavailablee);

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                wbWOPCollection.setLooseFruitWeight(Integer.parseInt(loosefruitweight.getText().toString()));

            }
        }

        return wbWOPCollection;
    }

    //Bind Collection Data for Farmer with Plot
    public Collection bindData() {
        Collection wbCollection = new Collection();
        wbCollection.setWeighingDate(date_and_time.getText().toString());
        wbCollection.setCollectioncentercode(selectedCollectionCenter.getCode());
        wbCollection.setCode(ccDataAccessHandler.getGeneratedCollectionCode(selectedCollectionCenter.getCode(), ccDataAccessHandler.COLLECTION_CENTER_CODE_INITIAL, ccDataAccessHandler.TABLE_COLLECTION,days));
        if (mtokenTable.getVehicleNumber().equalsIgnoreCase("null")){

            wbCollection.setVehiclenumber("null");
        }else{
            wbCollection.setVehiclenumber(vehicleNumber.toUpperCase());

        }

      //  wbCollection.setVehiclenumber(vehicleNumber.toUpperCase());
        wbCollection.setDrivername(vehicleDriverName);
        wbCollection.setTokenNo(Integer.parseInt(tokenNoEt.getText().toString()));
        wbCollection.setGrossweight(CommonUtils.isValidDouble(grossWeight) ? Double.parseDouble(grossWeight) : 0);
        wbCollection.setNetweight(CommonUtils.isValidDouble(netWeight) ? Double.parseDouble(netWeight) : 0);
        wbCollection.setTareweight(CommonUtils.isValidDouble(tareWeight) ? Double.parseDouble(tareWeight) : 0);
        wbCollection.setOperatorname(operatorName);
        wbCollection.setRecieptGeneratedDate(post_date_and_time.getText().toString());
        if (!TextUtils.isEmpty(bunchesNumber) && bunchesNumber.length() > 0) {
            wbCollection.setTotalbunches(Integer.parseInt(bunchesNumber));
        }
        if (!TextUtils.isEmpty(rejectedBunches) && rejectedBunches.length() > 0) {
            wbCollection.setRejectedbunches(Integer.parseInt(rejectedBunches));
        }
        if (typeSelected == PRIVATE_WEIGHBRIDGE || typeSelected == NO_WEIGHBRIDGE) {
            if (WeighbridgeId!=null) {

                String wbId = WeighbridgeId;

                wbCollection.setWeighbridgecenterid(Integer.parseInt(wbId));
            } else {
                wbCollection.setWeighbridgecenterid(null);
            }
        } else {
            wbCollection.setWeighbridgecenterid(null);
        }
        wbCollection.setRemarks(remarksS);
        wbCollection.setGradername(graderName);
        wbCollection.setComments(commentsEdit.getText().toString());
        wbCollection.setRecieptlocation(mCurrentPhotoPath);
        wbCollection.setRecieptextension(".jpg");

        wbCollection.setVehicleTypeId(mtokenTable.getVehicleTypeId());

        if (!TextUtils.isEmpty(harvestername.getText().toString())) {
            wbCollection.setName(harvestername.getText().toString());
        }else {
            wbCollection.setName("");
        }

        if (!TextUtils.isEmpty(harvestermobilenumber.getText().toString())) {
            wbCollection.setMobileNumber(harvestermobilenumber.getText().toString());
        }else{
            wbCollection.setMobileNumber("");
        }

        if (!TextUtils.isEmpty(harvestervillage.getText().toString())) {
            wbCollection.setVillage(harvestervillage.getText().toString());
        }else{
            wbCollection.setVillage("");
        }

        if (!TextUtils.isEmpty(harvestermandal.getText().toString())) {
            wbCollection.setMandal(harvestermandal.getText().toString());
        }else{
            wbCollection.setMandal("");
        }
        if (selectedCollectionCenter.getStateId() == 1) {

            wbCollection.setUnRipen(Integer.parseInt(unripen.getText().toString()));
            wbCollection.setUnderRipe(Integer.parseInt(underripe.getText().toString()));
            wbCollection.setRipen(Integer.parseInt(ripen.getText().toString()));
            wbCollection.setOverRipe(Integer.parseInt(overripe.getText().toString()));
            wbCollection.setDiseased(Integer.parseInt(diseased.getText().toString()));
            wbCollection.setEmptyBunches(Integer.parseInt(emptybunches.getText().toString()));
            wbCollection.setFFBQualityLong(Integer.parseInt(longstalk.getText().toString()));
            wbCollection.setFFBQualityMedium(Integer.parseInt(mediumstalk.getText().toString()));
            wbCollection.setFFBQualityShort(Integer.parseInt(shortstalk.getText().toString()));
            wbCollection.setFFBQualityOptimum(Integer.parseInt(optimum.getText().toString()));


            int isfruitavailable = 0;

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                isfruitavailable = 1;
            } else if (isloosefruitavailable_spinner.getSelectedItemPosition() == 2) {
                isfruitavailable = 0;
            }

            wbCollection.setLooseFruit(isfruitavailable);

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                wbCollection.setLooseFruitWeight(Integer.parseInt(loosefruitweight.getText().toString()));

            }
        }

        return wbCollection;
    }


    //Validations
    public boolean validationsInPrivateWbCase() {


        return validationsInOwWbCase(true);
    }

    //Validations
    public boolean validationsInNoWbCase() {


        if (currentBitmap == null) {
            UiUtils.showCustomToastMessage("Please Capture Photo", getActivity(), 1);
            return false;
        }


        if (!TextUtils.isEmpty(harvestermobilenumber.getText().toString())) {
            if (harvestermobilenumber.getText().toString().length() < 10) {
                UiUtils.showCustomToastMessage("Please Enter Proper Mobile Number", getActivity(), 0);
                return false;
            }
        }

        ///////////////////

        if (selectedCollectionCenter.getStateId() == 1) {
            if (TextUtils.isEmpty(unripen.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Unripen", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(underripe.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Underripe", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(ripen.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Ripen", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(overripe.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Overripe", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(diseased.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Diseased", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(emptybunches.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Empty Bunches", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(longstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Long Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(mediumstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Medium Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(shortstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Short Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(optimum.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Optimum Stock Quality", getActivity(), 0);
                return false;
            }

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 0) {
                UiUtils.showCustomToastMessage("Please Select Is Loose Fruit Available", getActivity(), 0);
                return false;
            }

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {
                if (TextUtils.isEmpty(loosefruitweight.getText().toString())) {
                    UiUtils.showCustomToastMessage("Please Enter Loose Fruit Weight", getActivity(), 0);
                    return false;
                }
            }

            if ((Double.parseDouble(unripen.getText().toString()) + Double.parseDouble(underripe.getText().toString()) + Double.parseDouble(ripen.getText().toString()) + Double.parseDouble(overripe.getText().toString()) + Double.parseDouble(diseased.getText().toString()) + Double.parseDouble(emptybunches.getText().toString())) != 100) {
                UiUtils.showCustomToastMessage("FFB Bunch Quality should be equal to 100%", getActivity(), 0);
                return false;
            }

            if ((Double.parseDouble(longstalk.getText().toString()) + Double.parseDouble(mediumstalk.getText().toString()) + Double.parseDouble(shortstalk.getText().toString()) + Double.parseDouble(optimum.getText().toString())) != 100) {
                UiUtils.showCustomToastMessage("FFB Stalk Quality should be equal to 100%", getActivity(), 0);
                return false;
            }
        }


        return true;
    }

    //Validations
    public boolean validationsInOwWbCase(boolean applyValidation) {
//




        if (applyValidation && TextUtils.isEmpty(tareWeight)) {
            UiUtils.showCustomToastMessage("Enter Tare Weight", getActivity(), 1);
            return false;
        }
        if (TextUtils.isEmpty(netWeight)) {
            UiUtils.showCustomToastMessage("Enter Net Weight", getActivity(), 1);
            return false;
        }

        if (applyValidation && currentBitmap == null) {
            UiUtils.showCustomToastMessage("Please Capture Photo", getActivity(), 1);
            return false;
        }


        if (!TextUtils.isEmpty(harvestermobilenumber.getText().toString())) {
            if (harvestermobilenumber.getText().toString().length() < 10) {
                UiUtils.showCustomToastMessage("Please Enter Proper Mobile Number", getActivity(), 0);
                return false;
            }
        }

        ///////////////////

        if (selectedCollectionCenter.getStateId() == 1) {
            if (TextUtils.isEmpty(unripen.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Unripen", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(underripe.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Underripe", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(ripen.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Ripen", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(overripe.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Overripe", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(diseased.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Diseased", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(emptybunches.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Empty Bunches", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(longstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Long Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(mediumstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Medium Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(shortstalk.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Short Stock Quality", getActivity(), 0);
                return false;
            }
            if (TextUtils.isEmpty(optimum.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Optimum Stock Quality", getActivity(), 0);
                return false;
            }

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 0) {
                UiUtils.showCustomToastMessage("Please Select Is Loose Fruit Available", getActivity(), 0);
                return false;
            }

            if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {
                if (TextUtils.isEmpty(loosefruitweight.getText().toString())) {
                    UiUtils.showCustomToastMessage("Please Enter Loose Fruit Weight", getActivity(), 0);
                    return false;
                }
            }

            if ((Double.parseDouble(unripen.getText().toString()) + Double.parseDouble(underripe.getText().toString()) + Double.parseDouble(ripen.getText().toString()) + Double.parseDouble(overripe.getText().toString()) + Double.parseDouble(diseased.getText().toString()) + Double.parseDouble(emptybunches.getText().toString())) != 100) {
                UiUtils.showCustomToastMessage("FFB Bunch Quality should be equal to 100%", getActivity(), 0);
                return false;
            }

            if ((Double.parseDouble(longstalk.getText().toString()) + Double.parseDouble(mediumstalk.getText().toString()) + Double.parseDouble(shortstalk.getText().toString()) + Double.parseDouble(optimum.getText().toString())) != 100) {
                UiUtils.showCustomToastMessage("FFB Stalk Quality should be equal to 100%", getActivity(), 0);
                return false;
            }
        }


        return true;
    }

    //Hides Keyboard
    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

//Binding Data
    public void injectData() {
        vehicleNumber = vehicle_number.getText().toString();
        vehicleDriverName = vehicle_driver.getText().toString();
        tareWeight = tare_weight.getText().toString();
        if (netWeightTxt.getVisibility() == View.VISIBLE) {
            netWeight = netWeightTxt.getText().toString();
        } else {
            netWeight = netWeightTxt.getText().toString();
        }
        operatorName = operator_name.getText().toString();
        bunchesNumber = number_of_bunches.getText().toString();
        rejectedBunches = number_of_bunches_rejected.getText().toString();
        remarksS = remarks.getText().toString();
        graderName = grader_name.getText().toString();
//        dateAndTimeStr = dateTimeEdit.getText().toString();
//        postedDateAndTimeStr = postDateTimeEdit.getText().toString();
    }

    //Creates Images File in the mentioned File
    private File createImageFile() throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File rootDirectory = new File(root + "/3F_Pictures");
        File pictureDirectory = new File(root + "/3F_Pictures/" + "CollectionPhotos");

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }

        if (!pictureDirectory.exists()) {
            pictureDirectory.mkdirs();
        }

        File finalFile = new File(pictureDirectory, collection_ID.getText().toString() + CommonConstants.JPEG_FILE_SUFFIX);
        return finalFile;
    }

    //Setup Photo File
    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch (actionCode) {
            case CAMERA_REQUEST:
                File f = null;
                mCurrentPhotoPath = null;
                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(getActivity(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            f);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch
        startActivityForResult(takePictureIntent, actionCode);
    }


    //Load Image from Storage
    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "@@@ check on resume called");
        if (!TextUtils.isEmpty(mCurrentPhotoPath) && !TextUtils.isEmpty(collection_ID.getText().toString()) && null != slip_image) {
            loadImageFromStorage(mCurrentPhotoPath);
            slip_image.invalidate();
        }
    }

    private void loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            b = ImageUtility.rotatePicture(90, b);
            slip_image.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
//            mCurrentPhotoPath = null;
        }

    }

    private void setPic() {

        /* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

        /* Get the size of the ImageView */
        int targetW = slip_image.getWidth();
        int targetH = slip_image.getHeight();

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        /* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        bitmap = ImageUtility.rotatePicture(90, bitmap);

        currentBitmap = bitmap;
        slip_image.setImageBitmap(bitmap);

//		/* Associate the Bitmap to the ImageView */
//        if (null != rotatedBitmap) {
//            String convertedImage = CommonUtils.getBase64String(rotatedBitmap);
//            Log.v(LOG_TAG, "@@@ converted image "+convertedImage.length());
//            slipImage.setImageBitmap(rotatedBitmap);
//            currentBitmap = rotatedBitmap;
//        } else {
//            currentBitmap = bitmap;
//            slipImage.setImageBitmap(bitmap);
//        }

        slip_image.setVisibility(View.VISIBLE);
        slip_icon.setVisibility(View.GONE);
        slip_image.invalidate();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

        } // switch
    }


}