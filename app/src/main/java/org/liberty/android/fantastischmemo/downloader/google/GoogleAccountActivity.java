/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.google;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.BaseActivity;

import java.io.IOException;

public class GoogleAccountActivity extends BaseActivity {

    private static final String TAG = GoogleAccountActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 100;

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth2_account_activity);

        if (!activityComponents().googlePlayUtil().checkPlayServices(RC_SIGN_IN)) {
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        activityComponents().errorUtil().showFatalError("Connection failure: " + connectionResult.getErrorMessage(), null);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result == null) {
                activityComponents().errorUtil().showFatalError("Null google sign in result", null);
                return;
            }
            if (!result.isSuccess()) {
                activityComponents().errorUtil().showFatalError("Google sign in is not successful", null);
                return;
            }

            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct == null) {
                activityComponents().errorUtil().showFatalError("Getting null Google sign in account", null);
                return;
            }

            // Get account information
            final String email = acct.getEmail();

            appComponents().executorService().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = GoogleAuthUtil.getToken(GoogleAccountActivity.this,
                                email, AMEnv.GDRIVE_SCOPE);
                        onAuthenticated(token);
                    } catch (IOException e) {
                        activityComponents().errorUtil().showFatalError("IO Error", e);
                    } catch (UserRecoverableAuthException e) {
                        startActivityForResult(e.getIntent(), RC_SIGN_IN);
                    } catch (GoogleAuthException e) {
                        activityComponents().errorUtil().showFatalError("GoogleAuthException", e);
                    }
                }
            });
        }
    }

    private void onAuthenticated(@NonNull String token) {
        finish();
        Intent intent = new Intent(this, SpreadsheetListScreen.class);
        intent.putExtra(SpreadsheetListScreen.EXTRA_AUTH_TOKEN, token);
        startActivity(intent);
    }
}

