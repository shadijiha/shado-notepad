package com.utils;

import com.observer.*;
import com.shadocloud.nest.*;

public final class ShadoCloudFieldsObserver implements Observer<AppSettings> {

	@Override
	public void update(AppSettings data) {
		System.out.println(getClass() + " called!");
		if (AppSettings.hasChanged("shado_cloud_email") || AppSettings.hasChanged("shado_cloud_pass"))	{
			AppSettings.client = new ShadoCloudClient(
					AppSettings.get("shado_cloud_email"),
					AppSettings.get("shado_cloud_pass")
			);

			Util.execute(() ->{
				try {
					AppSettings.client.auth.login();
				} catch (Exception e) {
					Actions.assertDialog(e);
				}
			});
		}
	}
}
