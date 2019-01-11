package com.instamaven.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.facebook.drawee.view.SimpleDraweeView;
import com.instamaven.app.R;
import com.instamaven.app.adapters.PackagesListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.models.PackageEntity;
import com.instamaven.app.providers.PackageProvider;
import com.instamaven.app.utils.ProfileHelper;
import com.instamaven.app.utils.RequestClient;
import com.instamaven.app.utils.SelectedFile;
import com.instamaven.app.utils.SettingsHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static int REQUEST_CODE = 110;

    private final static int SEND_PAYMENT = 1;
    private final static int SEND_WITHDRAW = 2;

    private final static int ACTION_GET_CONTENT = 3;

    private PackageProvider packageProvider;
    private ArrayList<Entity> packages;
    protected PackagesListAdapter adapter;

    private BraintreeFragment mBraintreeFragment;
    private String coinsWithdraw = null, mAuthorization, msg;
    protected RequestClient.Builder client;
    protected PackageEntity choosenPackage = null;
    private Handler mHandler;

    public ImageButton rechargeBtn, withdrawBtn;
    private TextView userName, userEmail, balanceTV, statusTV;
    private Switch switchStatus;
    private Button myBadgeBtn, notificationBtn, activitiesBtn, chatlogsBtn, settingsBtn, positiveBtn, negativeBtn, addImageBtn;
    private int withdraw_min = 0, withdraw_max = 0;
    private ConstraintLayout balanceConstraintLayout;
    private Intent i;
    private Uri uri, resultUri;
    private SimpleDraweeView draweeView;
    private Object file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_new);

        mHandler = new MessageHandler();
        client = new RequestClient.Builder(this)
                .addHeader("Accept", "application/json");
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        JSONObject myProfile = ProfileHelper.getUser(this);
        try {
            // extract user info
            final JSONObject user = (JSONObject) myProfile.get("user");
            if (user == JSONObject.NULL) {
                throw new JSONException(getString(R.string.profile_corrupted));
            }
            JSONObject social = null;
            if (myProfile.get("social") != JSONObject.NULL) {
                social = (JSONObject) myProfile.get("social");
            }
            JSONObject profile = null;
            if (myProfile.get("profile") != JSONObject.NULL) {
                profile = (JSONObject) myProfile.get("profile");
            }

            ImageView iconLoggedWith = findViewById(R.id.loggedWith);
            SimpleDraweeView draweeView = findViewById(R.id.userAvatar);
            draweeView.setImageResource(R.drawable.ic_no_ava);

            if (profile != null && social != null) {
                String imgSrc = "";
                if (profile.opt("avatar") != JSONObject.NULL) {
                    imgSrc = profile.optString("avatar");
                }
                if (imgSrc.equals("") && social.opt("avatar") != JSONObject.NULL) {
                    imgSrc = social.optString("avatar");
                }
                if (!imgSrc.equals("")) {
                    Uri uri = Uri.parse(imgSrc);
                    draweeView.setImageURI(uri);
                }
                switch (social.getString("provider")) {
                    case "facebook":
                        iconLoggedWith.setImageResource(R.drawable.ic_facebook);
                        break;
                    case "twitter":
                        iconLoggedWith.setImageResource(R.drawable.ic_twitter);
                        break;
                    case "google":
                        iconLoggedWith.setImageResource(R.drawable.ic_google);
                        break;
                    case "user":
                    default:
                        iconLoggedWith.setImageResource(R.drawable.ic_instamaven_logo_round);
                        break;
                }
            }

            addImageBtn = findViewById(R.id.addImageBtn);
            addImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setRequestedSize(400, 400)
                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setOutputCompressQuality(50)
                            .start(ProfileActivity.this);
                }
            });

            userName = findViewById(R.id.userName);
            userName.setText(user.getString("name"));

            userEmail = findViewById(R.id.userEmail);
            userEmail.setText(user.getString("email"));

            balanceTV = findViewById(R.id.userBalance);
            balanceTV.setText(IMApp.getBalance(this));

            statusTV = findViewById(R.id.statusTV);
            statusTV.setText(IMApp.getVisibility(this));

            switchStatus = findViewById(R.id.switchStatus);
            switchStatus.setChecked(IMApp.visibility == 1);
            switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean checked) {
                    IMApp.visibility = checked ? 1 : 0;
                    ProfileHelper.setVisibility(ProfileActivity.this, IMApp.visibility);
                    recreate();
                }
            });

            // Button For Recharge Wallet
            rechargeBtn = findViewById(R.id.rechargeBtn);
            rechargeBtn.setOnClickListener(new View.OnClickListener() {
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosenPackage = (PackageEntity) adapter.getItem(which);
                        Thread thread = new ThreadSetPayment();
                        thread.start();
                    }
                };

                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
                    rechargeBtn.startAnimation(anim);

                    View view = getLayoutInflater().inflate(R.layout.package_list, null);
                    ListView packageListView = view.findViewById(R.id.packageListView);
                    packageProvider = new PackageProvider(ProfileActivity.this, packageListView);
                    if ((packages = packageProvider.getData()) != null) {
                        adapter = new PackagesListAdapter(ProfileActivity.this, packages);
                        new AlertDialog.Builder(ProfileActivity.this)
                                .setTitle(getString(R.string.choose_package))
                                //.setView(R.layout.package_list)
                                .setAdapter(adapter, clickListener)
                                .setCancelable(false)
                                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                })
                                .show();
                    }
                }
            });

            // Button For Withdraw Coins
            withdrawBtn = findViewById(R.id.withdrawBtn);
            withdrawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
                    withdrawBtn.startAnimation(anim);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.withdraw_coins_form_item, null);

                    builder.setCancelable(false);
                    builder.setView(dialogView);

                    positiveBtn = dialogView.findViewById(R.id.positiveBtn);
                    negativeBtn = dialogView.findViewById(R.id.negativeBtn);
                    final EditText withdrawEditText = dialogView.findViewById(R.id.withdrawEditText);
                    try {
                        JSONObject settings = (JSONObject) IMApp.myProfile.get("settings");
                        withdraw_min = settings.getInt("withdraw_min");
                        withdraw_max = settings.getInt("withdraw_max");
                    } catch (JSONException e) {
//                        e.printStackTrace();
                    }
                    msg = String.format(Locale.getDefault(), getString(R.string.min_withdraw_coins), withdraw_min, withdraw_max);
                    withdrawEditText.setHint(msg);

                    final AlertDialog dialog = builder.create();

                    positiveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            coinsWithdraw = withdrawEditText.getText().toString();
                            if (coinsWithdraw.isEmpty()) {
                                Toast.makeText(ProfileActivity.this, R.string.empty_error, Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (Integer.parseInt(coinsWithdraw) < withdraw_min || Integer.parseInt(coinsWithdraw) > withdraw_max) {
                                String msg = String.format(getString(R.string.withdraw_error), withdraw_min, withdraw_max);
                                Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
                                return;
                            }
                            dialog.cancel();
                            Thread thread = new ThreadSetWithdraw();
                            thread.start();
                        }
                    });

                    negativeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String btn = extras.getString("autoClick", "");
            switch (btn) {
                case "rechargeBtn":
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rechargeBtn.performClick();
                        }
                    }, 10);
                    break;
                case "withdrawBtn":
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            withdrawBtn.performClick();
                        }
                    }, 10);
                    break;
            }
        }

        // Button My Badge
        myBadgeBtn = findViewById(R.id.badgesBtn);
        myBadgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, MyBadgeListActivity.class);
                startActivity(i);
            }
        });

        // Button Favorites
        myBadgeBtn = findViewById(R.id.favoritesBtn);
        myBadgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, FavoritesBadgeActivity.class);
                startActivity(i);
            }
        });

        // Button Notifications
        notificationBtn = findViewById(R.id.notificationsBtn);
        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, NotificationsActivity.class);
                startActivity(i);
            }
        });

        // Button Activities
        activitiesBtn = findViewById(R.id.activitiesBtn);
        activitiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, ActivitiesTabsActivity.class);
                startActivity(i);
            }
        });

        // Button Chatlogs
        chatlogsBtn = findViewById(R.id.chatlogsBtn);
        chatlogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, ChatLogsTabsActivity.class);
                startActivity(i);
            }
        });

        // Button Settings
        settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
    }


    protected class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Context context = getApplicationContext();
            switch (msg.what) {
                case SEND_PAYMENT:
                    if (choosenPackage == null) {
                        return;
                    }
                    try {
                        // reset payment token
                        String response = new RequestClient.Builder(context)
                                .addHeader("Accept", "application/json")
                                .setUrl(SettingsHelper.getUrl(context, "get_payment_token"))
                                .setMethod("GET")
                                .send();
                        JSONObject obj = new JSONObject(response);
                        if (obj.opt("error") != null) {
                            String message = obj.optString("message");
                            if (message.isEmpty()) {
                                message = "Error: " + obj.opt("status");
                            }
                            throw new Exception(message);
                        } else {
                            JSONObject data = (JSONObject) obj.get("data");
                            IMApp.paymentToken = data.getString("token");
                            DropInRequest dropInRequest = new DropInRequest()
                                    .clientToken(IMApp.paymentToken);
                            startActivityForResult(dropInRequest.getIntent(context), REQUEST_CODE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
                case SEND_WITHDRAW:
                    if (coinsWithdraw == null) {
                        return;
                    }
                    try {
                        //
                        String response = new RequestClient.Builder(context)
                                .addHeader("Accept", "application/json")
                                .setUrl(SettingsHelper.getUrl(context, "post_withdraw"))
                                .setMethod("POST")
                                .addField("coins", coinsWithdraw)
                                .send();
                        JSONObject obj = new JSONObject(response);
                        if (obj.opt("error") != null) {
                            String message = obj.optString("message");
                            if (message.isEmpty()) {
                                message = "Error: " + obj.opt("status");
                            }
                            throw new Exception(message);
                        }
                        new AlertDialog.Builder(ProfileActivity.this)
                                .setTitle(getString(R.string.alarm_title_complete))
                                .setMessage(getString(R.string.withdraw_completed))
                                .setIcon(R.drawable.ic_complete_round)
                                .setCancelable(false)
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        reloadActivity();
                                    }
                                }).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    public class ThreadSetPayment extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(SEND_PAYMENT);
        }
    }

    public class ThreadSetWithdraw extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(SEND_WITHDRAW);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String message;
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK && choosenPackage != null) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                try {
                    String nonce = result.getPaymentMethodNonce().getNonce();
                    String packageId = choosenPackage.getInt("id").toString();
                    choosenPackage = null;
                    // make payment
                    String response = client
                            .setUrl(SettingsHelper.getUrl(this, "post_payment_create"))
                            .setMethod("POST")
                            .addField("nonce", nonce)
                            .addField("package", packageId)
                            .send();
                    JSONObject obj = new JSONObject(response);
                    if (obj.opt("error") != null) {
                        message = obj.optString("message");
                        if (message.isEmpty()) {
                            message = "Error: " + obj.opt("status");
                        }
                    } else {
                        message = getString(R.string.payment_completed);
                    }
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle(getString(R.string.payment_transaction))
                            .setMessage(message)
                            .setIcon(R.drawable.ic_complete_round)
                            .setCancelable(false)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    reloadActivity();
                                }
                            }).show();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                try {
                    String response = client
                            .setUrl(SettingsHelper.getUrl(this, "post_create_avatar"))
                            .setMethod("POST")
                            .addFile("image", new SelectedFile(getContentResolver(), resultUri))
                            .send();
                    JSONObject obj = new JSONObject(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                reloadActivity();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void reloadActivity() {
        JSONObject obj = null;
        try {
            String response = new RequestClient.Builder(this)
                    .addHeader("Accept", "application/json")
                    .setUrl(SettingsHelper.getUrl(this, "get_profile"))
                    .setMethod("GET")
                    .send();
            obj = new JSONObject(response);
            if (obj.opt("data") != null) {
                ProfileHelper.saveUser(this, (JSONObject) obj.get("data"));
                Intent refresh = new Intent(this, ProfileActivity.class);
                startActivity(refresh);
                this.finish();
            }
        } catch (Exception e) {
            String message = obj.optString("message");
            if (message.isEmpty()) {
                message = "Error: " + obj.opt("status");
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}

