package shindy_works.simplelevel;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import processing.android.PFragment;
import processing.core.PApplet;


public final class MainActivity extends AppCompatActivity implements SensorEventListener {
    private PApplet sketch = null;
    private SensorManager sm = null;
    private Menu menu = null;
    private AdView adView = null;


    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* フルスクリーンモード */
        final int screenFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(screenFlags);

        decorView.setOnSystemUiVisibilityChangeListener((visibility) -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                decorView.setSystemUiVisibility(screenFlags);
        });

        // レイアウトの設定
        setContentView(R.layout.activity_main);

        /* Processingを描画するフレーム */
        final FrameLayout Ppanel = new FrameLayout(this);
        Ppanel.setId(R.id.processingPanel);
        // Processingクラス
        sketch = new Processing_LEVEL_APP();
        //sketch.registerMethod("pre", sketch);
        final PFragment fragment = new PFragment(sketch);
        fragment.setView(Ppanel, this);

        // ActionBarの設定
        setSupportActionBar(findViewById(R.id.toolbar));

        /* センサーの設定 */
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sm.getDefaultSensor(Sensor.TYPE_GRAVITY) == null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.Error))
                    .setMessage(getString(R.string.noSensor))
                    .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {
                        finish();
                    }).show();
        }

        /* 広告設定 */
        MobileAds.initialize(this, getString(R.string.adsAppID));
        adView = findViewById(R.id.adView);

        final AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected final void onResume() {
        super.onResume();
        if (menu != null && menu.findItem(R.id.menuLock) != null) {
            if (!menu.findItem(R.id.menuLock).isChecked())
                startListener();
        } else
            startListener();
        if(adView != null)
            adView.resume();
    }

    @Override
    protected final void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isInPictureInPictureMode())
                stopListener();
        } else
            stopListener();
        if(adView != null)
            adView.pause();
        super.onPause();
    }

    @Override
    public final void onDestroy() {
        if (adView != null)
            adView.destroy();
        super.onDestroy();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        // pip対応機種のみボタン表示
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            menu.removeItem(R.id.menuPIP);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuLock:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(android.R.drawable.ic_media_play);
                    stopListener();
                } else {
                    item.setIcon(android.R.drawable.ic_media_pause);
                    startListener();
                }
                return true;

            case R.id.menuRotation:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(R.drawable.portrait);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    item.setIcon(R.drawable.landscape);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!isInPictureInPictureMode())
                        Processing_LEVEL_APP.setLandscape(item.isChecked());
                } else
                    Processing_LEVEL_APP.setLandscape(item.isChecked());
                return true;

            case R.id.menuPIP:
                Processing_LEVEL_APP.setPipMode(true);
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                builder.setAspectRatio(new Rational(9, 16));
                enterPictureInPictureMode(builder.build());
                return true;

            case R.id.menuPERCENT:
                item.setChecked(!item.isChecked());
                ((Processing_LEVEL_APP) sketch).setShowDegree(item.isChecked());
                return true;

            case R.id.menuMODE1:
                item.setChecked(!item.isChecked());
                Processing_LEVEL_APP.setHmode(item.isChecked());
                Toast.makeText(this, item.getTitle() + getString(R.string.MODE), Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menuMODE2:
                item.setChecked(!item.isChecked());
                Processing_LEVEL_APP.setVmode(item.isChecked());
                Toast.makeText(this, item.getTitle() + getString(R.string.MODE), Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menuMODE3:
                item.setChecked(!item.isChecked());
                Processing_LEVEL_APP.setMmode(item.isChecked());
                Toast.makeText(this, item.getTitle() + getString(R.string.MODE), Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public final void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            findViewById(R.id.toolbar).setVisibility(View.GONE);
            findViewById(R.id.adView).setVisibility(View.GONE);
            Processing_LEVEL_APP.setLandscape(false);
        } else {
            findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            findViewById(R.id.adView).setVisibility(View.VISIBLE);
            Processing_LEVEL_APP.setPipMode(false);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (sketch != null)
            sketch.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected final void onNewIntent(Intent intent) {
        if (sketch != null)
            sketch.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (sketch != null)
            Processing_LEVEL_APP.setAccValue(event.values);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startListener() {
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void stopListener() {
        if (sm != null)
            sm.unregisterListener(this);
    }
}