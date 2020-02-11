package com.grain.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.grain.annotations.LoginRequrired;
import com.grain.api.bean.order.OmsOrder;
import com.grain.api.bean.pay.PaymentInfo;
import com.grain.api.service.order.OrderService;
import com.grain.api.service.pay.PaymentService;
import com.grain.payment.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
@Controller
public class PaymentController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private PaymentService paymentService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/mx/submit")
    @LoginRequrired(loginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap mapMode) {
        return null;
    }

    @RequestMapping("/alipay/submit")
    @LoginRequrired(loginSuccess = true)
    public String alipay(String outTradeNo, BigDecimal taltalAmount, HttpServletRequest request, ModelMap mapMode) {
        //获得一个支付宝请求客户端（他不是一个链接，而是一个封装好的http的表单请求）
        String from = null;
        AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest();//创建对应的api请求
        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String, Object> map = new HashMap<>();
        map.put("out_trader_no", outTradeNo);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("tatal_amount", taltalAmount);
        map.put("subject", "grain商城");
        String parm = JSON.toJSONString(map);
        alipayRequest.setBizContent(parm);
        try {
            from = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
            System.out.println(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //生成并保存用户信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCallbackTime(LocalDateTime.now());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("grain商城商品一件");
        paymentInfo.setTotalAmount(taltalAmount);
        paymentService.savePaymentInfo(paymentInfo);
        //提交请求到支付宝
        return from;
    }

    @RequestMapping("/alipay/callback/return")
    @LoginRequrired(loginSuccess = true)
    public String aliPayCallBackReturn(HttpServletRequest request, ModelMap modelMap) {
        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();
        //通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法延签
        if (!StringUtils.isEmpty(sign)) {
            //延签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符
            paymentInfo.setCreateTime(LocalDateTime.now());
            //更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }
        return "finish";
    }


    @RequestMapping("/index")
    @LoginRequrired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal tatalAmount, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("meberId");
        String nickname = (String) request.getAttribute("nickname");
        modelMap.put("nickname", nickname);
        modelMap.put("outTradeNo", outTradeNo);
        modelMap.put("tatalAmount", tatalAmount);
        return "index";
    }


}
