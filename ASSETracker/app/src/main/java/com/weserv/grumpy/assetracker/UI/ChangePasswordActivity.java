package com.weserv.grumpy.assetracker.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.weserv.grumpy.assetracker.Core.Connection.APIStringRequestFactory;
import com.weserv.grumpy.assetracker.Core.Connection.HttpVolleyRequestSender;
import com.weserv.grumpy.assetracker.Core.Connection.MessageConstants;
import com.weserv.grumpy.assetracker.Core.Connection.RequestResponseCallback;
import com.weserv.grumpy.assetracker.R;
import com.weserv.grumpy.assetracker.Utils.AccountMgmtUtils;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity
        implements RequestResponseCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button submit = (Button) findViewById(R.id.button_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmit();
            }
        });
    }

    public void validateAndSubmit() {
        EditText oldPass = (EditText) findViewById(R.id.text_old_password);
        EditText newPass = (EditText) findViewById(R.id.text_new_password);
        EditText confirmPass = (EditText) findViewById(R.id.text_confirm_password);

        if (!(AccountMgmtUtils.isNotNull(oldPass.getText().toString()) &&
                (AccountMgmtUtils.isNotNull(newPass.getText().toString()) &&
                        (AccountMgmtUtils.isNotNull(confirmPass.getText().toString()))))) {
            Log.d("PASS_VALIDATION", "Empty field(s)");
            clearFields();
            return;
        }

        String hashedOldPass = AccountMgmtUtils.generateHash(oldPass.getText().toString());
        String hashedNewPass = AccountMgmtUtils.generateHash(newPass.getText().toString());
        String hashedConfirmPass = AccountMgmtUtils.generateHash(confirmPass.getText().toString());

        if (passwordMatch(hashedNewPass, hashedConfirmPass)) {
            sendChangePasswordRequest(hashedOldPass, hashedNewPass);
        } else {
            Log.d("PASS_VALIDATION", "Password did not match!");
            Toast.makeText(getApplicationContext(), "Please fill all the fields!", Toast.LENGTH_LONG).show();
            clearFields();
        }

    }

    public boolean passwordMatch(String hashedNewPass, String hashedConfirmPass) {


        if (AccountMgmtUtils.confirmPassword(hashedNewPass, hashedConfirmPass)) {
            Log.d("PASSWORD_VALIDATION", "Match: true");
            return true;
        }
        Log.d("PASSWORD_VALIDATION", "Match: false");
        return false;
    }

    public void sendChangePasswordRequest(String oldPass, String newPass) {
        MyApp app = (MyApp) getApplication();
        String empID = String.valueOf(app.getSession().getUser().getEmployeeID());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("EmployeeID", empID);
        params.put("HashedOldPassword", oldPass);
        params.put("HashedNewPassword", newPass);

        String ip = app.getHostAddress();
        String api = MessageConstants.USERS_API;
        String message = MessageConstants.USERS_MESSAGE_CAT_CHANGE_PASSWORD;
        int callType = Request.Method.PUT;


        APIStringRequestFactory request = new APIStringRequestFactory(ip, api, message, callType);

        request.setParams(params);
        request.registerCallback(this);

        HttpVolleyRequestSender sender = new HttpVolleyRequestSender(this);
        sender.sendRequest(request);
    }

    @Override
    public void onResponse(String response) {
        Log.d("RESPONSE", response);
                if (response.equals("\"SUCCESSFUL\"")) {
            Log.d("CHANGE_PASSWORD", "Successful!");
                    Toast.makeText(getApplicationContext(), "Password Changed!!", Toast.LENGTH_LONG).show();
                    clearFields();
            finish();
        } else {
            Log.d("CHANGE_PASSWORD", "Failed!");
                    Toast.makeText(getApplicationContext(), "Password Change Failed!!", Toast.LENGTH_LONG).show();
                    clearFields();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("CHANGE_PASSWORD", "Failed!");
        Log.d("ERR_MSG", error.getMessage());
        Toast.makeText(getApplicationContext(), "Password Change Failed!!", Toast.LENGTH_LONG).show();
        clearFields();
    }

    public void clearFields(){
        EditText oldPass = (EditText) findViewById(R.id.text_old_password);
        EditText newPass = (EditText) findViewById(R.id.text_new_password);
        EditText confirmPass = (EditText) findViewById(R.id.text_confirm_password);

        oldPass.setText("");
        newPass.setText("");
        confirmPass.setText("");
    }
}
