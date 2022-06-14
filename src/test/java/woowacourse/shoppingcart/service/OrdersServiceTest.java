package woowacourse.shoppingcart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static woowacourse.fixture.Fixture.PRICE;
import static woowacourse.fixture.Fixture.PRODUCT_NAME;
import static woowacourse.fixture.Fixture.QUANTITY;
import static woowacourse.fixture.Fixture.TEST_EMAIL;
import static woowacourse.fixture.Fixture.TEST_PASSWORD;
import static woowacourse.fixture.Fixture.TEST_USERNAME;
import static woowacourse.fixture.Fixture.THUMBNAIL_URL;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import woowacourse.shoppingcart.dao.CartItemDao;
import woowacourse.shoppingcart.dao.CustomerDao;
import woowacourse.shoppingcart.dao.ProductDao;
import woowacourse.shoppingcart.domain.Customer;
import woowacourse.shoppingcart.domain.Product;
import woowacourse.shoppingcart.dto.OrdersDetailDto;
import woowacourse.shoppingcart.dto.OrdersResponseDto;
import woowacourse.shoppingcart.exception.InvalidQuantityException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrdersServiceTest {

    private final int ORDER_COUNT = 1;

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private CartItemDao cartItemDao;

    private Product product;
    private Long customerId;


    @BeforeEach
    void setUp() {
        final Product tempProduct = Product.createWithoutId(PRODUCT_NAME, PRICE, THUMBNAIL_URL, QUANTITY);
        final Long productId = productDao.save(tempProduct);
        product = new Product(
                productId,
                tempProduct.getName(),
                tempProduct.getPrice(),
                tempProduct.getThumbnailUrl(),
                tempProduct.getQuantity()
        );
        customerId = customerDao.save(Customer.createWithoutId(TEST_EMAIL, TEST_PASSWORD, TEST_USERNAME));
        cartItemDao.addCartItem(customerId, productId, ORDER_COUNT);
    }

    @Test
    @DisplayName("사용자의 주문을 처리한다.")
    void order() {
        // when
        final Long ordersId = ordersService.order(List.of(product.getId()), customerId);

        // then
        final OrdersResponseDto ordersResponseDto = ordersService.findOrders(ordersId);
        final OrdersDetailDto ordersDetailDto = ordersResponseDto.getOrdersDetails().get(0);

        assertAll(
                () -> assertThat(ordersDetailDto.getProduct().getProductId()).isEqualTo(product.getId()),
                () -> assertThat(ordersDetailDto.getCount()).isEqualTo(ORDER_COUNT)
        );
    }

    @Test
    @DisplayName("사용자의 주문을 처리시 상품의 주문 수량만큼 상품 재고가 감소한다.")
    void order_decreaseQuantity() {
        // when
        ordersService.order(List.of(product.getId()), customerId);

        // then
        final int decreasedQuantity = productDao
                .findProductById(product.getId())
                .get()
                .getQuantity();

        assertThat(decreasedQuantity).isEqualTo(QUANTITY - ORDER_COUNT);
    }

    @Test
    @DisplayName("사용자의 주문을 처리시 상품의 재고보다 주문 수량이 높으면 예외가 발생한다.")
    void order_orderCountOver() {
        // given
        final Product product2 = Product.createWithoutId("test2", 1000, "testUrl", QUANTITY);
        final Long product2Id = productDao.save(product2);
        cartItemDao.addCartItem(customerId, product2Id, QUANTITY + 1);

        assertThatThrownBy(() -> ordersService.order(List.of(product2Id), customerId))
                .isInstanceOf(InvalidQuantityException.class);
    }
}