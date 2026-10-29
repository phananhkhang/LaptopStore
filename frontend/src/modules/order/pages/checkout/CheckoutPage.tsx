import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartApi } from '@/modules/cart/api/cartApi';
import { Cart } from '@/modules/cart/types';
import { userApi } from '@/modules/user/api/userApi';
import { Address } from '@/modules/user/types';
import { orderApi } from '../../api/orderApi';
import { paymentApi } from '../../api/paymentApi';
import { CheckoutPaymentMethod, CheckoutPreviewResponse } from '../../types';

const formatMoney = (amount: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);

const formatSavedAddress = (address: Address) =>
  [address.street, address.ward, address.district, address.city, address.postalCode]
    .filter((part): part is string => Boolean(part && part.trim()))
    .join(', ');

const createIdempotencyKey = () =>
  `checkout-${Date.now()}-${Math.random().toString(36).substring(2, 10)}`;

export const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();

  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [useSavedAddress, setUseSavedAddress] = useState<boolean>(false);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);

  const [recipientName, setRecipientName] = useState<string>('');
  const [recipientPhone, setRecipientPhone] = useState<string>('');
  const [shippingAddress, setShippingAddress] = useState<string>('');
  const [paymentMethod, setPaymentMethod] = useState<CheckoutPaymentMethod>('COD');
  const [couponCode, setCouponCode] = useState<string>('');
  const [note, setNote] = useState<string>('');

  const [preview, setPreview] = useState<CheckoutPreviewResponse | null>(null);
  const [idempotencyKey, setIdempotencyKey] = useState<string>(() => createIdempotencyKey());
  const [loading, setLoading] = useState<boolean>(true);
  const [previewLoading, setPreviewLoading] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [couponMessage, setCouponMessage] = useState<string | null>(null);

  const loadCheckoutData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [cartData, addressList] = await Promise.all([
        cartApi.get(),
        userApi.getAddresses().catch(() => [] as Address[]),
      ]);
      setCart(cartData);
      setAddresses(addressList);

      if (addressList.length > 0) {
        const defaultAddress = addressList.find((addr) => addr.isDefault) ?? addressList[0];
        setUseSavedAddress(true);
        setSelectedAddressId(defaultAddress.id);
      }
    } catch {
      setError('Không thể tải thông tin giỏ hàng để checkout. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCheckoutData();
  }, [loadCheckoutData]);

  const selectedAddress = useMemo(
    () => addresses.find((addr) => addr.id === selectedAddressId) ?? null,
    [addresses, selectedAddressId]
  );

  const handlePreviewCoupon = async () => {
    try {
      setPreviewLoading(true);
      setError(null);
      setCouponMessage(null);
      const result = await orderApi.previewCheckout({
        addressId: useSavedAddress ? selectedAddressId : null,
        recipientName: useSavedAddress ? undefined : recipientName,
        recipientPhone: useSavedAddress ? undefined : recipientPhone,
        shippingAddress: useSavedAddress ? undefined : shippingAddress,
        paymentMethod,
        couponCode: couponCode.trim() || undefined,
      });
      setPreview(result);
      setIdempotencyKey(createIdempotencyKey());
      if (result.couponCode) {
        setCouponMessage(`Đã áp dụng mã ${result.couponCode}: giảm ${formatMoney(result.discountAmount)}`);
      } else {
        setCouponMessage('Đã cập nhật tạm tính đơn hàng.');
      }
    } catch {
      setPreview(null);
      setError('Mã giảm giá không hợp lệ hoặc giỏ hàng có sản phẩm vượt quá tồn kho.');
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleSubmitOrder = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!cart || cart.items.length === 0) {
      setError('Giỏ hàng đang trống, không thể tạo đơn hàng.');
      return;
    }

    if (useSavedAddress) {
      if (!selectedAddressId) {
        setError('Vui lòng chọn địa chỉ giao hàng.');
        return;
      }
    } else if (!recipientName.trim() || !recipientPhone.trim() || !shippingAddress.trim()) {
      setError('Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ nhận hàng.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      const createdOrder = await orderApi.createOrder(
        {
          addressId: useSavedAddress ? selectedAddressId : null,
          recipientName: useSavedAddress ? undefined : recipientName.trim(),
          recipientPhone: useSavedAddress ? undefined : recipientPhone.trim(),
          shippingAddress: useSavedAddress ? undefined : shippingAddress.trim(),
          paymentMethod,
          couponCode: couponCode.trim() || undefined,
          note: note.trim() || undefined,
          idempotencyKey,
        },
        idempotencyKey
      );
      try {
        await paymentApi.start(createdOrder.id, paymentMethod);
      } catch {
        // The order is committed. Its detail page can resume payment initialization.
      }
      navigate(`/orders/${createdOrder.id}`);
    } catch {
      setError('Không thể tạo đơn hàng. Vui lòng kiểm tra lại tồn kho, địa chỉ hoặc mã giảm giá.');
    } finally {
      setSubmitting(false);
    }
  };

  const subtotal = preview ? preview.subtotal : cart?.totalAmount ?? 0;
  const discountAmount = preview ? preview.discountAmount : 0;
  const shippingFee = preview ? preview.shippingFee : 0;
  const totalAmount = preview ? preview.totalAmount : Math.max(0, subtotal - discountAmount + shippingFee);

  if (loading) {
    return <main style={{ maxWidth: 840, margin: '0 auto', padding: '2rem' }}>Đang tải thông tin checkout...</main>;
  }

  return (
    <main style={{ maxWidth: 840, margin: '0 auto', padding: '2rem' }}>
      <h1>Thanh toán đơn hàng (Checkout)</h1>
      <p>Kiểm tra địa chỉ nhận hàng, phương thức thanh toán, mã giảm giá và xác nhận đặt hàng.</p>

      {error && (
        <p role="alert" style={{ color: '#b42318', background: '#fef3f2', padding: '0.75rem 1rem', borderRadius: 6 }}>
          {error}
        </p>
      )}

      {(!cart || cart.items.length === 0) ? (
        <section>
          <p>Giỏ hàng của bạn đang trống.</p>
          <Link to="/cart">Quay lại giỏ hàng</Link>
        </section>
      ) : (
        <form onSubmit={(e) => void handleSubmitOrder(e)}>
          <section style={{ marginBottom: '1.5rem', padding: '1rem', border: '1px solid #ddd', borderRadius: 8 }}>
            <h2>1. Địa chỉ giao hàng</h2>
            {addresses.length > 0 && (
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ marginRight: '1.5rem' }}>
                  <input
                    type="radio"
                    name="addressMode"
                    checked={useSavedAddress}
                    onChange={() => setUseSavedAddress(true)}
                  />{' '}
                  Dùng địa chỉ đã lưu
                </label>
                <label>
                  <input
                    type="radio"
                    name="addressMode"
                    checked={!useSavedAddress}
                    onChange={() => setUseSavedAddress(false)}
                  />{' '}
                  Nhập địa chỉ mới
                </label>
              </div>
            )}

            {useSavedAddress && addresses.length > 0 ? (
              <div>
                <label style={{ display: 'block', marginBottom: 8 }}>
                  Chọn sổ địa chỉ
                  <select
                    value={selectedAddressId ?? ''}
                    onChange={(e) => setSelectedAddressId(Number(e.target.value))}
                    style={{ display: 'block', width: '100%', padding: '0.5rem', marginTop: 4 }}
                  >
                    {addresses.map((addr) => (
                      <option key={addr.id} value={addr.id}>
                        {addr.recipientName} ({addr.phone}) — {formatSavedAddress(addr)}
                        {addr.isDefault ? ' [Mặc định]' : ''}
                      </option>
                    ))}
                  </select>
                </label>
                {selectedAddress && (
                  <p style={{ margin: '0.5rem 0 0', color: '#475467' }}>
                    Người nhận: <strong>{selectedAddress.recipientName}</strong> · SĐT:{' '}
                    <strong>{selectedAddress.phone}</strong> · Địa chỉ: {formatSavedAddress(selectedAddress)}
                  </p>
                )}
              </div>
            ) : (
              <div>
                <label style={{ display: 'block', marginBottom: 12 }}>
                  Họ tên người nhận *
                  <input
                    type="text"
                    required={!useSavedAddress}
                    maxLength={100}
                    value={recipientName}
                    onChange={(e) => setRecipientName(e.target.value)}
                    placeholder="Nguyễn Văn A"
                    style={{ display: 'block', width: '100%', padding: '0.5rem', marginTop: 4 }}
                  />
                </label>
                <label style={{ display: 'block', marginBottom: 12 }}>
                  Số điện thoại *
                  <input
                    type="tel"
                    required={!useSavedAddress}
                    maxLength={20}
                    value={recipientPhone}
                    onChange={(e) => setRecipientPhone(e.target.value)}
                    placeholder="0901234567"
                    style={{ display: 'block', width: '100%', padding: '0.5rem', marginTop: 4 }}
                  />
                </label>
                <label style={{ display: 'block', marginBottom: 12 }}>
                  Địa chỉ nhận hàng *
                  <input
                    type="text"
                    required={!useSavedAddress}
                    value={shippingAddress}
                    onChange={(e) => setShippingAddress(e.target.value)}
                    placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"
                    style={{ display: 'block', width: '100%', padding: '0.5rem', marginTop: 4 }}
                  />
                </label>
              </div>
            )}
          </section>

          <section style={{ marginBottom: '1.5rem', padding: '1rem', border: '1px solid #ddd', borderRadius: 8 }}>
            <h2>2. Phương thức thanh toán</h2>
            <label style={{ display: 'block', marginBottom: 8 }}>
              <input
                type="radio"
                name="paymentMethod"
                value="COD"
                checked={paymentMethod === 'COD'}
                onChange={() => setPaymentMethod('COD')}
              />{' '}
              <strong>COD</strong> — Thanh toán tiền mặt khi nhận hàng
            </label>
            <label style={{ display: 'block' }}>
              <input
                type="radio"
                name="paymentMethod"
                value="VNPAY"
                checked={paymentMethod === 'VNPAY'}
                onChange={() => setPaymentMethod('VNPAY')}
              />{' '}
              <strong>VNPAY</strong> — Thanh toán trực tuyến qua cổng VNPay
            </label>
          </section>

          <section style={{ marginBottom: '1.5rem', padding: '1rem', border: '1px solid #ddd', borderRadius: 8 }}>
            <h2>3. Mã khuyến mãi & Ghi chú</h2>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12 }}>
              <input
                type="text"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value)}
                placeholder="Nhập mã giảm giá (ví dụ: STUDENT10)"
                style={{ flex: 1, padding: '0.5rem' }}
              />
              <button
                type="button"
                disabled={previewLoading}
                onClick={() => void handlePreviewCoupon()}
                style={{ padding: '0.5rem 1rem' }}
              >
                {previewLoading ? 'Đang kiểm tra...' : 'Áp dụng / Tính thử'}
              </button>
            </div>
            {couponMessage && <p style={{ color: '#027a48', margin: '0 0 12px' }}>{couponMessage}</p>}
            <label style={{ display: 'block' }}>
              Ghi chú giao hàng (tùy chọn)
              <textarea
                maxLength={500}
                rows={2}
                value={note}
                onChange={(e) => setNote(e.target.value)}
                style={{ display: 'block', width: '100%', padding: '0.5rem', marginTop: 4 }}
              />
            </label>
          </section>

          <section style={{ marginBottom: '1.5rem', padding: '1rem', border: '1px solid #ddd', borderRadius: 8 }}>
            <h2>4. Danh sách sản phẩm & Tổng kết thanh toán</h2>
            {cart.items.map((item) => (
              <div
                key={item.cartItemId}
                style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #eee', padding: '0.5rem 0' }}
              >
                <div>
                  <strong>{item.productName}</strong>
                  <div style={{ fontSize: '0.9rem', color: '#475467' }}>
                    {item.variantName} · SKU: {item.sku} · SL: {item.quantity} × {formatMoney(item.unitPrice)}
                  </div>
                </div>
                <div>
                  <strong>{formatMoney(item.lineTotal)}</strong>
                </div>
              </div>
            ))}

            <div style={{ marginTop: '1rem', lineHeight: 1.8 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Tạm tính (Subtotal):</span>
                <span>{formatMoney(subtotal)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Giảm giá (Discount):</span>
                <span>-{formatMoney(discountAmount)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Phí vận chuyển (Shipping Fee):</span>
                <span>{formatMoney(shippingFee)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.15rem', marginTop: 8 }}>
                <strong>Tổng thanh toán (Total):</strong>
                <strong>{formatMoney(totalAmount)}</strong>
              </div>
            </div>
          </section>

          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <button type="submit" disabled={submitting} style={{ padding: '0.65rem 1.5rem', fontWeight: 600 }}>
              {submitting ? 'Đang tạo đơn hàng...' : 'Xác nhận đặt hàng'}
            </button>
            <Link to="/cart">Quay lại giỏ hàng</Link>
          </div>
        </form>
      )}
    </main>
  );
};
