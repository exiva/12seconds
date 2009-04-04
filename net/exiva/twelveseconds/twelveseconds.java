package net.exiva.twelveseconds;

import danger.app.Application;
import danger.app.AppResources;
import danger.app.Event;
import danger.app.EventType;
import danger.app.SettingsDB;
import danger.app.SettingsDBException;
import danger.app.Timer;
import danger.app.IPCMessage;
import danger.app.Registrar;

import danger.storage.MountPoint;
import danger.storage.StorageManager;

import danger.video.Recorder;
import danger.video.VideoException;

import danger.ui.AlertWindow;
import danger.ui.StaticTextBox;
import danger.ui.SplashScreen;

import danger.util.DEBUG;
import danger.util.format.DateFormat;

import java.io.File;

import java.util.Date;

public class twelveseconds extends Application implements Resources, Commands {
	static public AlertWindow aError, aWelcome;
	private static boolean isRecording = false;
	static public Date date;
	public static int status, timeLeft = 12;
 	static final int kCmd_RecordingDone = 1;
	static public LoginView mLogin;
	public static Recorder rec;
	public static SettingsDB tsSettings;
	public static String email, tsfilepath;;
	public static Timer mTimer, recBlinkTimer;
	static public videoView mWindow;
	static {
		try {
			rec = Recorder.getInstance(null);
		} catch (VideoException e) { DEBUG.p("Exception: "+e); }
	}

	public twelveseconds() {
		mLogin = LoginView.create();
		mWindow = videoView.create();
		mLogin.show();
		rec.setCaptureView(mWindow.vview);
		StorageManager.registerStorageDeviceListener(this);
		mTimer = new Timer(1000, true, this, 1);
		recBlinkTimer = new Timer(500, true, this, 2);
		aError = Application.getCurrentApp().getResources().getAlert(ID_ERROR, this);
		aWelcome = Application.getCurrentApp().getResources().getAlert(ID_WELCOME, this);
	}

	public void launch() {
		restoreData();
		makeDirectory();
	}

	public void resume() {
		rec.startPreview();
		makeDirectory();
	}

	public void suspend() {
		rec.stopPreview();
	}

	public void restoreData() {
		if (SettingsDB.findDB("12secSettings") == false) {
			tsSettings = new SettingsDB("12secSettings", true);
			tsSettings.setAutoSyncNotifyee(this);
			aWelcome.show();
		} else {
			tsSettings = new SettingsDB("12secSettings", true);
			email = tsSettings.getStringValue("email");
			mLogin.setLogin(email);
			mWindow.show();
			mLogin.hide();
		}
	}

	public void makeDirectory() {
		tsfilepath = new String();
		try {
			tsfilepath = new String(((String[])StorageManager.getRemovablePaths())[0]);
		}
		catch (Exception e2) {
			tsfilepath = null;
		}
		if(tsfilepath != null) {
			File basedirs = new File(tsfilepath+"/DCIM/12seconds.tv/");
			try {
				if(!basedirs.exists())
				basedirs.mkdir();
			}
			catch (Exception e2) {}
		}
	}

	public static void setEmail(String inMail) {
		email = inMail;
		tsSettings.setStringValue("email", inMail);
		mWindow.show();
		mLogin.hide();
	}

	public static void sendMail() {
		IPCMessage ipc = new IPCMessage();
		ipc.addItem("action" , "send");
		ipc.addItem("to" , email+"@12seconds.tv");
		ipc.addItem("subject" , "12seconds from hiptop");
		Registrar.sendMessage("Email", ipc, null);
	}

	public static void record() {
		if (!rec.isRecordingActive()) {
			try { rec.setMaxDuration(12000);
			} catch (IllegalStateException d) { DEBUG.p("Caught! "+d); }
			String[] storage;
			storage = StorageManager.getRemovablePaths();
			if(storage.length > 0) {
				date = new Date();
				rec.startRecording(storage[0]+"/DCIM/12seconds.tv/12seconds_"+DateFormat.withFormat("MMddyyyy_hhmmss", date)+".3gp", new Event(Application.getCurrentApp(), kCmd_RecordingDone));
				mTimer.start();
				recBlinkTimer.start();
			}
		}
	}

	public static void recalcTime() {
		timeLeft=timeLeft-1;
		mWindow.updateTime(timeLeft);
		if (timeLeft==0) {
			mTimer.stop();
			recBlinkTimer.stop();
			mWindow.recClear();
			timeLeft=12;
			status=0;
			mWindow.setState(0);
			mWindow.updateTime(timeLeft);
		}
	}

	public boolean receiveEvent(Event e) {
		switch (e.type) {
			case Event.EVENT_DATASTORE_RESTORED: {
				restoreData();
				return true;
			}
			case Event.EVENT_TIMER: {
				if (e.data==1) {
					recalcTime();
				} else if (e.data==2) {
					mWindow.recBlink();
				}
			}
			case kCmd_RecordingDone: {
				switch (e.what) {
					case Recorder.RECORDING_FINISHED:
					DEBUG.p("12s: Recording Finished.");
					if (e.data == Recorder.Errors.OK) {
						DEBUG.p("12s: Recorder said OK.");
						mWindow.setState(0);
					}
					if (e.data != Recorder.Errors.OK) {
						DEBUG.p("12s: I'm here...");
						rec.stopRecording();
						mWindow.setState(0);
						rec.startPreview();
						mTimer.stop();
						recBlinkTimer.stop();
						mWindow.recClear();
						timeLeft=12;
						status=0;
						switch (e.data) {
							case Recorder.Errors.ABORTED:
							DEBUG.p("12s: The Recording was Aborted.");
							return true;
							case Recorder.Errors.FILE_SYSTEM_FULL:
							DEBUG.p("12s: The memory card is full.");
							aError.setMessage("The SD card inserted is full. Remove some items and try to record again.");
							aError.show();
							break;
							case Recorder.Errors.HARDWARE_FAILED:
							DEBUG.p("12s: There was a hardware error.");
							aError.setMessage("There was an unkown hardware error. If this persists, contact support.");
							aError.show();
							break;
							case Recorder.Errors.HARDWARE_IN_USE:
							DEBUG.p("12s: The camera is in use.");
							aError.setMessage("The camera is in use.");
							aError.show();							
							break;
							case Recorder.Errors.IO_FAILED:
							DEBUG.p("12s: Filesystem Error.");
							aError.setMessage("There was an unknown Filesystem error. If this persists, contact support.");
							aError.show();
							break;
							case Recorder.Errors.ON_PHONE:
							DEBUG.p("12s: The Phone is Active");
							aError.setMessage("Cannot record while phone is in use. Please hang up and try again.");
							aError.show();
							break;
							case Recorder.Errors.OUT_OF_MEMORY:
							DEBUG.p("12s: Device is out of memory...");
							aError.setMessage("The device is out of memory. Try removing some Applications or Rebooting.");
							aError.show();
							break;
							case Recorder.Errors.WRITE_SUSPENDED_ON_FULL:
							DEBUG.p("12s: Couldn't save. SD Too Full.");
							aError.setMessage("The video was not saved because SD card inserted is full. Remove some items and try to record again.");
							aError.show();
							break;
							case Recorder.Errors.WRITE_SUSPENDED_ON_IO_ERROR:
							DEBUG.p("12s: Couldn't save. SD Too Full.");
							aError.setMessage("The video was not saved due to an unknown Filesystem error. If this persists, contact support.");
							aError.show();
							break;
						}
					}
				}
			}
			break;
			default:
			break;
		}
		switch (e.what) {
				case EventType.WHAT_STORAGE_DEVICE_MOUNTED: {
				DEBUG.p("12s: What Storage Device Mounted");
				mWindow.setState(0);
				status=0;
				aError.hide();
				makeDirectory();
				return true;
				}
				case EventType.WHAT_STORAGE_DEVICE_UNMOUNTED: {
				DEBUG.p("12s: What Storage Device Unmounted");
				aError.setMessage("Cannot record video until the SD card is inserted or the USB cable is removed.");
				aError.show();
				mWindow.setState(2);
				status=1;
				return true;
				}
				case EventType.WHAT_STORAGE_DEVICE_PUBLISHED: {
				DEBUG.p("12s: What Storage Device Published");
				aError.setMessage("Cannot record video until the SD card is inserted or the USB cable is removed.");
				aError.show();
				mWindow.setState(2);
				status=1;
				return true;
				}
				case EventType.WHAT_STORAGE_DEVICE_UNPUBLISHED: {
				DEBUG.p("12s: What Storage Device Unpublished");
				mWindow.setState(0);
				status=0;
				aError.hide();
				makeDirectory();
				return true;
				}
		}
		return super.receiveEvent(e);
	}
}