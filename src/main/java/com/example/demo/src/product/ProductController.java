package com.example.demo.src.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.product.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController // Rest API 또는 WebAPI를 개발하기 위한 어노테이션. @Controller + @ResponseBody 를 합친것.
                // @Controller      [Presentation Layer에서 Contoller를 명시하기 위해 사용]
                //  [Presentation Layer?] 클라이언트와 최초로 만나는 곳으로 데이터 입출력이 발생하는 곳
                //  Web MVC 코드에 사용되는 어노테이션. @RequestMapping 어노테이션을 해당 어노테이션 밑에서만 사용할 수 있다.
                // @ResponseBody    모든 method의 return object를 적절한 형태로 변환 후, HTTP Response Body에 담아 반환.
@RequestMapping("/app/products")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/Products)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class ProductController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final ProductProvider productProvider;
    @Autowired
    private final ProductService productService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public ProductController(ProductProvider productProvider, ProductService productService, JwtService jwtService) {
        this.productProvider = productProvider;
        this.productService = productService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************

    /**
     * 모든 회원들의  조회 API
     * [GET] /Products
     * <p>
     * 또는
     * <p>
     * 해당 닉네임을 같는 유저들의 정보 조회 API
     * [GET] /Products? NickName=
     */
    //Query String
    @ResponseBody   // return되는 자바 객체를 JSON으로 바꿔서 HTTP body에 담는 어노테이션.
    //  JSON은 HTTP 통신 시, 데이터를 주고받을 때 많이 쓰이는 데이터 포맷.
    @GetMapping("") // (GET) 127.0.0.1:9000/app/Products
    // GET 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<List<GetProductRes>> getProducts(@RequestParam(required = false) String title) {
        //  @RequestParam은, 1개의 HTTP Request 파라미터를 받을 수 있는 어노테이션(?뒤의 값). default로 RequestParam은 반드시 값이 존재해야 하도록 설정되어 있지만, (전송 안되면 400 Error 유발)
        //  지금 예시와 같이 required 설정으로 필수 값에서 제외 시킬 수 있음
        //  defaultValue를 통해, 기본값(파라미터가 없는 경우, 해당 파라미터의 기본값 설정)을 지정할 수 있음
        try {
            if (title == null) { // query string인 nickname이 없을 경우, 그냥 전체 유저정보를 불러온다.
                List<GetProductRes> getProductRes = productProvider.getProducts();
                return new BaseResponse<>(getProductRes);
            }
            // query string인 nickname이 있을 경우, 조건을 만족하는 유저정보들을 불러온다.
            List<GetProductRes> getProductsRes = productProvider.getProductsByTitle(title);
            return new BaseResponse<>(getProductsRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * /**
     * 회원 1명 조회 API
     * [GET] /Products/:ProductIdx
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{productIdx}") // (GET) 127.0.0.1:9000/app/Products/:ProductIdx
    public BaseResponse<GetProductRes> getProduct(@PathVariable("productIdx") int productIdx) {
        // @PathVariable RESTful(URL)에서 명시된 파라미터({})를 받는 어노테이션, 이 경우 ProductId값을 받아옴.
        //  null값 or 공백값이 들어가는 경우는 적용하지 말 것
        //  .(dot)이 포함된 경우, .을 포함한 그 뒤가 잘려서 들어감
        // Get Products
        try {
            GetProductRes getProductRes = productProvider.getProduct(productIdx);
            return new BaseResponse<>(getProductRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }


    }

    /**
     * 물품등록 API
     * [POST] /products
     */
    // Body
    @ResponseBody
    @PostMapping("/post-up")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostProductRes> createProduct(@RequestBody PostProductReq postProductReq) {
        try {
            PostProductRes postPrpductRes = productService.createProduct(postProductReq);
            return new BaseResponse<>(postPrpductRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 유저정보변경 API
     * [PATCH] /Products/:ProductIdx
     */
    @ResponseBody
    @PatchMapping("/{ProductIdx}")
    public BaseResponse<String> modifyProductName(@PathVariable("ProductIdx") int productIdx, @RequestBody Product product) {
        try {
/**
  *********** 해당 부분은 7주차 - JWT 수업 후 주석해체 해주세요!  ****************
            //jwt에서 idx 추출.
            int ProductIdxByJwt = jwtService.getProductIdx();
            //ProductIdx와 접근한 유저가 같은지 확인
            if(ProductIdx != ProductIdxByJwt){
                return new BaseResponse<>(INVALID_Product_JWT);
            }
            //같다면 유저네임 변경
  **************************************************************************
 */
            PatchProductReq patchProductReq = new PatchProductReq(productIdx, product.getTitle(), product.getContent(), product.getPrice());
            productService.modifyProductName(patchProductReq);

            String result = "회원정보가 수정되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**

     * [DELETE] /{productIdx}
     */
    // Path-variable
    @ResponseBody
    @DeleteMapping("/{productIdx}") // (GET) 127.0.0.1:9000/app/Products/:ProductIdx
    public BaseResponse<Integer> DeleteProduct(@PathVariable("productIdx") int productIdx){
        try {
            int result = productService.deleteProduct(productIdx);
            return new BaseResponse<>(result);
            } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }
}
