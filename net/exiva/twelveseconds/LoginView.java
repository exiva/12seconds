package net.exiva.twelveseconds;

import danger.app.Application;
import danger.app.Event;
import danger.app.Timer;

import danger.ui.AlertWindow;
import danger.ui.DialogWindow;
import danger.ui.ScreenWindow;
import danger.ui.TextField;

public class LoginView extends ScreenWindow implements Resources, Commands {
	private static TextField email;

	public LoginView() {}

	public void onDecoded() {
		email = (TextField)this.getDescendantWithID(ID_EMAIL);
	}

	public static LoginView create() {
		LoginView me = (LoginView) Application.getCurrentApp().getResources().getScreen(ID_LOGIN_SCREEN, null);
		return me;
	}

	public void setLogin(String inEmail) {
		email.setText(inEmail);
	}

	public void showAbout() {
		AlertWindow about = getApplication().getAlert(ID_ABOUT, this);
		about.show();
	}

	public boolean receiveEvent(Event e) {
		switch (e.type) {
			case EVENT_SIGN_UP: {
				try{
					danger.net.URL.gotoURL("http://12seconds.tv/");
				}
				catch (danger.net.URLException exc) {}
				return false;
			}
			case EVENT_STORE_LOGIN: {
				twelveseconds.setEmail(email.toString());
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
			Application.getCurrentApp().returnToLauncher();
			return true;
			case Event.DEVICE_BUTTON_BACK:
			Application.getCurrentApp().returnToLauncher();
			return true;
		}
		return super.eventWidgetUp(inWidget, e);
	}
}