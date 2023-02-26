package practice.hellospring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "practice.hellospring.member",
        basePackageClasses = AutoAppConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)   // <- 전에 학습을 위해서 만들어 두었던 AppConfig, TestConfig 등 앞서 만들어두었던 설정 정보도 함께 등록되고, 실행되어 버린다.
public class AutoAppConfig {

}
/*
 ComponentScan에서 "basePackages", "basePackageClasses"를 지정하지 않으면
 @ComponentScan이 붙은 설정 정보 클래스의 패키지가 시작 위치가 된다.

 권장하는 방법
 패키지 위치를 지정하지 않고, 설정 정보 클래스의 위치를 프로젝트 최상단에 두는 것이다.
 최근 스프링 부트도 이 방법을 기본으로 제공한다.
 */