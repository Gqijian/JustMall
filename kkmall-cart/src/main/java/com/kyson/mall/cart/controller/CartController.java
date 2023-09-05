package com.kyson.mall.cart.controller;

import com.kyson.mall.cart.service.CartService;
import com.kyson.mall.cart.vo.Cart;
import com.kyson.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){

        return cartService.getUserCartItems();
    }


    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){

        cartService.deleteItem(skuId);
        return "redirect:http://cart/kysonmall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num")Integer num){

        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart/kysonmall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check")Integer check){
        cartService.checkItem(skuId, check);
        return "redirect:http://cart/kysonmall.com/cart.html";
    }

    /**
     *
     * 浏览器 cookie 存储了一个 user-key，标识身份，一个月过期
     * 如果第一次使用 京东购物车 都会给一个临时用户身份
     *
     * 登录使用 session
     * 未登录 按照 cookie 中的 user-key
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListString(Model model) throws ExecutionException, InterruptedException
    {

        //UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //System.out.println(userInfoTo);

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    /**
     *
     * RedirectAttributes.addFlashAttribute()
     * 模拟session 方式，将数据放在session 中，可以页面取出来，只能取出一次
     *
     * attributes.addAttribute("skuId", skuId);将数据放在url后面
     *
     *
     * @param skuId
     * @param num
     * @param attributes
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num")Integer num, RedirectAttributes attributes) throws ExecutionException, InterruptedException
    {

        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId", skuId);


        return "redirect:http://cart/kysonmall.com/addToCartSuccess.html";
    }

    /**
     *
     * 跳转购物车成功页 防止刷新无限添加商品
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam Long skuId, Model model){

        //重定向到成功页面再次查询购物车数据
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);

        return "success";
    }
}
