import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { cartApi } from "../api/cartApi";
import { Cart } from "../types";
import { orderApi } from "@/modules/order/api/orderApi";
import { CheckoutPreviewResponse } from "@/modules/order/types";
import { StoreLayout } from "@/modules/order/components/CommerceLayout";
import {
  Alert,
  ConfirmDialog,
  Loading,
  ProductVisual,
  Totals,
  errorMessage,
  money,
} from "@/modules/order/components/OrderUI";

export function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [preview, setPreview] = useState<CheckoutPreviewResponse | null>(null);
  const [code, setCode] = useState(
    () => sessionStorage.getItem("commerce.coupon") || "",
  );
  const [applied, setApplied] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [removing, setRemoving] = useState<number | null>(null);
  const lock = useRef(false);
  const refreshTotals = useCallback(async (data: Cart, coupon: string) => {
    setPreview(null);
    if (!data.items.length) return;
    try {
      const result = await orderApi.previewCheckout({
        couponCode: coupon || undefined,
      });
      setPreview(result);
      setApplied(result.couponCode || "");
      if (result.couponCode)
        sessionStorage.setItem("commerce.coupon", result.couponCode);
      else sessionStorage.removeItem("commerce.coupon");
    } catch (err) {
      setApplied("");
      sessionStorage.removeItem("commerce.coupon");
      setError(errorMessage(err));
    }
  }, []);
  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await cartApi.get();
      setCart(data);
      await refreshTotals(
        data,
        sessionStorage.getItem("commerce.coupon") || "",
      );
    } catch (err) {
      setError(errorMessage(err, "Không thể tải giỏ hàng. Vui lòng thử lại."));
    } finally {
      setLoading(false);
    }
  }, [refreshTotals]);
  useEffect(() => {
    void load();
  }, [load]);
  async function mutate(action: () => Promise<Cart>) {
    if (lock.current) return;
    lock.current = true;
    setBusy(true);
    setError("");
    try {
      const data = await action();
      setCart(data);
      setRemoving(null);
      await refreshTotals(data, applied);
    } catch (err) {
      setRemoving(null);
      setError(errorMessage(err));
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  async function coupon(e: FormEvent, clear = false) {
    e.preventDefault();
    if (!cart || lock.current) return;
    lock.current = true;
    setBusy(true);
    setError("");
    try {
      if (clear) setCode("");
      await refreshTotals(cart, clear ? "" : code.trim().toUpperCase());
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  const invalid = cart?.items.some(
    (i) =>
      i.sellable === false ||
      (i.availableQuantity != null && i.quantity > i.availableQuantity),
  );
  return (
    <StoreLayout>
      <div className="page-heading">
        <div>
          <h1>Giỏ hàng của bạn</h1>
          <p>Một chiếc máy tuyệt vời đang chờ bạn.</p>
        </div>
        <Link className="button ghost" to="/products">
          Tiếp tục mua sắm
        </Link>
      </div>
      {error && (
        <Alert>
          {error}{" "}
          <button
            className="text-button"
            disabled={busy}
            onClick={() => void load()}
          >
            Thử lại
          </button>
        </Alert>
      )}
      {loading ? (
        <Loading />
      ) : cart?.items.length === 0 ? (
        <div className="empty cart-empty">
          <img src="/commerce/empty-cart.svg" width="300" height="240" alt="" />
          <p className="eyebrow">GIỎ HÀNG CỦA BẠN</p>
          <h1>Giỏ hàng đang chờ điều thú vị</h1>
          <p className="muted">
            Một chiếc laptop phù hợp có thể mở ra nhiều điều mới.
            <br />
            Khám phá các mẫu máy đang có tại LaptopStore nhé.
          </p>
          <Link className="button primary" to="/products">
            Khám phá laptop
          </Link>
          <small className="muted">Cần hỗ trợ? Hotline 1800 2026</small>
        </div>
      ) : (
        cart && (
          <div className="columns">
            <div className="stack">
              {cart.items.map((item) => (
                <article className="panel product-row" key={item.cartItemId}>
                  <ProductVisual productId={item.productId} />
                  <div className="product-copy">
                    <span
                      className={
                        item.sellable === false || item.availableQuantity === 0
                          ? "badge red"
                          : "badge green"
                      }
                    >
                      {item.sellable === false
                        ? "Ngừng bán"
                        : item.availableQuantity === 0
                          ? "Hết hàng"
                          : "Có hàng"}
                    </span>
                    <h2>{item.productName}</h2>
                    <p>{item.variantName}</p>
                    <small>SKU {item.sku}</small>
                    <strong className="product-price">
                      {money(item.lineTotal)}
                    </strong>
                  </div>
                  <div className="item-actions">
                    <div
                      className="quantity"
                      aria-label={"Số lượng " + item.productName}
                    >
                      <button
                        aria-label={"Giảm số lượng " + item.productName}
                        disabled={busy || item.quantity <= 1}
                        onClick={() =>
                          void mutate(() =>
                            cartApi.updateItem(
                              item.cartItemId,
                              item.quantity - 1,
                            ),
                          )
                        }
                      >
                        −
                      </button>
                      <output aria-live="polite">{item.quantity}</output>
                      <button
                        aria-label={"Tăng số lượng " + item.productName}
                        disabled={
                          busy ||
                          item.sellable === false ||
                          (item.availableQuantity != null &&
                            item.quantity >= item.availableQuantity)
                        }
                        onClick={() =>
                          void mutate(() =>
                            cartApi.updateItem(
                              item.cartItemId,
                              item.quantity + 1,
                            ),
                          )
                        }
                      >
                        +
                      </button>
                    </div>
                    <button
                      className="text-button"
                      disabled={busy}
                      onClick={() => setRemoving(item.cartItemId)}
                    >
                      Xóa sản phẩm
                    </button>
                  </div>
                </article>
              ))}
              <section className="panel stack">
                <h3>Thêm một chút ưu đãi</h3>
                <form className="coupon-form" onSubmit={(e) => void coupon(e)}>
                  <label>
                    Mã giảm giá
                    <input
                      value={code}
                      maxLength={100}
                      disabled={busy}
                      placeholder="Nhập mã giảm giá"
                      onChange={(e) => setCode(e.target.value)}
                    />
                  </label>
                  <button className="button" disabled={busy || !code.trim()}>
                    Áp dụng
                  </button>
                </form>
                {applied && (
                  <>
                    <span className="badge green">
                      Đã giảm {money(preview?.discountAmount || 0)} cho đơn hàng
                      này
                    </span>
                    <button
                      className="text-button"
                      disabled={busy}
                      onClick={(e) => void coupon(e, true)}
                    >
                      Bỏ mã giảm giá
                    </button>
                  </>
                )}
              </section>
            </div>
            <Totals
              subtotal={preview?.subtotal ?? cart.totalAmount}
              discountAmount={preview?.discountAmount ?? 0}
              shippingFee={preview?.shippingFee ?? 0}
              totalAmount={preview?.totalAmount ?? cart.totalAmount}
              couponCode={applied}
            >
              {invalid && (
                <Alert>
                  Giỏ có sản phẩm không còn đủ tồn hoặc đã ngừng bán. Vui lòng
                  cập nhật giỏ.
                </Alert>
              )}
              <Link
                className="button primary wide"
                to="/checkout"
                aria-disabled={busy || invalid || !preview}
                onClick={(e) => {
                  if (busy || invalid || !preview) e.preventDefault();
                }}
              >
                Tiến hành thanh toán
              </Link>
            </Totals>
          </div>
        )
      )}
      <ConfirmDialog
        open={removing !== null}
        busy={busy}
        title="Xóa sản phẩm khỏi giỏ?"
        confirmLabel="Xóa sản phẩm"
        onClose={() => setRemoving(null)}
        onConfirm={() =>
          void mutate(async () => {
            await cartApi.removeItem(removing!);
            return cartApi.get();
          })
        }
      >
        Bạn có thể thêm lại sản phẩm bất cứ lúc nào.
      </ConfirmDialog>
    </StoreLayout>
  );
}
