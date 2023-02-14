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
    /* 获取权限 */
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      getPermissionX();
    }
    mediaPlayerUtil.init();
    mediaPlayerUtil.setOnMediaStateListener(mediaListner());
    initView();
    // 默认调节媒体音量
    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  private String getBitRate(String origin) {
    if (origin == null) {
      return "获取失败";
    } else {
      Long Lbitrate = Long.valueOf(origin) / 1000;
      return Lbitrate.toString() + "kbps";
    }
  }
  ;

  private String getBitsDeep(String origin) {
    if (origin == null) {
      return "获取失败";
    } else {
      return origin + "bit";
    }
  }
  ;

  private String getSampleRate(String origin) {
    if (origin == null) {
      return "获取失败";
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
    String errorText = "获取失败";

    title = (title == null) ? errorText : title;
    album = (album == null) ? errorText : album;
    artist = (artist == null) ? errorText : artist;
    author = (author == null) ? errorText : author;
    mimetype = (mimetype == null) ? errorText : mimetype;
    composer = (composer == null) ? errorText : composer;

    String info =
        "标题："
            + title
            + "\n专辑："
            + album
            + "\n艺术家："
            + artist
            + "\n作者："
            + author
            + "\n作曲家："
            + composer
            + "\n位深："
            + getBitsDeep(originBitsDeep)
            + "\n采样率："
            + getSampleRate(originSampleRate)
            + "\n码率："
            + getBitRate(orginBitRate)
            + "\n文件类型："
            + mimetype;
    return info;
  }
  ;

  private void getPermission() {
    List<String> permissionLists = new ArrayList<>();
    // 添加权限获取弹窗
    // 存储权限(安卓11特性：管理所有文件)
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
    // 电话权限
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED) {
      permissionLists.add(Manifest.permission.READ_PHONE_STATE);
      // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},
      // 1);
    }
    // 前台服务 Android 10
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
          != PackageManager.PERMISSION_GRANTED)
        permissionLists.add(Manifest.permission.FOREGROUND_SERVICE);
    }
    // requestCode = 1 开启权限
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
              Snackbar.make(mBinding.mContent, "文件不存在", Snackbar.LENGTH_LONG).show();
            }
          } catch (Exception e) {
            // e.printStackTrace();
            Snackbar.make(mBinding.mContent, "读取失败", Snackbar.LENGTH_LONG).show();
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
              .setTitle("音频信息")
              .setMessage(getAudioInfo())
              .setPositiveButton("确定", null)
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
            // 音频加载完成,有加载进度条的可以在这取消
            mediaPlayerUtil
                .getPlayer()
                .setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayerUtil.start();
          }

          @Override
          public void onSeekUpdate(int curTimeInt) {
            // 更新进度条
            String time = TimeUtil.main(String.valueOf(curTimeInt));
            String allTime =
                TimeUtil.main(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mBinding.mTextState.setText("播放状态：" + time + "/" + allTime);
          }

          @Override
          public void onCompletion() {
            // 播放完成
            mBinding.mTextState.setText("播放状态：播放完成");
          }

          @Override
          public boolean onError() {
            mediaPlayerUtil.reset();
            // 重置UI状态
            return true;
          }
        };
    return listner;
  }

  /** * 获取存储访问权限 */
  private void getPermissionX() {
    ArrayList<String> permissionLists =
        new ArrayList<>(); /* 添加所需权限 */ /* 外部存储管理权限, 大于安卓11需要申请管理所有权限, 旧权限已不适用 */
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
    } /* 查看手机状态(可选) */
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED)
      permissionLists.add(Manifest.permission.READ_PHONE_STATE); /* 使用PermissionX获取权限 */
    PermissionX.init(this).permissions(permissionLists);
  }
  /** 获取存储访问权限 * */
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
