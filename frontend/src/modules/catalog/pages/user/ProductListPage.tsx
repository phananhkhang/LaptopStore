import { EmptyState, Icon, PageHeading, Pager } from '@/components/ui';
import React, { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { catalogApi } from '../../api/catalogApi';
import ProductCard, { ProductCardSkeleton } from '../../components/ProductCard';
import { Brand, Category, ProductSearchCriteria, ProductSummaryResponse } from '../../types/catalog';

const PAGE_SIZE = 12;
const RAM_OPTIONS = ['8GB', '16GB', '32GB'];
const PRICE_OPTIONS: { value: string; label: string; min?: number; max?: number }[] = [
  { value: '', label: 'Tất cả mức giá' },
  { value: 'u15', label: 'Dưới 15 triệu', max: 15_000_000 },
  { value: '15-25', label: '15 – 25 triệu', min: 15_000_000, max: 25_000_000 },
  { value: '25-35', label: '25 – 35 triệu', min: 25_000_000, max: 35_000_000 },
  { value: 'o35', label: 'Trên 35 triệu', min: 35_000_000 },
];
const SORT_OPTIONS = [
  { value: '', label: 'Mới nhất' },
  { value: 'asc', label: 'Giá: thấp đến cao' },
  { value: 'desc', label: 'Giá: cao đến thấp' },
];
const FILTER_KEYS = ['brandName', 'categoryName', 'ram', 'price', 'keyword'] as const;

const ProductListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductSummaryResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [filtersOpen, setFiltersOpen] = useState(false);

  const keyword = searchParams.get('keyword') ?? '';
  const brandName = searchParams.get('brandName') ?? '';
  const categoryName = searchParams.get('categoryName') ?? '';
  const ram = searchParams.get('ram') ?? '';
  const price = searchParams.get('price') ?? '';
  const sort = searchParams.get('sortPriceDirection') ?? '';
  const page = Math.max(0, Number(searchParams.get('page') ?? 0) || 0);
  const isOffer = searchParams.get('offer') === '1';

  const [searchInput, setSearchInput] = useState(keyword);
  useEffect(() => setSearchInput(keyword), [keyword]);

  useEffect(() => {
    catalogApi.getBrands().then((list) => setBrands(list.filter((b) => b.active !== false))).catch(() => setBrands([]));
    catalogApi.getCategories().then((list) => setCategories(list.filter((c) => c.active !== false))).catch(() => setCategories([]));
  }, []);

  const criteria = useMemo<ProductSearchCriteria>(() => {
    const range = PRICE_OPTIONS.find((p) => p.value === price);
    return {
      keyword: keyword || undefined,
      brandName: brandName || undefined,
      categoryName: categoryName || undefined,
      ram: ram || undefined,
      minPrice: range?.min,
      maxPrice: range?.max,
      sortPriceDirection: sort || undefined,
      page,
      size: PAGE_SIZE,
    };
  }, [keyword, brandName, categoryName, ram, price, sort, page]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(false);
    catalogApi.searchProducts(criteria)
      .then((res) => {
        if (cancelled) return;
        setProducts(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      })
      .catch(() => !cancelled && setError(true))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [criteria]);

  /** Cập nhật một tham số URL; đổi bộ lọc thì quay về trang đầu. */
  const updateParam = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(key, value); else next.delete(key);
    if (key !== 'page') next.delete('page');
    setSearchParams(next);
  };

  const toggleParam = (key: string, value: string) => updateParam(key, searchParams.get(key) === value ? '' : value);

  const clearFilters = () => {
    const next = new URLSearchParams(searchParams);
    FILTER_KEYS.forEach((k) => next.delete(k));
    next.delete('page');
    setSearchParams(next);
  };

  const changePage = (p: number) => {
    updateParam('page', p > 0 ? String(p) : '');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const activeFilters = [
    keyword && { key: 'keyword', label: `“${keyword}”` },
    brandName && { key: 'brandName', label: brandName },
    categoryName && { key: 'categoryName', label: categoryName },
    ram && { key: 'ram', label: `RAM ${ram}` },
    price && { key: 'price', label: PRICE_OPTIONS.find((p) => p.value === price)?.label ?? price },
  ].filter(Boolean) as { key: string; label: string }[];

  const checkGroup = (title: string, key: string, values: string[], current: string) => (
    <div className="filter-group">
      <h4>{title}</h4>
      {values.length === 0 && <p className="small muted">Đang cập nhật</p>}
      {values.map((v) => (
        <label key={v} className="check-label">
          <input type="checkbox" checked={current === v} onChange={() => toggleParam(key, v)} />
          {v}
        </label>
      ))}
    </div>
  );

  return (
    <div className="ls-container section">
      <div className="breadcrumb">
        <Link to="/">Trang chủ</Link><span>/</span>
        {categoryName ? <><Link to="/products">Laptop</Link><span>/</span><span>{categoryName}</span></> : <span>Tất cả laptop</span>}
      </div>
      <PageHeading
        eyebrow={isOffer ? 'Ưu đãi' : 'Khám phá công nghệ'}
        title={categoryName ? `Laptop ${categoryName.toLowerCase()}` : 'Tìm chiếc laptop dành cho bạn.'}
        sub="Một lựa chọn phù hợp, hàng ngàn điều có thể."
      />

      {isOffer && (
        <div className="notice" style={{ marginBottom: 24 }}>
          <Icon name="tag" />
          <div>
            <strong>Giá tốt nhất được xếp trước</strong>
            <p>Có mã giảm giá? Nhập ở bước thanh toán để hệ thống kiểm tra điều kiện và trừ trực tiếp vào đơn.</p>
          </div>
        </div>
      )}

      <div className="catalog-layout">
        <aside className={`filters ${filtersOpen ? 'open' : ''}`} aria-label="Bộ lọc">
          <div className="row between">
            <h3>Bộ lọc tìm kiếm</h3>
            <button type="button" className="text-btn small" onClick={clearFilters}>Xóa tất cả</button>
          </div>
          {checkGroup('Thương hiệu', 'brandName', brands.map((b) => b.name), brandName)}
          {checkGroup('Nhu cầu sử dụng', 'categoryName', categories.map((c) => c.name), categoryName)}
          {checkGroup('RAM', 'ram', RAM_OPTIONS, ram)}
          <div className="filter-group">
            <h4>Khoảng giá</h4>
            <select className="select" value={price} onChange={(e) => updateParam('price', e.target.value)} aria-label="Khoảng giá">
              {PRICE_OPTIONS.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
            </select>
          </div>
        </aside>

        <div>
          <div className="toolbar">
            <span className="results-count"><strong style={{ color: 'var(--ink)' }}>{loading ? '…' : totalElements}</strong> laptop phù hợp</span>
            <button type="button" className="btn sm mobile-only" onClick={() => setFiltersOpen((v) => !v)} aria-expanded={filtersOpen}>
              <Icon name="filter" /> Bộ lọc
            </button>
            <select className="select" value={sort} onChange={(e) => updateParam('sortPriceDirection', e.target.value)} aria-label="Sắp xếp sản phẩm">
              {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
            <form
              className="search"
              role="search"
              onSubmit={(e) => { e.preventDefault(); updateParam('keyword', searchInput.trim()); }}
            >
              <Icon name="search" />
              <input value={searchInput} onChange={(e) => setSearchInput(e.target.value)} placeholder="Tìm tên laptop hoặc SKU..." aria-label="Tìm trong danh sách laptop" />
              <button type="submit" className="text-btn" aria-label="Tìm kiếm"><Icon name="arrow-right" /></button>
            </form>
          </div>

          {activeFilters.length > 0 && (
            <div className="chips" style={{ marginTop: 0 }}>
              {activeFilters.map((f) => (
                <button key={f.key} type="button" className="chip active" onClick={() => updateParam(f.key, '')} aria-label={`Bỏ lọc ${f.label}`}>
                  {f.label} ×
                </button>
              ))}
              <button type="button" className="text-btn small" onClick={clearFilters}>Xóa bộ lọc</button>
            </div>
          )}

          {loading ? (
            <div className="products-grid catalog-products">
              {Array.from({ length: 6 }, (_, i) => <ProductCardSkeleton key={i} />)}
            </div>
          ) : error ? (
            <EmptyState icon="alert" title="Không tải được danh sách" copy="Máy chủ chưa phản hồi. Kiểm tra backend rồi thử lại." />
          ) : products.length === 0 ? (
            <EmptyState icon="search" title="Chưa tìm thấy chiếc máy phù hợp" copy="Thử đổi từ khóa hoặc bớt một vài bộ lọc. Chiếc laptop của bạn đang chờ được khám phá.">
              <button type="button" className="btn primary" onClick={clearFilters}>Xem tất cả laptop</button>
            </EmptyState>
          ) : (
            <>
              <div className="products-grid catalog-products">
                {products.map((p) => <ProductCard key={p.id} product={p} />)}
              </div>
              <Pager page={page} totalPages={totalPages} onChange={changePage} />
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductListPage;
