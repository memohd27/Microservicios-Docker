package se.magnus.microservices.composite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import static java.util.Collections.singletonList;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	@Autowired 
	private WebTestClient client;
	private static final int PRODUCT_ID_OK=1;
	private static final int PRODUCT_ID_NOT_FOUND=2;
	private static final int PRODUCT_ID_INVALID=3;
	
	@MockBean
	private ProductCompositeIntegration compositeIntegration;
	
	@BeforeEach
	void  setup() {
		when(compositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK,"name",1,"mock-address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
		.thenReturn(singletonList( new Recommendation(PRODUCT_ID_OK,1,"author",1,"content","mock-address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
		.thenReturn(singletonList( new Review(PRODUCT_ID_OK,1,"author","subject","content","mock-address")));
	}
	
	
	@Test
	void contextLoads() {
	}
	
	@Test
	public void getProductById() {
		client.get()
			.uri("/product-composite/"+ PRODUCT_ID_OK)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
			.jsonPath("$.recommendations.length()").isEqualTo(1)
			.jsonPath("$.reviews.length()").isEqualTo(1)
			;
	}
	
	@Test
	public void getProductNotFound() {
		client.get()
		.uri("/product-composite/"+ PRODUCT_ID_NOT_FOUND)
		.accept(APPLICATION_JSON)
		.exchange()
		.expectStatus().isNotFound()
		.expectHeader().contentType(APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.path").isEqualTo("/product-composite/"+ PRODUCT_ID_NOT_FOUND)
		.jsonPath("$.message").isEqualTo("No product found for productId: "+ PRODUCT_ID_NOT_FOUND);
	}
	
	 

}
