import { Link } from 'react-router-dom';

export const CheckoutPage = () => (
  <main style={{ maxWidth: 720, margin: '0 auto', padding: '2rem' }}>
    <h1>Checkout</h1>
    <p>Kiểm tra thông tin giao hàng và đơn hàng trước khi xác nhận.</p>
    <section>
      <h2>Thông tin nhận hàng</h2>
      <label style={{ display: 'block', marginBottom: 12 }}>Họ tên<input disabled style={{ display: 'block', width: '100%' }} /></label>
      <label style={{ display: 'block', marginBottom: 12 }}>Số điện thoại<input disabled style={{ display: 'block', width: '100%' }} /></label>
      <label style={{ display: 'block', marginBottom: 12 }}>Địa chỉ<input disabled style={{ display: 'block', width: '100%' }} /></label>
    </section>
    <section>
      <h2>Đơn hàng</h2>
      <p>Giá, tồn kho và khuyến mãi sẽ được kiểm tra lại ở bước xác nhận đơn.</p>
    </section>
    <p>Chức năng xác nhận đơn sẽ được kết nối khi checkout backend hoàn tất.</p>
    <Link to="/cart">Quay lại giỏ hàng</Link>
  </main>
);
