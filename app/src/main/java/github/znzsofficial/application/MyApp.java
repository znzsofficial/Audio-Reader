package github.znzsofficial.application;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import github.znzsofficial.activity.MainActivity;

public class MyApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    DynamicColors.applyToActivitiesIfAvailable(this);
  }
}
