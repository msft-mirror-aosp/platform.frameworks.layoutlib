package com.android.layoutlib.bridge.intensive;

import org.junit.Before;
import java.util.Locale;

import android.view.View;

public class RenderTestBase extends BridgeClient {
    public static final String S_PACKAGE_NAME = "com.android.layoutlib.test.myapplication";

    public String getAppTestDir() { return "testApp/MyApplication"; }
    public String getAppTestRes() { return  getAppTestDir() + "/src/main/res"; }
    public String getAppResources() { return  getAppTestRes(); }
    public String getAppTestAsset() { return getAppTestDir() + "/src/main/assets/"; }
    public String getAppClassesLocation() { return getAppTestDir() + "/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/"; }
    public View getView() { return null; }
    public String getAppGoldenDir() {
      String goldenImagePath = getAppTestDir();
      boolean isMac = System.getProperty("os.name").toLowerCase(Locale.US).contains("mac");
      if (isMac) {
        goldenImagePath += "/golden-mac/";
      } else {
        goldenImagePath += "/golden/";
      }
      return goldenImagePath;
    }


    @Before
    public void initPackageName() {
      setPackageName(S_PACKAGE_NAME);
    }

}
