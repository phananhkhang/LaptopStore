package org.akira.laptopstore.catalog.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.akira.laptopstore.catalog.dto.request.ProductSearchCriteria;
import org.akira.laptopstore.catalog.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecs {
public static Specification<Product> filterBy(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Keyword (Tìm trên Name của Product HOẶC SKU của Variant)
            if (StringUtils.hasText(criteria.getKeyword())) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                
                // Cần LEFT JOIN bảng variants để lấy trường sku
                Join<Object, Object> variantJoin = root.join("variants", JoinType.LEFT);
                Predicate skuMatch = cb.like(cb.lower(variantJoin.get("sku")), pattern);
                
                predicates.add(cb.or(nameMatch, skuMatch));
            }

            // 2. Lọc theo Brand
            if (StringUtils.hasText(criteria.getBrandName())) {
                Join<Object, Object> brandJoin = root.join("brand", JoinType.INNER);
                predicates.add(cb.equal(brandJoin.get("name"), criteria.getBrandName()));
            }

            // 3. Lọc theo Category
            if (StringUtils.hasText(criteria.getCategoryName())) {
                Join<Object, Object> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("name"), criteria.getCategoryName()));
            }

            // 4. Lọc theo khoảng giá (Dữ liệu giá nằm trong bảng ProductVariant)
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                Join<Object, Object> variantJoin = root.join("variants", JoinType.INNER);
                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), criteria.getMaxPrice()));
                }
            }

            // Xử lý nòng cốt: Vì JOIN với bảng 1-N (Variants), kết quả trả về có thể bị nhân bản các dòng Product.
            // Bắt buộc gọi query.distinct(true) để lọc bỏ dữ liệu trùng lặp ở tầng database.
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
