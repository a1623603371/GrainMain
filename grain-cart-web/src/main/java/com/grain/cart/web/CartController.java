package com.grain.cart.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.grain.annotations.LoginRequrired;
import com.grain.api.bean.cart.OmsCartItem;
import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.service.cart.CartService;
import com.grain.api.service.manage.SkuService;

import com.grain.utils.CookieUtil;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;

import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CartController {
    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    @RequestMapping("checkCart")
    @LoginRequrired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap map) {
        String memberId = "";
        //调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        //将数据渲染给内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        map.put("cartList", omsCartItems);
        return "cartListInner";

    }

    @RequestMapping("cartList")
    @LoginRequrired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = "";
        if (!StringUtils.isEmpty(memberId)) {
            //已经登录查询db
            omsCartItems = cartService.cartList(memberId);

        } else {
            //没有登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (!StringUtils.isEmpty(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        omsCartItems.forEach(omsCartItem -> {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        });
        modelMap.put("cartList", omsCartItems);
        //被勾选商品的总额
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    @RequestMapping(value = "addToCart", method = RequestMethod.POST)
    @LoginRequrired(loginSuccess = false)
    public String addToCart(String skuId, Integer quantity, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, "");
        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(LocalDateTime.now());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(LocalDateTime.now());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("111111111222333");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (StringUtils.isEmpty(memberId)) {
            //用户没有登录
            //cookie里原有的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isEmpty(cartListCookie)) {
                //如果为空添加
                omsCartItems.add(omsCartItem);
            } else {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exist = if_cart_exist(omsCartItems, omsCartItem);
                if (exist) {
                    //之前添加过,增加数量
                    omsCartItems.forEach(omsCartItem1 -> {
                        if (omsCartItem1.equals(omsCartItem.getProductSkuId())) {
                            omsCartItem1.setQuantity(omsCartItem1.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    });
                } else {
                    //之前没有添加，新增单前购物车
                    omsCartItems.add(omsCartItem);
                }

            }
            //更新cookie
            CookieUtil.setCookieValue(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);

        } else {
            //用户已登录
            //从db中查询出购物车的数据
            OmsCartItem omsCartItemFromDB = cartService.ifCartExistByUser(omsCartItem.getMemberId(), omsCartItem.getProductSkuId());
            if (ObjectUtils.isEmpty(omsCartItemFromDB)) {
                //该用户没填加过单前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickname);
                omsCartItem.setQuantity(omsCartItem.getQuantity());
                omsCartItems.add(omsCartItem);

            } else {
                //该用户添加了单前商品
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDB);
            }
            //同步缓存
            cartService.flushCartCache(memberId);

        }
        return "redirect:/success.html";
    }

    /**
     * 判断cookie中是否存在
     *
     * @param omsCartItems
     * @param omsCartItem
     * @return
     */
    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = false;
        Optional<OmsCartItem> itemOptional = omsCartItems.stream()
                .filter(omsCartItem1 -> omsCartItem1.getProductSkuId().
                        equals(omsCartItem.getProductSkuId())).findFirst();
        if (itemOptional.isPresent()) {
            b = true;
        }

        return b;
    }

}
