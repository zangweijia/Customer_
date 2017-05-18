package com.bopinjia.customer.constants;

public class Constants {

    /**
     * 消息页面的INTENT的КEY
     */
    public static final String INTENT_EXTRA_MSG = "msg";
    public static final String INTENT_EXTRA_MSG_INFO = "msg_info";
    public static final String INTENT_EXTRA_MSG_STATUS = "msg_status";

    /**
     * 采购列表的INTENT的КEY
     */
    public static final String INTENT_EXTRA_PURCHASE_STATUS = "status";
    public static final String INTENT_EXTRA_PURCHASE_AMOUNT = "amount";

    /**
     * 扫码结果数据传递的KEY
     */
    public static final String INTENT_EXTRA_SCAN_RESULT = "scan_result";

    /**
     * 获取验证码意图的KEY
     */
    public static final String INTENT_EXTRA_SECURITY = "security";

    /**
     * 检索用关键字的KEY
     */
    public static final String INTENT_EXTRA_SEARCH_WORD = "search_word";

    /**
     * 首选项保存用的KEY
     */
    public static final String KEY_PREFERENCE = "bopinjia_customer";
    /**
     * 首选项:用户ID
     */
    public static final String KEY_PREFERENCE_USER_ID = "user_id";
    /**
     * 首选项：用户信息
     */
    public static final String KEY_PREFERENCE_USER_INFO = "user_info";
    /**
     * 首选项:用户是否登录
     */
    public static final String KEY_PREFERENCE_LOGIN_FLG = "login_flg";
    /**
     * 首选项:用户是否显示更新
     */
    public static final String KEY_PREFERENCE_SHOW_UPDATA = "show_updata";
    /**
     * 首选项:注册手机号
     */
    public static final String KEY_PREFERENCE_PHONE = "phone";
    /**
     * 首选项:密码
     */
    public static final String KEY_PREFERENCE_PASSWORD = "password";
    /**
     * 首选项:头像URL
     */
    public static final String KEY_PREFERENCE_HEADPORTRAIT = "headportrait";
    /**
     * 首选项:绑定店铺
     */
    public static final String KEY_PREFERENCE_BINDING_SHOP = "shop";
    /**
     * 首选项:临时店铺
     */
    public static final String KEY_PREFERENCE_TEMP_SHOP = "shop_temp";
    /**
     * 首选项:最近的搜索内容
     */
    public static final String KEY_PREFERENCE_LATEST_SEARCH = "search";
    /**
     * 首选项：订单id
     */
    public static final String KEY_PREFERENCE_ORDER_ID = "order_id";
    /**
     * 首选项：支付进入方式
     */
    public static final String KEY_PREFERENCE_PAY_TYPE = "pay_type";

    /**
     * 消息页面状态:失败
     */
    public static final String MSG_STATUS_FAIL = "1";


    /**
     * 照片类型：头像
     */
    public static final String IMAGE_TYPE_HEAD_PORTRAIT = "HeadPortrait_";
    /**
     * 照片类型：证件照正面
     */
    public static final String IMAGE_TYPE_ID_FRONT = "IdFront_";
    /**
     * 照片类型：证件照背面
     */
    public static final String IMAGE_TYPE_ID_BACK = "IdBack_";
    /**
     * 照片类型：退货
     */
    public static final String IMAGE_TUI_HUO = "IdTuiHuo";


    /**
     * 订单状态
     */
    public static final String ORDER_STATUS_ALL = "0";
    public static final String ORDER_STATUS_UNPAID = "1";
    public static final String ORDER_STATUS_UNSHIPPING = "2";
    public static final String ORDER_STATUS_UNRECEIVING = "3";
    public static final String ORDER_STATUS_COMPLETE = "4";


    // -------------------↑此分割线以上的配置信息，勿动！----------------------//

    // -------------------↓下列配置信息，如有需要请修改 ----------------------//
    /** WebService地址 */
    //public static final String WEBSERVICE_ADDRESS = "service.bopinjia.com";
    // public static final String WEBSERVICE_ADDRESS = "192.168.2.120:8012";

    /**
     * WebAPI 地址
     */
//	public static final String WEBAPI_ADDRESS = "http://newapi.bopinwang.com/";
    public static final String WEBAPI_ADDRESS = "http://testapi.bopinwang.com/";

    /**
     * WebAPI 地址
     */
    public static final String WEBAPI_BOPINWANG = "http://newapi.bopinwang.com/";
    /**
     * WEBAPI KEY
     */
    public static final String WEBAPI_KEY = "a3a665be98dc60e212365ee77979cdshbpj";

    /**
     * 命名空间
     */
    public static final String WEBSERVICE_NAME_SPACE = "http://www.bopinjia.com/";
    /**
     * 命名空间
     */
    public static final boolean WEBSERVICE_DEBUG = true;

    /**
     * 新闻类别：支持
     */
    public static final String NEWS_CLASS_SUPPORT = "support";
    /**
     * 新闻类别：服务条款
     */
    public static final String NEWS_CLASS_SERVICE = "service";

    /**
     * 验证码：倒计时读秒
     */
    public static final int MAX_INTERVAL_FOR_SECURITY = 60;

    /**
     * 行政区划管理文件(assets目录下)
     */
    public static final String FILE_PATH_DIVISIONS = "divisions.json";

    /**
     * 默认坐标系:百度标准
     */
    public static final String DEFAULT_COORDINATE_SYSTEM = "bd09ll";


    ///-------------------分销参数

    /**
     * 分销商门店
     */
    public static final String FXSMD = "fenxiaoshangmendian";
    /**
     * 分销商头像
     */
    public static final String FXSMDTS = "fenxiaoshangmendiantouxiang";
    /**
     * 分销商id
     */
    public static final String KEY_PREFERENCE_BINDING_GDSUSERID = "gdsuserid";
    /**
     * 分销商号
     */
    public static final String KEY_FXS_NUMBER = "fenxiaoshangnumber";
    /**
     * 分销商等级
     */
    public static final String KEY_FXS_LEVEL = "fenxiaoshangdengji";
    /**
     * 是否是分销商 0 不是 1 是
     */
    public static final String ISFXS = "isfenxiaoshang";
    /**
     * 分销商店铺名称
     */
    public static final String FXSSHOPNAME = "fenxiaoshangshopname";

    ////---------------


    // 微信支付:AppId
    public static final String WEIXIN_APP_ID = "wx56e280c9e1fca11c";
    // 支付宝:商户PID
    public static final String ALIPAY_PARTNER = "2088421636704234";
    // 支付宝:商户收款账号
    public static final String ALIPAY_SELLER = "Ken@bopinwang.com";

    // 支付宝:商户私钥，pkcs8格式
    public static final String ALIPAY_RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANsFa6FES/OyRCwxquYwbAQFYXeAZmQiAcVIRGXdbHju7ybKofqyb/hW5A80Bq3XR7XwUGxFkWHO+w6f3tVdr/q7imkpuHioA7Ux7imZfsyKKA9zNN2nznMVLiUxwnjJXBmiFG8J2G3++RFXF9/3ULC2RjaL2MGulC+r6Khb2Fr9AgMBAAECgYBj+klzSBXTnGB6PAHjKd9kxoADaN1UgCGGyMJQLY9CdO8+Kele4u0x05gvSHR0Dn1xk81iXy9KEo2P03KIu0eEuL5qnzyKoJZf6126E9rGv88WVMdq7gHGIUxxmjE5s9ndSJoRIwt6jgBPkMWoGv25RtiCk83Nt7qeKcjGUsX7EQJBAO8QFDASE/kE1K08rLQn3J0h9JPNgRNfBFNxJkxzaIhROIbeAzG4q/bq2g2VjWs0Vvggr3a6WRk2sqX12PQm9SsCQQDqidfaeW83bK4MtLov0KJFoKGiUu3nmy0lu+AiqFWGU3g9wWB4e4M+FDFSbrOdntRzERSm1EFPOguEDs8ISyx3AkA0m581rOTESfHbZZzD0HnWAmDmHbUn5CL5kc7RyBva07TSyQx+5prBLZFqp9yFGPGCjP7P69YrnEPYDa/+mowdAkEA5K0vvAGosZaSgSr5WkOG5HKBBj7rXWXMeD0dhU0xzj22QCg6wl5TEYUoreHn2SgSpnqh8yOC5heHQOQbck+xcwJAU5OH/r2xqWyL5NOADh/EYR8uAHMlLjYSapPQf4aLm/090ex9BWUeQTw8C6CWeWS+PhZKPdQzHOcQciDWbQaJnA==";
    // 支付宝:支付宝公钥
    public static final String ALIPAY_RSA_PUBLIC =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    /**
     * 是否是调试模式
     */
    public static final boolean IS_DEBUG = true;
}
