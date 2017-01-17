package com.pxy.action;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;




import com.opensymphony.xwork2.ActionContext;
import com.pxy.model.Cart;
import com.pxy.model.CartItem;
import com.pxy.model.Order;
import com.pxy.model.OrderItem;
import com.pxy.model.User;
import com.pxy.service.OrderService;
import com.pxy.service.impl.OrderServiceImpl;
import com.pxy.utils.PaymentUtil;

/**
 * 订单的Action
 */
public class OrderAction extends SuperAction {
	private Order order;
	private String pd_FrpId;
	// 付款成功后的需要的参数:
	private String r3_Amt;
	private String r6_Order;
	// 
	private Integer oid;
	
	// 注入OrderService
	private OrderService orderService = new OrderServiceImpl();
	
	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
	public void setOid(Integer oid) {
		this.oid = oid;
	}

	public void setR3_Amt(String r3_Amt) {
		this.r3_Amt = r3_Amt;
	}

	public void setR6_Order(String r6_Order) {
		this.r6_Order = r6_Order;
	}

	public void setPd_FrpId(String pd_FrpId) {
		this.pd_FrpId = pd_FrpId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	

	// 保存订单执行的方法;
	public String saveOrder(){

		order = new Order();
		/****************封装订单的数据*********/
		order.setOrdertime(new Date());
		order.setState(1); // 1 未付款   2 已经付款.  3.已经发货   4 已经收货.
		// 有些数据需要从购物车中获取:
		// 获得购物车:
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart == null){
			this.addActionMessage("您还没有购物!请先去购物!");
			return "msg";
		}
		//将购物车总额赋值给订单
		order.setTotal(cart.getTotal());
		// 订单所属的用户:
		User loginUser = (User) session.getAttribute("loginUser");
		if(loginUser == null){
			this.addActionMessage("您还没有登录!请先去登录!");
			return "msg";
		}
		order.setUser(loginUser);
		/********************封装订单项数据*************/
		// 订单项数据从 购物项的数据获得.
		for (CartItem cartItem : cart.getCartItems()) {
			//每一个订单项
			OrderItem orderItem = new OrderItem();
			orderItem.setCount(cartItem.getCount());
			orderItem.setSubtotal(cartItem.getSubtotal());
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setOrder(order);
			//订单将每一个订单项装进来
			order.getOrderItems().add(orderItem);
		}
		// 清空购物车.
		cart.clearCart();
		
		// 保存订单:
		Integer oid = orderService.save(order);
		order.setOid(oid);
		return "saveOrderSuccess";
	}
	
	
	/**
	 * 为订单付款的方法:
	 * @throws IOException 
	 */
	public String payOrder() throws IOException{
		// 修改订单:
		// 查询这个id的订单:
		//Order currOrder = orderService.findByOid(order.getOid());
		//currOrder.setAddr(order.getAddr());
		//currOrder.setUserName(order.getUserName());
		//currOrder.setPhone(order.getPhone());
		
		//orderService.update(currOrder);
		// 付款:
		// 定义付款的参数:
		/*String p0_Cmd = "Buy";
		String p1_MerId = "10001126856";
		String p2_Order = order.getOid().toString();
		String p3_Amt = "0.01";
		String p4_Cur = "CNY";
		String p5_Pid = "";
		String p6_Pcat = "";
		String p7_Pdesc = "";
		String p8_Url = "http://192.168.40.110:8080/shop/order_callBack.action";
		String p9_SAF = "";
		String pa_MP = "";
		String pr_NeedResponse = "1";
		String keyValue = "69cl522AV6q613Ii4W6u8K6XuW8vM1N6bFgyv769220IuYe9u37N4y7rI4Pl";
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt, p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,pd_FrpId , pr_NeedResponse, keyValue);
		
		StringBuffer sb = new StringBuffer("https://www.yeepay.com/app-merchant-proxy/node?");
		sb.append("p0_Cmd=").append(p0_Cmd).append("&");
		sb.append("p1_MerId=").append(p1_MerId).append("&");
		sb.append("p2_Order=").append(p2_Order).append("&");
		sb.append("p3_Amt=").append(p3_Amt).append("&");
		sb.append("p4_Cur=").append(p4_Cur).append("&");
		sb.append("p5_Pid=").append(p5_Pid).append("&");
		sb.append("p6_Pcat=").append(p6_Pcat).append("&");
		sb.append("p7_Pdesc=").append(p7_Pdesc).append("&");
		sb.append("p8_Url=").append(p8_Url).append("&");
		sb.append("p9_SAF=").append(p9_SAF).append("&");
		sb.append("pa_MP=").append(pa_MP).append("&");
		sb.append("pd_FrpId=").append(pd_FrpId).append("&");
		sb.append("pr_NeedResponse=").append(pr_NeedResponse).append("&");
		sb.append("hmac=").append(hmac);
		System.out.println(sb.toString());
		HttpServletResponse response = ServletActionContext.getResponse();
		response.sendRedirect(sb.toString());*/
		return "payOrderSuccess";
	}
	
	
	// 付款成功后的回调方法
	public String callBack(){
		Order currOrder = orderService.findByOid(Integer.parseInt(r6_Order));
		currOrder.setState(2);// 修改订单状态.
		orderService.update(currOrder);
		
		this.addActionMessage("订单付款成功!订单号:"+r6_Order+" 付款金额:"+r3_Amt);
		return "msg";
	}
	
	//按用户id查询订单:
	public String findByUid(){
		// 获得用户对象:
		User existUser = (User)session.getAttribute("existUser");
		List<Order> oList = orderService.findByUid(existUser);
		// 压栈
		ActionContext.getContext().getValueStack().set("oList", oList);
		return "findByUidSuccess";
	}
	
	//查询订单:
	public String findByOid(){
		order = orderService.findByOid(oid);
		return "findByOidSuccess";
	}
	
	public String findAllOrder(){
		List<Order> allOrder = orderService.findAllOrder();
		// 压栈
		ActionContext.getContext().getValueStack().set("allOrder", allOrder);
		return "findAllOrderSuccess";
	}
	public String myOrder(){
		return "myOrder";
	}
}
