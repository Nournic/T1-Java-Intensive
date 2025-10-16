package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.mapper.ProductMapper;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.request.ProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ProductResponse;
import ru.t1.nour.microservice.model.enums.ProductKey;
import ru.t1.nour.microservice.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    // ARRANGE
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void should_returnProductDto_when_productIdExists() {

        // --- ARRANGE (Подготовка) ---

        Long id = 1L;
        String name = "TEST";
        ProductKey pKey = ProductKey.AC;
        String productId = ProductKey.AC.getValue() + "1";
        LocalDateTime createDate = LocalDateTime.now();

        Product foundProduct = new Product(name, pKey, createDate, productId);
        ReflectionTestUtils.setField(foundProduct, "id", id);

        ProductResponse expectedDto = new ProductResponse();

        expectedDto.setId(id);
        expectedDto.setName(name);
        expectedDto.setProductKey(pKey);
        expectedDto.setCreateDate(createDate);
        expectedDto.setProductId(productId);

        when(productRepository.findById(id)).thenReturn(Optional.of(foundProduct));

        when(productMapper.toProductResponse(foundProduct)).thenReturn(expectedDto);


        // --- ACT ---

        ProductResponse actualDto = productService.findById(id);


        // --- ASSERT ---

        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getId()).isEqualTo(id);
        assertThat(actualDto.getName()).isEqualTo(name);
        assertThat(actualDto.getProductId()).isEqualTo(productId);
        assertThat(actualDto.getCreateDate()).isEqualTo(createDate);
        assertThat(actualDto.getProductKey()).isEqualTo(pKey);

        verify(productRepository, times(1)).findById(id);
        verify(productMapper, times(1)).toProductResponse(foundProduct);
    }

    @Test
    void should_throwRuntimeException_when_IdDoesNotExist() {
        // --- ARRANGE ---
        long nonExistentId = 99L;

        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> productService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with that ID is not found");

        verify(productMapper, never()).toProductResponse(any());
    }

    @Test
    void should_deleteProduct_when_productExists() {

        // --- ARRANGE ---

        long id = 1L;
        when(productRepository.existsById(id)).thenReturn(true);

        // --- ACT ---

        assertDoesNotThrow(() -> productService.delete(id));

        // --- ASSERT ---

        verify(productRepository, times(1)).existsById(id);
        verify(productRepository, times(1)).deleteById(id);
    }

    @Test
    void should_throwRuntimeException_when_productNoExists() {

        // --- ARRANGE ---

        long id = 1L;
        when(productRepository.existsById(id)).thenReturn(false);

        // --- ACT ---

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("Product with that ID is not found");

        // --- ASSERT ---

        verify(productRepository, times(1)).existsById(id);
        verify(productRepository, never()).deleteById(id);
    }

    @Test
    void should_updateAndReturnProduct_when_productExists() {
        // --- ARRANGE (Подготовка) ---

        // 1. Входные данные
        long id = 1L;
        ProductUpdateRequest updateDto = new ProductUpdateRequest();
        updateDto.setName("New Name");
        updateDto.setProductKey(ProductKey.CC);

        // 2. Старый объект
        Product originalProduct = new Product();
        ReflectionTestUtils.setField(originalProduct, "id", id);
        originalProduct.setName("Old Name");
        originalProduct.setKey(ProductKey.AC);

        // 3. "Новый" объект
        Product newProduct = new Product();
        ReflectionTestUtils.setField(newProduct, "id", id);
        newProduct.setName("New Name");
        newProduct.setKey(ProductKey.CC);

        // 4. DTO, который вернет маппер
        ProductResponse expectedDto = new ProductResponse();
        expectedDto.setId(id);
        expectedDto.setName("New Name");
        expectedDto.setProductKey(ProductKey.CC);

        // 5. "Обучаем" моки
        when(productRepository.findById(id)).thenReturn(Optional.of(originalProduct));
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);
        when(productMapper.toProductResponse(newProduct)).thenReturn(expectedDto);


        // --- ACT (Действие) ---

        ProductResponse actualDto = productService.update(id, updateDto);

        // --- ASSERT (Проверка) ---

        // 1. Проверяем возвращенный результат
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getName()).isEqualTo("New Name");

        // 2. --- САМАЯ ВАЖНАЯ ПРОВЕРКА ДЛЯ UPDATE ---
        // Мы должны убедиться, что в метод save() был передан ПРАВИЛЬНО ОБНОВЛЕННЫЙ объект.
        // Для этого используем ArgumentCaptor.

        // Создаем "ловушку" для объекта Product
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        // Проверяем, что save() был вызван, и "ловим" переданный в него аргумент
        verify(productRepository, times(1)).save(productCaptor.capture());

        // Теперь достаем "пойманный" объект
        Product capturedProduct = productCaptor.getValue();

        // И проверяем его состояние в момент перед сохранением
        assertThat(capturedProduct.getId()).isEqualTo(id);
        assertThat(capturedProduct.getName()).isEqualTo("New Name");
        assertThat(capturedProduct.getKey()).isEqualTo(ProductKey.CC);
    }

    @Test
    void should_throwException_when_productToUpdateNotFound(){
        // --- ARRANGE (Подготовка) ---

        // 1. Входные данные
        long notExistsId = 100L;
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest();

        when(productRepository.findById(notExistsId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---

        assertThatThrownBy(()->productService.update(notExistsId, productUpdateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with that ID is not found");
    }

    @Test
    void should_createAndReturnProduct_when_createDtoIsValid() {

        // --- ARRANGE ---

        long id = 1L;
        String productName = "New test product";
        ProductKey productKey = ProductKey.CC;

        // 1. Входные данные
        ProductCreateRequest createDto = new ProductCreateRequest();
        createDto.setName(productName);
        createDto.setProductKey(productKey);

        // 2. Сущность, которую вернет маппер
        Product productToSave = new Product();
        productToSave.setName(productName);
        productToSave.setKey(productKey);

        // 3. Сущность, которую якобы вернет репозиторий ПОСЛЕ сохранения
        Product savedProduct = new Product();
        ReflectionTestUtils.setField(savedProduct, "id", id);
        savedProduct.setName(productName);
        savedProduct.setKey(productKey);

        // 4. Финальный DTO, который вернет маппер из сохраненной сущности
        ProductResponse expectedDto = new ProductResponse();
        expectedDto.setId(1L);
        expectedDto.setName(productName);

        // 5. "Обучаем" моки
        when(productMapper.toEntity(createDto)).thenReturn(productToSave);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toProductResponse(savedProduct)).thenReturn(expectedDto);


        // --- ACT ---

        // Вызываем метод, который мы тестируем
        ProductResponse actualDto = productService.create(createDto);


        // --- ASSERT ---

        // 1. Проверяем возвращенный результат
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getId()).isEqualTo(id);
        assertThat(actualDto.getName()).isEqualTo(productName);

        // "Ловим" объект, который был передан в метод save()
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        // Проверяем, что на сохранение пошел объект БЕЗ id, но с правильными данными из DTO
        assertThat(capturedProduct.getId()).isNull();
        assertThat(capturedProduct.getName()).isEqualTo(createDto.getName());

        // 2. Проверяем, что все моки были вызваны ровно по одному разу
        verify(productMapper, times(1)).toEntity(createDto);
        verify(productRepository, times(1)).save(productToSave);
        verify(productMapper, times(1)).toProductResponse(savedProduct);
    }

    @Test
    void should_returnPagedProductDtos_when_productsExist() {
        // --- ARRANGE ---

        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Product product1 = new Product();
        Product product2 = new Product();
        List<Product> productList = List.of(product1, product2);

        long totalElements = 25L;
        Page<Product> productPageFromDb = new PageImpl<>(productList, pageable, totalElements);

        ProductResponse dto1 = new ProductResponse();
        ProductResponse dto2 = new ProductResponse();

        when(productRepository.findAll(pageable)).thenReturn(productPageFromDb);
        when(productMapper.toProductResponse(product1)).thenReturn(dto1);
        when(productMapper.toProductResponse(product2)).thenReturn(dto2);


        // --- ACT ---

        Page<ProductResponse> actualDtoPage = productService.findAll(pageable);


        // --- ASSERT ---

        assertThat(actualDtoPage).isNotNull();
        assertThat(actualDtoPage.getTotalElements()).isEqualTo(totalElements);
        assertThat(actualDtoPage.getTotalPages()).isEqualTo(3);
        assertThat(actualDtoPage.getNumber()).isEqualTo(pageNumber);
        assertThat(actualDtoPage.getContent()).hasSize(2);

        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(2)).toProductResponse(any(Product.class));
    }

    @Test
    void should_returnEmptyPage_when_noProductsExist() {

        // --- ARRANGE ---

        Pageable pageable = PageRequest.of(0, 10);

        // Создаем пустую страницу
        Page<Product> emptyPageFromDb = Page.empty(pageable);

        // "Обучаем" репозиторий возвращать пустую страницу
        when(productRepository.findAll(pageable)).thenReturn(emptyPageFromDb);


        // --- ACT ---

        Page<ProductResponse> actualDtoPage = productService.findAll(pageable);


        // --- ASSERT ---

        assertThat(actualDtoPage).isNotNull();
        assertThat(actualDtoPage.isEmpty()).isTrue(); // Проверяем, что страница пуста
        assertThat(actualDtoPage.getContent()).isEmpty();
        assertThat(actualDtoPage.getTotalElements()).isEqualTo(0);

        verify(productRepository, times(1)).findAll(pageable);

        verify(productMapper, never()).toProductResponse(any(Product.class));
    }

}
