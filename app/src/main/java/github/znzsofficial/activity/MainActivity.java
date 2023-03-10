package github.znzsofficial.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.inputmethod.InputBinding;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.itsaky.androidide.logsender.LogSender;
import com.permissionx.guolindev.PermissionX;
import github.znzsofficial.test.databinding.ActivityMainBinding;
import github.znzsofficial.test.R;
import github.znzsofficial.utils.MediaPlayerUtil;
import github.znzsofficial.utils.TimeUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public ActivityMainBinding mBinding;

  MediaPlayerUtil mediaPlayerUtil = new MediaPlayerUtil();

  MediaMetadataRetriever mmr = new MediaMetadataRetriever();

  RequestOptions options =
      new RequestOptions()
          // .placeholder(R.drawable.ic_launcher_background)
          // .error(R.drawable.ic_launcher_background)
          .skipMemoryCache(true)
          .diskCacheStrategy(DiskCacheStrategy.NONE);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // AndroidIDE logsender
    LogSender.startLogging(this);
    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    // Setup toolbar
    setSupportActionBar(mBinding.toolbar);
    // set content view to binding's root
    setContentView(mBinding.getRoot());
    /* ???????????? */
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      getPermissionX();
    }
    mediaPlayerUtil.init();
    mediaPlayerUtil.setOnMediaStateListener(mediaListner());
    initView();
    // ????????????????????????
    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  private String getBitRate(String origin) {
    if (origin == null) {
      return "????????????";
    } else {
      Long Lbitrate = Long.valueOf(origin) / 1000;
      return Lbitrate.toString() + "kbps";
    }
  }
  ;

  private String getBitsDeep(String origin) {
    if (origin == null) {
      return "????????????";
    } else {
      return origin + "bit";
    }
  }
  ;

  private String getSampleRate(String origin) {
    if (origin == null) {
      return "????????????";
    } else {
      return origin + "hz";
    }
  }
  ;

  private String getAudioInfo() {

    String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
    String mimetype = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
    String originSampleRate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE);
    String originBitsDeep =
        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE);
    String orginBitRate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
    String composer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
    String errorText = "????????????";

    title = (title == null) ? errorText : title;
    album = (album == null) ? errorText : album;
    artist = (artist == null) ? errorText : artist;
    author = (author == null) ? errorText : author;
    mimetype = (mimetype == null) ? errorText : mimetype;
    composer = (composer == null) ? errorText : composer;

    String info =
        "?????????"
            + title
            + "\n?????????"
            + album
            + "\n????????????"
            + artist
            + "\n?????????"
            + author
            + "\n????????????"
            + composer
            + "\n?????????"
            + getBitsDeep(originBitsDeep)
            + "\n????????????"
            + getSampleRate(originSampleRate)
            + "\n?????????"
            + getBitRate(orginBitRate)
            + "\n???????????????"
            + mimetype;
    return info;
  }
  ;

  private void getPermission() {
    List<String> permissionLists = new ArrayList<>();
    // ????????????????????????
    // ????????????(??????11???????????????????????????)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        // Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        // startActivity(intent.setData(Uri.parse("package:" + getPackageName())));
        // alertDialogShow();
      }
    } else {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        permissionLists.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        // ActivityCompat.requestPermissions(this,new
        // String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
      }
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        permissionLists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // ActivityCompat.requestPermissions(this,new
        // String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
      }
    }
    // ????????????
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED) {
      permissionLists.add(Manifest.permission.READ_PHONE_STATE);
      // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},
      // 1);
    }
    // ???????????? Android 10
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
          != PackageManager.PERMISSION_GRANTED)
        permissionLists.add(Manifest.permission.FOREGROUND_SERVICE);
    }
    // requestCode = 1 ????????????
    if (!permissionLists.isEmpty()) {
      ActivityCompat.requestPermissions(this, permissionLists.toArray(new String[0]), 1);
    }
  }

  public void initView() {
    ExtendedFloatingActionButton efab = mBinding.fab;
    efab.setOnClickListener(
        v -> {
          try {

            mediaPlayerUtil.stop();

            File file = new File(mBinding.mEdit.getText().toString());
            if (file.exists()) {
              String FileName = file.getAbsolutePath();
              mmr.setDataSource(FileName);

              byte[] pic = mmr.getEmbeddedPicture();

              // load icon with glide
              try {
                Glide.with(this).asDrawable().load(pic).apply(options).into(mBinding.mImg);
              } catch (Exception e) {
              }
              // play audio
              try {
                mediaPlayerUtil.prepare(FileName);
                if (mBinding.loopSwitch.isChecked()) {
                  mediaPlayerUtil.getPlayer().setLooping(true);
                } else {
                  mediaPlayerUtil.getPlayer().setLooping(false);
                }
                ;
              } catch (IOException e) {
                e.printStackTrace();
              }

            } else {
              Snackbar.make(mBinding.mContent, "???????????????", Snackbar.LENGTH_LONG).show();
            }
          } catch (Exception e) {
            // e.printStackTrace();
            Snackbar.make(mBinding.mContent, "????????????", Snackbar.LENGTH_LONG).show();
          }
        });

    mBinding.btnPlay.setOnClickListener(
        v -> {
          if (mediaPlayerUtil.getPlayer().isPlaying()) {
            mediaPlayerUtil.pause();
          } else {
            mediaPlayerUtil.start();
          }
        });
    mBinding.btnInfo.setOnClickListener(
        v -> {
          new MaterialAlertDialogBuilder(MainActivity.this)
              .setTitle("????????????")
              .setMessage(getAudioInfo())
              .setPositiveButton("??????", null)
              .show();
        });

    mBinding.loopSwitch.setOnCheckedChangeListener(
        (CompoundButton buttonView, boolean isChecked) -> {
          if (buttonView.isChecked()) {
            mediaPlayerUtil.getPlayer().setLooping(true);
          } else {
            mediaPlayerUtil.getPlayer().setLooping(false);
          }
        });
  }

  private MediaPlayerUtil.OnMediaStateListener mediaListner() {
    MediaPlayerUtil.OnMediaStateListener listner =
        new MediaPlayerUtil.OnMediaStateListener() {
          @Override
          public void onPrepared() {
            // ??????????????????,???????????????????????????????????????
            mediaPlayerUtil
                .getPlayer()
                .setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayerUtil.start();
          }

          @Override
          public void onSeekUpdate(int curTimeInt) {
            // ???????????????
            String time = TimeUtil.main(String.valueOf(curTimeInt));
            String allTime =
                TimeUtil.main(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mBinding.mTextState.setText("???????????????" + time + "/" + allTime);
          }

          @Override
          public void onCompletion() {
            // ????????????
            mBinding.mTextState.setText("???????????????????????????");
          }

          @Override
          public boolean onError() {
            mediaPlayerUtil.reset();
            // ??????UI??????
            return true;
          }
        };
    return listner;
  }

  /** * ???????????????????????? */
  private void getPermissionX() {
    ArrayList<String> permissionLists =
        new ArrayList<>(); /* ?????????????????? */ /* ????????????????????????, ????????????11??????????????????????????????, ????????????????????? */
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        permissionLists.add(Manifest.permission.READ_EXTERNAL_STORAGE);
      }
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        permissionLists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
      }
    } else {
      if (!Environment.isExternalStorageManager())
        permissionLists.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
    } /* ??????????????????(??????) */
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED)
      permissionLists.add(Manifest.permission.READ_PHONE_STATE); /* ??????PermissionX???????????? */
    PermissionX.init(this).permissions(permissionLists);
  }
  /** ???????????????????????? * */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mediaPlayerUtil.getPlayer() != null) {
      mediaPlayerUtil.stop();
      // mediaPlayerUtil.release();
      mediaPlayerUtil.onDestroy();
    }
    if (mmr != null) {
      try {
        mmr.release();
        mmr.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
