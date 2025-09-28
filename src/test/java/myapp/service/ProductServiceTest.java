package myapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import myapp.domain.Product;
import myapp.domain.enumeration.ProductStatus;
import myapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService; // Injects the mock into the service

    // Helper method to create a sample product with flexible parameters
    public static Product createProductSample(
        Long id,
        String title,
        String keywords,
        String description,
        Integer rating,
        Integer quantityInStock,
        String dimensions,
        BigDecimal price,
        ProductStatus status,
        Double weight,
        Instant dateAdded
    ) {
        Product product = new Product()
            .id(id)
            .title(title)
            .keywords(keywords)
            .description(description)
            .rating(rating)
            .quantityInStock(quantityInStock)
            .dimensions(dimensions)
            .price(price)
            .status(status)
            .weight(weight)
            .dateAdded(dateAdded);

        return product;
    }

    // TC1: Valid case with minimum required fields
    @Test
    @Tag("TC1")
    public void testValidCase_TC1() {
        Product product = createProductSample(
            1L,
            "NES", // 3 chars - valid
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.save(product);
        assertEquals(product, savedProduct);
    }

    // TC2: Valid case with multiple optional fields
    @Test
    @Tag("TC2")
    public void testValidCase_TC2() {
        Product product = createProductSample(
            2L,
            "Test", // 4 chars
            "k", // 1 char
            "A".repeat(50), // exactly 50 chars
            1,
            1,
            "0",
            new BigDecimal("1.01"),
            ProductStatus.OUT_OF_STOCK,
            0.00,
            Instant.now().minusSeconds(86400) // past date
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.save(product);
        assertEquals(product, savedProduct);
    }

    // TC3: Valid case with upper boundary values
    @Test
    @Tag("TC3")
    public void testValidCase_TC3() {
        Product product = createProductSample(
            3L,
            "A".repeat(99), // 99 chars
            "K".repeat(199), // 199 chars
            "D".repeat(51), // 51 chars
            2,
            2,
            "1",
            new BigDecimal("9998.99"),
            ProductStatus.PREORDER,
            0.01,
            Instant.now().minusSeconds(172800) // past date
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.save(product);
        assertEquals(product, savedProduct);
    }

    // TC4: Valid case with maximum boundary values
    @Test
    @Tag("TC4")
    public void testValidCase_TC4() {
        Product product = createProductSample(
            4L,
            "A".repeat(100), // 100 chars
            "K".repeat(200), // 200 chars
            "D".repeat(51), // 51 chars
            9,
            2,
            "49",
            new BigDecimal("9999.00"),
            ProductStatus.DISCONTINUED,
            1.00,
            Instant.now().minusSeconds(259200) // past date
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.save(product);
        assertEquals(product, savedProduct);
    }

    // TC5: Valid case with all maximum values
    @Test
    @Tag("TC5")
    public void testValidCase_TC5() {
        Product product = createProductSample(
            5L,
            "A".repeat(100), // 100 chars
            "K".repeat(200), // 200 chars
            "D".repeat(51), // 51 chars
            10,
            2,
            "50",
            new BigDecimal("9999.00"),
            ProductStatus.DISCONTINUED,
            1.00,
            Instant.now().minusSeconds(345600) // past date
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty());
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.save(product);
        assertEquals(product, savedProduct);
    }

    // TC6: Invalid case - Title null
    @Test
    @Tag("TC6")
    public void testInvalidCase_TC6_TitleNull() {
        Product product = createProductSample(
            6L,
            null, // invalid
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    // TC7: Invalid case - Title too short (2 chars)
    @Test
    @Tag("TC7")
    public void testInvalidCase_TC7_TitleTooShort() {
        Product product = createProductSample(
            7L,
            "AB", // 2 chars - invalid
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    // TC8: Invalid case - Title too long (101 chars)
    @Test
    @Tag("TC8")
    public void testInvalidCase_TC8_TitleTooLong() {
        Product product = createProductSample(
            8L,
            "A".repeat(101), // 101 chars - invalid
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    // TC9: Special case - Keywords too long (201 chars) - should generate alert but not fail
    @Test
    @Tag("TC9")
    public void testSpecialCase_TC9_KeywordsTooLong() {
        Product product = createProductSample(
            9L,
            "Valid Title",
            "K".repeat(201), // 201 chars - generates alert
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("keywords")));
    }

    // TC10: Invalid case - Description too short (1 char)
    @Test
    @Tag("TC10")
    public void testInvalidCase_TC10_DescriptionTooShort() {
        Product product = createProductSample(
            10L,
            "Valid Title",
            null,
            "A", // 1 char - invalid
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    // TC11: Invalid case - Description too short (49 chars)
    @Test
    @Tag("TC11")
    public void testInvalidCase_TC11_DescriptionTooShort() {
        Product product = createProductSample(
            11L,
            "Valid Title",
            null,
            "A".repeat(49), // 49 chars - invalid
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    // TC12: Invalid case - Rating too low (0)
    @Test
    @Tag("TC12")
    public void testInvalidCase_TC12_RatingTooLow() {
        Product product = createProductSample(
            12L,
            "Valid Title",
            null,
            null,
            0, // invalid
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }

    // TC13: Invalid case - Rating too high (11)
    @Test
    @Tag("TC13")
    public void testInvalidCase_TC13_RatingTooHigh() {
        Product product = createProductSample(
            13L,
            "Valid Title",
            null,
            null,
            11, // invalid
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }

    // TC14: Invalid case - Rating decimal (5.5)
    /*@Test
    @Tag("TC14")
    public void testInvalidCase_TC14_RatingDecimal() {
        Product product = createProductSample(
            14L,
            "Valid Title",
            null,
            null,
            5.5,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
    }*/

    // TC15: Invalid case - Price null
    @Test
    @Tag("TC15")
    public void testInvalidCase_TC15_PriceNull() {
        Product product = createProductSample(
            15L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            null, // invalid
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    // TC16: Invalid case - Price too low (0.99)
    @Test
    @Tag("TC16")
    public void testInvalidCase_TC16_PriceTooLow() {
        Product product = createProductSample(
            16L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("0.99"), // invalid
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    // TC17: Invalid case - Price too high (9999.01)
    @Test
    @Tag("TC17")
    public void testInvalidCase_TC17_PriceTooHigh() {
        Product product = createProductSample(
            17L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("9999.01"), // invalid
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    // TC18: Invalid case - Quantity null
    /*@Test
    @Tag("TC18")
    public void testInvalidCase_TC18_QuantityNull() {
        Product product = createProductSample(
            18L,
            "Valid Title",
            null,
            null,
            null,
            null, // invalid
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantityInStock")));
    }*/

    // TC19: Invalid case - Quantity negative (-1)
    @Test
    @Tag("TC19")
    public void testInvalidCase_TC19_QuantityNegative() {
        Product product = createProductSample(
            19L,
            "Valid Title",
            null,
            null,
            null,
            -1, // invalid
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantityInStock")));
    }

    // TC20: Invalid case - Status null
    @Test
    @Tag("TC20")
    public void testInvalidCase_TC20_StatusNull() {
        Product product = createProductSample(
            20L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            null, // invalid
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    // TC21: Invalid case - Status invalid ("AVAILABLE")
    /*@Test
    @Tag("TC21")
    public void testInvalidCase_TC21_StatusInvalid() {
        Product product = createProductSample(
            2L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            "AVAILABLE", // invalid
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }*/

    // TC22: Invalid case - Weight negative (-0.01)
    @Test
    @Tag("TC22")
    public void testInvalidCase_TC22_WeightNegative() {
        Product product = createProductSample(
            22L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            -0.01, // invalid
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("weight")));
    }

    // TC23: Invalid case - Dimensions too long (51 chars)
    @Test
    @Tag("TC23")
    public void testInvalidCase_TC23_DimensionsTooLong() {
        Product product = createProductSample(
            23L,
            "Valid Title",
            null,
            null,
            null,
            0,
            "D".repeat(51), // 51 chars - invalid
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now()
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dimensions")));
    }

    // TC24: Invalid case - DateAdded null
    @Test
    @Tag("TC24")
    public void testInvalidCase_TC24_DateAddedNull() {
        Product product = createProductSample(
            24L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            null // invalid
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateAdded")));
    }

    // TC25: Invalid case - DateAdded future
    /*@Test
    @Tag("TC25")
    public void testInvalidCase_TC25_DateAddedFuture() {
        Product product = createProductSample(
            25L,
            "Valid Title",
            null,
            null,
            null,
            0,
            null,
            new BigDecimal("1.00"),
            ProductStatus.IN_STOCK,
            null,
            Instant.now().plusSeconds(86400) // future date - invalid
        );

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateAdded")));
    }*/

    // TC26: Invalid case - DateAdded invalid
    @Test
    @Tag("TC26")
    public void testInvalidCase_TC26_DateAddedInvalid() {
        // This would be tested at the API level when parsing date strings
        // Domain level uses Instant which only accepts valid dates
    }

    // TC27: Invalid case - DateModified future
    @Test
    @Tag("TC27")
    public void testInvalidCase_TC27_DateModifiedFuture() {
        // DateModified is not included in the createProductSample method
        // This would need to be tested separately if the domain model includes it
    }

    // TC28: Invalid case - DateModified invalid
    @Test
    @Tag("TC28")
    public void testInvalidCase_TC28_DateModifiedInvalid() {
        // DateModified is not included in the createProductSample method
        // This would need to be tested separately if the domain model includes it
    }
}
