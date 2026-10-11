package org.akira.laptopstore.catalog.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import org.akira.laptopstore.catalog.dto.request.ProductSearchCriteria;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecs {
    public static Specification<Product> filterBy(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Keyword (Dùng EXISTS Subquery cho SKU để tránh duplicate)
            if (StringUtils.hasText(criteria.getKeyword())) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);

                Subquery<Integer> skuSubquery = query.subquery(Integer.class);
                Root<ProductVariant> skuVariant = skuSubquery.from(ProductVariant.class);
                skuSubquery.select(cb.literal(1));
                skuSubquery.where(
                        cb.equal(skuVariant.get("product"), root),
                        cb.like(cb.lower(skuVariant.get("sku")), pattern)
                );
                Predicate skuMatch = cb.exists(skuSubquery);

                predicates.add(cb.or(nameMatch, skuMatch));
            }

            // 2. Lọc theo Brand & Category (ManyToOne -> INNER JOIN an toàn, không sinh duplicate)
            if (StringUtils.hasText(criteria.getBrandName())) {
                predicates.add(cb.equal(root.join("brand", JoinType.INNER).get("name"), criteria.getBrandName()));
            }
            if (StringUtils.hasText(criteria.getCategoryName())) {
                predicates.add(cb.equal(root.join("category", JoinType.INNER).get("name"), criteria.getCategoryName()));
            }

            // 3. Lọc theo Variant: Giá, RAM, Storage (Dùng chung 1 EXISTS Subquery để tránh duplicate)
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null || 
                StringUtils.hasText(criteria.getRam()) || StringUtils.hasText(criteria.getStorage())) {
                
                Subquery<Integer> variantSubquery = query.subquery(Integer.class);
                Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);
                variantSubquery.select(cb.literal(1));

                List<Predicate> variantPredicates = new ArrayList<>();
                variantPredicates.add(cb.equal(variantRoot.get("product"), root));

                // Điều kiện Giá
                if (criteria.getMinPrice() != null) {
                    variantPredicates.add(cb.greaterThanOrEqualTo(variantRoot.get("price"), criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    variantPredicates.add(cb.lessThanOrEqualTo(variantRoot.get("price"), criteria.getMaxPrice()));
                }

                // Điều kiện RAM
                if (StringUtils.hasText(criteria.getRam())) {
                    String searchRam = "%" + criteria.getRam().toLowerCase().replace(" ", "").replace("-", "") + "%";
                    jakarta.persistence.criteria.Expression<String> ramDb = cb.function("REPLACE", String.class, cb.lower(variantRoot.get("ram")), cb.literal(" "), cb.literal(""));
                    ramDb = cb.function("REPLACE", String.class, ramDb, cb.literal("-"), cb.literal(""));
                    variantPredicates.add(cb.like(ramDb, searchRam));
                }

                // Điều kiện Storage
                if (StringUtils.hasText(criteria.getStorage())) {
                    String searchStorage = "%" + criteria.getStorage().toLowerCase().replace(" ", "").replace("-", "") + "%";
                    jakarta.persistence.criteria.Expression<String> storageDb = cb.function("REPLACE", String.class, cb.lower(variantRoot.get("storage")), cb.literal(" "), cb.literal(""));
                    storageDb = cb.function("REPLACE", String.class, storageDb, cb.literal("-"), cb.literal(""));
                    variantPredicates.add(cb.like(storageDb, searchStorage));
                }

                variantSubquery.where(variantPredicates.toArray(new Predicate[0]));
                predicates.add(cb.exists(variantSubquery));
            }


            // 5. SORT THEO PRICE BẰNG SUBQUERY (Bây giờ không còn DISTINCT nên Postgres sẽ cho phép)
            if (StringUtils.hasText(criteria.getSortPriceDirection())) {
                Subquery<BigDecimal> minPriceSubquery = query.subquery(BigDecimal.class);
                Root<ProductVariant> sortVariant = minPriceSubquery.from(ProductVariant.class);
                minPriceSubquery.select(cb.min(sortVariant.get("price")));
                minPriceSubquery.where(cb.equal(sortVariant.get("product"), root));

                if ("asc".equalsIgnoreCase(criteria.getSortPriceDirection())) {
                    query.orderBy(cb.asc(minPriceSubquery));
                } else if ("desc".equalsIgnoreCase(criteria.getSortPriceDirection())) {
                    query.orderBy(cb.desc(minPriceSubquery));
                }
            }

            // ĐÃ XÓA: query.distinct(true); 
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}