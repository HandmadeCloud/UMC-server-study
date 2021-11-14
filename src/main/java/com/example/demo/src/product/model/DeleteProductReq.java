package com.example.demo.src.product.model;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, nickname)를 받는 생성자를 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 해당 클래스의 파라미터가 없는 생성자를 생성, 접근제한자를 PROTECTED로 설정.


public class DeleteProductReq {
    private int productIdx;
}
