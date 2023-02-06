package practice.hellospring;

import practice.hellospring.member.Grade;
import practice.hellospring.member.Member;
import practice.hellospring.member.MemberService;
import practice.hellospring.member.MemberServiceImpl;
import practice.hellospring.order.Order;
import practice.hellospring.order.OrderService;
import practice.hellospring.order.OrderServiceImpl;

public class OrderApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        Long memberId = 1L;
        Member member = new Member(memberId, "mamberA", Grade.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "itemA", 10000);

        System.out.println("order = " + order);
    }
}
