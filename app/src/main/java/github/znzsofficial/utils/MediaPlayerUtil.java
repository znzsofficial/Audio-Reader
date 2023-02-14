package github.znzsofficial.utils;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaPlayerUtil {

  private static final String TAG = MediaPlayerUtil.class.getSimpleName();

  public MediaPlayerUtil() {}

  private static final int HANDLER_PLAY_TIME = 1;
  // 进度条状态刷新时间间隔
  private static final int TIMER_SEEK = 20;

  // 播放器
  private MediaPlayer player;

  private Handler mUiHandler; // 刷新ui
  private Handler mTimerHandler; // 定时轮询：更新seekBar
  private Runnable timerRun; // 定时任务

  private MediaPlayStatus status = MediaPlayStatus.IDLE;

  private ExecutorService singleExecutor;

  public void init() {
    singleExecutor = Executors.newSingleThreadExecutor();
    initData();
    initPlay();
  }

  private void initData() {
    mUiHandler =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 更新当前时间
            try {
              int curP = player.getCurrentPosition();
              if (mListener != null) {
                mListener.onSeekUpdate(curP);
              }
            } catch (Exception e) {
              Log.i(TAG, "handleMessage: e = " + e);
              reset();
            }
          }
        };

    HandlerThread handlerThread = new HandlerThread("media_timer");
    handlerThread.start();
    mTimerHandler = new Handler(handlerThread.getLooper());
    timerRun =
        new Runnable() {
          @Override
          public void run() {
            mTimerHandler.postDelayed(this, TIMER_SEEK);
            Message msg = Message.obtain();
            msg.what = HANDLER_PLAY_TIME;
            mUiHandler.sendMessage(msg);
          }
        };
  }

  private void initPlay() {
    Log.i(TAG, "initPlay: status = " + status);
    player = new MediaPlayer();
    status = MediaPlayStatus.IDLE;

    // 当流媒体播放完毕的时候回调。
    player.setOnCompletionListener(
        mediaPlayer -> {
          Log.i(TAG, "OnCompletion: status = " + status);
          stopTimer();
          status = MediaPlayStatus.PLAYBACK_COMPLETED;
          if (mListener != null) {
            mListener.onCompletion();
          }
        });

    // 需要注意的是，一旦发生错误，即使应用程序尚未注册错误侦听器，MediaPlayer 对象也会进入Error状态。
    // 为了从处于 Error状态的 MediaPlayer 对象并从错误中恢复,reset()可以调用将对象恢复到其Idle 状态。
    player.setOnErrorListener(
        (mediaPlayer, i, i1) -> {
          status = MediaPlayStatus.ERROR;
          if (mListener != null) {
            return mListener.onError(); // 在外部onError回调中调用reset()重置状态，并重置相关UI状态
          }
          // 如果该方法处理了错误，则为true，否则为false。 返回false或根本没有OnErrorListener，
          // 将导致调用OnCompletionListener
          return true;
        });

    // prepare完成后，才可以播放->start
    player.setOnPreparedListener(
        mediaPlayer -> {
          Log.i(TAG, "Prepared: status = " + status);
          status = MediaPlayStatus.PREPARED;

          // 取消转圈
          if (mListener != null) {
            mListener.onPrepared();
          }

          // 可以准备好自动start(),也可以自己控制start()。
          // start();

        });
  }

  // 资源可能为空,或者太大，获取其他原因导致无法加载，所以加上try catch
  public void prepare(String audioPath) throws IOException {

    // 先保证恢复空闲状态
    if (player != null) {
      reset();
    }

    // 设置资源
    if (status == MediaPlayStatus.IDLE) {
      player.setDataSource(audioPath);
      // 设置部分属性
      player.setAudioAttributes(
          new AudioAttributes.Builder()
              // 检测采样率输出
              .setUsage(AudioAttributes.USAGE_UNKNOWN)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
              .build());
      status = MediaPlayStatus.INITIALIZED;
    }

    // 准备
    if (status == MediaPlayStatus.INITIALIZED || status == MediaPlayStatus.STOPPED) {
      player.prepareAsync();
      status = MediaPlayStatus.PREPARING;
    }
  }

  // 暂停播放。 调用 start() 恢复。
  public void pause() {
    Log.i(TAG, "pause: status = " + status);
    if (status == MediaPlayStatus.STARTED || status == MediaPlayStatus.PAUSED) {
      singleExecutor.execute(
          () -> {
            player.pause();
            status = MediaPlayStatus.PAUSED;
          });
      // 停止刷新进度条
      stopTimer();
    }
  }

  // 播放开始或暂停后停止播放。
  // 调用 stop()停止播放并导致处于Started、Paused、Prepared 或PlaybackCompleted状态的 MediaPlayer进入 Stopped状态。
  // 一旦处于Stopped状态，播放不能开始，直到prepare()或被prepareAsync()调用以将 MediaPlayer 对象再次设置为Prepared状态。
  public void stop() {
    Log.i(TAG, "stop: status = " + status);
    if (status == MediaPlayStatus.PAUSED
        || status == MediaPlayStatus.STARTED
        || status == MediaPlayStatus.PREPARED
        || status == MediaPlayStatus.PLAYBACK_COMPLETED
        || status == MediaPlayStatus.STOPPED) {

      singleExecutor.execute(
          () -> {
            player.stop();
            status = MediaPlayStatus.STOPPED;
          });

      stopTimer();
    }
  }

  // 开始或恢复播放。 如果之前已暂停播放，将从暂停的位置继续播放。 如果播放已停止或从未开始，播放将从头开始。
  // isPlaying()可以调用来测试MediaPlayer对象是否处于Started状态
  public void start() {
    Log.i(TAG, "start: status = " + status);
    if (status == MediaPlayStatus.PREPARED
        || status == MediaPlayStatus.STARTED
        || status == MediaPlayStatus.PAUSED
        || status == MediaPlayStatus.PLAYBACK_COMPLETED) {
      singleExecutor.execute(
          () -> {
            player.start();
            status = MediaPlayStatus.STARTED;
          });

      startTimer();
    }
  }

  public void seekTo(int position) {
    Log.i(TAG, "seekTo: status = " + status);
    if (status == MediaPlayStatus.PREPARED
        || status == MediaPlayStatus.STARTED
        || status == MediaPlayStatus.PAUSED
        || status == MediaPlayStatus.PLAYBACK_COMPLETED) {

      singleExecutor.execute(
          () -> {
            player.seekTo(position);
          });
    }
  }

  // 将 MediaPlayer 重置为其未初始化状态。 调用此方法后，您必须通过设置数据源并调用prepare() 来再次初始化它
  public void reset() {
    Log.i(TAG, "reset: status = " + status);
    player.reset();
    status = MediaPlayStatus.IDLE;
  }

  // 開始計時
  public void startTimer() {
    Log.i(TAG, "startTimer: ");
    mTimerHandler.postDelayed(timerRun, TIMER_SEEK);
  }

  // 停止计时
  public void stopTimer() {
    Log.i(TAG, "stopTimer: ");
    mTimerHandler.removeCallbacks(timerRun);
  }

  // 释放与此 MediaPlayer 对象关联的资源。 使用完 MediaPlayer 后调用此方法被认为是一种很好的做法
  public void release() {
    Log.i(TAG, "release: status = " + status);
    singleExecutor.execute(
        () -> {
          player.release();
          player = null;
          status = MediaPlayStatus.END;
        });

    stopTimer();
  }

  public void onDestroy() {

    release();

    if (mUiHandler != null) {
      mUiHandler.removeCallbacksAndMessages(null);
    }

    mListener = null;
  }

  public MediaPlayer getPlayer() {
    return player;
  }

  // 播放状态监听
  public interface OnMediaStateListener {

    // 准备完成
    void onPrepared();

    // 当前播放进度
    void onSeekUpdate(int curTimeInt);

    // 播放完成
    void onCompletion();

    boolean onError();
  }

  private OnMediaStateListener mListener;

  public void setOnMediaStateListener(OnMediaStateListener listener) {
    mListener = listener;
  }

  // MediaPlay生命周期在END和IDLE状态中间流转
  enum MediaPlayStatus {
    // 空闲状态
    // 1.使用new创建->IDLE
    // 2.调用reset()->IDLE
    // 这两种方式的区别是：如果测试发生错误（调用getCurrentPosition()等方法），
    // 1方式不会回调方法 OnErrorListener.onError() 并且对象状态保持不变
    // 2方式会回调方法 OnErrorListener.onError()并且对象将转移到ERROR状态
    IDLE,
    // 初始化状态
    // 1.setDataSource()->INITIALIZED
    INITIALIZED,
    // 1.INITIALIZED->prepareAsync()->PREPARING
    // 2.STOPPED->prepareAsync()->PREPARING
    PREPARING,
    // MediaPlayer 对象必须先进入Prepared状态，然后才能开始播放
    // 1.PREPARING->OnPreparedListener.onPrepare()->PREPARED
    // 2.INITIALIZED->prepare()->PREPARED
    // 3.STOPPED->prepare()->PREPARED
    // 4.PREPARED->seekTo()->PREPARED
    PREPARED,
    // 1.PREPARED->start()->STARTED
    // 2.PAUSED->start()->STARTED
    // 3.STARTED->seekTo(),start()->STARTED
    // 4.PLAYBACK_COMPLETED&&setLooping(true)->STARTED
    // 5.PLAYBACK_COMPLETED->start()->STARTED
    STARTED,
    // 1.STARTED->pause()->PAUSED
    // 2.PAUSED->pause(),seekTo()->PAUSED
    PAUSED,
    // 1.STARTED->stop()->STOPPED
    // 2.PAUSED->stop()->PAUSE
    // 3.PLAYBACK_COMPLETED->stop()->PAUSE
    // 4.STOPPED->stop()->STOPPED
    // 5.PREPARED->stop()->STOPPED
    STOPPED,
    // 1.STARTED->PLAYBACK_COMPLETED&&setLooping(true)->PLAYBACK_COMPLETED
    // 1.PLAYBACK_COMPLETED->seekTo()->PLAYBACK_COMPLETED
    PLAYBACK_COMPLETED, // 播放完成，在此状态调用seekTo()
    // 1.onErrorListener.onError()
    ERROR, // onErrorListener.onError -> ERROR
    // release()->END
    // 一旦不再使用 MediaPlayer 对象，release()立即调用
    // 一旦 MediaPlayer 对象处于End状态，就不能再使用它，也无法将其恢复到任何其他状态。
    END //
  }
}
