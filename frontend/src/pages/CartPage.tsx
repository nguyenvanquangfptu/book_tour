import React from 'react';
import { useCart } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import { FaTrash, FaShoppingCart } from 'react-icons/fa';

const CartPage: React.FC = () => {
  const { cart, removeFromCart, getCartCount } = useCart();
  const navigate = useNavigate();

  const handleCheckout = (tourId: number) => {
    navigate(`/checkout/${tourId}`);
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.guests), 0);
  };

  return (
    <div className="container" style={{ paddingTop: '100px', minHeight: '70vh' }}>
      <h1 style={{ marginBottom: '30px', display: 'flex', alignItems: 'center', gap: '10px' }}>
        <FaShoppingCart /> Giỏ Hàng Của Bạn ({getCartCount()} tour)
      </h1>

      {cart.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px', background: '#f8fafc', borderRadius: '12px' }}>
          <h3>Giỏ hàng đang trống</h3>
          <p style={{ color: '#64748b', marginBottom: '20px' }}>Bạn chưa chọn tour nào. Hãy quay lại trang chủ để khám phá nhé!</p>
          <button onClick={() => navigate('/')} className="btn btn-primary">Khám Phá Tour</button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
          {cart.map((item, index) => (
            <div key={`${item.tourId}-${item.startDate}-${index}`} style={{ display: 'flex', gap: '20px', background: '#fff', padding: '20px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', alignItems: 'center', flexWrap: 'wrap' }}>
              <img src={item.imageUrl} alt={item.tourTitle} style={{ width: '150px', height: '100px', objectFit: 'cover', borderRadius: '8px' }} />
              
              <div style={{ flex: 1, minWidth: '200px' }}>
                <h3 style={{ margin: '0 0 10px 0', fontSize: '1.2rem', color: '#0f172a' }}>{item.tourTitle}</h3>
                <p style={{ margin: '0 0 5px 0', color: '#475569' }}>Khởi hành: <strong>{item.startDate}</strong></p>
                <p style={{ margin: '0', color: '#475569' }}>Số lượng khách: <strong>{item.guests}</strong></p>
              </div>

              <div style={{ textAlign: 'right', minWidth: '150px' }}>
                <p style={{ margin: '0 0 5px 0', fontSize: '0.9rem', color: '#64748b' }}>Đơn giá: {item.price.toLocaleString()} đ</p>
                <p style={{ margin: '0 0 15px 0', fontSize: '1.2rem', fontWeight: 'bold', color: '#e11d48' }}>
                  Thành tiền: {(item.price * item.guests).toLocaleString()} đ
                </p>
                
                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                  <button 
                    onClick={() => removeFromCart(item.tourId, item.startDate)}
                    style={{ padding: '8px 12px', background: '#f1f5f9', border: 'none', borderRadius: '6px', cursor: 'pointer', color: '#64748b', display: 'flex', alignItems: 'center', gap: '5px' }}
                  >
                    <FaTrash /> Xóa
                  </button>
                  <button 
                    onClick={() => handleCheckout(item.tourId)}
                    className="btn btn-primary"
                    style={{ padding: '8px 16px' }}
                  >
                    Thanh Toán
                  </button>
                </div>
              </div>
            </div>
          ))}

          <div style={{ marginTop: '20px', padding: '20px', background: '#fff', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ margin: 0 }}>Tổng cộng:</h2>
            <h2 style={{ margin: 0, color: '#e11d48' }}>{calculateTotal().toLocaleString()} VNĐ</h2>
          </div>
        </div>
      )}
    </div>
  );
};

export default CartPage;
