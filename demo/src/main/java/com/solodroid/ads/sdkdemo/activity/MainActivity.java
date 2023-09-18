package com.solodroid.ads.sdkdemo.activity;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_DISCOVERY;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.FAN;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;
import static com.solodroid.ads.sdkdemo.data.Constant.STYLE_DEFAULT;
import static com.solodroid.ads.sdkdemo.data.Constant.STYLE_NEWS;
import static com.solodroid.ads.sdkdemo.data.Constant.STYLE_RADIO;
import static com.solodroid.ads.sdkdemo.data.Constant.STYLE_VIDEO_LARGE;
import static com.solodroid.ads.sdkdemo.data.Constant.STYLE_VIDEO_SMALL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.AppOpenAd;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdView;
import com.solodroid.ads.sdk.format.RewardedAd;
import com.solodroid.ads.sdk.util.OnRewardedAdCompleteListener;
import com.solodroid.ads.sdk.util.OnRewardedAdDismissedListener;
import com.solodroid.ads.sdk.util.OnRewardedAdErrorListener;
import com.solodroid.ads.sdkdemo.BuildConfig;
import com.solodroid.ads.sdkdemo.R;
import com.solodroid.ads.sdkdemo.data.Constant;
import com.solodroid.ads.sdkdemo.database.SharedPref;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Toolbar toolbar;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    RewardedAd.Builder rewardedAd;
    NativeAd.Builder nativeAd;
    NativeAdView.Builder nativeAdView;
    SwitchMaterial switchMaterial;
    SharedPref sharedPref;
    Button btnInterstitial;
    Button btnRewarded;
    Button btnSelectAds;
    Button btnNativeAdStyle;
    LinearLayout nativeAdViewContainer;
    LinearLayout bannerAdView;
    AppOpenAd.Builder appOpenAdBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = new SharedPref(this);
        getAppTheme();
        setContentView(R.layout.activity_main);

        if (Constant.FORCE_TO_SHOW_APP_OPEN_AD_ON_START) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bannerAdView = findViewById(R.id.banner_ad_view);
        bannerAdView.addView(View.inflate(this, R.layout.view_banner_ad, null));

        initAds();
        loadOpenAds();
        loadBannerAd();
        loadInterstitialAd();
        loadRewardedAd();

        nativeAdViewContainer = findViewById(R.id.native_ad);
        setNativeAdStyle(nativeAdViewContainer);
        loadNativeAd();

        btnInterstitial = findViewById(R.id.btn_interstitial);
        btnInterstitial.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SecondActivity.class));
            showInterstitialAd();
            destroyBannerAd();
        });

        btnRewarded = findViewById(R.id.btn_rewarded);
        btnRewarded.setOnClickListener(view -> showRewardedAd());

        btnSelectAds = findViewById(R.id.btn_select_ads);
        btnSelectAds.setOnClickListener(v -> showAdChooser());

        btnNativeAdStyle = findViewById(R.id.btn_native_ad_style);
        btnNativeAdStyle.setOnClickListener(v -> changeNativeAdStyle());

        switchAppTheme();

    }

    private void initAds() {
        adNetwork = new AdNetwork.Initialize(this)
                .setAdStatus(Constant.AD_STATUS)
                .setAdNetwork(Constant.AD_NETWORK)
                .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                .setAdMobAppId(null)
                .setAppLovinSdkKey(getResources().getString(R.string.applovin_sdk_key))
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    private void loadOpenAds() {
        if (Constant.OPEN_ADS_ON_RESUME) {
            appOpenAdBuilder = new AppOpenAd.Builder(this)
                    .setAdStatus(Constant.AD_STATUS)
                    .setAdNetwork(Constant.AD_NETWORK)
                    .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                    .setAdMobAppOpenId(Constant.ADMOB_APP_OPEN_AD_ID)
                    .setAdManagerAppOpenId(Constant.GOOGLE_AD_MANAGER_APP_OPEN_AD_ID)
                    .setApplovinAppOpenId(Constant.APPLOVIN_APP_OPEN_AP_ID)
                    .build();
        }
    }

    LifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Constant.OPEN_ADS_ON_RESUME) {
                    if (AppOpenAd.isAppOpenAdLoaded) {
                        appOpenAdBuilder.show();
                    }
                }
            }, 100);
        }
    };

    private void loadBannerAd() {
        bannerAd = new BannerAd.Builder(this)
                .setAdStatus(Constant.AD_STATUS)
                .setAdNetwork(Constant.AD_NETWORK)
                .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                .setAdMobBannerId(Constant.ADMOB_BANNER_ID)
                .setGoogleAdManagerBannerId(Constant.GOOGLE_AD_MANAGER_BANNER_ID)
                .setFanBannerId(Constant.FAN_BANNER_ID)
                .setAppLovinBannerId(Constant.APPLOVIN_BANNER_ID)
                .setAppLovinBannerZoneId(Constant.APPLOVIN_BANNER_ZONE_ID)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .build();
    }

    private void loadInterstitialAd() {
        interstitialAd = new InterstitialAd.Builder(this)
                .setAdStatus(Constant.AD_STATUS)
                .setAdNetwork(Constant.AD_NETWORK)
                .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                .setAdMobInterstitialId(Constant.ADMOB_INTERSTITIAL_ID)
                .setGoogleAdManagerInterstitialId(Constant.GOOGLE_AD_MANAGER_INTERSTITIAL_ID)
                .setFanInterstitialId(Constant.FAN_INTERSTITIAL_ID)
                .setAppLovinInterstitialId(Constant.APPLOVIN_INTERSTITIAL_ID)
                .setAppLovinInterstitialZoneId(Constant.APPLOVIN_INTERSTITIAL_ZONE_ID)
                .setInterval(Constant.INTERSTITIAL_AD_INTERVAL)
                .build(() -> {

                    Toast.makeText(this, "Hello From load******", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "onAdDismissed");
                });
    }

    private void loadRewardedAd() {
        rewardedAd = new RewardedAd.Builder(this)
                .setAdStatus(Constant.AD_STATUS)
                .setMainAds(Constant.AD_NETWORK)
                .setBackupAds(Constant.BACKUP_AD_NETWORK)
                .setAdMobRewardedId(Constant.ADMOB_REWARDED_ID)
                .setAdManagerRewardedId(Constant.GOOGLE_AD_MANAGER_REWARDED_ID)
                .setFanRewardedId(Constant.FAN_REWARDED_ID)
                .setApplovinMaxRewardedId(Constant.APPLOVIN_MAX_REWARDED_ID)
                .setApplovinDiscRewardedZoneId(Constant.APPLOVIN_DISC_REWARDED_ZONE_ID)
                .build(new OnRewardedAdCompleteListener() {
                    @Override
                    public void onRewardedAdComplete() {
                        Toast.makeText(getApplicationContext(), "Rewarded complete", Toast.LENGTH_SHORT).show();
                    }
                }, new OnRewardedAdDismissedListener() {
                    @Override
                    public void onRewardedAdDismissed() {

                    }
                });
    }

    private void showRewardedAd() {
        rewardedAd.show(new OnRewardedAdCompleteListener() {
            @Override
            public void onRewardedAdComplete() {
                Toast.makeText(getApplicationContext(), "Rewarded complete", Toast.LENGTH_SHORT).show();
            }
        }, new OnRewardedAdDismissedListener() {
            @Override
            public void onRewardedAdDismissed() {

            }
        }, new OnRewardedAdErrorListener() {
            @Override
            public void onRewardedAdError() {
                Toast.makeText(getApplicationContext(), "Rewarded error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInterstitialAd() {
        interstitialAd.show(() -> {
            Log.d(TAG, "onAdShowed");
        }, () -> {
            Toast.makeText(this, "Hello From showwwwwwwwwwww", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onAdDismissed");
        });

    }

    private void loadNativeAd() {
        nativeAd = new NativeAd.Builder(this)
                .setAdStatus(Constant.AD_STATUS)
                .setAdNetwork(Constant.AD_NETWORK)
                .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                .setAdMobNativeId(Constant.ADMOB_NATIVE_ID)
                .setAdManagerNativeId(Constant.GOOGLE_AD_MANAGER_NATIVE_ID)
                .setFanNativeId(Constant.FAN_NATIVE_ID)
                .setAppLovinNativeId(Constant.APPLOVIN_NATIVE_MANUAL_ID)
                .setAppLovinDiscoveryMrecZoneId(Constant.APPLOVIN_BANNER_MREC_ZONE_ID)
                .setNativeAdStyle(Constant.NATIVE_STYLE)
                .setNativeAdBackgroundColor(R.color.colorNativeBackgroundLight, R.color.colorNativeBackgroundDark)
                .setPadding(0, 0, 0, 0)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .build(()-> {
                    Toast.makeText(this, "Native event *****", Toast.LENGTH_LONG).show();
                });
    }

    private void loadNativeAdView(View view) {
        nativeAdView = new NativeAdView.Builder(this)
                .setAdStatus(Constant.AD_STATUS)
                .setAdNetwork(Constant.AD_NETWORK)
                .setBackupAdNetwork(Constant.BACKUP_AD_NETWORK)
                .setAdMobNativeId(Constant.ADMOB_NATIVE_ID)
                .setAdManagerNativeId(Constant.GOOGLE_AD_MANAGER_NATIVE_ID)
                .setFanNativeId(Constant.FAN_NATIVE_ID)
                .setAppLovinNativeId(Constant.APPLOVIN_NATIVE_MANUAL_ID)
                .setAppLovinDiscoveryMrecZoneId(Constant.APPLOVIN_BANNER_MREC_ZONE_ID)
                .setNativeAdStyle(Constant.NATIVE_STYLE)
                .setNativeAdBackgroundColor(R.color.colorNativeBackgroundLight, R.color.colorNativeBackgroundDark)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setView(view)
                .build();

        nativeAdView.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyBannerAd();
        destroyAppOpenAd();
        Constant.isAppOpen = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        bannerAd.loadBannerAd();
    }

    public void getAppTheme() {
        if (sharedPref.getIsDarkTheme()) {
            setTheme(R.style.AppDarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void switchAppTheme() {
        switchMaterial = findViewById(R.id.switch_theme);
        switchMaterial.setChecked(sharedPref.getIsDarkTheme());
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setIsDarkTheme(isChecked);
            recreate();
        });
    }

    private void showAdChooser() {
        final String[] ads = {"AdMob", "Google Ad Manager", "AppLovin MAX", "AppLovin Discovery", "FAN (Waterfall)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Ad");
        builder.setItems(ads, (dialog, which) -> {
            String selectedItem = ads[which];
            switch (selectedItem) {
                case "AdMob":
                    Constant.AD_NETWORK = ADMOB;
                    break;
                case "Google Ad Manager":
                    Constant.AD_NETWORK = GOOGLE_AD_MANAGER;
                    break;

                case "AppLovin MAX":
                    Constant.AD_NETWORK = APPLOVIN_MAX;
                    break;
                case "AppLovin Discovery":
                    Constant.AD_NETWORK = APPLOVIN_DISCOVERY;
                    break;

                case "FAN (Waterfall)":
                    Constant.AD_NETWORK = FAN;
                    break;

                default:
                    Constant.AD_NETWORK = ADMOB;
                    break;
            }
            recreate();
        });
        builder.show();
    }

    private void setNativeAdStyle(LinearLayout nativeAdView) {
        switch (Constant.NATIVE_STYLE) {
            case "news":
                nativeAdView.addView(View.inflate(this, R.layout.view_native_ad_news, null));
                break;
            case "radio":
                nativeAdView.addView(View.inflate(this, R.layout.view_native_ad_radio, null));
                break;
            case "video_small":
                nativeAdView.addView(View.inflate(this, R.layout.view_native_ad_video_small, null));
                break;
            case "video_large":
                nativeAdView.addView(View.inflate(this, R.layout.view_native_ad_video_large, null));
                break;
            default:
                nativeAdView.addView(View.inflate(this, R.layout.view_native_ad_medium, null));
                break;
        }
    }

    private void changeNativeAdStyle() {
        final String[] styles = {"Default", "News", "Radio", "Video Small", "Video Large"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Native Style");
        builder.setItems(styles, (dialog, which) -> {
            String selectedItem = styles[which];
            switch (selectedItem) {
                case "Default":
                    Constant.NATIVE_STYLE = STYLE_DEFAULT;
                    break;
                case "News":
                    Constant.NATIVE_STYLE = STYLE_NEWS;
                    break;
                case "Radio":
                    Constant.NATIVE_STYLE = STYLE_RADIO;
                    break;
                case "Video Small":
                    Constant.NATIVE_STYLE = STYLE_VIDEO_SMALL;
                    break;
                case "Video Large":
                    Constant.NATIVE_STYLE = STYLE_VIDEO_LARGE;
                    break;
                default:
                    Constant.NATIVE_STYLE = STYLE_DEFAULT;
                    break;
            }
            recreate();
        });
        builder.show();
    }

    private void showExitDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_exit, null);

        LinearLayout nativeAdViewContainer = view.findViewById(R.id.native_ad_view);
        setNativeAdStyle(nativeAdViewContainer);
        loadNativeAdView(view);

        AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Exit", (dialogInterface, i) -> {
            super.onBackPressed();
            destroyBannerAd();
            destroyAppOpenAd();
            Constant.isAppOpen = false;
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }

    private void destroyBannerAd() {
        bannerAd.destroyAndDetachBanner();
    }

    private void destroyAppOpenAd() {
        if (Constant.FORCE_TO_SHOW_APP_OPEN_AD_ON_START) {
            appOpenAdBuilder.destroyOpenAd();
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(lifecycleObserver);
        }
    }

}