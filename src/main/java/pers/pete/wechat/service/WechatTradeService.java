package pers.pete.wechat.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.util.StringUtils;
import pers.pete.wechat.request.BaseParam;
import pers.pete.wechat.request.OrderqueryParam;
import pers.pete.wechat.request.RefundParam;
import pers.pete.wechat.request.UnifiedorderParam;
import pers.pete.wechat.response.Orderquery;
import pers.pete.wechat.response.Refund;
import pers.pete.wechat.response.UnifiedOrder;
import pers.pete.wechat.utils.SignType;
import pers.pete.wechat.utils.WechatConstants;
import pers.pete.wechat.utils.WechatUtil;

import java.io.IOException;

public class WechatTradeService {

  private String appid;

  private String mchId;

  private String apikey;

  private String notifyUrl;

  public WechatTradeService(String appid, String mchId, String apikey, String notifyUrl) {
    this.appid = appid;
    this.mchId = mchId;
    this.apikey = apikey;
    this.notifyUrl = notifyUrl;
  }

  /**
   * 统一下单.
   * <p>
   * https://pay.weixin.qq.com/wiki/doc/api/native_sl.php?chapter=9_1
   */
  public UnifiedOrder unifiedOrder(UnifiedorderParam param) {
    param.setAppid(appid);
    param.setMchId(mchId);
    if (!StringUtils.isEmpty(notifyUrl)) {
      param.setNotifyUrl(notifyUrl);
    }
    return post(param, WechatConstants.UNIFIED_ORDER_URL, UnifiedOrder.class);
  }

  /**
   * 查询订单.
   * <p>
   * https://pay.weixin.qq.com/wiki/doc/api/native_sl.php?chapter=9_2
   */
  public Orderquery orderquery(OrderqueryParam param) {
    param.setAppid(appid);
    param.setMchId(mchId);
    return post(param, WechatConstants.ORDER_QUERY_URL, Orderquery.class);
  }

  /**
   * 申请退款.
   * <p>
   * https://pay.weixin.qq.com/wiki/doc/api/native_sl.php?chapter=9_4
   */
  public Refund refund(RefundParam param) {
    return post(param, WechatConstants.REFUND_URL, Refund.class);
  }

  private <T, P extends BaseParam> T post(P param, String url, Class<T> cls) {
    param.setSign(WechatUtil.generateSignature(param, apikey, SignType.MD5));

    OkHttpClient okHttpClient = new OkHttpClient();
    String xml = WechatUtil.convertToXml(param);
    System.out.println(xml);
    MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TEXT, xml);
    // Request
    Request request = new Request.Builder().url(url).post(requestBody).build();
    // Response
    Response response;
    try {
      response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()) {
        return null;
      }
      // ResponseBody
      ResponseBody responseBody = response.body();
      if (responseBody == null) {
        throw new RuntimeException("HTTP response body is empty.");
      }
      return WechatUtil.converyToJavaBean(responseBody.string(), cls);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
