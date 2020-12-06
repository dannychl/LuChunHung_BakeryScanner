package com.example.bakeryscanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakeryscanner.model.Customer;
import com.example.bakeryscanner.model.CustomerCart;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderDetails extends AppCompatActivity {

    private String displayUserOrderDetailsURL = "http://192.168.8.107";
    private String loadCashURL = "http://192.168.8.107";
    private TextView mTvCustName, mTvDateOrdered, mTvTimeOrdered, mTvOrderDescriptions, mTvOrderRm,
            mTvTotalPaymentOrderDetails, mTvTotalPaymentOrderDetailsAmt, mTvTotalPaymentDiscOrderDetails,
            mTvTotalPaymentDiscOrderDetailsAmt;
    private ImageView mIvOrderDetailsBg, mIvDateOrdered, mIvTimeOrdered, mIvOrderDescriptionBg, mIvCustName;
    private RecyclerView rV;
    private PaymentAdapter adapter;
    private List<CustomerCart> mCustomerCartList;
    private List<Customer> mCustomerList;
    private String dateString= "", timeString = "", hour = "", min = "", amPm = "", cashHolder, userNameHolder, paymentType;
    private double totalPrice = 0.0, discountRate = 0.06;
    private ConnectionClass connectionClass;
    private int customerId;
    private double totalPriceIntent, totalPriceAfterDiscountIntent, totalAmountDeduct;
    private Button mPayByCash, mPayByEWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        getSupportActionBar().setTitle("My Order");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.headerColor)));

        mTvDateOrdered = findViewById(R.id.tvDateOrdered);
        mTvTimeOrdered = findViewById(R.id.tvTimeOrdered);
        mTvOrderDescriptions = findViewById(R.id.tvOrderDescriptions);
        mTvOrderRm = findViewById(R.id.tvOrderRM);
        mTvTotalPaymentOrderDetails = findViewById(R.id.tvTotalPaymentOrderDetails);
        mTvTotalPaymentDiscOrderDetailsAmt = findViewById(R.id.tvTotalPaymentDiscOrderDetailsAmt);
        mTvTotalPaymentOrderDetailsAmt = findViewById(R.id.tvTotalPaymentOrderDetailsAmt);
        mTvTotalPaymentDiscOrderDetails = findViewById(R.id.tvTotalPaymentDiscOrderDetails);
        mPayByCash = findViewById(R.id.paidByCash);
        mPayByEWallet = findViewById(R.id.paidByEWallet);

        Intent intent = getIntent();
        customerId = Integer.parseInt(intent.getStringExtra("CustId"));

        mIvOrderDescriptionBg = findViewById(R.id.ivOrderDescriptionBg);
        mIvDateOrdered = findViewById(R.id.ivDateOrdered);
        mIvTimeOrdered = findViewById(R.id.ivTimeOrdered);
        mIvOrderDetailsBg = findViewById(R.id.ivOrderDetailsBg);
        mIvCustName = findViewById(R.id.ivCustName);
        mTvCustName = findViewById(R.id.tvCustName);

        rV = findViewById(R.id.recyclerView);
        rV.setHasFixedSize(true);
        rV.setLayoutManager(new LinearLayoutManager(this));
        rV.setAdapter(adapter);

        connectionClass = new ConnectionClass();

        ViewCompat.setNestedScrollingEnabled(rV, false);

        getUserDetails(String.valueOf(customerId));
        
        mPayByEWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentType = "E-Wallet";

                totalAmountDeduct = Double.parseDouble(mTvTotalPaymentDiscOrderDetailsAmt.getText().toString());

                if (totalAmountDeduct < Double.parseDouble(cashHolder) && String.valueOf(cashHolder)!=null && !cashHolder.equals("")) {
                    transactionMadeByEWallet();
                }else{
                    Toast.makeText(OrderDetails.this, "Customer Wallet Amount Insufficient, Please reload now...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        mPayByCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentType = "Cash";
                transactionMadeByCash();
            }
        });


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder().client(httpClient.build())
                .baseUrl(displayUserOrderDetailsURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolder jsonPlaceHolderApi = retrofit.create(JsonPlaceHolder.class);

        Call<List<CustomerCart>> displayUserCart = jsonPlaceHolderApi.getUserOrderDetails("GET", String.valueOf(customerId));

        displayUserCart.enqueue(new Callback<List<CustomerCart>>() {
            @Override
            public void onResponse(Call<List<CustomerCart>> call, Response<List<CustomerCart>> response) {
                if (!response.isSuccessful()) {
                    String error = "Code "+ response.code();
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                mCustomerCartList = response.body();

                if (mCustomerCartList.size()>0){
                    adapter = new PaymentAdapter(getApplicationContext(), mCustomerCartList);
                    rV.setHasFixedSize(true);
                    rV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    ViewCompat.setNestedScrollingEnabled(rV, false);

                    for (int i = 0; i<mCustomerCartList.size(); i++){
                        totalPrice += Double.parseDouble(mCustomerCartList.get(i).getPrice());
                    }

                    dateString = mCustomerCartList.get(mCustomerCartList.size()-1).getDate();
                    timeString = mCustomerCartList.get(mCustomerCartList.size()-1).getTime();

                    setDateTime();

                    mTvTotalPaymentOrderDetailsAmt.setText(String.format("%.2f", totalPrice));
                    mTvTotalPaymentDiscOrderDetailsAmt.setText(String.format("%.2f", totalPrice-(totalPrice*discountRate)));
                }else{
                    Toast.makeText(OrderDetails.this, "No Order Placed... ", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(Call<List<CustomerCart>> call, Throwable t) {

            }
        });
    }

    private void transactionMadeByEWallet() {

        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetails.this);
        StringBuilder strBuilder = new StringBuilder();
        builder.setTitle("Press proceed to continue customer payment (By E-Wallet)");

        builder.setMessage(strBuilder.toString());
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                insertOrderDetailsHistory();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();

    }

    private void transactionMadeByCash() {

        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetails.this);
        StringBuilder strBuilder = new StringBuilder();
        builder.setTitle("Press proceed to continue customer payment (By Cash)");

        builder.setMessage(strBuilder.toString());
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                insertOrderDetailsHistory();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();

    }

    private void insertOrderDetailsHistory() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder().client(httpClient.build())
                .baseUrl(displayUserOrderDetailsURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolder jsonPlaceHolderApi = retrofit.create(JsonPlaceHolder.class);

        Call<List<CustomerCart>> displayUserCart = jsonPlaceHolderApi.getUserOrderDetails("GET", String.valueOf(customerId));

        displayUserCart.enqueue(new Callback<List<CustomerCart>>() {
            @Override
            public void onResponse(Call<List<CustomerCart>> call, Response<List<CustomerCart>> response) {
                if (!response.isSuccessful()) {
                    String error = "Code " + response.code();
                    Toast.makeText(OrderDetails.this, error, Toast.LENGTH_SHORT).show();
                    return;
                }
                mCustomerCartList = response.body();

                for (int i =0; i<mCustomerCartList.size(); i++){
                    insertIntoOrderHistory(i, mCustomerCartList);
                }

            }

            @Override
            public void onFailure(Call<List<CustomerCart>> call, Throwable t) {

            }
        });

    }

    private void insertIntoOrderHistory(int i, List<CustomerCart> customerCartList) {

        String z = "";
        boolean isSuccess = false;

        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Please check your internet connection";
            } else {

                if (paymentType.equals("Cash")){
                        String query= "insert into order_history(custId, orderStatus, productId, orderDate, orderTime, price, product_kg, cakeOnMessage, paymentType) VALUES ("+ customerId
                                +",'"+ "Done" + "',"+ customerCartList.get(i).getProductId()+",'"+ dateString+"','"+ timeString
                                +"','"+ customerCartList.get(i).getPrice()+
                                "','"+customerCartList.get(i).getProduct_kg()+"','"+ customerCartList.get(i).getCakeOnMessage()+"','"+ "Cash" + "')";
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                }
                else if (paymentType.equals("E-Wallet")){

                    String query= "insert into order_history(custId, orderStatus, productId, orderDate, orderTime, price, product_kg, cakeOnMessage, paymentType) VALUES ("+ customerId
                            +",'"+ "Done" + "',"+ customerCartList.get(i).getProductId()+",'"+ dateString+"','"+ timeString
                            +"','"+ customerCartList.get(i).getPrice()+
                            "','"+customerCartList.get(i).getProduct_kg()+"','"+ customerCartList.get(i).getCakeOnMessage()+"','"+ "E-Wallet" + "')";

                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(query);
                }

                    isSuccess = true;

            }
        }catch (Exception ex)
        {
            isSuccess = false;
            z = "Exceptions" + ex;
        }

        if(!isSuccess) {
            Toast.makeText(OrderDetails.this, z, Toast.LENGTH_SHORT).show();
        }else{
            deletCartRecord();
        }

    }

    private void deletCartRecord() {

        String z = "";
        boolean isSuccess = false;

        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Please check your internet connection";
            } else {

                if (paymentType.equals("E-Wallet")){
                    double totalAmountAfterDeduct = Double.parseDouble(cashHolder) - totalAmountDeduct;

                    String query = "update customer_acc SET cash = '"+ String.format("%.2f", totalAmountAfterDeduct)+ "' where id = " + customerId+";";
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(query);
                }

                String query = "delete from customer_cart where custId = " + customerId + " AND status = 'PendingConfirm' ;";
                String query2 = "delete from order_details where custId = " + customerId + " AND status = 'Confirm' ;";

                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                stmt.executeUpdate(query2);

                z = "Delete Item Successfully";
                isSuccess = true;
            }
        }catch (Exception ex)
        {
            isSuccess = false;
            z = "Exceptions" + ex;
        }

        if(!isSuccess) {
            Toast.makeText(OrderDetails.this, z, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Transaction Made Successfully By " + paymentType, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void getUserDetails(String id) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            Retrofit retrofit = new Retrofit.Builder().client(httpClient.build())
                    .baseUrl(loadCashURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            JsonPlaceHolder jsonPlaceHolderApi = retrofit.create(JsonPlaceHolder.class);

            Call<List<Customer>> listCall = jsonPlaceHolderApi.getUserCash("GET", id);

            listCall.enqueue(new Callback<List<Customer>>() {

                @Override
                public void onResponse(Call<List<Customer>> call, Response<List<Customer>> response) {
                    if (!response.isSuccessful()) {
                        String error = "Code " + response.code();
                        Toast.makeText(OrderDetails.this, error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCustomerList = response.body();
                    if (!mCustomerList.isEmpty() && mCustomerList != null) {
                        userNameHolder = mCustomerList.get(0).getUsername();
                        mTvCustName.setText("Customer Name: "+ userNameHolder);
                        cashHolder = mCustomerList.get(0).getCash();
                    }
                }

                @Override
                public void onFailure(Call<List<Customer>> call, Throwable t) {
                    Toast.makeText(OrderDetails.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void setDateTime() {

        hour = timeString.substring(0, 2);

        if (Integer.parseInt(hour) >= 12)
            amPm = "PM";
        else
            amPm = "AM";

        mTvDateOrdered.setText("Date Ordered: "+ dateString);
        mTvTimeOrdered.setText("Time Ordered: "+ timeString+" " +amPm);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    
    

}