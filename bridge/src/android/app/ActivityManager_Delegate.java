/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import com.android.internal.os.IResultReceiver;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.PendingIntentInfo;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityTaskManager.RootTaskInfo;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.LocusId;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.StrictMode.ViolationInfo;
import android.os.WorkSource;

import java.util.List;

public class ActivityManager_Delegate {
    private static final IActivityManager sStubManager = new IActivityManager() {

        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public ParcelFileDescriptor openContentUri(String s) throws RemoteException {
            return null;
        }

        @Override
        public void registerUidObserver(IUidObserver iUidObserver, int i, int i1, String s)
                throws RemoteException {

        }

        @Override
        public void unregisterUidObserver(IUidObserver iUidObserver) throws RemoteException {

        }

        @Override
        public boolean isUidActive(int i, String s) throws RemoteException {
            return false;
        }

        @Override
        public int getUidProcessState(int i, String s) throws RemoteException {
            return 0;
        }

        @Override
        public int checkPermission(String s, int i, int i1) throws RemoteException {
            return 0;
        }

        @Override
        public void handleApplicationCrash(IBinder iBinder,
                ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo)
                throws RemoteException {

        }

        @Override
        public int startActivity(IApplicationThread iApplicationThread, String s, Intent intent,
                String s1, IBinder iBinder, String s2, int i, int i1, ProfilerInfo profilerInfo,
                Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override
        public int startActivityWithFeature(IApplicationThread iApplicationThread, String s,
                String s1, Intent intent, String s2, IBinder iBinder, String s3, int i, int i1,
                ProfilerInfo profilerInfo, Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override
        public void unhandledBack() throws RemoteException {

        }

        @Override
        public boolean finishActivity(IBinder iBinder, int i, Intent intent, int i1)
                throws RemoteException {
            return false;
        }

        @Override
        public Intent registerReceiver(IApplicationThread iApplicationThread, String s,
                IIntentReceiver iIntentReceiver, IntentFilter intentFilter, String s1, int i,
                int i1) throws RemoteException {
            return null;
        }

        @Override
        public Intent registerReceiverWithFeature(IApplicationThread iApplicationThread,
                String s, String s1, String s2, IIntentReceiver iIntentReceiver,
                IntentFilter intentFilter, String s3, int i, int i1) throws RemoteException {
            return null;
        }

        @Override
        public void unregisterReceiver(IIntentReceiver iIntentReceiver) throws RemoteException {

        }

        @Override
        public int broadcastIntent(IApplicationThread iApplicationThread, Intent intent,
                String s, IIntentReceiver iIntentReceiver, int i, String s1, Bundle bundle,
                String[] strings, int i1, Bundle bundle1, boolean b, boolean b1, int i2)
                throws RemoteException {
            return 0;
        }

        @Override
        public int broadcastIntentWithFeature(IApplicationThread iApplicationThread, String s,
                Intent intent, String s1, IIntentReceiver iIntentReceiver, int i, String s2,
                Bundle bundle, String[] strings, String[] strings1, String[] strings2, int i1,
                Bundle bundle1, boolean b, boolean b1, int i2) throws RemoteException {
            return 0;
        }

        @Override
        public void unbroadcastIntent(IApplicationThread iApplicationThread, Intent intent,
                int i) throws RemoteException {

        }

        @Override
        public void finishReceiver(IBinder iBinder, int i, String s, Bundle bundle, boolean b,
                int i1) throws RemoteException {

        }

        @Override
        public void attachApplication(IApplicationThread iApplicationThread, long l)
                throws RemoteException {

        }

        @Override
        public List<RunningTaskInfo> getTasks(int i) throws RemoteException {
            return null;
        }

        @Override
        public void moveTaskToFront(IApplicationThread iApplicationThread, String s, int i,
                int i1, Bundle bundle) throws RemoteException {

        }

        @Override
        public int getTaskForActivity(IBinder iBinder, boolean b) throws RemoteException {
            return 0;
        }

        @Override
        public ContentProviderHolder getContentProvider(IApplicationThread iApplicationThread,
                String s, String s1, int i, boolean b) throws RemoteException {
            return null;
        }

        @Override
        public void publishContentProviders(IApplicationThread iApplicationThread,
                List<ContentProviderHolder> list) throws RemoteException {

        }

        @Override
        public boolean refContentProvider(IBinder iBinder, int i, int i1)
                throws RemoteException {
            return false;
        }

        @Override
        public PendingIntent getRunningServiceControlPanel(ComponentName componentName)
                throws RemoteException {
            return null;
        }

        @Override
        public ComponentName startService(IApplicationThread iApplicationThread, Intent intent,
                String s, boolean b, String s1, String s2, int i) throws RemoteException {
            return null;
        }

        @Override
        public int stopService(IApplicationThread iApplicationThread, Intent intent, String s,
                int i) throws RemoteException {
            return 0;
        }

        @Override
        public int bindService(IApplicationThread iApplicationThread, IBinder iBinder,
                Intent intent, String s, IServiceConnection iServiceConnection, int i,
                String s1, int i1) throws RemoteException {
            return 0;
        }

        @Override
        public int bindServiceInstance(IApplicationThread iApplicationThread, IBinder iBinder,
                Intent intent, String s, IServiceConnection iServiceConnection, int i,
                String s1, String s2, int i1) throws RemoteException {
            return 0;
        }

        @Override
        public void updateServiceGroup(IServiceConnection iServiceConnection, int i, int i1)
                throws RemoteException {

        }

        @Override
        public boolean unbindService(IServiceConnection iServiceConnection)
                throws RemoteException {
            return false;
        }

        @Override
        public void publishService(IBinder iBinder, Intent intent, IBinder iBinder1)
                throws RemoteException {

        }

        @Override
        public void setDebugApp(String s, boolean b, boolean b1) throws RemoteException {

        }

        @Override
        public void setAgentApp(String s, String s1) throws RemoteException {

        }

        @Override
        public void setAlwaysFinish(boolean b) throws RemoteException {

        }

        @Override
        public boolean startInstrumentation(ComponentName componentName, String s, int i,
                Bundle bundle, IInstrumentationWatcher iInstrumentationWatcher,
                IUiAutomationConnection iUiAutomationConnection, int i1, String s1)
                throws RemoteException {
            return false;
        }

        @Override
        public void addInstrumentationResults(IApplicationThread iApplicationThread,
                Bundle bundle) throws RemoteException {

        }

        @Override
        public void finishInstrumentation(IApplicationThread iApplicationThread, int i,
                Bundle bundle) throws RemoteException {

        }

        @Override
        public Configuration getConfiguration() throws RemoteException {
            return null;
        }

        @Override
        public boolean updateConfiguration(Configuration configuration) throws RemoteException {
            return false;
        }

        @Override
        public boolean updateMccMncConfiguration(String s, String s1) throws RemoteException {
            return false;
        }

        @Override
        public boolean stopServiceToken(ComponentName componentName, IBinder iBinder, int i)
                throws RemoteException {
            return false;
        }

        @Override
        public void setProcessLimit(int i) throws RemoteException {

        }

        @Override
        public int getProcessLimit() throws RemoteException {
            return 0;
        }

        @Override
        public int checkUriPermission(Uri uri, int i, int i1, int i2, int i3, IBinder iBinder)
                throws RemoteException {
            return 0;
        }

        @Override
        public int[] checkUriPermissions(List<Uri> list, int i, int i1, int i2, int i3,
                IBinder iBinder) throws RemoteException {
            return new int[0];
        }

        @Override
        public void grantUriPermission(IApplicationThread iApplicationThread, String s, Uri uri,
                int i, int i1) throws RemoteException {

        }

        @Override
        public void revokeUriPermission(IApplicationThread iApplicationThread, String s,
                Uri uri, int i, int i1) throws RemoteException {

        }

        @Override
        public void setActivityController(IActivityController iActivityController, boolean b)
                throws RemoteException {

        }

        @Override
        public void showWaitingForDebugger(IApplicationThread iApplicationThread, boolean b)
                throws RemoteException {

        }

        @Override
        public void signalPersistentProcesses(int i) throws RemoteException {

        }

        @Override
        public ParceledListSlice getRecentTasks(int i, int i1, int i2) throws RemoteException {
            return null;
        }

        @Override
        public void serviceDoneExecuting(IBinder iBinder, int i, int i1, int i2)
                throws RemoteException {

        }

        @Override
        public IIntentSender getIntentSender(int i, String s, IBinder iBinder, String s1,
                int i1, Intent[] intents, String[] strings, int i2, Bundle bundle, int i3)
                throws RemoteException {
            return null;
        }

        @Override
        public IIntentSender getIntentSenderWithFeature(int i, String s, String s1,
                IBinder iBinder, String s2, int i1, Intent[] intents, String[] strings, int i2,
                Bundle bundle, int i3) throws RemoteException {
            return null;
        }

        @Override
        public void cancelIntentSender(IIntentSender iIntentSender) throws RemoteException {

        }

        @Override
        public PendingIntentInfo getInfoForIntentSender(IIntentSender iIntentSender)
                throws RemoteException {
            return null;
        }

        @Override
        public boolean registerIntentSenderCancelListenerEx(IIntentSender iIntentSender,
                IResultReceiver iResultReceiver) throws RemoteException {
            return false;
        }

        @Override
        public void unregisterIntentSenderCancelListener(IIntentSender iIntentSender,
                IResultReceiver iResultReceiver) throws RemoteException {

        }

        @Override
        public void enterSafeMode() throws RemoteException {

        }

        @Override
        public void noteWakeupAlarm(IIntentSender iIntentSender, WorkSource workSource, int i,
                String s, String s1) throws RemoteException {

        }

        @Override
        public void removeContentProvider(IBinder iBinder, boolean b) throws RemoteException {

        }

        @Override
        public void setRequestedOrientation(IBinder iBinder, int i) throws RemoteException {

        }

        @Override
        public void unbindFinished(IBinder iBinder, Intent intent, boolean b)
                throws RemoteException {

        }

        @Override
        public void setProcessImportant(IBinder iBinder, int i, boolean b, String s)
                throws RemoteException {

        }

        @Override
        public void setServiceForeground(ComponentName componentName, IBinder iBinder, int i,
                Notification notification, int i1, int i2) throws RemoteException {

        }

        @Override
        public int getForegroundServiceType(ComponentName componentName, IBinder iBinder)
                throws RemoteException {
            return 0;
        }

        @Override
        public boolean moveActivityTaskToBack(IBinder iBinder, boolean b)
                throws RemoteException {
            return false;
        }

        @Override
        public void getMemoryInfo(MemoryInfo memoryInfo) throws RemoteException {

        }

        @Override
        public List<ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
            return null;
        }

        @Override
        public boolean clearApplicationUserData(String s, boolean b,
                IPackageDataObserver iPackageDataObserver, int i) throws RemoteException {
            return false;
        }

        @Override
        public void stopAppForUser(String s, int i) throws RemoteException {

        }

        @Override
        public boolean registerForegroundServiceObserver(
                IForegroundServiceObserver iForegroundServiceObserver) throws RemoteException {
            return false;
        }

        @Override
        public void forceStopPackage(String s, int i) throws RemoteException {

        }

        @Override
        public boolean killPids(int[] ints, String s, boolean b) throws RemoteException {
            return false;
        }

        @Override
        public List<RunningServiceInfo> getServices(int i, int i1) throws RemoteException {
            return null;
        }

        @Override
        public List<RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
            return null;
        }

        @Override
        public IBinder peekService(Intent intent, String s, String s1) throws RemoteException {
            return null;
        }

        @Override
        public boolean profileControl(String s, int i, boolean b, ProfilerInfo profilerInfo,
                int i1) throws RemoteException {
            return false;
        }

        @Override
        public boolean shutdown(int i) throws RemoteException {
            return false;
        }

        @Override
        public void stopAppSwitches() throws RemoteException {

        }

        @Override
        public void resumeAppSwitches() throws RemoteException {

        }

        @Override
        public boolean bindBackupAgent(String s, int i, int i1, int i2) throws RemoteException {
            return false;
        }

        @Override
        public void backupAgentCreated(String s, IBinder iBinder, int i)
                throws RemoteException {

        }

        @Override
        public void unbindBackupAgent(ApplicationInfo applicationInfo) throws RemoteException {

        }

        @Override
        public int handleIncomingUser(int i, int i1, int i2, boolean b, boolean b1, String s,
                String s1) throws RemoteException {
            return 0;
        }

        @Override
        public void addPackageDependency(String s) throws RemoteException {

        }

        @Override
        public void killApplication(String s, int i, int i1, String s1) throws RemoteException {

        }

        @Override
        public void closeSystemDialogs(String s) throws RemoteException {

        }

        @Override
        public Debug.MemoryInfo[] getProcessMemoryInfo(int[] ints) throws RemoteException {
            return new Debug.MemoryInfo[0];
        }

        @Override
        public void killApplicationProcess(String s, int i) throws RemoteException {

        }

        @Override
        public boolean handleApplicationWtf(IBinder iBinder, String s, boolean b,
                ApplicationErrorReport.ParcelableCrashInfo parcelableCrashInfo,
                int i) throws RemoteException {
            return false;
        }

        @Override
        public void killBackgroundProcesses(String s, int i) throws RemoteException {

        }

        @Override
        public boolean isUserAMonkey() throws RemoteException {
            return false;
        }

        @Override
        public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
            return null;
        }

        @Override
        public void finishHeavyWeightApp() throws RemoteException {

        }

        @Override
        public void handleApplicationStrictModeViolation(IBinder iBinder, int i,
                ViolationInfo violationInfo) throws RemoteException {

        }

        @Override
        public boolean isTopActivityImmersive() throws RemoteException {
            return false;
        }

        @Override
        public void crashApplicationWithType(int i, int i1, String s, int i2, String s1,
                boolean b, int i3) throws RemoteException {

        }

        @Override
        public void crashApplicationWithTypeWithExtras(int i, int i1, String s, int i2,
                String s1, boolean b, int i3, Bundle bundle) throws RemoteException {

        }

        @Override
        public String getProviderMimeType(Uri uri, int i) throws RemoteException {
            return null;
        }

        @Override
        public void getProviderMimeTypeAsync(Uri uri, int i, RemoteCallback remoteCallback)
                throws RemoteException {

        }

        @Override
        public boolean dumpHeap(String s, int i, boolean b, boolean b1, boolean b2, String s1,
                ParcelFileDescriptor parcelFileDescriptor, RemoteCallback remoteCallback)
                throws RemoteException {
            return false;
        }

        @Override
        public boolean isUserRunning(int i, int i1) throws RemoteException {
            return false;
        }

        @Override
        public void setPackageScreenCompatMode(String s, int i) throws RemoteException {

        }

        @Override
        public boolean switchUser(int i) throws RemoteException {
            return false;
        }

        @Override
        public String getSwitchingFromUserMessage() throws RemoteException {
            return null;
        }

        @Override
        public String getSwitchingToUserMessage() throws RemoteException {
            return null;
        }

        @Override
        public void setStopUserOnSwitch(int i) throws RemoteException {

        }

        @Override
        public boolean removeTask(int i) throws RemoteException {
            return false;
        }

        @Override
        public void registerProcessObserver(IProcessObserver iProcessObserver)
                throws RemoteException {

        }

        @Override
        public void unregisterProcessObserver(IProcessObserver iProcessObserver)
                throws RemoteException {

        }

        @Override
        public boolean isIntentSenderTargetedToPackage(IIntentSender iIntentSender)
                throws RemoteException {
            return false;
        }

        @Override
        public void updatePersistentConfiguration(Configuration configuration)
                throws RemoteException {

        }

        @Override
        public void updatePersistentConfigurationWithAttribution(Configuration configuration,
                String s, String s1) throws RemoteException {

        }

        @Override
        public long[] getProcessPss(int[] ints) throws RemoteException {
            return new long[0];
        }

        @Override
        public void showBootMessage(CharSequence charSequence, boolean b)
                throws RemoteException {

        }

        @Override
        public void killAllBackgroundProcesses() throws RemoteException {

        }

        @Override
        public ContentProviderHolder getContentProviderExternal(String s, int i,
                IBinder iBinder, String s1) throws RemoteException {
            return null;
        }

        @Override
        public void removeContentProviderExternal(String s, IBinder iBinder)
                throws RemoteException {

        }

        @Override
        public void removeContentProviderExternalAsUser(String s, IBinder iBinder, int i)
                throws RemoteException {

        }

        @Override
        public void getMyMemoryState(RunningAppProcessInfo runningAppProcessInfo)
                throws RemoteException {

        }

        @Override
        public boolean killProcessesBelowForeground(String s) throws RemoteException {
            return false;
        }

        @Override
        public UserInfo getCurrentUser() throws RemoteException {
            return null;
        }

        @Override
        public int getCurrentUserId() throws RemoteException {
            return 0;
        }

        @Override
        public int getLaunchedFromUid(IBinder iBinder) throws RemoteException {
            return 0;
        }

        @Override
        public void unstableProviderDied(IBinder iBinder) throws RemoteException {

        }

        @Override
        public boolean isIntentSenderAnActivity(IIntentSender iIntentSender)
                throws RemoteException {
            return false;
        }

        @Override
        public int startActivityAsUser(IApplicationThread iApplicationThread, String s,
                Intent intent, String s1, IBinder iBinder, String s2, int i, int i1,
                ProfilerInfo profilerInfo, Bundle bundle, int i2) throws RemoteException {
            return 0;
        }

        @Override
        public int startActivityAsUserWithFeature(IApplicationThread iApplicationThread,
                String s, String s1, Intent intent, String s2, IBinder iBinder, String s3,
                int i, int i1, ProfilerInfo profilerInfo, Bundle bundle, int i2)
                throws RemoteException {
            return 0;
        }

        @Override
        public int stopUser(int i, boolean b, IStopUserCallback iStopUserCallback)
                throws RemoteException {
            return 0;
        }

        @Override
        public int stopUserWithDelayedLocking(int i, boolean b,
                IStopUserCallback iStopUserCallback) throws RemoteException {
            return 0;
        }

        @Override
        public void registerUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver,
                String s) throws RemoteException {

        }

        @Override
        public void unregisterUserSwitchObserver(IUserSwitchObserver iUserSwitchObserver)
                throws RemoteException {

        }

        @Override
        public int[] getRunningUserIds() throws RemoteException {
            return new int[0];
        }

        @Override
        public void requestSystemServerHeapDump() throws RemoteException {

        }

        @Override
        public void requestBugReport(int i) throws RemoteException {

        }

        @Override
        public void requestBugReportWithDescription(String s, String s1, int i)
                throws RemoteException {

        }

        @Override
        public void requestTelephonyBugReport(String s, String s1) throws RemoteException {

        }

        @Override
        public void requestWifiBugReport(String s, String s1) throws RemoteException {

        }

        @Override
        public void requestInteractiveBugReportWithDescription(String s, String s1)
                throws RemoteException {

        }

        @Override
        public void requestInteractiveBugReport() throws RemoteException {

        }

        @Override
        public void requestFullBugReport() throws RemoteException {

        }

        @Override
        public void requestRemoteBugReport(long l) throws RemoteException {

        }

        @Override
        public boolean launchBugReportHandlerApp() throws RemoteException {
            return false;
        }

        @Override
        public List<String> getBugreportWhitelistedPackages() throws RemoteException {
            return null;
        }

        @Override
        public Intent getIntentForIntentSender(IIntentSender iIntentSender)
                throws RemoteException {
            return null;
        }

        @Override
        public String getLaunchedFromPackage(IBinder iBinder) throws RemoteException {
            return null;
        }

        @Override
        public void killUid(int i, int i1, String s) throws RemoteException {

        }

        @Override
        public void setUserIsMonkey(boolean b) throws RemoteException {

        }

        @Override
        public void hang(IBinder iBinder, boolean b) throws RemoteException {

        }

        @Override
        public List<RootTaskInfo> getAllRootTaskInfos() throws RemoteException {
            return null;
        }

        @Override
        public void moveTaskToRootTask(int i, int i1, boolean b) throws RemoteException {

        }

        @Override
        public void setFocusedRootTask(int i) throws RemoteException {

        }

        @Override
        public RootTaskInfo getFocusedRootTaskInfo() throws RemoteException {
            return null;
        }

        @Override
        public void restart() throws RemoteException {

        }

        @Override
        public void performIdleMaintenance() throws RemoteException {

        }

        @Override
        public void appNotRespondingViaProvider(IBinder iBinder) throws RemoteException {

        }

        @Override
        public Rect getTaskBounds(int i) throws RemoteException {
            return null;
        }

        @Override
        public boolean setProcessMemoryTrimLevel(String s, int i, int i1)
                throws RemoteException {
            return false;
        }

        @Override
        public String getTagForIntentSender(IIntentSender iIntentSender, String s)
                throws RemoteException {
            return null;
        }

        @Override
        public boolean startUserInBackground(int i) throws RemoteException {
            return false;
        }

        @Override
        public boolean isInLockTaskMode() throws RemoteException {
            return false;
        }

        @Override
        public int startActivityFromRecents(int i, Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override
        public void startSystemLockTaskMode(int i) throws RemoteException {

        }

        @Override
        public boolean isTopOfTask(IBinder iBinder) throws RemoteException {
            return false;
        }

        @Override
        public void bootAnimationComplete() throws RemoteException {

        }

        @Override
        public void registerTaskStackListener(ITaskStackListener iTaskStackListener)
                throws RemoteException {

        }

        @Override
        public void unregisterTaskStackListener(ITaskStackListener iTaskStackListener)
                throws RemoteException {

        }

        @Override
        public void notifyCleartextNetwork(int i, byte[] bytes) throws RemoteException {

        }

        @Override
        public void setTaskResizeable(int i, int i1) throws RemoteException {

        }

        @Override
        public void resizeTask(int i, Rect rect, int i1) throws RemoteException {

        }

        @Override
        public int getLockTaskModeState() throws RemoteException {
            return 0;
        }

        @Override
        public void setDumpHeapDebugLimit(String s, int i, long l, String s1)
                throws RemoteException {

        }

        @Override
        public void dumpHeapFinished(String s) throws RemoteException {

        }

        @Override
        public void updateLockTaskPackages(int i, String[] strings) throws RemoteException {

        }

        @Override
        public void noteAlarmStart(IIntentSender iIntentSender, WorkSource workSource, int i,
                String s) throws RemoteException {

        }

        @Override
        public void noteAlarmFinish(IIntentSender iIntentSender, WorkSource workSource, int i,
                String s) throws RemoteException {

        }

        @Override
        public int getPackageProcessState(String s, String s1) throws RemoteException {
            return 0;
        }

        @Override
        public boolean startBinderTracking() throws RemoteException {
            return false;
        }

        @Override
        public boolean stopBinderTrackingAndDump(ParcelFileDescriptor parcelFileDescriptor)
                throws RemoteException {
            return false;
        }

        @Override
        public void enableBinderTracing() throws RemoteException {

        }

        @Override
        public void suppressResizeConfigChanges(boolean b) throws RemoteException {

        }

        @Override
        public boolean unlockUser(int i, byte[] bytes, byte[] bytes1,
                IProgressListener iProgressListener) throws RemoteException {
            return false;
        }

        @Override
        public void killPackageDependents(String s, int i) throws RemoteException {

        }

        @Override
        public void makePackageIdle(String s, int i) throws RemoteException {

        }

        @Override
        public int getMemoryTrimLevel() throws RemoteException {
            return 0;
        }

        @Override
        public boolean isVrModePackageEnabled(ComponentName componentName)
                throws RemoteException {
            return false;
        }

        @Override
        public void notifyLockedProfile(int i) throws RemoteException {

        }

        @Override
        public void startConfirmDeviceCredentialIntent(Intent intent, Bundle bundle)
                throws RemoteException {

        }

        @Override
        public void sendIdleJobTrigger() throws RemoteException {

        }

        @Override
        public int sendIntentSender(IIntentSender iIntentSender, IBinder iBinder, int i,
                Intent intent, String s, IIntentReceiver iIntentReceiver, String s1,
                Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override
        public boolean isBackgroundRestricted(String s) throws RemoteException {
            return false;
        }

        @Override
        public void setRenderThread(int i) throws RemoteException {

        }

        @Override
        public void setHasTopUi(boolean b) throws RemoteException {

        }

        @Override
        public int restartUserInBackground(int i) throws RemoteException {
            return 0;
        }

        @Override
        public void cancelTaskWindowTransition(int i) throws RemoteException {

        }

        @Override
        public void scheduleApplicationInfoChanged(List<String> list, int i)
                throws RemoteException {

        }

        @Override
        public void setPersistentVrThread(int i) throws RemoteException {

        }

        @Override
        public void waitForNetworkStateUpdate(long l) throws RemoteException {

        }

        @Override
        public void backgroundAllowlistUid(int i) throws RemoteException {

        }

        @Override
        public boolean startUserInBackgroundWithListener(int i,
                IProgressListener iProgressListener) throws RemoteException {
            return false;
        }

        @Override
        public void startDelegateShellPermissionIdentity(int i, String[] strings)
                throws RemoteException {

        }

        @Override
        public void stopDelegateShellPermissionIdentity() throws RemoteException {

        }

        @Override
        public List<String> getDelegatedShellPermissions() throws RemoteException {
            return null;
        }

        @Override
        public ParcelFileDescriptor getLifeMonitor() throws RemoteException {
            return null;
        }

        @Override
        public boolean startUserInForegroundWithListener(int i,
                IProgressListener iProgressListener) throws RemoteException {
            return false;
        }

        @Override
        public void appNotResponding(String s) throws RemoteException {

        }

        @Override
        public ParceledListSlice<ApplicationExitInfo> getHistoricalProcessExitReasons(String s,
                int i, int i1, int i2) throws RemoteException {
            return null;
        }

        @Override
        public void killProcessesWhenImperceptible(int[] ints, String s)
                throws RemoteException {

        }

        @Override
        public void setActivityLocusContext(ComponentName componentName, LocusId locusId,
                IBinder iBinder) throws RemoteException {

        }

        @Override
        public void setProcessStateSummary(byte[] bytes) throws RemoteException {

        }

        @Override
        public boolean isAppFreezerSupported() throws RemoteException {
            return false;
        }

        @Override
        public boolean isAppFreezerEnabled() throws RemoteException {
            return false;
        }

        @Override
        public void killUidForPermissionChange(int i, int i1, String s) throws RemoteException {

        }

        @Override
        public void resetAppErrors() throws RemoteException {

        }

        @Override
        public boolean enableAppFreezer(boolean b) throws RemoteException {
            return false;
        }

        @Override
        public boolean enableFgsNotificationRateLimit(boolean b) throws RemoteException {
            return false;
        }

        @Override
        public void holdLock(IBinder iBinder, int i) throws RemoteException {

        }

        @Override
        public boolean startProfile(int i) throws RemoteException {
            return false;
        }

        @Override
        public boolean stopProfile(int i) throws RemoteException {
            return false;
        }

        @Override
        public ParceledListSlice queryIntentComponentsForIntentSender(
                IIntentSender iIntentSender, int i) throws RemoteException {
            return null;
        }

        @Override
        public int getUidProcessCapabilities(int i, String s) throws RemoteException {
            return 0;
        }

        @Override
        public void waitForBroadcastIdle() throws RemoteException {

        }

        @Override
        public int getBackgroundRestrictionExemptionReason(int i) throws RemoteException {
            return 0;
        }
    };

    @LayoutlibDelegate
    public static IActivityManager getService() {
        return sStubManager;
    }
}
