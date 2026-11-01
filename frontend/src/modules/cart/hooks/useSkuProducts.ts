import { useEffect, useState } from 'react';
import { catalogApi } from '@/modules/catalog/api/catalogApi';

export interface SkuProduct {
  productId: number;
  thumbnailImage: string | null;
  categoryName?: string;
}

// Cache trong phiên: mỗi SKU chỉ tra cứu một lần.
const cache = new Map<string, Promise<SkuProduct | null>>();

const lookup = (sku: string) => {
  if (!cache.has(sku)) {
    cache.set(
      sku,
      catalogApi.searchProducts({ keyword: sku, page: 0, size: 1 })
        .then((page) => {
          const p = page.content[0];
          return p ? { productId: p.id, thumbnailImage: p.thumbnailImage, categoryName: p.categoryName } : null;
        })
        .catch(() => {
          cache.delete(sku);
          return null;
        })
    );
  }
  return cache.get(sku)!;
};

/**
 * Giỏ hàng/đơn hàng chỉ trả SKU, không có ảnh hay productId.
 * API tìm kiếm công khai khớp theo SKU nên dùng nó để lấy ảnh + link chi tiết.
 */
export const useSkuProducts = (skus: string[]) => {
  const [map, setMap] = useState<Record<string, SkuProduct | null>>({});
  const key = [...new Set(skus)].sort().join('|');

  useEffect(() => {
    if (!key) return;
    let cancelled = false;
    const list = key.split('|');
    Promise.all(list.map((sku) => lookup(sku).then((v) => [sku, v] as const))).then((entries) => {
      if (!cancelled) setMap(Object.fromEntries(entries));
    });
    return () => { cancelled = true; };
  }, [key]);

  return map;
};
