/**
 * 
 */
package se.magnus.microservices.composite.product.services;

import java.io.IOException;
import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

/**
 * @author Galleta
 *
 */
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

	private final RestTemplate restTemplate;
	private final ObjectMapper mapper;
	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;

	@Autowired
	public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper objectMapper,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") String productServicePort,
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") String recommendationServicePort,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") String reviewServicePort) {

		this.restTemplate = restTemplate;
		this.mapper = objectMapper;
		this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
		this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort
				+ "/recommendation?productId=";
		this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

	}

	@Override
	public Product getProduct(int productId) {
		
		try {
		String url = productServiceUrl + productId;
		LOG.debug("Will call product with id:{}",url);
		
		Product product = restTemplate.getForObject(url, Product.class);
		LOG.debug("Foound a product with id: {}"+product.getProductId());
		
		return product;
		}catch(HttpClientErrorException ex) {
			switch(ex.getStatusCode()) {
				case NOT_FOUND:
					throw new NotFoundException(getErrorMessage(ex));
				case UNPROCESSABLE_ENTITY:
					throw new InvalidInputException(getErrorMessage(ex));
				default:
					LOG.warn("Got an unexpected HTTP error: {}, will rethrow it,", ex.getStatusCode());
					LOG.warn("Error body: {}",ex.getResponseBodyAsString());
					throw ex;
			}
		}
	}

	private String getErrorMessage(HttpClientErrorException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		} 
	}

	public List<Recommendation> getRecommendations(int productId) {
		String url = recommendationServiceUrl + productId;
		List<Recommendation> recommendations = restTemplate
				.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
				}).getBody();

		return recommendations;

	}

	
	public List<Review> getReviews(int productId) {
		String url = reviewServiceUrl + productId;
		List<Review> reviews = restTemplate
				.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
				}).getBody();

		return reviews;

	}

}
