package practice.hellospring.singleton;

public class StatefulService {

    /*
    * 클라이언트의 의도는 값을 저장하는 필드(price)를 두고
    * 주문시에 그 값을 저장
    */

    private int price_state; // 상태를 유지하는 필드 <- 이 필드는 없어져야 한다!!

    public int order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        //this.price_state = price_state;     // 여기가 문제!
        return price;
    }

    public int getPrice() {
       return price_state;
    }

}
