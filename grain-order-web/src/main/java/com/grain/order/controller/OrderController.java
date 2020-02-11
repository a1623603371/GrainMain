package com.grain.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.annotations.LoginRequrired;
import com.grain.api.bean.cart.OmsCartItem;
import com.grain.api.bean.order.OmsOrder;
import com.grain.api.bean.order.OmsOrderItem;
import com.grain.api.bean.user.UmsMemberReceiveAddress;
import com.grain.api.service.cart.CartService;
import com.grain.api.service.manage.SkuService;
import com.grain.api.service.order.OrderService;
import com.grain.api.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 */
@Controller
public class OrderController {
    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private SkuService skuService;

    @RequestMapping("/submitOrder")
    @LoginRequrired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("oickname");
        //检查交易码
        String success = orderService.ckeckTrandeCode(memberId, nickname);
        if ("success".equals(success)) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCommentTime(LocalDateTime.now());
            omsOrder.setDiscountAmount(null);
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快发货");
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();//将毫秒时间戳拼接到外部订单号
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            outTradeNo = outTradeNo + LocalDateTime.now().format(dtf);//将时间字符拼接到外部定单号
            omsOrder.setOrderSn(outTradeNo);//外部订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            //当天日期加一天 一天后配送
            omsOrder.setReceiveTime(LocalDateTime.now().plusHours(24));
            omsOrder.setOrderType(0);
            omsOrder.setStatus(0);
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);
            //根据用户id获得要购买的商品列表（购物车），和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    //获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //检价
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (b == false) {
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    //验库存，远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setOrderSn(outTradeNo);//外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("11111111111111111");
                    omsOrderItem.setProductId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductSn(omsCartItem.getProductSn());
                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);
            //将订单和订单详情写入库
            orderService.saveOrder(omsOrder);
            //重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:hhtp://127.0.0.1:8910/index");
            mv.addObject("outTradeNo", outTradeNo);
            mv.addObject("totalAmount", totalAmount);
            return mv;
        } else {
            //重定向到支付系统
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

    @RequestMapping("/toTrade")
    @LoginRequrired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //收件 人地址列表
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);
        //将购物车集合转化为页面计算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        omsCartItems.forEach(omsCartItem -> {
            //循环一个购物车对象，就封装一个商品的详情到OmsOrderItem
            if ("1".equals(omsCartItem.getIsChecked())) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        });
        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("userAddressList", umsMemberReceiveAddresses);
        modelMap.put("totalAmount", getToTalAmount(omsCartItems));
        //生成交易码，为了在提交订单时做交易码的效验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";

    }

    private BigDecimal getToTalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }

        }
        return totalAmount;
    }
}
