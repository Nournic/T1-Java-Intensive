package ru.t1.nour.microservice.service.impl;

import org.checkerframework.checker.units.qual.C;
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
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.mapper.ClientProductMapper;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.ClientProduct;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.request.ClientProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ClientProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;
import ru.t1.nour.microservice.model.enums.ProductKey;
import ru.t1.nour.microservice.model.enums.Status;
import ru.t1.nour.microservice.repository.ClientProductRepository;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientProductServiceImplTest {

    @Mock
    private ClientProductRepository clientProductRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventProducer productEventProducer;

    @Mock
    private ClientProductMapper mapper;

    @InjectMocks
    private ClientProductServiceImpl clientProductService;

    @Test
    void should_createProductAndSendEvent_when_requestIsValid() {
        // --- ARRANGE (Подготовка) ---
        // 1. Входные данные
        long clientId = 1L;
        long productId = 10L;
        ClientProductCreateRequest request = new ClientProductCreateRequest(clientId, productId, BigDecimal.TEN, 12);

        // 2. Сущности, которые "найдутся" в базе
        Client client = new Client();
        ReflectionTestUtils.setField(client, "id", clientId);

        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", productId);
        ProductKey productKey = ProductKey.PC;
        product.setKey(productKey);

        // 3. Сущность, которая "сохранится"
        ClientProduct savedClientProduct = new ClientProduct();
        ReflectionTestUtils.setField(savedClientProduct, "id", 100L);
        savedClientProduct.setClient(client);
        savedClientProduct.setProduct(product);
        savedClientProduct.setStatus(Status.ACTIVE);

        // 4. Финальный DTO для ответа
        ClientProductResponse expectedResponse = new ClientProductResponse();
        expectedResponse.setId(100L);

        // 5. Настройка поведения моков
        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(clientProductRepository.save(any(ClientProduct.class))).thenReturn(savedClientProduct);
        when(mapper.toClientProductResponse(savedClientProduct)).thenReturn(expectedResponse);

        // --- ACT (Действие) ---
        ClientProductResponse actualResponse = clientProductService.create(request);

        // --- ASSERT (Проверка) ---
        // Проверяем ответ
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(expectedResponse.getId());

        // Проверяем, что на сохранение ушел правильный объект
        ArgumentCaptor<ClientProduct> captor = ArgumentCaptor.forClass(ClientProduct.class);
        verify(clientProductRepository).save(captor.capture());
        ClientProduct capturedProduct = captor.getValue();
        assertThat(capturedProduct.getClient().getId()).isEqualTo(clientId);
        assertThat(capturedProduct.getProduct().getId()).isEqualTo(productId);
        assertThat(capturedProduct.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(capturedProduct.getOpenDate()).isNotNull();

        // Проверяем, что событие было отправлено
        verify(productEventProducer, times(1)).sendProductEvent(any(ClientProductEventDTO.class));
    }

    @Test
    void should_throwException_when_clientProductAlreadyExists() {
        // --- ARRANGE ---
        long clientId = 1L;
        long productId = 10L;
        ClientProductCreateRequest request = new ClientProductCreateRequest(clientId, productId, BigDecimal.TEN, 12);

        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(true);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> clientProductService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ClientProduct is already exists with that clientId and productId");

        // Убеждаемся, что дальнейшая логика не выполнялась
        verify(clientRepository, never()).findById(anyLong());
        verify(productRepository, never()).findById(anyLong());
        verify(clientProductRepository, never()).save(any());
        verify(productEventProducer, never()).sendProductEvent(any());
    }

    @Test
    void should_throwException_when_clientNotFound() {
        // --- ARRANGE ---
        long clientId = 1L;
        long productId = 10L;
        ClientProductCreateRequest request = new ClientProductCreateRequest(clientId, productId, BigDecimal.TEN, 12);

        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> clientProductService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client is not exists by ID " + clientId);

        // Убеждаемся, что дальнейшая логика не выполнялась
        verify(productRepository, never()).findById(anyLong());
        verify(clientProductRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_productNotFound() {
        // --- ARRANGE ---
        long clientId = 1L;
        long productId = 10L;
        ClientProductCreateRequest request = new ClientProductCreateRequest(clientId, productId, BigDecimal.TEN, 12);
        Client client = new Client(); // Клиент для успешного прохождения первой проверки

        when(clientProductRepository.existsByClientIdAndProductId(clientId, productId)).thenReturn(false);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> clientProductService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product is not exists by ID " + productId);

        // Убеждаемся, что сохранение не вызывалось
        verify(clientProductRepository, never()).save(any());
    }

    @Test
    void should_returnClientProductResponse_when_idExists() {
        // --- ARRANGE (Подготовка) ---
        long existingId = 1L;

        // 1. Создаем сущность, которую якобы вернет репозиторий
        var foundClientProduct = new ClientProduct();
        ReflectionTestUtils.setField(foundClientProduct, "id", existingId);

        // 2. Создаем DTO, который якобы вернет маппер
        var expectedResponse = new ClientProductResponse();
        expectedResponse.setId(existingId);

        // 3. "Обучаем" моки
        when(clientProductRepository.findById(existingId)).thenReturn(Optional.of(foundClientProduct));
        when(mapper.toClientProductResponse(foundClientProduct)).thenReturn(expectedResponse);

        // --- ACT (Действие) ---
        ClientProductResponse actualResponse = clientProductService.findById(existingId);

        // --- ASSERT (Проверка) ---
        // 1. Проверяем, что результат соответствует ожиданиям
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(existingId);

        // 2. Проверяем, что зависимости были вызваны
        verify(clientProductRepository, times(1)).findById(existingId);
        verify(mapper, times(1)).toClientProductResponse(foundClientProduct);
    }

    @Test
    void should_throwException_when_findByIdReturnsEmpty() {
        // --- ARRANGE ---
        long nonExistentId = 99L;

        // "Обучаем" репозиторий возвращать "ничего"
        when(clientProductRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Проверяем, что будет выброшено исключение
        assertThatThrownBy(() -> clientProductService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client product with ID " + nonExistentId + "is not exists.");

        // Убеждаемся, что маппер не был вызван, так как до него не дошло дело
        verify(mapper, never()).toClientProductResponse(any());
    }

    @Test
    void should_returnPagedResponses_when_dataExists() {
        // --- ARRANGE (Подготовка) ---
        // 1. Создаем объект Pageable для запроса
        Pageable pageable = PageRequest.of(0, 5); // Запрашиваем первую страницу, 5 элементов

        // 2. Создаем "ответ" от репозитория
        var product1 = new ClientProduct();
        var product2 = new ClientProduct();
        List<ClientProduct> productList = List.of(product1, product2);

        // 3. Создаем объект Page, который вернет мок репозитория
        // Конструктор: (содержимое, pageable, общее количество элементов в базе)
        Page<ClientProduct> productPage = new PageImpl<>(productList, pageable, 2L);

        // 4. "Обучаем" мок репозитория
        when(clientProductRepository.findAll(pageable)).thenReturn(productPage);

        // 5. "Обучаем" мок маппера. Здесь мы не будем создавать DTO,
        when(mapper.toClientProductResponse(any(ClientProduct.class))).thenReturn(new ClientProductResponse());


        // --- ACT (Действие) ---
        Page<ClientProductResponse> actualPage = clientProductService.findAll(pageable);


        // --- ASSERT (Проверка) ---
        // 1. Проверяем метаданные страницы
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getTotalElements()).isEqualTo(2L);
        assertThat(actualPage.getTotalPages()).isEqualTo(1);

        // 2. Проверяем содержимое
        assertThat(actualPage.getContent().size()).isEqualTo(2);

        // 3. Проверяем, что зависимости были вызваны
        verify(clientProductRepository, times(1)).findAll(pageable);
        // Проверяем, что маппер был вызван для каждого из двух элементов
        verify(mapper, times(2)).toClientProductResponse(any(ClientProduct.class));
    }

    @Test
    void should_returnEmptyPage_when_noDataExists() {
        // --- ARRANGE ---
        Pageable pageable = PageRequest.of(0, 5);

        // "Обучаем" репозиторий возвращать пустую страницу
        when(clientProductRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        // --- ACT ---
        Page<ClientProductResponse> actualPage = clientProductService.findAll(pageable);

        // --- ASSERT ---
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.isEmpty()).isTrue();

        // Убеждаемся, что маппер не вызывался, так как нечего было мапить
        verify(mapper, never()).toClientProductResponse(any());
    }

    @Test
    void should_updateProductAndSendEvent_when_requestIsValid() {
        // --- ARRANGE (Подготовка) ---
        long existingId = 1L;
        long clientId = 10L;
        long productId = 20L;

        // 1. Входные данные
        var updateRequest = new ClientProductUpdateRequest();
        updateRequest.setNewStatus(Status.CLOSED);
        updateRequest.setCloseDate(LocalDateTime.now());

        // 2. --- СОЗДАЕМ ПОЛНОЦЕННУЮ ИСХОДНУЮ СУЩНОСТЬ ---
        // Создаем "моки" для вложенных сущностей
        var mockClient = new Client();
        ReflectionTestUtils.setField(mockClient, "id", clientId);

        var mockProductKey = ProductKey.AC;

        var mockProduct = new Product();
        ReflectionTestUtils.setField(mockProduct, "id", productId);
        mockProduct.setKey(mockProductKey);

        // Создаем основной объект, который "найдет" репозиторий
        var originalProduct = new ClientProduct();
        originalProduct.setStatus(Status.ACTIVE);
        originalProduct.setClient(mockClient); // <-- Устанавливаем мок клиента
        originalProduct.setProduct(mockProduct); // <-- Устанавливаем мок продукта
        ReflectionTestUtils.setField(originalProduct, "id", existingId);

        // 3. Сущность, которую "вернет" метод save()
        when(clientProductRepository.save(any(ClientProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 4. Финальный DTO ответа
        var expectedResponse = new ClientProductResponse();
        expectedResponse.setId(existingId);
        expectedResponse.setStatus(Status.CLOSED);

        // 5. "Обучаем" остальные моки
        when(clientProductRepository.findById(existingId)).thenReturn(Optional.of(originalProduct));
        when(mapper.toClientProductResponse(any(ClientProduct.class))).thenReturn(expectedResponse);

        // --- ACT (Действие) ---
        ClientProductResponse actualResponse = clientProductService.update(existingId, updateRequest);

        // --- ASSERT (Проверка) ---
        // 1. Проверяем возвращенный DTO
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getStatus()).isEqualTo(Status.CLOSED);

        // 2. Используем ArgumentCaptor для проверки объекта, переданного в save()
        ArgumentCaptor<ClientProduct> productCaptor = ArgumentCaptor.forClass(ClientProduct.class);
        verify(clientProductRepository).save(productCaptor.capture());
        ClientProduct capturedProduct = productCaptor.getValue();

        // Убеждаемся, что поля обновились правильно
        assertThat(capturedProduct.getStatus()).isEqualTo(Status.CLOSED);
        assertThat(capturedProduct.getCloseDate()).isEqualTo(updateRequest.getCloseDate());
        // Убеждаемся, что вложенные сущности не "потерялись"
        assertThat(capturedProduct.getClient()).isNotNull();
        assertThat(capturedProduct.getProduct()).isNotNull();

        // 3. Проверяем, что событие было отправлено
        ArgumentCaptor<ClientProductEventDTO> eventCaptor = ArgumentCaptor.forClass(ClientProductEventDTO.class);
        verify(productEventProducer).sendProductEvent(eventCaptor.capture());
        ClientProductEventDTO capturedEvent = eventCaptor.getValue();

        // Проверяем, что событие сформировано корректно
        assertThat(capturedEvent.getEventType()).isEqualTo("UPDATED");
        assertThat(capturedEvent.getClientId()).isEqualTo(clientId);
        assertThat(capturedEvent.getProductId()).isEqualTo(productId);
        assertThat(capturedEvent.getProductKey()).isEqualTo("AC");
    }
    @Test
    void should_throwException_when_productToUpdateNotFound() {
        // --- ARRANGE ---
        long nonExistentId = 99L;
        var updateRequest = new ClientProductUpdateRequest();

        when(clientProductRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> clientProductService.update(nonExistentId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client Product with ID " + nonExistentId + "is not exists.");

        // Убеждаемся, что сохранение и отправка события не вызывались
        verify(clientProductRepository, never()).save(any());
        verify(productEventProducer, never()).sendProductEvent(any());
    }

    @Test
    void should_deleteProductAndSendEvent_when_idExists() {
        // --- ARRANGE (Подготовка) ---
        long existingId = 1L;

        // 1. Создаем сущность, которую "найдет" репозиторий для удаления.
        // Нам нужно создать ее с вложенными объектами, так как метод createEvent() их использует.
        var mockClient = new Client();
        ReflectionTestUtils.setField(mockClient, "id", 10L);
        var mockProductKey = ProductKey.CC;
        var mockProduct = new Product();
        ReflectionTestUtils.setField(mockProduct, "id", 20L);
        mockProduct.setKey(mockProductKey);

        var productToDelete = new ClientProduct();
        ReflectionTestUtils.setField(productToDelete, "id", existingId);
        productToDelete.setClient(mockClient);
        productToDelete.setProduct(mockProduct);
        productToDelete.setStatus(Status.ACTIVE);

        // 2. "Обучаем" мок findById
        when(clientProductRepository.findById(existingId)).thenReturn(Optional.of(productToDelete));

        // 3. Для void-методов deleteById и sendProductEvent нам не нужно настраивать .thenReturn()
        // Mockito по умолчанию выполнит их "впустую".


        // --- ACT (Действие) ---
        // Вызываем метод. Ожидаем, что он отработает без ошибок.
        clientProductService.delete(existingId);


        // --- ASSERT (Проверка) ---
        // 1. Убеждаемся, что метод deleteById у репозитория был вызван с правильным id.
        verify(clientProductRepository, times(1)).deleteById(existingId);

        // 2. Используем ArgumentCaptor, чтобы проверить, какое событие было отправлено.
        ArgumentCaptor<ClientProductEventDTO> eventCaptor = ArgumentCaptor.forClass(ClientProductEventDTO.class);
        verify(productEventProducer, times(1)).sendProductEvent(eventCaptor.capture());
        ClientProductEventDTO capturedEvent = eventCaptor.getValue();

        // Проверяем поля "пойманного" события
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getEventType()).isEqualTo("DELETED");
        assertThat(capturedEvent.getClientProductId()).isEqualTo(existingId);
        assertThat(capturedEvent.getClientId()).isEqualTo(10L);
        assertThat(capturedEvent.getProductId()).isEqualTo(20L);
    }

    @Test
    void should_throwException_when_productToDeleteNotFound() {
        // --- ARRANGE ---
        long nonExistentId = 99L;

        // "Обучаем" репозиторий не находить сущность
        when(clientProductRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Проверяем, что будет выброшено исключение
        assertThatThrownBy(() -> clientProductService.delete(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");

        // Убеждаемся, что до отправки события и удаления дело не дошло
        verify(productEventProducer, never()).sendProductEvent(any());
        verify(clientProductRepository, never()).deleteById(anyLong());
    }
}
