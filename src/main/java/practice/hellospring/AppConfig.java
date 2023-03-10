package practice.hellospring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practice.hellospring.discount.DiscountPolicy;
import practice.hellospring.discount.FixDiscountPolicy;
import practice.hellospring.discount.RateDiscountPolicy;
import practice.hellospring.member.MemberRepository;
import practice.hellospring.member.MemberService;
import practice.hellospring.member.MemberServiceImpl;
import practice.hellospring.member.MemoryMemberRepository;
import practice.hellospring.order.OrderService;
import practice.hellospring.order.OrderServiceImpl;


// AppConfig는 애플리케이션의 실제 동작에 필요한 "구현 객체를 생성"한다.
// 또한 생성한 객체 인스턴스의 참조(레퍼런스)를 "생성자를 통해서 주입(연결)"해준다.
// 단순히 의존관계를 주입하는 것보다 역할이 드러나도록 리팩토링하는 것이 중요하다.
@Configuration
public class AppConfig {

    // @Bean memberService -> new MemberServiceImpl() -> memberRepository() -> new MemoryMemberRepository()
    // @Bean orderService -> ~~ -> new MemoryMemberRepository()

    //호출 순서를 따라가보면 찍히는 로그
    //call AppConfig.memberService
    //call AppConfig.memberRepository
    //call AppConfig.memberRepository
    //call AppConfig.orderService
    //call AppConfig.memberRepository

    //실제로 찍힌 로그
    //call AppConfig.memberService
    //call AppConfig.memberRepository
    //call AppConfig.orderService
    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        //return new FixDiscountPolicy();
        return new RateDiscountPolicy();
    }

}
