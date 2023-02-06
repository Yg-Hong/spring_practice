package practice.hellospring.order;

import practice.hellospring.discount.DiscountPolicy;
import practice.hellospring.discount.FixDiscountPolicy;
import practice.hellospring.member.Member;
import practice.hellospring.member.MemberRepository;
import practice.hellospring.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
