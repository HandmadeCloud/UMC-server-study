package com.example.demo.src.product;


import com.example.demo.src.product.model.*;
import com.example.demo.src.product.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class ProductDao {

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // ******************************************************************************

    /**
     * DAO관련 함수코드의 전반부는 크게 String ~~~Query와 Object[] ~~~~Params, jdbcTemplate함수로 구성되어 있습니다.(보통은 동적 쿼리문이지만, 동적쿼리가 아닐 경우, Params부분은 없어도 됩니다.)
     * Query부분은 DB에 SQL요청을 할 쿼리문을 의미하는데, 대부분의 경우 동적 쿼리(실행할 때 값이 주입되어야 하는 쿼리) 형태입니다.
     * 그래서 Query의 동적 쿼리에 입력되어야 할 값들이 필요한데 그것이 Params부분입니다.
     * Params부분은 클라이언트의 요청에서 제공하는 정보(~~~~Req.java에 있는 정보)로 부터 getXXX를 통해 값을 가져옵니다. ex) getEmail -> email값을 가져옵니다.
     *      Notice! get과 get의 대상은 카멜케이스로 작성됩니다. ex) item -> getItem, password -> getPassword, email -> getEmail, ProductIdx -> getProductIdx
     * 그 다음 GET, POST, PATCH 메소드에 따라 jabcTemplate의 적절한 함수(queryForObject, query, update)를 실행시킵니다(DB요청이 일어납니다.).
     *      Notice!
     *      POST, PATCH의 경우 jdbcTemplate.update
     *      GET은 대상이 하나일 경우 jdbcTemplate.queryForObject, 대상이 복수일 경우, jdbcTemplate.query 함수를 사용합니다.
     * jdbcTeplate이 실행시킬 때 Query 부분과 Params 부분은 대응(값을 주입)시켜서 DB에 요청합니다.
     * <p>
     * 정리하자면 < 동적 쿼리문 설정(Query) -> 주입될 값 설정(Params) -> jdbcTemplate함수(Query, Params)를 통해 Query, Params를 대응시켜 DB에 요청 > 입니다.
     * <p>
     * <p>
     * DAO관련 함수코드의 후반부는 전반부 코드를 실행시킨 후 어떤 결과값을 반환(return)할 것인지를 결정합니다.
     * 어떠한 값을 반환할 것인지 정의한 후, return문에 전달하면 됩니다.
     * ex) return this.jdbcTemplate.query( ~~~~ ) -> ~~~~쿼리문을 통해 얻은 결과를 반환합니다.
     */

    /**
     * 참고 링크
     * https://jaehoney.tistory.com/34 -> JdbcTemplate 관련 함수에 대한 설명
     * https://velog.io/@seculoper235/RowMapper%EC%97%90-%EB%8C%80%ED%95%B4 -> RowMapper에 대한 설명
     */

    // 올릴 물품 생성
    public int createProduct(PostProductReq PostProductReq) {
        String createProductQuery = "insert into Product (categoryIdx, title, price,content) VALUES (?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createProductParams = new Object[]{PostProductReq.getCategoryIdx(), PostProductReq.getTitle(), PostProductReq.getPrice(), PostProductReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createProductQuery, createProductParams);
        // email -> postProductReq.getEmail(), password -> postProductReq.getPassword(), nickname -> postProductReq.getNickname() 로 매핑(대응)시킨다음 쿼리문을 실행한다.
        // 즉 DB의 Product Table에 (email, password, nickname)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 ProductIdx번호를 반환한다.
    }

    // 물품정보 변경
    public int modifyProductName(PatchProductReq patchProductReq) {
        String modifyProductNameQuery = "update Product set title = ? where ProductIdx = ? "; // 해당 ProductIdx를 만족하는 Product를 해당 nickname으로 변경한다.
        Object[] modifyProductNameParams = new Object[]{patchProductReq.getTitle(), patchProductReq.getProductIdx()}; // 주입될 값들(nickname, ProductIdx) 순

        return this.jdbcTemplate.update(modifyProductNameQuery, modifyProductNameParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0) 
    }
    // 물품정보 삭제
    public int deleteProduct(int productIdx) {
        String deleteProductNameQuery = "delete from Product where ProductIdx = ? "; // 해당 ProductIdx를 만족하는 Product를 해당 nickname으로 변경한다.
        int deleteProductParams = productIdx;
        return this.jdbcTemplate.update(deleteProductNameQuery, deleteProductParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }


    // Product 테이블에 존재하는 전체 정보 조회
    public List<GetProductRes> getProducts() {
        String getProductsQuery = "select * from Product"; //Product 테이블에 존재하는 모든정보를 조회하는 쿼리
        return this.jdbcTemplate.query(getProductsQuery,
                (rs, rowNum) -> new GetProductRes(
                        rs.getInt("productIdx"),
                        rs.getInt("userIdx"),
                        rs.getInt("categoryIdx"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("price"))


// RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
        ); // 복수개의 회원정보들을 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보)의 결과 반환(동적쿼리가 아니므로 Parmas부분이 없음)
    }

    // 해당 title을 갖는 정보 조회
    public List<GetProductRes> getProductsByTitle(String title) {
        String getProductsByTitleQuery = "select * from Product where title =?"; // 해당 이메일을 만족하는 유저를 조회하는 쿼리문
        String getProductsByTitleParams = title;
        return this.jdbcTemplate.query(getProductsByTitleQuery,
                (rs, rowNum) -> new GetProductRes(
                        rs.getInt("productIdx"),
                        rs.getInt("userIdx"),
                        rs.getInt("categoryIdx"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("price")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getProductsByTitleParams); // 해당 닉네임을 갖는 모든 Product 정보를 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 해당 ProductIdx를 갖는 유저조회
    public GetProductRes getProduct(int productIdx) {
        String getProductQuery = "select * from Product where productIdx = ?"; // 해당 ProductIdx를 만족하는 유저를 조회하는 쿼리문
        int getProductParams = productIdx;
        return this.jdbcTemplate.queryForObject(getProductQuery,
                (rs, rowNum) -> new GetProductRes(
                        rs.getInt("productIdx"),
                        rs.getInt("userIdx"),
                        rs.getInt("categoryIdx"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("price")),  // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getProductParams); // 한 개의 회원정보를 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }
}
