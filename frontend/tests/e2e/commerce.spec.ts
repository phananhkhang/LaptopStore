import { test, expect, Page } from "@playwright/test";

async function mockShop(
  page: Page,
  options: {
    staff?: boolean;
    failFirstOrder?: boolean;
    payment?: string;
    unavailable?: boolean;
    pages?: boolean;
  } = {},
) {
  const item = {
    id: 1,
    cartItemId: 1,
    productId: 1,
    variantId: 7,
    productName: "ASUS ROG Strix G15",
    variantName: "16GB · 512GB · Đen",
    sku: "ROG-G15-01",
    unitPrice: 25000000,
    quantity: 1,
    lineTotal: 25000000,
    sellable: !options.unavailable,
    availableQuantity: options.unavailable ? 0 : 5,
  };
  let cartItems = [item];
  let status = "WAITING_CONFIRMATION";
  let payment = options.payment || "PENDING";
  const keys: string[] = [];
  const payloads: any[] = [];
  const history = [
    {
      id: 1,
      fromStatus: null,
      toStatus: status,
      changedAt: "2026-10-24T02:42:00Z",
      changedBy: 42,
    },
  ];
  const transitions: Record<string, string[]> = {
    WAITING_CONFIRMATION: ["CONFIRMED", "CANCELLED"],
    CONFIRMED: ["PACKING", "CANCELLED"],
    PACKING: ["SHIPPING"],
    SHIPPING: ["DELIVERED", "DELIVERY_FAILED"],
    DELIVERY_FAILED: ["SHIPPING", "CANCELLED"],
    DELIVERED: ["RETURNED"],
    CANCELLED: [],
  };
  const order = () => ({
    id: 1,
    userId: 42,
    orderCode: "LS24102601",
    status,
    totalItems: 1,
    subtotal: 25000000,
    discountAmount: 500000,
    shippingFee: 0,
    totalAmount: 24500000,
    recipientName: "Nguyễn Minh An",
    recipientPhone: "0901234567",
    shippingAddress: "273 An Dương Vương, Phường 3, Quận 5, TP. Hồ Chí Minh",
    paymentMethod: "COD",
    customerNote: null,
    couponCodeSnapshot: "SAVE500",
    items: [item],
    statusHistories: history,
    allowedTransitions: transitions[status] || [],
    createdAt: "2026-10-24T02:42:00Z",
    updatedAt: "2026-10-24T02:42:00Z",
  });
  const cart = () => ({
    id: 1,
    items: cartItems,
    totalItems: cartItems.reduce((n, i) => n + i.quantity, 0),
    totalAmount: cartItems.reduce((n, i) => n + i.lineTotal, 0),
  });
  await page.route("**/api/v1/**", async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname.replace("/api/v1", "");
    const method = request.method();
    const json = (body: unknown, status = 200) =>
      route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify(body),
      });
    if (path === "/auth/refresh") return json({ accessToken: "test-token" });
    if (path === "/me")
      return json({
        id: 42,
        email: "minhan@example.com",
        fullName: "Minh An",
        roles: options.staff ? ["STAFF", "USER"] : ["USER"],
        status: "ACTIVE",
      });
    if (path === "/me/addresses")
      return json([
        {
          id: 1,
          recipientName: "Nguyễn Minh An",
          phone: "0901234567",
          street: "273 An Dương Vương",
          city: "TP. Hồ Chí Minh",
          isDefault: true,
        },
      ]);
    if (path === "/products/1") return json({ images: [] });
    if (path === "/me/cart") return json(cart());
    if (path === "/me/cart/items/1" && method === "PATCH") {
      const q = request.postDataJSON().quantity;
      cartItems = [{ ...item, quantity: q, lineTotal: q * item.unitPrice }];
      return json(cart());
    }
    if (path === "/me/cart/items/1" && method === "DELETE") {
      cartItems = [];
      return route.fulfill({ status: 204 });
    }
    if (path === "/me/orders/preview") {
      if (options.unavailable)
        return json({ message: "Sản phẩm không còn bán" }, 400);
      const data = request.postDataJSON();
      if (data.couponCode && data.couponCode !== "SAVE500")
        return json({ message: "Mã giảm giá không hợp lệ" }, 400);
      return json({
        ...order(),
        items: cartItems,
        subtotal: cart().totalAmount,
        couponCode: data.couponCode || null,
        discountAmount: data.couponCode ? 500000 : 0,
        totalAmount: cart().totalAmount - (data.couponCode ? 500000 : 0),
        paymentMethod: data.paymentMethod || "COD",
      });
    }
    if (path === "/me/orders" && method === "POST") {
      keys.push(request.headers()["idempotency-key"]);
      payloads.push(request.postDataJSON());
      if (options.failFirstOrder && keys.length === 1)
        return route.abort("failed");
      cartItems = [];
      return json(order(), 201);
    }
    if (path === "/me/orders" || path === "/staff/orders")
      return json({
        content: [order()],
        totalElements: options.pages ? 21 : 1,
        totalPages: options.pages ? 3 : 1,
        number: 0,
        size: 10,
      });
    if (path === "/me/orders/1" || path === "/staff/orders/1")
      return json(order());
    if (path === "/me/orders/1/cancel" || path === "/staff/orders/1/status") {
      const next = path.endsWith("/cancel")
        ? "CANCELLED"
        : request.postDataJSON().status;
      history.push({
        id: history.length + 1,
        fromStatus: status,
        toStatus: next,
        changedAt: new Date().toISOString(),
        changedBy: 42,
      });
      status = next;
      return json(order());
    }
    if (path === "/payments/cod") {
      payment = "PAID";
      return json({ id: 1, orderId: 1, method: "COD", status: payment });
    }
    if (path.startsWith("/payments"))
      return json({
        id: 1,
        orderId: 1,
        method: "COD",
        status: payment,
        amount: 24500000,
      });
    return json({ message: "Unexpected API " + path }, 404);
  });
  return { keys, payloads };
}

async function noOverflow(page: Page) {
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBeTruthy();
  const images = page.locator(".commerce img");
  for (const img of await images.all())
    await expect
      .poll(() =>
        img.evaluate(
          (el: HTMLImageElement) => el.complete && el.naturalWidth > 0,
        ),
      )
      .toBeTruthy();
}

test("cart quantity, coupon and confirmed removal use real API contracts", async ({
  page,
}, info) => {
  await mockShop(page);
  await page.goto("/cart");
  await expect(
    page.getByRole("heading", { name: "Giỏ hàng của bạn" }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Tăng số lượng ASUS" }).click();
  await expect(page.locator("output")).toHaveText("2");
  await page.getByLabel("Mã giảm giá", { exact: true }).fill("SAVE500");
  await page.getByRole("button", { name: "Áp dụng", exact: true }).click();
  await expect(page.getByText(/Đã giảm/)).toBeVisible();
  await noOverflow(page);
  await page.screenshot({ path: info.outputPath("cart.png"), fullPage: true });
  await page.getByRole("button", { name: "Xóa sản phẩm", exact: true }).click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Xóa sản phẩm" })
    .click();
  await expect(
    page.getByRole("heading", { name: "Giỏ hàng đang chờ điều thú vị" }),
  ).toBeVisible();
  await noOverflow(page);
});

test("checkout network retry retains idempotency key and creates one visible order", async ({
  page,
}, info) => {
  const mock = await mockShop(page, { failFirstOrder: true });
  await page.goto("/checkout");
  await page
    .getByLabel("Ghi chú giao hàng (tùy chọn)")
    .fill("Giao giờ hành chính");
  await expect(
    page.getByRole("button", { name: "Đặt hàng", exact: true }),
  ).toBeEnabled();
  await noOverflow(page);
  await page.screenshot({
    path: info.outputPath("checkout.png"),
    fullPage: true,
  });
  await page.getByRole("button", { name: "Đặt hàng", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("Chưa nhận được kết quả");
  await page.getByRole("button", { name: "Đặt hàng", exact: true }).click();
  await expect(page).toHaveURL(/orders\/1\/success/);
  expect(mock.keys).toHaveLength(2);
  expect(mock.keys[0]).toBe(mock.keys[1]);
  expect(mock.payloads[0].note).toBe("Giao giờ hành chính");
});

test("invalid coupon blocks checkout and can be removed", async ({ page }) => {
  await mockShop(page);
  await page.goto("/checkout");
  await page.getByLabel("Mã ưu đãi", { exact: true }).fill("EXPIRED");
  await page.getByRole("button", { name: "Áp dụng" }).click();
  await expect(page.getByRole("alert")).toContainText(
    "Mã giảm giá không hợp lệ",
  );
  await expect(
    page.getByRole("button", { name: "Đặt hàng", exact: true }),
  ).toBeDisabled();
  await page.getByRole("button", { name: "Bỏ mã", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Đặt hàng", exact: true }),
  ).toBeEnabled();
});

test("owner cancellation needs confirmation and updates lifecycle", async ({
  page,
}, info) => {
  await mockShop(page);
  await page.goto("/orders/1");
  await expect(
    page.getByRole("button", { name: "Hủy đơn hàng", exact: true }),
  ).toBeVisible();
  await noOverflow(page);
  await page.screenshot({ path: info.outputPath("order.png"), fullPage: true });
  await page.getByRole("button", { name: "Hủy đơn hàng", exact: true }).click();
  await page.keyboard.press("Escape");
  await expect(page.getByRole("dialog")).not.toBeVisible();
  await page.getByRole("button", { name: "Hủy đơn hàng", exact: true }).click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Xác nhận", exact: true })
    .click();
  await expect(
    page.getByRole("button", { name: "Hủy đơn hàng", exact: true }),
  ).toHaveCount(0);
  await expect(page.getByText("Đã hủy", { exact: true }).first()).toBeVisible();
});

test("staff sees only allowed actions and can complete delivery and COD", async ({
  page,
}, info) => {
  await mockShop(page, { staff: true });
  await page.goto("/staff/orders");
  await expect(
    page.getByRole("heading", { name: "Mỗi đơn hàng, một trải nghiệm tốt." }),
  ).toBeVisible();
  await noOverflow(page);
  await page.screenshot({
    path: info.outputPath("staff-orders.png"),
    fullPage: true,
  });
  await page.getByRole("link", { name: "Xử lý →" }).click();
  await expect(
    page.getByRole("button", { name: "Xác nhận đã giao", exact: true }),
  ).toHaveCount(0);
  for (const label of [
    "Xác nhận đơn",
    "Chuẩn bị đơn",
    "Bàn giao vận chuyển",
    "Ghi nhận giao thất bại",
    "Bàn giao vận chuyển",
    "Xác nhận đã giao",
    "Xác nhận đã thu COD",
  ]) {
    await page.getByRole("button", { name: label, exact: true }).click();
    await page
      .getByRole("dialog")
      .getByRole("button", { name: "Xác nhận", exact: true })
      .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  }
  await expect(page.getByText("Đã thanh toán", { exact: true })).toBeVisible();
  await noOverflow(page);
});

test("regular user cannot open staff order management", async ({ page }) => {
  await mockShop(page);
  await page.goto("/staff/orders");
  await expect(page).toHaveURL(/\/403$/);
});

test("browser VNPay success parameter cannot mark payment paid", async ({
  page,
}) => {
  await mockShop(page);
  await page.addInitScript(() =>
    sessionStorage.setItem("commerce.paymentOrderId", "1"),
  );
  await page.goto("/payment/vnpay-return?vnp_ResponseCode=00");
  await expect(
    page.getByRole("heading", { name: "Đang xác minh thanh toán" }).first(),
  ).toBeVisible();
  await expect(page.getByText("Đã thanh toán", { exact: true })).toHaveCount(0);
  await noOverflow(page);
});

test("unavailable product remains removable and blocks checkout", async ({
  page,
}) => {
  await mockShop(page, { unavailable: true });
  await page.goto("/cart");
  await expect(page.getByText("Ngừng bán", { exact: true })).toBeVisible();
  await expect(
    page.getByRole("link", { name: "Tiến hành thanh toán" }),
  ).toHaveAttribute("aria-disabled", "true");
  await page.getByRole("button", { name: "Xóa sản phẩm", exact: true }).click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Xóa sản phẩm" })
    .click();
  await expect(
    page.getByRole("heading", { name: "Giỏ hàng đang chờ điều thú vị" }),
  ).toBeVisible();
});

test("order list pagination and filters update server query and reset page", async ({
  page,
}) => {
  await mockShop(page, { pages: true });
  await page.goto("/orders");
  await page.getByRole("button", { name: "Trang sau", exact: true }).click();
  await expect(page).toHaveURL(/page=1/);
  await page.getByRole("button", { name: "Đang giao", exact: true }).click();
  await expect(page).toHaveURL(/status=SHIPPING/);
  expect(new URL(page.url()).searchParams.has("page")).toBeFalsy();
  await page.getByRole("textbox", { name: "Tìm đơn hàng" }).fill("LS24102601");
  const request = page.waitForRequest(
    (r) =>
      r.url().includes("/me/orders?") && r.url().includes("search=LS24102601"),
  );
  await page.getByRole("button", { name: "Tìm đơn hàng", exact: true }).click();
  await request;
  await noOverflow(page);
});

test("preview and removal API errors remain visible and retryable", async ({
  page,
}) => {
  await mockShop(page);
  let failPreview = true;
  await page.route("**/api/v1/me/orders/preview", async (route) => {
    if (!failPreview) return route.fallback();
    failPreview = false;
    return route.fulfill({
      status: 503,
      contentType: "application/json",
      body: JSON.stringify({ message: "Tạm thời chưa tính được đơn" }),
    });
  });
  await page.goto("/checkout");
  await expect(page.getByRole("alert")).toContainText(
    "Tạm thời chưa tính được đơn",
  );
  await page.getByRole("button", { name: "Kiểm tra lại", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Đặt hàng", exact: true }),
  ).toBeEnabled();
  await page.route("**/api/v1/me/cart/items/1", (route) =>
    route.request().method() === "DELETE"
      ? route.fulfill({
          status: 503,
          contentType: "application/json",
          body: JSON.stringify({ message: "Chưa xóa được sản phẩm" }),
        })
      : route.fallback(),
  );
  await page.goto("/cart");
  await page.getByRole("button", { name: "Xóa sản phẩm", exact: true }).click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Xóa sản phẩm", exact: true })
    .click();
  await expect(page.getByRole("dialog")).not.toBeVisible();
  await expect(page.getByRole("alert")).toContainText("Chưa xóa được sản phẩm");
});

test("StrictMode session bootstrap refreshes a rotating cookie once per page load", async ({
  page,
}) => {
  await mockShop(page);
  let refreshes = 0;
  await page.route("**/api/v1/auth/refresh", async (route) => {
    refreshes++;
    await new Promise((resolve) => setTimeout(resolve, 50));
    return route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ accessToken: "rotated-test-token" }),
    });
  });
  await page.goto("/orders/1");
  await expect(
    page.getByRole("heading", { name: "Chi tiết đơn hàng", exact: true }),
  ).toBeVisible();
  expect(refreshes).toBe(1);
  await page.reload();
  await expect(
    page.getByRole("heading", { name: "Chi tiết đơn hàng", exact: true }),
  ).toBeVisible();
  expect(refreshes).toBe(2);
});
