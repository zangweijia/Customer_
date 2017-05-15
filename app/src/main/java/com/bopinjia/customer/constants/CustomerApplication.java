package com.bopinjia.customer.constants;

import android.support.multidex.MultiDexApplication;

import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

import org.xutils.x;

public class CustomerApplication extends MultiDexApplication {

	{
		PlatformConfig.setWeixin("wx56e280c9e1fca11c", "0eb9e3bb55b9a1bb1b3b426d6349db0c");
		PlatformConfig.setSinaWeibo("1972925836", "f45865fda8916c685de68b657afd463a",
				"http://sns.whalecloud.com/sina2/callback");
		PlatformConfig.setQQZone("1105245015", "SZJ9qANLph3JghD8");
		Config.isJumptoAppStore = true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Config.DEBUG = true;
		UMShareAPI.get(this);
		x.Ext.init(this);

		// Config.DEBUG = true;
		// CrashHandler.getInstance().init(this);
	}
}
