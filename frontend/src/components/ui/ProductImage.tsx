import React, { useEffect, useState } from 'react';
import { resolveImageUrl } from '@/utils/image';

export const PLACEHOLDER_SILVER = '/images/product-silver.svg';
export const PLACEHOLDER_DARK = '/images/product-dark.svg';

/** Ảnh minh họa thay thế khi sản phẩm chưa có ảnh hoặc ảnh lỗi (gaming → máy tối màu). */
export const placeholderFor = (hint?: string | null) =>
  hint && /gaming|rog|tuf|legion|nitro|predator|victus|loq|g15/i.test(hint) ? PLACEHOLDER_DARK : PLACEHOLDER_SILVER;

interface ProductImageProps extends Omit<React.ImgHTMLAttributes<HTMLImageElement>, 'src'> {
  src?: string | null;
  /** Gợi ý chọn ảnh thay thế (tên/danh mục sản phẩm). */
  hint?: string | null;
}

/** <img> cho ảnh sản phẩm: chuẩn hóa URL /uploads và tự đổi sang ảnh minh họa nếu tải lỗi. */
export const ProductImage: React.FC<ProductImageProps> = ({ src, hint, alt = '', ...rest }) => {
  const fallback = placeholderFor(hint ?? alt);
  const resolved = resolveImageUrl(src) || fallback;
  const [current, setCurrent] = useState(resolved);

  useEffect(() => setCurrent(resolved), [resolved]);

  return (
    <img
      {...rest}
      src={current}
      alt={alt}
      loading={rest.loading ?? 'lazy'}
      decoding="async"
      onError={() => {
        if (current !== fallback) setCurrent(fallback);
      }}
    />
  );
};
