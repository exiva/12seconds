package net.exiva.twelveseconds;

import danger.app.Application;
import danger.app.Event;

import danger.ui.AlertWindow;
import danger.ui.Button;
import danger.ui.DialogWindow;
import danger.ui.ImageView;
import danger.ui.Menu;
import danger.ui.MenuItem;
import danger.ui.ScreenWindow;
import danger.ui.StaticText;
import danger.ui.VideoCaptureView;

public class videoView extends ScreenWindow implements Resources, Commands {
	VideoCaptureView vview;
	Button record;
	StaticText recording, stopped, time;
	MenuItem menuRecord;
	ImageView screenRecord, screenStop;
	ImageView screenTime12, screenTime11, screenTime10, screenTime09;
	ImageView screenTime08, screenTime07, screenTime06, screenTime05;
	ImageView screenTime04, screenTime03, screenTime02, screenTime01;
	ImageView screenTime00;
	AlertWindow vAbortAlert;
	public static int recstate;
	public static boolean recShowing;
	
	public videoView() {
		vview = new VideoCaptureView(twelveseconds.rec, 176, 144);
		vview.setPosition(112,18);
		addChild(vview);
		vview.show();
	}

	public void onDecoded() {
		record = (Button)this.getDescendantWithID(vRecordButton);
		recording = (StaticText)this.getDescendantWithID(vRecordText);
		stopped = (StaticText)this.getDescendantWithID(vStoppedText);
		screenRecord = (ImageView)this.getDescendantWithID(vRecordIcon);
		screenStop = (ImageView)this.getDescendantWithID(vStoppedIcon);
		
		screenTime12 = (ImageView)this.getDescendantWithID(vSeconds_12);
		screenTime11 = (ImageView)this.getDescendantWithID(vSeconds_11);
		screenTime10 = (ImageView)this.getDescendantWithID(vSeconds_10);
		screenTime09 = (ImageView)this.getDescendantWithID(vSeconds_09);
		screenTime08 = (ImageView)this.getDescendantWithID(vSeconds_08);
		screenTime07 = (ImageView)this.getDescendantWithID(vSeconds_07);
		screenTime06 = (ImageView)this.getDescendantWithID(vSeconds_06);
		screenTime05 = (ImageView)this.getDescendantWithID(vSeconds_05);
		screenTime04 = (ImageView)this.getDescendantWithID(vSeconds_04);
		screenTime03 = (ImageView)this.getDescendantWithID(vSeconds_03);
		screenTime02 = (ImageView)this.getDescendantWithID(vSeconds_02);
		screenTime01 = (ImageView)this.getDescendantWithID(vSeconds_01);
		screenTime00 = (ImageView)this.getDescendantWithID(vSeconds_00);
		
		setState(0);
		recstate=0;
	}
	
	public boolean blocksAutomaticKeyGuard() {
		return true;
	}
	
	public boolean blocksKeyGuard() {
		return true;
	}
	
	public void recBlink() {
		if (!recShowing) {
			recording.setVisible(true);
			screenRecord.setVisible(true);
			recShowing=true;
		} else {
			recording.setVisible(false);
			screenRecord.setVisible(false);
			recShowing=false;			
		}
	}
	
	public void recClear() {
		recording.setVisible(false);
		screenRecord.setVisible(false);
		recShowing=false;
	}
	
	public final void adjustActionMenuState(Menu menu) {
		menu.removeAllItems();
		menu.addFromResource(Application.getCurrentApp().getResources(), ID_MAIN_MENU, this);
		menuRecord = menu.getItemWithID(vMenuRecord);
		if (recstate==0) {
			menuRecord.enable();
		} else if (recstate==1) {
			menuRecord.disable();
		}
    }
	
	public static videoView create() {
		videoView me = (videoView) Application.getCurrentApp().getResources().getScreen(ID_MAIN_SCREEN, null);
		return me;
	}
	
	public void updateTime(int inTime) {
		switch (inTime) {
			case 12:
			screenTime00.setVisible(false);
			screenTime12.setVisible(true);
			break;
			case 11:
			screenTime12.setVisible(false);
			screenTime11.setVisible(true);
			break;
			case 10:
			screenTime11.setVisible(false);
			screenTime10.setVisible(true);
			break;
			case 9:
			screenTime10.setVisible(false);
			screenTime09.setVisible(true);
			break;
			case 8:
			screenTime09.setVisible(false);
			screenTime08.setVisible(true);
			break;
			case 7:
			screenTime08.setVisible(false);
			screenTime07.setVisible(true);
			break;
			case 6:
			screenTime07.setVisible(false);
			screenTime06.setVisible(true);
			break;
			case 5:
			screenTime06.setVisible(false);
			screenTime05.setVisible(true);
			break;
			case 4:
			screenTime05.setVisible(false);
			screenTime04.setVisible(true);
			break;
			case 3:
			screenTime04.setVisible(false);
			screenTime03.setVisible(true);
			break;
			case 2:
			screenTime03.setVisible(false);
			screenTime02.setVisible(true);
			break;
			case 1:
			screenTime02.setVisible(false);
			screenTime01.setVisible(true);
			break;
			case 0:
			screenTime01.setVisible(false);
			screenTime00.setVisible(true);
			break;
		}
	}

 	public void setState(int state) {
		switch (state) {
			case 0:
				record.enable();
				recording.setVisible(false);
				stopped.setVisible(true);
				screenStop.setVisible(true);
				screenRecord.setVisible(false);
				recstate=0;
				break;
			case 1:
				record.disable();
				stopped.setVisible(false);
				recording.setVisible(true);
				screenStop.setVisible(false);
				screenRecord.setVisible(true);
				recstate=1;
				break;
			case 2:
				record.disable();
				stopped.setVisible(true);
				recording.setVisible(false);
				screenStop.setVisible(true);
				screenRecord.setVisible(true);
				recstate=1;
				break;
		}
	}

	public void showLogin() {
		twelveseconds.mLogin.show();
	}

	public boolean receiveEvent(Event e) {
		switch (e.type) {
			case EVENT_START_RECORDING: {
				twelveseconds.record();
				setState(1);
				return false;
			}
			case EVENT_SETUP: {
				showLogin();
				return false;
			}
			case EVENT_EMAIL: {
				twelveseconds.sendMail();
				return true;
			}
			case EVENT_TIPS: {
				DialogWindow tips = getApplication().getDialog(helpDialog, this);
				tips.show();
				return true;
			}
			case EVENT_ABOUT: {
				AlertWindow about = getApplication().getAlert(ID_ABOUT, this);
				about.show();
				return true;
			}
			default:
			break;
		}
		return super.receiveEvent(e);
	}

	public boolean eventWidgetUp(int inWidget, Event e) {
		switch (inWidget) {
			case Event.DEVICE_BUTTON_CANCEL:
				if (recstate==0 || recstate==3) {
					Application.getCurrentApp().returnToLauncher();
				} else if (recstate==1) {
					vAbortAlert.show();
				}
			return true;
			case Event.DEVICE_BUTTON_BACK:
			Application.getCurrentApp().returnToLauncher();
			return true;
		}
		return super.eventWidgetUp(inWidget, e);
	}
}//NOM NOM NOM