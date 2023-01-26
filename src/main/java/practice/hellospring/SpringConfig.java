package practice.hellospring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practice.hellospring.repository.JpaMemberRepository;
import practice.hellospring.repository.MemberRepository;

@Configuration
public class SpringConfig {
/*

    private final MemberRepository memberRepository;

    @Autowired
    public SpringConfig(@Qualifier("memoryMemberRepository") MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
*/

    /*

    @Bean
    public MembenrService memberService(){
        return new MemberService(memberRepository());
    }
    */
    //@Bean
    //public JpaMemberRepository memberRepository(){
    //    return new JpaMemberRepository(em);
    //}
}
