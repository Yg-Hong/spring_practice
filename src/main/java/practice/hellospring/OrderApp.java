package practice.hellospring;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import practice.hellospring.member.Grade;
import practice.hellospring.member.Member;
import practice.hellospring.member.MemberService;
import practice.hellospring.member.MemberServiceImpl;
import practice.hellospring.order.Order;
import practice.hellospring.order.OrderService;
import practice.hellospring.order.OrderServiceImpl;

public class OrderApp {
    public static void main(String[] args) {
//        AppConfig appConfig = new AppConfig();
//        MemberService memberService = appConfig.memberService();
//        OrderService orderService = appConfig.orderService();

        /*
        * 코드가 복잡해진 것 같은 데 스프링 컨테이너를 사용하면 어떤 장점이 있을까?
        *  */
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
        OrderService orderService = applicationContext.getBean("orderService", OrderService.class);

        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "itemA", 20000);

        System.out.println("order = " + order);
    }
}
