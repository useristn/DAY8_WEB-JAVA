package vn.iostar.service.impl;

import vn.iostar.entity.Product;
import vn.iostar.repository.ProductRepository;
import vn.iostar.service.ProductService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        return productRepository.findById(id)
                .map(p -> {
                    p.setName(product.getName());
                    p.setPrice(product.getPrice());
                    p.setCategory(product.getCategory());
                    return productRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

	@Override
	public Page<Product> getAllProducts(Pageable pageable) {
		return productRepository.findAll(pageable);
	}
}
