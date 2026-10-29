import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/providers/AuthProvider";
import { cartApi } from "@/modules/cart/api/cartApi";
import { Cart } from "@/modules/cart/types";
import { userApi } from "@/modules/user/api/userApi";
import { Address } from "@/modules/user/types";
import { orderApi } from "../../api/orderApi";
import { openVnpay, paymentApi } from "../../api/paymentApi";
import {
  CheckoutPaymentMethod,
  CheckoutPreviewResponse,
  CreateOrderRequest,
} from "../../types";
import { StoreLayout } from "../../components/CommerceLayout";
import {
  Alert,
  Loading,
  OrderItems,
  Totals,
  errorMessage,
} from "../../components/OrderUI";

export function CheckoutPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [addressId, setAddressId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [method, setMethod] = useState<CheckoutPaymentMethod>("COD");
  const [code, setCode] = useState(
    () => sessionStorage.getItem("commerce.coupon") || "",
  );
  const [applied, setApplied] = useState(code);
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [preview, setPreview] = useState<CheckoutPreviewResponse | null>(null);
  const [previewBusy, setPreviewBusy] = useState(false);
  const [error, setError] = useState("");
  const [previewError, setPreviewError] = useState("");
  const [previewRetry, setPreviewRetry] = useState(0);
  const [reload, setReload] = useState(0);
  const lock = useRef(false);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    Promise.all([cartApi.get(), userApi.getAddresses()])
      .then(([data, saved]) => {
        if (!active) return;
        setCart(data);
        setAddresses(saved);
        setAddressId(
          String((saved.find((a) => a.isDefault) || saved[0])?.id || ""),
        );
      })
      .catch((err) => {
        if (active)
          setError(errorMessage(err, "Không thể tải thông tin checkout."));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reload]);
  const payload = useMemo<CreateOrderRequest>(
    () => ({
      addressId: addressId ? Number(addressId) : null,
      recipientName: addressId ? undefined : name.trim(),
      recipientPhone: addressId ? undefined : phone.trim(),
      shippingAddress: addressId ? undefined : address.trim(),
      paymentMethod: method,
      couponCode: applied.trim().toUpperCase() || undefined,
      note: note.trim() || undefined,
    }),
    [addressId, name, phone, address, method, applied, note],
  );
  useEffect(() => {
    if (loading || !cart?.items.length) return;
    let active = true;
    setPreview(null);
    setPreviewBusy(true);
    setPreviewError("");
    const timer = setTimeout(() => {
      orderApi
        .previewCheckout(payload)
        .then((data) => {
          if (active) setPreview(data);
        })
        .catch((err) => {
          if (active) setPreviewError(errorMessage(err));
        })
        .finally(() => {
          if (active) setPreviewBusy(false);
        });
    }, 250);
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [payload, loading, cart, previewRetry]);
  async function submit(e: FormEvent) {
    e.preventDefault();
    if (lock.current || !preview || previewBusy) return;
    lock.current = true;
    setBusy(true);
    setError("");
    let createdId: number | undefined;
    try {
      const signature = JSON.stringify({
        payload,
        lines: cart?.items.map((i) => [i.variantId, i.quantity]),
      });
      const storageKey = "commerce.checkout:" + user?.id;
      let previous: { signature: string; key: string } | null = null;
      try {
        previous = JSON.parse(sessionStorage.getItem(storageKey) || "null");
      } catch {
        /* replace malformed local data */
      }
      const key =
        previous?.signature === signature ? previous.key : crypto.randomUUID();
      sessionStorage.setItem(storageKey, JSON.stringify({ signature, key }));
      const order = await orderApi.createOrder(payload, key);
      createdId = order.id;
      sessionStorage.removeItem("commerce.coupon");
      if (method === "VNPAY") await openVnpay(order.id);
      else {
        await paymentApi.start(order.id, "COD");
        navigate("/orders/" + order.id + "/success", { replace: true });
      }
    } catch (err) {
      if (createdId)
        navigate("/orders/" + createdId, {
          replace: true,
          state: {
            paymentError: errorMessage(
              err,
              "Đơn đã được tạo. Vui lòng tiếp tục thanh toán tại đây.",
            ),
          },
        });
      else
        setError(
          errorMessage(
            err,
            "Chưa nhận được kết quả đặt hàng. Bạn có thể thử lại với cùng thông tin.",
          ),
        );
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  const saved = addresses.find((a) => a.id === Number(addressId));
  return (
    <StoreLayout>
      <div className="page-heading">
        <div>
          <h1>Hoàn tất đơn hàng</h1>
          <p>Thông tin rõ ràng. Mua sắm an tâm.</p>
        </div>
        <Link className="button ghost" to="/cart">
          Quay lại giỏ hàng
        </Link>
      </div>
      {error && (
        <Alert>
          {error}{" "}
          {!cart && (
            <button
              className="text-button"
              onClick={() => setReload((n) => n + 1)}
            >
              Thử lại
            </button>
          )}
        </Alert>
      )}
      {loading ? (
        <Loading />
      ) : cart && !cart.items.length ? (
        <div className="empty panel">
          <h2>Giỏ hàng đang trống</h2>
          <Link className="button primary" to="/cart">
            Quay lại giỏ hàng
          </Link>
        </div>
      ) : (
        cart && (
          <form onSubmit={(e) => void submit(e)} className="columns">
            <fieldset
              className="stack"
              disabled={busy}
              style={{ border: 0, padding: 0, margin: 0, minWidth: 0 }}
            >
              <section className="panel stack">
                <h2>01 / Địa chỉ nhận hàng</h2>
                {addresses.length > 0 && (
                  <label>
                    Chọn địa chỉ
                    <select
                      value={addressId}
                      onChange={(e) => setAddressId(e.target.value)}
                    >
                      {addresses.map((a) => (
                        <option key={a.id} value={a.id}>
                          {a.recipientName} · {a.street}
                          {a.isDefault ? " (Mặc định)" : ""}
                        </option>
                      ))}
                      <option value="">Nhập địa chỉ khác</option>
                    </select>
                  </label>
                )}
                {saved ? (
                  <div className="stack">
                    <strong>
                      {saved.recipientName} · {saved.phone}
                    </strong>
                    <p className="muted">
                      {[saved.street, saved.ward, saved.district, saved.city]
                        .filter(Boolean)
                        .join(", ")}
                    </p>
                    {saved.isDefault && (
                      <span className="badge">Địa chỉ mặc định</span>
                    )}
                  </div>
                ) : (
                  <>
                    <div className="inline-fields">
                      <label>
                        Họ và tên
                        <input
                          required
                          maxLength={100}
                          autoComplete="shipping name"
                          value={name}
                          onChange={(e) => setName(e.target.value)}
                        />
                      </label>
                      <label>
                        Số điện thoại
                        <input
                          required
                          type="tel"
                          maxLength={20}
                          autoComplete="shipping tel"
                          value={phone}
                          onChange={(e) => setPhone(e.target.value)}
                        />
                      </label>
                    </div>
                    <label>
                      Địa chỉ nhận hàng
                      <textarea
                        required
                        maxLength={1000}
                        autoComplete="shipping street-address"
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                      />
                    </label>
                  </>
                )}
              </section>
              <section className="panel stack">
                <h2>02 / Phương thức thanh toán</h2>
                <div className="payment-choices">
                  {(["COD", "VNPAY"] as const).map((m) => (
                    <label
                      key={m}
                      className={
                        "payment-option " + (method === m ? "selected" : "")
                      }
                    >
                      <input
                        type="radio"
                        name="paymentMethod"
                        value={m}
                        checked={method === m}
                        onChange={() => setMethod(m)}
                      />
                      <span>
                        {m === "COD" ? "Thanh toán khi nhận hàng" : "VNPay"}
                        <small>
                          {m === "COD"
                            ? "COD · Thanh toán trực tiếp"
                            : "Thanh toán trực tuyến"}
                        </small>
                      </span>
                    </label>
                  ))}
                </div>
              </section>
              <label>
                Ghi chú giao hàng (tùy chọn)
                <input
                  maxLength={500}
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="Ví dụ: Giao trong giờ hành chính"
                />
              </label>
              <section className="panel stack">
                <h3>Mã giảm giá</h3>
                <div className="coupon-form">
                  <label>
                    Mã ưu đãi
                    <input
                      value={code}
                      maxLength={100}
                      onChange={(e) => setCode(e.target.value)}
                      placeholder="Nhập mã giảm giá"
                    />
                  </label>
                  <button
                    type="button"
                    className="button"
                    onClick={() => setApplied(code)}
                  >
                    Áp dụng
                  </button>
                  <button
                    type="button"
                    className="text-button"
                    onClick={() => {
                      setApplied("");
                      setCode("");
                    }}
                  >
                    Bỏ mã
                  </button>
                </div>
              </section>
              <OrderItems
                items={
                  preview?.items ||
                  cart.items.map((i) => ({
                    ...i,
                    id: i.cartItemId,
                    productId: i.productId || 0,
                  }))
                }
              />
            </fieldset>
            <Totals
              subtotal={preview?.subtotal ?? cart.totalAmount}
              discountAmount={preview?.discountAmount ?? 0}
              shippingFee={preview?.shippingFee ?? 0}
              totalAmount={preview?.totalAmount ?? cart.totalAmount}
              couponCode={preview?.couponCode}
            >
              {previewError && (
                <Alert>
                  {previewError}
                  <button
                    type="button"
                    className="text-button"
                    onClick={() => setPreviewRetry((n) => n + 1)}
                  >
                    Kiểm tra lại
                  </button>
                </Alert>
              )}
              {previewBusy && (
                <p role="status" className="muted">
                  Đang kiểm tra giỏ hàng và ưu đãi...
                </p>
              )}
              <button
                className="button primary wide"
                disabled={busy || previewBusy || !preview}
              >
                {busy
                  ? "Đang đặt hàng..."
                  : method === "VNPAY"
                    ? "Đặt hàng và thanh toán VNPay"
                    : "Đặt hàng"}
              </button>
            </Totals>
          </form>
        )
      )}
    </StoreLayout>
  );
}
