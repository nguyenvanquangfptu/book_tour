import React from 'react';
import { useCart } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import { FaTrash, FaShoppingCart } from 'react-icons/fa';

const CartPage: React.FC = () => {
  const { cart, removeFromCart, getCartCount } = useCart();
  const navigate = useNavigate();

  const handleCheckout = (item: any) => {
    navigate(`/checkout/${item.tourId}`, {
      state: {
        tourId: item.tourId,
        tourTitle: item.tourTitle,
        guests: item.guests,
        startDate: item.startDate,
        totalPrice: item.price * item.guests
      }
    });
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.guests), 0);
  };

  return (
    <div className="container" style={{ paddingTop: '120px', paddingBottom: '60px', minHeight: '80vh' }}>
      <div style={{ background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', padding: '30px', borderRadius: '20px', marginBottom: '40px', color: 'white', display: 'flex', alignItems: 'center', gap: '15px', boxShadow: '0 10px 25px rgba(15, 23, 42, 0.2)' }}>
        <FaShoppingCart style={{ fontSize: '2.5rem', color: '#38bdf8' }} />
        <div>
          <h1 style={{ margin: 0, fontSize: '2rem', fontWeight: 800 }}>Giỏ Hàng Của Bạn</h1>
          <p style={{ margin: '5px 0 0', color: '#94a3b8', fontSize: '1.1rem' }}>Bạn đang có {getCartCount()} tour chờ thanh toán</p>
        </div>
      </div>

      {cart.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '80px 20px', background: '#fff', borderRadius: '24px', boxShadow: '0 4px 20px rgba(0,0,0,0.05)', border: '1px dashed #cbd5e1' }}>
          <FaShoppingCart style={{ fontSize: '5rem', color: '#e2e8f0', marginBottom: '20px' }} />
          <h3 style={{ fontSize: '1.5rem', color: '#1e293b', marginBottom: '10px' }}>Giỏ hàng đang trống</h3>
          <p style={{ color: '#64748b', marginBottom: '30px', fontSize: '1.1rem' }}>Bạn chưa chọn tour nào. Hãy quay lại trang chủ để khám phá nhé!</p>
          <button onClick={() => navigate('/')} className="btn btn-primary" style={{ padding: '12px 32px', fontSize: '1.1rem', borderRadius: '12px' }}>Khám Phá Tour Ngay</button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
          {cart.map((item, index) => (
            <div 
              key={`${item.tourId}-${item.startDate}-${index}`} 
              style={{ 
                display: 'flex', 
                gap: '24px', 
                background: '#fff', 
                padding: '24px', 
                borderRadius: '20px', 
                boxShadow: '0 10px 25px -5px rgba(0,0,0,0.05), 0 8px 10px -6px rgba(0,0,0,0.01)', 
                alignItems: 'center', 
                flexWrap: 'wrap',
                border: '1px solid #f1f5f9',
                transition: 'transform 0.3s ease, box-shadow 0.3s ease'
              }}
              onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 20px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.01)'; }}
              onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 10px 25px -5px rgba(0,0,0,0.05), 0 8px 10px -6px rgba(0,0,0,0.01)'; }}
            >
              <div style={{ width: '180px', height: '120px', borderRadius: '12px', overflow: 'hidden', flexShrink: 0 }}>
                <img src={item.imageUrl} alt={item.tourTitle} style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.5s ease' }} onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.1)'} onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'} />
              </div>
              
              <div style={{ flex: 1, minWidth: '250px' }}>
                <h3 style={{ margin: '0 0 12px 0', fontSize: '1.3rem', color: '#0f172a', fontWeight: 700 }}>{item.tourTitle}</h3>
                <div style={{ display: 'flex', gap: '20px', color: '#475569', fontSize: '0.95rem' }}>
                  <p style={{ margin: 0, background: '#f8fafc', padding: '6px 12px', borderRadius: '8px' }}>Khởi hành: <strong style={{ color: '#0f172a' }}>{item.startDate}</strong></p>
                  <p style={{ margin: 0, background: '#f8fafc', padding: '6px 12px', borderRadius: '8px' }}>Số khách: <strong style={{ color: '#0f172a' }}>{item.guests}</strong></p>
                </div>
              </div>

              <div style={{ textAlign: 'right', minWidth: '200px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div>
                  <p style={{ margin: '0 0 4px 0', fontSize: '0.9rem', color: '#64748b' }}>Đơn giá: {item.price.toLocaleString()} đ</p>
                  <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 800, color: '#e11d48' }}>
                    {(item.price * item.guests).toLocaleString()} đ
                  </p>
                </div>
                
                <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                  <button 
                    onClick={() => removeFromCart(item.tourId, item.startDate)}
                    style={{ padding: '10px 16px', background: '#fee2e2', border: 'none', borderRadius: '10px', cursor: 'pointer', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, transition: 'all 0.2s' }}
                    onMouseEnter={(e) => e.currentTarget.style.background = '#fecaca'}
                    onMouseLeave={(e) => e.currentTarget.style.background = '#fee2e2'}
                  >
                    <FaTrash />
                  </button>
                  <button 
                    onClick={() => handleCheckout(item)}
                    className="btn btn-primary"
                    style={{ padding: '10px 24px', borderRadius: '10px', fontWeight: 600, fontSize: '1rem', boxShadow: '0 4px 12px rgba(37, 99, 235, 0.2)' }}
                  >
                    Thanh Toán
                  </button>
                </div>
              </div>
            </div>
          ))}

          <div style={{ marginTop: '30px', padding: '30px 40px', background: 'linear-gradient(to right, #ffffff, #f8fafc)', border: '1px solid #e2e8f0', borderRadius: '20px', boxShadow: '0 10px 25px -5px rgba(0,0,0,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h2 style={{ margin: 0, color: '#0f172a', fontSize: '1.5rem' }}>Tổng cộng giỏ hàng</h2>
              <p style={{ margin: '5px 0 0', color: '#64748b' }}>Bao gồm tất cả các tour đã chọn</p>
            </div>
            <div style={{ textAlign: 'right' }}>
              <h2 style={{ margin: 0, color: '#e11d48', fontSize: '2.5rem', fontWeight: 800 }}>{calculateTotal().toLocaleString()} VNĐ</h2>
              <p style={{ margin: '5px 0 0', color: '#10b981', fontWeight: 600, fontSize: '0.9rem' }}>Đã bao gồm thuế (VAT)</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CartPage;
